var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var parkSchema = new Schema({
	'name' : String,
	'id' : Number
});

module.exports = mongoose.model('park', parkSchema);
