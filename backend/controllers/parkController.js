var ParkModel = require('../models/parkModel.js');

/**
 * parkController.js
 *
 * @description :: Server-side logic for managing parks.
 */
module.exports = {

    /**
     * parkController.list()
     */
    list: function (req, res) {
        ParkModel.find(function (err, parks) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting park.',
                    error: err
                });
            }

            return res.json(parks);
        });
    },

    /**
     * parkController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        ParkModel.findOne({_id: id}, function (err, park) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting park.',
                    error: err
                });
            }

            if (!park) {
                return res.status(404).json({
                    message: 'No such park'
                });
            }

            return res.json(park);
        });
    },

    /**
     * parkController.create()
     */
    create: function (req, res) {
        var park = new ParkModel({
			name : req.body.name,
			id : req.body.id
        });

        park.save(function (err, park) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating park',
                    error: err
                });
            }

            return res.status(201).json(park);
        });
    },

    /**
     * parkController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        ParkModel.findOne({_id: id}, function (err, park) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting park',
                    error: err
                });
            }

            if (!park) {
                return res.status(404).json({
                    message: 'No such park'
                });
            }

            park.name = req.body.name ? req.body.name : park.name;
			park.id = req.body.id ? req.body.id : park.id;
			
            park.save(function (err, park) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating park.',
                        error: err
                    });
                }

                return res.json(park);
            });
        });
    },

    /**
     * parkController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        ParkModel.findByIdAndRemove(id, function (err, park) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the park.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
