const crypto = require('crypto');
const fs = require('fs');

class Block {
    constructor(index, timestamp, data, previousHash = '') {
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.previousHash = previousHash;
        this.hash = this.calculateHash();
    }

    calculateHash() {
        return crypto
            .createHash('sha256')
            .update(this.index + this.timestamp + this.previousHash + JSON.stringify(this.data))
            .digest('hex');
    }
}

class Blockchain {
    constructor() {
        if (fs.existsSync('blockchain.json')) {
            const data = fs.readFileSync('blockchain.json');
            this.chain = JSON.parse(data);
        } else {
            this.chain = [this.createGenesisBlock()];
        }
    }

    createGenesisBlock() {
        return new Block(0, new Date().toISOString(), 'Genesis Block', '0');
    }

    getLatestBlock() {
        return this.chain[this.chain.length - 1];
    }

    addBlock(newBlock) {
        newBlock.previousHash = this.getLatestBlock().hash;
        newBlock.hash = newBlock.calculateHash();
        this.chain.push(newBlock);
        this.saveBlockchain();
    }

    saveBlockchain() {
        fs.writeFileSync('blockchain.json', JSON.stringify(this.chain, null, 4));
    }

    isChainValid() {
        for (let i = 1; i < this.chain.length; i++) {
            const currentBlock = this.chain[i];
            const previousBlock = this.chain[i - 1];

            if (currentBlock.hash !== currentBlock.calculateHash()) {
                return false;
            }

            if (currentBlock.previousHash !== previousBlock.hash) {
                return false;
            }
        }

        return true;
    }
}

module.exports = { Block, Blockchain };

// const environmentalBlockchain = new Blockchain();
//
// const environmentalData = {
//     _id: "678cdab5120f0d66bacf72fe",
//     message: "Extreme levels of CO2",
//     location: "Lat: 46.5632094, Lng: 15.6261507",
//     date: "2025-01-19T11:58:01.000Z",
//     category: "Air Quality",
//     __v: 0
// };

// const newBlock = new Block(1, new Date().toISOString(), environmentalData);
// environmentalBlockchain.addBlock(newBlock);

// console.log('Blockchain valid?', environmentalBlockchain.isChainValid());
// console.log(JSON.stringify(environmentalBlockchain, null, 4));
