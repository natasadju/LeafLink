var FriendshipModel = require('../models/friendshipModel.js');

/**
 * friendshipController.js
 *
 * @description :: Server-side logic for managing friendships.
 */
module.exports = {

    /**
     * friendshipController.list()
     */
    list: function (req, res) {
        FriendshipModel.find(function (err, friendships) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting friendship.',
                    error: err
                });
            }

            return res.json(friendships);
        });
    },

    /**
     * friendshipController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        FriendshipModel.findOne({_id: id}, function (err, friendship) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting friendship.',
                    error: err
                });
            }

            if (!friendship) {
                return res.status(404).json({
                    message: 'No such friendship'
                });
            }

            return res.json(friendship);
        });
    },

    /**
     * friendshipController.create()
     */
    create: function (req, res) {
        var friendship = new FriendshipModel({
			user : req.body.user,
			friend : req.body.friend
        });

        friendship.save(function (err, friendship) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating friendship',
                    error: err
                });
            }

            return res.status(201).json(friendship);
        });
    },

    /**
     * friendshipController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        FriendshipModel.findOne({_id: id}, function (err, friendship) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting friendship',
                    error: err
                });
            }

            if (!friendship) {
                return res.status(404).json({
                    message: 'No such friendship'
                });
            }

            friendship.user = req.body.user ? req.body.user : friendship.user;
			friendship.friend = req.body.friend ? req.body.friend : friendship.friend;
			
            friendship.save(function (err, friendship) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating friendship.',
                        error: err
                    });
                }

                return res.json(friendship);
            });
        });
    },

    /**
     * friendshipController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        FriendshipModel.findByIdAndRemove(id, function (err, friendship) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the friendship.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
