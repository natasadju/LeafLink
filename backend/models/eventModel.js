var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var eventSchema = new Schema({
	'name' : String,
	'location' : String,
	'date' : Date,
	'organizer' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'attendees' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	}
});

module.exports = mongoose.model('event', eventSchema);
