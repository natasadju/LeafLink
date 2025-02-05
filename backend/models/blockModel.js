const mongoose = require('mongoose');

const blockSchema = new mongoose.Schema({
    index: Number,
    timestamp: String,
    data: Object,
    previousHash: String,
    hash: String,
});

module.exports = mongoose.model('Block', blockSchema);
