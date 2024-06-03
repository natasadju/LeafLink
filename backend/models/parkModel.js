var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var parkSchema = new Schema({
	'name' : String,
	'parkId' : Number,
	'lat': String,
	'long': String
});

module.exports = mongoose.model('park', parkSchema);
