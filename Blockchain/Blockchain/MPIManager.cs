using MPI;
using System;
using System.Collections.Generic;
using System.Linq;

namespace Blockchain
{
    public static class MPIManager
    {
        /// Handles the master node logic, including collecting mined blocks and broadcasting updates.
        public static Action<List<Block>> OnBlockchainUpdated;

        public static void MasterNode(Intracommunicator comm)
        {
            List<Block> blockChain = new List<Block>();
            Block receivedBlock = null;

            Console.WriteLine("Master node started.");

            while (true)
            {
                // Receive a mined block from any worker node
                receivedBlock = comm.Receive<Block>(MPI.Unsafe.MPI_ANY_SOURCE, 0);

                Console.WriteLine($"Master received block {receivedBlock.index} from a worker.");

                // Validate and update the chain
                if (ValidateBlock(receivedBlock, blockChain))
                {
                    blockChain.Add(receivedBlock);
                    Console.WriteLine($"Block {receivedBlock.index} added to the blockchain.");

                    // Notify GUI about the updated blockchain
                    OnBlockchainUpdated?.Invoke(blockChain);

                    // Broadcast updated blockchain to all worker nodes
                    BroadcastBlockchain(comm, blockChain);
                }
                else
                {
                    Console.WriteLine($"Invalid block received: {receivedBlock.index}");
                }
            }
        }


        /// Handles the worker node logic, including mining and sending valid blocks to the master.
        public static void WorkerNode(Intracommunicator comm)
        {
            Console.WriteLine($"Worker node {comm.Rank} started.");

            while (true)
            {
                // Receive the current blockchain from the master
                List<Block> blockChain = comm.Receive<List<Block>>(0, 1);
                Block lastBlock = blockChain.LastOrDefault();

                // Create a new block based on the last block
                Block newBlock = CreateBlock(lastBlock);

                // Mine the block locally
                bool minedSuccessfully = MineBlock(newBlock);

                if (minedSuccessfully)
                {
                    Console.WriteLine($"Worker {comm.Rank} mined block {newBlock.index}.");

                    // Send the mined block to the master node
                    comm.Send(newBlock, 0, 0);
                }
            }
        }

        /// Broadcasts the blockchain to all worker nodes.
        private static void BroadcastBlockchain(Intracommunicator comm, List<Block> blockchain)
        {
            for (int i = 1; i < comm.Size; i++) // Exclude the master node (rank 0)
            {
                comm.Send(blockchain, i, 1);
            }

            Console.WriteLine("Blockchain broadcasted to all workers.");
        }

        /// Validates a block against the current blockchain.
        /// </summary>
        private static bool ValidateBlock(Block block, List<Block> chain)
        {
            if (chain.Count == 0)
            {
                // First block must have a previous hash of "0"
                return block.previousHash == "0";
            }

            Block lastBlock = chain.Last();

            // Validate block index, previous hash, and timestamp
            return block.index == lastBlock.index + 1 &&
                   block.previousHash == lastBlock.hash &&
                   block.timeStamp > lastBlock.timeStamp &&
                   sha256_hash(block) == block.hash;
        }

        /// <summary>
        /// Creates a new block based on the last block in the chain.
        /// </summary>
        private static Block CreateBlock(Block lastBlock)
        {
            return new Block
            {
                index = (lastBlock?.index ?? 0) + 1,
                previousHash = lastBlock?.hash ?? "0",
                data = "Sample data",
                timeStamp = DateTime.Now,
                difficulty = lastBlock?.difficulty ?? 1,
                nonce = 0
            };
        }

        /// <summary>
        /// Mines a block by finding a valid hash that meets the difficulty criteria.
        /// </summary>
        private static bool MineBlock(Block block)
        {
            string target = new string('0', block.difficulty);
            while (true)
            {
                block.hash = sha256_hash(block);

                if (block.hash.StartsWith(target))
                {
                    return true;
                }

                block.nonce++;
            }
        }

        /// <summary>
        /// Calculates the SHA256 hash of a block.
        /// </summary>
        private static string sha256_hash(Block block)
        {
            using (var sha256 = System.Security.Cryptography.SHA256.Create())
            {
                string rawData = block.index + block.data + block.timeStamp.ToString() + block.previousHash + block.difficulty + block.nonce;
                byte[] bytes = sha256.ComputeHash(System.Text.Encoding.UTF8.GetBytes(rawData));
                return BitConverter.ToString(bytes).Replace("-", "").ToLower();
            }
        }
    }
}
