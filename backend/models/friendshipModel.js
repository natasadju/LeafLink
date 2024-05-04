var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var friendshipSchema = new Schema({
	'user' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	},
	'friend' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'user'
	}
});

module.exports = mongoose.model('friendship', friendshipSchema);
