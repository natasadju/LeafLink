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
using System.Net.NetworkInformation;


namespace Blockchain
{
    public partial class Form1 : Form
    {
        public Form1(string[] args)
        {
            argsGlobal = args;
            InitializeComponent();
            label_diff.Text = globalDifficulty.ToString();

        }



        //constants
        static string ip = GetWlanIp();
        static double blockGenerationInterval = 10; //seconds
        static int diffAdjustInterval = 10; //blocks
        static double timeExpected = blockGenerationInterval * diffAdjustInterval;
        int globalDifficulty = 0;

        static List<Tuple<string, int>> connection_addresses = new List<Tuple<string, int>>();
        static List<Block> blockChain = new List<Block>();

        string[] argsGlobal;


        static string GetWlanIp()
        {
            string wlanIp = null;

            foreach (NetworkInterface nic in NetworkInterface.GetAllNetworkInterfaces())
            {
                if (nic.NetworkInterfaceType == NetworkInterfaceType.Wireless80211 &&
                    nic.OperationalStatus == OperationalStatus.Up)
                {
                    var ipProps = nic.GetIPProperties();
                    foreach (var address in ipProps.UnicastAddresses)
                    {
                        if (address.Address.AddressFamily == AddressFamily.InterNetwork)
                        {
                            wlanIp = address.Address.ToString();
                            break;
                        }
                    }
                }

                if (!string.IsNullOrEmpty(wlanIp))
                    break;
            }

            return wlanIp ?? Dns.GetHostEntry(Dns.GetHostName()).AddressList
                .FirstOrDefault(x => x.AddressFamily == AddressFamily.InterNetwork)?.ToString();
        }



        static string randomString()
        {
            Random _random = new Random();
            int stringLen = _random.Next(1, 100);
            StringBuilder sb = new StringBuilder(stringLen);
            for (int i = 0; i < stringLen; i++)
            {
                sb.Append((char)_random.Next(33, 126));
            }
            return sb.ToString();
        }

        static string sha256_hash(Block block)
        {
            string str = block.index + block.data + block.previousHash + block.difficulty + block.nonce;
            StringBuilder Sb = new StringBuilder();

            using (SHA256 hash = SHA256Managed.Create())
            {
                Encoding enc = Encoding.UTF8;
                Byte[] result = hash.ComputeHash(enc.GetBytes(str));

                foreach (Byte b in result)
                    Sb.Append(b.ToString("x2"));
            }
            return Sb.ToString();
        }

        static string Receive(NetworkStream ns)
        {
            try
            {
                byte[] myReadBuffer = new byte[1024];
                int len = ns.Read(myReadBuffer, 0, myReadBuffer.Length);
                string message = Encoding.Default.GetString(myReadBuffer, 0, len);
                return message;
            }
            catch (Exception ex)
            {
                MessageBox.Show("Recive:\n" + ex.Message + "\n" + ex.StackTrace);
                return null;
            }
        }

        static void Send(NetworkStream ns, List<Block> chain)
        {
            try
            {
                string message = JsonConvert.SerializeObject(chain);
                message = message + "\n";
                byte[] myWriteBuffer = Encoding.Default.GetBytes(message);
                ns.Write(myWriteBuffer, 0, myWriteBuffer.Length);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Send:\n" + ex.Message + "\n" + ex.StackTrace);
            }
        }

        private void btn_connect_Click(object sender, EventArgs e) //Connect
        {
            btn_connect.Enabled = false;
            btn_mine.Enabled = true;
            textBox_username.Enabled = false;
            textBox_port.Enabled = false;
            textBox_connect_to.Enabled = true;

            //listener thread start
            Thread thListener = new Thread(new ThreadStart(ListenForConnections));
            thListener.IsBackground = true;
            thListener.Start();
        }

        private void ListenForConnections()
        {
            //start listener
            int port = Convert.ToInt32(textBox_port.Text);
            TcpListener listener = new TcpListener(IPAddress.Any, port);
            listener.Start();

            //listen forever
            while (true)
            {
                try
                {
                    //accept client
                    TcpClient tcpClient = listener.AcceptTcpClient();

                    //client thread start
                    Thread thListener = new Thread(new ParameterizedThreadStart(Comumication));
                    thListener.IsBackground = true;
                    thListener.Start(tcpClient);
                }
                catch (Exception ex)
                {
                    MessageBox.Show("Listener:\n" + ex.Message + "\n" + ex.StackTrace);
                }
            }
        }

        private void Comumication(object obj)
        {
            TcpClient tcpClient = (TcpClient)obj;
            NetworkStream ns = tcpClient.GetStream();

            List<Block> newBlockChain = new List<Block>();
            string buffer = "";
            string message;

            do
            {
                //read buffer
                buffer = buffer + Receive(ns);
                while (true)
                {
                    int index = buffer.IndexOf('\n');

                    //break if no more full chains
                    if (index == -1)
                        break;

                    //split one chain from buffer
                    message = buffer.Substring(0, index);
                    buffer = buffer.Substring(index + 1);

                    newBlockChain = JsonConvert.DeserializeObject<List<Block>>(message);
                    ManageBlock(newBlockChain);
                }
            } while (ns.DataAvailable);

            ns.Close();
            tcpClient.Close();
        }

        private void ManageBlock(List<Block> newBlockChain) //Manage Block
        {
            richTextBox_blocks.Invoke(new Action(() =>
            {
                richTextBox_validation.Clear();
                richTextBox_validation.Select(richTextBox_blocks.TextLength, 0);
                richTextBox_validation.SelectionColor = Color.Orange;
                richTextBox_validation.AppendText("Recived new chain!\n Length: " + newBlockChain.Count + "\n");

                //If valid
                if (Validate(newBlockChain) && CompareChain(newBlockChain))
                {
                    richTextBox_validation.SelectionColor = Color.Green;
                    richTextBox_validation.AppendText("Chain valid\n");
                }
                else
                {
                    richTextBox_validation.SelectionColor = Color.Red;
                    richTextBox_validation.AppendText("Chain not valid\n");
                }
            }));
        }

        private bool CompareChain(List<Block> newBlockChain)
        {
            //cumulative difficulty block of new block
            double cumulative_difficulty_newBlockChain = 0;
            foreach (Block block in newBlockChain)
            {
                cumulative_difficulty_newBlockChain += Math.Pow(2, block.difficulty);
            }

            //cumulative difficulty blockChain from block.index
            double cumulative_difficulty_blockChain = 0;
            foreach (Block block in blockChain)
            {
                cumulative_difficulty_blockChain += Math.Pow(2, block.difficulty);
            }

            if (cumulative_difficulty_newBlockChain > cumulative_difficulty_blockChain)
            {
                int numNewBlocks = newBlockChain.Count - blockChain.Count;
                int numBlocks = blockChain.Count;
                blockChain = JsonConvert.DeserializeObject<List<Block>>(JsonConvert.SerializeObject(newBlockChain)); ;

                //For time testing
                if (numBlocks >= 49)
                {
                    double time = (DateTime.Now - blockChain[0].timeStamp).TotalSeconds;
                    MessageBox.Show("Time for Blockchain to generate 50 blocks: " + time + "s");
                }

                for (int i = 1; i <= numNewBlocks; i++)
                {
                    if ((numBlocks + i) % diffAdjustInterval == 0)
                    {
                        AdjustDifficulty();
                        break;
                    }
                }

                label_len.Invoke(new Action(() => {
                    label_len.Text = blockChain.Count.ToString();
                }));

                Broadcast(newBlockChain);
                UpdataChainTextBox();
                return true;
            }
            else
            {
                return false;
            }
        }

        private void btn_mine_Click(object sender, EventArgs e) //Mine
        {
            btn_mine.Enabled = false;
            //listener thread start
            Thread thListener = new Thread(new ThreadStart(StartMining));
            thListener.IsBackground = true;
            thListener.Start();
        }

        private void StartMining()
        {
            using (new MPI.Environment(ref argsGlobal))
            {
                int rank = Communicator.world.Rank;
                int size = Communicator.world.Size;

                // Number of nodes (processes)
                int numProcesses = size;

                // Number of threads per node
                int numThreadsPerProcess = System.Environment.ProcessorCount;

                while (true)
                {
                    if (rank == 0)
                    {
                        // Master process (rank 0) coordinates mining
                        //MineBlocksParallel(size);
                        MineBlocksParallel(size, numThreadsPerProcess);
                    }
                    else
                    {
                        // Worker processes (rank > 0) assist in mining
                        MineBlocksWorker(16);
                    }

                    // Synchronize all processes
                    Communicator.world.Barrier();
                }
            }
        }

        //private void MineBlocksParallel(int numProcesses)
        private void MineBlocksParallel(int numProcesses, int numThreadsPerProcess)
        {
            Parallel.For(0, numProcesses, i =>
            {
                // Each thread within a process performs mining independently
                //MineBlocksWorker();
                MineBlocksWorker(numThreadsPerProcess);
            });
        }

        //private void MineBlocksWorker()
        private void MineBlocksWorker(int numThreads)
        {
            List<Block> newBlockChain = new List<Block>();
            newBlockChain = JsonConvert.DeserializeObject<List<Block>>(JsonConvert.SerializeObject(blockChain));
            Block block = new Block();

            // Creating a block
            block.index = blockChain.Count;
            //block.miner = textBox_username.Text;
            block.data = randomString();
            block.timeStamp = DateTime.Now;

            if (blockChain.Count > 0)
                block.previousHash = blockChain[blockChain.Count - 1].hash;
            else
                block.previousHash = "0";

            block.difficulty = globalDifficulty;
            block.nonce = 0;

            // Mining
            bool hashRun = true;
            while (hashRun)
            {
                hashRun = false;
                block.hash = sha256_hash(block);

                // Check if right difficulty
                for (int i = 0; i < block.difficulty; i++)
                {
                    if (block.hash[i] != '0')
                    {
                        hashRun = true;
                        block.nonce++;
                        if (block.nonce % 100 == 0)
                        {
                            // Print incorrect block
                            richTextBox_blocks.Invoke(new Action(() =>
                            {
                                richTextBox_blocks.Select(richTextBox_blocks.TextLength, 0);
                                richTextBox_blocks.SelectionColor = Color.Red;
                                richTextBox_blocks.AppendText("New Block (difficulty: " + block.difficulty + ")\nHash: " + block.hash + "\n");
                            }));
                        }
                        break;
                    }
                }
            }

            // Print correct block
            richTextBox_blocks.Invoke(new Action(() =>
            {
                richTextBox_blocks.Select(richTextBox_blocks.TextLength, 0);
                richTextBox_blocks.SelectionColor = Color.Green;
                richTextBox_blocks.AppendText("New Block (difficulty: " + block.difficulty + ")\nHash: " + block.hash + "\n");
            }));

            newBlockChain.Add(block);

            // Broadcast the new block chain to all nodes
            Communicator.world.Broadcast(ref newBlockChain, 0);

            // Process the received block chain and update local state
            ManageBlock(newBlockChain);
        }

        private bool Validate(List<Block> newBlockChain)
        {
            //first block (hash, prevHash, timeStamp)
            if (newBlockChain[0].hash != sha256_hash(newBlockChain[0]) || newBlockChain[0].previousHash != "0"
                || (newBlockChain[0].timeStamp - DateTime.Now).TotalMinutes > 1)
            {
                string test = sha256_hash(newBlockChain[0]);
                return false;
            }

            for (int i = 1; i <= newBlockChain.Count - 1; i++)
            {
                //hash
                if (newBlockChain[i].hash != sha256_hash(newBlockChain[i]) ||
                    //prevHash
                    newBlockChain[i].previousHash != newBlockChain[i - 1].hash ||
                    //timeStamp is max 1 min ahead of current time
                    (newBlockChain[i].timeStamp - DateTime.Now).TotalMinutes > 1 ||
                    //timeStamp is max 1 min behind last block timeStamp
                    (newBlockChain[i - 1].timeStamp - newBlockChain[i].timeStamp).TotalMinutes > 1)
                    return false;
            }

            return true;
        }

        private void Broadcast(List<Block> newBlockChain) //Broadcast block
        {
            List<Tuple<string, int>> failed_connections = new List<Tuple<string, int>>();

            foreach (Tuple<string, int> address in connection_addresses)
            {
                try
                {
                    TcpClient tcpClient = new TcpClient(address.Item1, address.Item2);
                    Send(tcpClient.GetStream(), newBlockChain);
                    tcpClient.Close();
                }
                catch (Exception ex)
                {
                    failed_connections.Add(new Tuple<string, int>(address.Item1, address.Item2));
                }

            }

            foreach (Tuple<string, int> port in failed_connections)
            {
                connection_addresses.Remove(port);
            }

            if (failed_connections.Count > 0)
            {
                richTextBox_connected_to.Invoke(new Action(() =>
                {
                    richTextBox_connected_to.Text = "";
                    foreach (Tuple<string, int> address in connection_addresses)
                    {
                        richTextBox_connected_to.AppendText(address.Item1 + " " + address.Item2.ToString() + "\n");
                    }
                }));
            }
        }

        private void AdjustDifficulty()
        {
            //calculating time eplasted bwtween last "diffAdjustInterval" blocks
            Block lastBlock = blockChain[blockChain.Count - 1];
            Block previousAdjustmentBlock = blockChain[blockChain.Count - diffAdjustInterval];
            double timeTaken = (lastBlock.timeStamp - previousAdjustmentBlock.timeStamp).TotalSeconds;

            //if half than expected
            if (timeTaken < (timeExpected / 2))
            {
                globalDifficulty++;

                label_diff.Invoke(new Action(() =>
                {
                    label_diff.ForeColor = Color.Green;
                    label_diff.Text = globalDifficulty.ToString() + "↑";
                }));
            }
            //if double than expected  
            else if (timeTaken > (timeExpected * 2))
            {
                globalDifficulty--;
                label_diff.Invoke(new Action(() =>
                {
                    label_diff.ForeColor = Color.Red;
                    label_diff.Text = globalDifficulty.ToString() + "↓";
                }));
            }
            //in bounds
            else
            {
                label_diff.Invoke(new Action(() =>
                {
                    label_diff.ForeColor = Color.Black;
                    label_diff.Text = globalDifficulty.ToString();
                }));
            }
        } //AdjustDifficulty

        private void btn_connect_to_Click(object sender, EventArgs e) // Connect to
        {
            try
            {
                // Use the local IP and get the port from the text box
                string ipLocal = ip; // Automatically fetched local IP
                int portLocal = int.Parse(textBox_connect_to.Text); // User-entered port

                // Check if the connection already exists
                var connection = new Tuple<string, int>(ipLocal, portLocal);
                if (connection_addresses.Contains(connection))
                    connection_addresses.Remove(connection);
                else
                {
                    connection_addresses.Add(connection);

                    // Test connectivity
                    if (TestConnection(ipLocal, portLocal))
                    {
                        MessageBox.Show($"Connected successfully to {ipLocal}:{portLocal}");
                    }
                    else
                    {
                        MessageBox.Show($"Failed to connect to {ipLocal}:{portLocal}");
                        connection_addresses.Remove(connection); // Remove failed connection
                    }
                }

                // Update the connected-to list in the UI
                richTextBox_connected_to.Clear();
                foreach (var address in connection_addresses)
                {
                    richTextBox_connected_to.AppendText($"{address.Item1}:{address.Item2}\n");
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error: {ex.Message}");
            }
            finally
            {
                textBox_connect_to.Text = ""; // Clear the text box
            }
        }

        private bool TestConnection(string ipAddress, int port)
        {
            try
            {
                using (var client = new TcpClient())
                {
                    // Try connecting with a timeout
                    var result = client.BeginConnect(ipAddress, port, null, null);
                    bool success = result.AsyncWaitHandle.WaitOne(TimeSpan.FromSeconds(2)); // 2-second timeout

                    if (!success)
                    {
                        return false; // Connection failed
                    }

                    client.EndConnect(result); // Complete the connection
                    return true; // Connection successful
                }
            }
            catch
            {
                return false; // Connection failed
            }
        }



        private void UpdataChainTextBox()
        {
            richTextBox_chain.Invoke(new Action(() => {
                richTextBox_chain.Clear();
                foreach (Block block in blockChain)
                    richTextBox_chain.AppendText("\n\n" + block.ToString());
            }));
        } //UpdataChainTextBox

        private void textBox_username_TextChanged(object sender, EventArgs e)
        {
            if (textBox_username.Text != "" && textBox_port.Text != "")
            {
                btn_connect.Enabled = true;
            }
            else
            {
                btn_connect.Enabled = false;
            }
        }

        private void textBox_port_TextChanged(object sender, EventArgs e)
        {
            if (textBox_username.Text != "" && textBox_port.Text != "")
            {
                btn_connect.Enabled = true;
            }
            else
            {
                btn_connect.Enabled = false;
            }
        }

        private void richTextBox_connections_TextChanged(object sender, EventArgs e)
        {

        }

        private void richTextBox_validation_TextChanged(object sender, EventArgs e)
        {

        }

        private void textBox_connect_to_TextChanged(object sender, EventArgs e)
        {
            if (int.TryParse(textBox_connect_to.Text, out int port) && port > 0 && port <= 65535)
                btn_connect_to.Enabled = true;
            else
                btn_connect_to.Enabled = false;
        }

        private void label11_Click(object sender, EventArgs e)
        {

        }

        private void Form1_Load(object sender, EventArgs e)
        {

        }



        private void richTextBox_connected_to_TextChanged(object sender, EventArgs e)
        {

        }
    }
}