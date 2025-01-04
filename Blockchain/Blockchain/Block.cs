using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Blockchain
{
    internal class Block
    {
        public int index { get; set; }
        public string miner { get; set; }
        public string data { get; set; }
        public DateTime timeStamp { get; set; }
        public string hash { get; set; }
        public string previousHash { get; set; }
        public int difficulty { get; set; }
        public int nonce { get; set; }
        override public string  ToString()
        {
            return "Index: " + index + "\nMiner: "+miner+"\nData: " + data + "\nTimeStamp: " + timeStamp.ToString() + "\nHash: " + hash + "\nPrevious Hash: " + previousHash + "\nDifficulty: " + difficulty + "\nNonce: " + nonce;
        }
    }
}
