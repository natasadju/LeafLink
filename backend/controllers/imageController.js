var ImageModel = require('../models/imageModel.js');

/**
 * imageController.js
 *
 * @description :: Server-side logic for managing images.
 */
module.exports = {

    /**
     * imageController.list()
     */
    list: function (req, res) {
        ImageModel.find(function (err, images) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting image.',
                    error: err
                });
            }

            return res.json(images);
        });
    },

    getByEvent: function (req, res) {
        var eventId = req.params.eventId;
    
        ImageModel.find({ event: eventId }, function (err, images) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting images for event.',
                    error: err
                });
            }

            if (!images.length) {
                return res.status(404).json({
                    message: 'No images found for this event'
                });
            }
    
            return res.json(images);
        });
    },
    

    /**
     * imageController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        ImageModel.findOne({_id: id}, function (err, image) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting image.',
                    error: err
                });
            }

            if (!image) {
                return res.status(404).json({
                    message: 'No such image'
                });
            }

            return res.json(image);
        });
    },

    /**
     * imageController.create()
     */
    create: function (req, res) {
        if (!req.file) {
            return res.status(400).json({
                message: 'No image uploaded'
            });
        }
    
        var imageUrl = "images/"+ req.file.filename;
        var event = req.body.event;
    
        var image = new ImageModel({
            imageUrl: imageUrl,
            event: event,
            createdAt: new Date()
        });
    
        image.save(function (err, image) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating image',
                    error: err
                });
            }
    
            return res.status(201).json(image);
        });
    },
    

    /**
     * imageController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        ImageModel.findOne({_id: id}, function (err, image) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting image',
                    error: err
                });
            }

            if (!image) {
                return res.status(404).json({
                    message: 'No such image'
                });
            }

            image.imageUrl = req.body.imageUrl ? req.body.imageUrl : image.imageUrl;
			image.event = req.body.event ? req.body.event : image.event;
			image.user = req.body.user ? req.body.user : image.user;
			
            image.save(function (err, image) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating image.',
                        error: err
                    });
                }

                return res.json(image);
            });
        });
    },

    /**
     * imageController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        ImageModel.findByIdAndRemove(id, function (err, image) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the image.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
