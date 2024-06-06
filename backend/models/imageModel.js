var mongoose = require('mongoose');
var Schema   = mongoose.Schema;

var imageSchema = new Schema({
	'imageUrl' : String,
	'event' : {
	 	type: Schema.Types.ObjectId,
	 	ref: 'event'
	},
	'createdAt': {
        type: Date,
        default: Date.now
    }
	// 'user' : {
	//  	type: Schema.Types.ObjectId,
	//  	ref: 'user'
	// }
});

module.exports = mongoose.model('image', imageSchema);
