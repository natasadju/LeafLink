var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var parkSchema = new Schema({
	'name' : String,
	'parkId' : Number
});

module.exports = mongoose.model('park', parkSchema);
