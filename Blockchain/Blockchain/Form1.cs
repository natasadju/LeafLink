using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;
using System.Security.Cryptography;
using MPI;
using System.Diagnostics;

namespace Blockchain
{
    public partial class Form1 : Form
    {
        static string ip = "127.0.0.1";

        // Difficulty adjustment parameters
        static double blockGenerationInterval = 10.0; // target: 10 seconds per block
        static int diffAdjustInterval = 10;           // adjust difficulty every 10 blocks
        static double timeExpected = blockGenerationInterval * diffAdjustInterval;

        int globalDifficulty = 0;

        static List<Block> blockChain = new List<Block>();

        static List<Tuple<string, int>> connection_addresses = new List<Tuple<string, int>>();

        string[] argsGlobal;

        public Form1(string[] args)
        {
            argsGlobal = args;
            InitializeComponent();
            label_diff.Text = globalDifficulty.ToString();
        }

        private void btn_connect_Click(object sender, EventArgs e) // Connect button
        {
            btn_connect.Enabled = false;
            btn_mine.Enabled = true;
            textBox_username.Enabled = false;
            textBox_port.Enabled = false;
            textBox_connect_to.Enabled = true;

            // Start listener thread
            Thread thListener = new Thread(new ThreadStart(ListenForConnections));
            thListener.IsBackground = true;
            thListener.Start();
        }

        private void btn_connect_to_Click(object sender, EventArgs e) // Connect to (another node)
        {
            try
            {
                string address = textBox_connect_to.Text;
                string[] parts = address.Split(':');
                string ipLocal = parts[0];
                int portLocal = int.Parse(parts[1]);

                var tupleAddr = new Tuple<string, int>(ipLocal, portLocal);
                if (connection_addresses.Contains(tupleAddr))
                {
                    connection_addresses.Remove(tupleAddr);
                }
                else
                {
                    connection_addresses.Add(tupleAddr);
                }

                // Update connected nodes info
                richTextBox_connected_to.Clear();
                foreach (Tuple<string, int> ipAddress in connection_addresses)
                {
                    richTextBox_connected_to.AppendText(ipAddress.Item1 + ":" + ipAddress.Item2 + System.Environment.NewLine);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
            finally
            {
                textBox_connect_to.Text = "";
            }
        }

        private void btn_mine_Click(object sender, EventArgs e) // Start Mining button
        {
            btn_mine.Enabled = false;

            // Start a new thread that does MPI + mining
            Thread thMining = new Thread(new ThreadStart(StartMining));
            thMining.IsBackground = true;
            thMining.Start();
        }

        private void ListenForConnections()
        {
            try
            {
                int port = Convert.ToInt32(textBox_port.Text);
                TcpListener listener = new TcpListener(IPAddress.Any, port);
                listener.Start();

                while (true)
                {
                    TcpClient tcpClient = listener.AcceptTcpClient();
                    Thread th = new Thread(new ParameterizedThreadStart(Communication));
                    th.IsBackground = true;
                    th.Start(tcpClient);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Listener:\n" + ex.Message + "\n" + ex.StackTrace);
            }
        }

        // One thread per accepted connection
        private void Communication(object obj)
        {
            TcpClient tcpClient = (TcpClient)obj;
            NetworkStream ns = tcpClient.GetStream();

            List<Block> newBlockChain = new List<Block>();
            string buffer = "";
            string message;

            try
            {
                // Receive data
                buffer += Receive(ns);
                while (true)
                {
                    int index = buffer.IndexOf('\n');
                    if (index == -1) break;

                    // Split one chain from buffer
                    message = buffer.Substring(0, index);
                    buffer = buffer.Substring(index + 1);

                    newBlockChain = JsonConvert.DeserializeObject<List<Block>>(message);

                    // Merge / manage this new block or chain
                    ManageBlock(newBlockChain);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Communication:\n" + ex.Message + "\n" + ex.StackTrace);
            }
            finally
            {
                ns.Close();
                tcpClient.Close();
            }
        }

        private void StartMining()
        {
            // MPI environment initialization
            using (new MPI.Environment(ref argsGlobal))
            {
                int rank = Communicator.world.Rank;
                int size = Communicator.world.Size;

                // Could also be user-configurable
                int localThreadCount = System.Environment.ProcessorCount;

                while (true)
                {
                    // All processes attempt to mine the next block simultaneously. Once found, broadcast + manage.
                    Block newBlock = MineNextBlockParallel(localThreadCount);

                    // Only the process that actually found the block
                    // broadcasts it (or we can allow all to broadcast the same block).
                    // We'll assume all do a check if they found it or not inside MineNextBlockParallel.

                    // After a block is found, rank 0 can gather timings or do any extra logic...
                    Communicator.world.Barrier();
                }
            }
        }

        private Block MineNextBlockParallel(int numThreads)
        {
            // Prepare the new block
            Block block = new Block
            {
                index = blockChain.Count,
                data = randomString(),
                timeStamp = DateTime.Now,
                previousHash = (blockChain.Count == 0)
                                ? "0"
                                : blockChain[blockChain.Count - 1].hash,
                difficulty = globalDifficulty,
                nonce = 0
            };

            // We'll use a shared variable to signal "found solution"
            bool found = false;
            object lockObj = new object();

            // The result block once found
            Block foundBlock = null;

            // For performance, you can chunk nonces so each thread tries a big range
            // to reduce lock contention.
            int chunkSize = 20000;
            long globalNonceOffset = 0;

            // Threaded loop
            Parallel.For(0, numThreads, (threadIdx, loopState) =>
            {
                // Each thread tries some chunk of nonces in a loop
                while (!found)
                {
                    long startNonce = Interlocked.Add(ref globalNonceOffset, chunkSize) - chunkSize;
                    Block localBlock = new Block
                    {
                        index = block.index,
                        data = block.data,
                        timeStamp = block.timeStamp,
                        previousHash = block.previousHash,
                        difficulty = block.difficulty,
                        nonce = (int)startNonce
                    };

                    // Try chunkSize nonces
                    for (int i = 0; i < chunkSize; i++)
                    {
                        if (found) break; // someone found the solution, exit quickly

                        localBlock.nonce = (int)(startNonce + i);
                        localBlock.hash = sha256_hash(localBlock);

                        if (IsBlockValidDifficulty(localBlock.hash, localBlock.difficulty))
                        {
                            // Mark found
                            lock (lockObj)
                            {
                                if (!found)
                                {
                                    found = true;
                                    foundBlock = localBlock;
                                }
                            }
                            break;
                        }
                    }
                }
            });

            // Now foundBlock holds the newly mined block
            List<Block> broadcastChain = new List<Block>(blockChain);
            broadcastChain.Add(foundBlock);
            Communicator.world.Broadcast(ref broadcastChain, 0);

            // Each rank merges this newly found block
            ManageBlock(broadcastChain);

            return foundBlock;
        }

        // Check if the block's hash starts with `difficulty` zeroes
        private bool IsBlockValidDifficulty(string hash, int difficulty)
        {
            for (int i = 0; i < difficulty; i++)
            {
                if (i >= hash.Length) return false; // edge case
                if (hash[i] != '0') return false;
            }
            return true;
        }

        private void ManageBlock(List<Block> newBlockChain)
        {
            richTextBox_blocks.Invoke(new Action(() =>
            {
                richTextBox_validation.Clear();
                richTextBox_validation.Select(richTextBox_blocks.TextLength, 0);
                richTextBox_validation.SelectionColor = Color.Orange;
                richTextBox_validation.AppendText("Received new chain! Length: " + newBlockChain.Count + "\n");

                // Validate
                if (ValidateChain(newBlockChain) && CompareChain(newBlockChain))
                {
                    richTextBox_validation.SelectionColor = Color.Green;
                    richTextBox_validation.AppendText("Chain valid. Updated.\n");
                }
                else
                {
                    richTextBox_validation.SelectionColor = Color.Red;
                    richTextBox_validation.AppendText("Chain not valid. Ignored.\n");
                }
            }));
        }
        private bool CompareChain(List<Block> newBlockChain)
        {
            double newChainDifficulty = 0.0;
            foreach (Block b in newBlockChain)
            {
                newChainDifficulty += Math.Pow(2, b.difficulty);
            }

            double currentChainDifficulty = 0.0;
            foreach (Block b in blockChain)
            {
                currentChainDifficulty += Math.Pow(2, b.difficulty);
            }

            if (newChainDifficulty > currentChainDifficulty)
            {
                // Possibly measure time after 50 blocks
                int oldCount = blockChain.Count;
                blockChain = JsonConvert.DeserializeObject<List<Block>>(JsonConvert.SerializeObject(newBlockChain));

                // Check if we should adjust difficulty
                // Did we pass a multiple of diffAdjustInterval?
                if ((oldCount) / diffAdjustInterval < (blockChain.Count) / diffAdjustInterval)
                {
                    // We just crossed the boundary (e.g. from 9->10 blocks, or 19->20, etc.).
                    AdjustDifficulty();
                }

                label_len.Invoke(new Action(() =>
                {
                    label_len.Text = blockChain.Count.ToString();
                }));

                // Re-broadcast if needed
                Broadcast(newBlockChain);
                UpdateChainTextBox();
                return true;
            }
            else
            {
                return false;
            }
        }

        private bool ValidateChain(List<Block> chainToValidate)
        {
            if (chainToValidate.Count == 0) return false;

            // 1) Check first block
            if (chainToValidate[0].previousHash != "0") return false;
            if (chainToValidate[0].hash != sha256_hash(chainToValidate[0])) return false;
            // Max 1 minute ahead of local time
            if ((chainToValidate[0].timeStamp - DateTime.Now).TotalMinutes > 1) return false;

            // 2) Check rest
            for (int i = 1; i < chainToValidate.Count; i++)
            {
                var currentBlock = chainToValidate[i];
                var prevBlock = chainToValidate[i - 1];

                // Check hash correctness
                if (currentBlock.hash != sha256_hash(currentBlock))
                    return false;
                // Check chain linking
                if (currentBlock.previousHash != prevBlock.hash)
                    return false;
                // Timestamps (no more than 1 minute ahead or behind)
                if ((currentBlock.timeStamp - DateTime.Now).TotalMinutes > 1)
                    return false;
                if ((prevBlock.timeStamp - currentBlock.timeStamp).TotalMinutes > 1)
                    return false;
            }

            return true;
        }
        private void AdjustDifficulty()
        {
            if (blockChain.Count < diffAdjustInterval) return;

            Block lastBlock = blockChain[blockChain.Count - 1];
            Block previousAdjustmentBlock = blockChain[blockChain.Count - diffAdjustInterval];
            double timeTaken = (lastBlock.timeStamp - previousAdjustmentBlock.timeStamp).TotalSeconds;

            if (timeTaken < (timeExpected / 2))
            {
                globalDifficulty++;
                label_diff.Invoke(new Action(() =>
                {
                    label_diff.ForeColor = Color.Green;
                    label_diff.Text = globalDifficulty.ToString() + " ↑";
                }));
            }
            else if (timeTaken > (timeExpected * 2))
            {
                globalDifficulty = Math.Max(0, globalDifficulty - 1); // avoid negative
                label_diff.Invoke(new Action(() =>
                {
                    label_diff.ForeColor = Color.Red;
                    label_diff.Text = globalDifficulty.ToString() + " ↓";
                }));
            }
            else
            {
                label_diff.Invoke(new Action(() =>
                {
                    label_diff.ForeColor = Color.Black;
                    label_diff.Text = globalDifficulty.ToString();
                }));
            }
        }

        private string Receive(NetworkStream ns)
        {
            byte[] myReadBuffer = new byte[4096];
            int len = ns.Read(myReadBuffer, 0, myReadBuffer.Length);
            return Encoding.Default.GetString(myReadBuffer, 0, len);
        }

        private void Send(NetworkStream ns, List<Block> chain)
        {
            string message = JsonConvert.SerializeObject(chain) + "\n";
            byte[] buffer = Encoding.Default.GetBytes(message);
            ns.Write(buffer, 0, buffer.Length);
        }

        private void Broadcast(List<Block> newBlockChain)
        {
            List<Tuple<string, int>> failed_connections = new List<Tuple<string, int>>();

            foreach (var address in connection_addresses)
            {
                try
                {
                    using (TcpClient tcpClient = new TcpClient(address.Item1, address.Item2))
                    {
                        Send(tcpClient.GetStream(), newBlockChain);
                    }
                }
                catch
                {
                    failed_connections.Add(address);
                }
            }

            // Remove failed connections
            foreach (var fc in failed_connections)
            {
                connection_addresses.Remove(fc);
            }

            if (failed_connections.Count > 0)
            {
                richTextBox_connected_to.Invoke(new Action(() =>
                {
                    richTextBox_connected_to.Clear();
                    foreach (var addr in connection_addresses)
                    {
                        richTextBox_connected_to.AppendText(addr.Item1 + ":" + addr.Item2 + System.Environment.NewLine);
                    }
                }));
            }
        }
        private void UpdateChainTextBox()
        {
            richTextBox_chain.Invoke(new Action(() =>
            {
                richTextBox_chain.Clear();
                foreach (Block b in blockChain)
                {
                    richTextBox_chain.AppendText("\n\n" + b.ToString());
                }
            }));
        }

        static string randomString()
        {
            Random rand = new Random();
            int length = rand.Next(5, 20);
            const string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            return new string(Enumerable.Repeat(chars, length)
                               .Select(s => s[rand.Next(s.Length)]).ToArray());
        }

        static string sha256_hash(Block block)
        {
            // Incorporate important block fields
            string str = block.index
                       + block.data
                       + block.previousHash
                       + block.difficulty
                       + block.nonce
                       + block.timeStamp.Ticks; // include timestamp

            using (SHA256 sha256 = SHA256.Create())
            {
                byte[] bytes = Encoding.UTF8.GetBytes(str);
                byte[] hash = sha256.ComputeHash(bytes);
                return BitConverter.ToString(hash).Replace("-", "").ToLower();
            }
        }

        // (Other event handlers for text changes, load, etc. remain unchanged)
        private void textBox_username_TextChanged(object sender, EventArgs e)
        {
            if (textBox_username.Text != "" && textBox_port.Text != "")
                btn_connect.Enabled = true;
            else
                btn_connect.Enabled = false;
        }

        private void textBox_port_TextChanged(object sender, EventArgs e)
        {
            if (textBox_username.Text != "" && textBox_port.Text != "")
                btn_connect.Enabled = true;
            else
                btn_connect.Enabled = false;
        }

        private void textBox_connect_to_TextChanged(object sender, EventArgs e)
        {
            btn_connect_to.Enabled = textBox_connect_to.Text != "";
        }

        private void Form1_Load(object sender, EventArgs e) { }
        private void richTextBox_validation_TextChanged(object sender, EventArgs e) { }
        private void richTextBox_connections_TextChanged(object sender, EventArgs e) { }
        private void label11_Click(object sender, EventArgs e) { }
    }
}
