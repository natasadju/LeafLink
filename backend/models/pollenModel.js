var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var pollenSchema = new Schema({
	type: String,
	value: Number,
	timestamp: {type: Date, default: Date.now},
	isFake: Boolean
});

module.exports = mongoose.model('pollen', pollenSchema);
