var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var airQualitySchema = new Schema({
    station: String,
    pm10: Number,
    pm25: Number,
    so2: Number,
    co: Number,
    ozon: Number,
    no2: Number,
    benzen: Number,
    timestamp: {type: Date, default: Date.now}
});

module.exports = mongoose.model('airQuality', airQualitySchema);
