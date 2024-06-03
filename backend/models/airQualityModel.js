var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var airQualitySchema = new Schema({
	'location' : String,
	'timestamp' : Date,
	'so2' : Number,
	'co' : Number,
	'pm10' : Number,
	'pm25' : Number,
	'03' : Number,
	'no2' : Number,
	'benzene' : Number
});

module.exports = mongoose.model('airQuality', airQualitySchema);
