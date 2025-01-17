var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var extremeSchema = new Schema({
    message: {
        type: String,
        required: true
    },
    location: {
        type: String,
        required: true
    },
    date: {
        type: Date,
        required: true
    },
    category: {
        type: String,
        required: true
    }
});

module.exports = mongoose.model('extreme', friendshipSchema);
