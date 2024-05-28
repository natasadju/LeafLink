var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var eventSchema = new Schema({
    name: String,
    parkId: {
        type: Schema.Types.ObjectId,
        ref: 'Park', // Assuming you have a 'Park' model
        required: true
    },
    date: Date,
    description: String,
    // organizer: {
    //     type: Schema.Types.ObjectId,
    //     ref: 'User',
    //     required: true
    // }
});

module.exports = mongoose.model('Event', eventSchema);
