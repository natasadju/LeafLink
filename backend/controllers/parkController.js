var ParkModel = require('../models/parkModel.js');
const User = require("../models/userModel");

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

    getPark : async (req, res) => {
        try {
            const parks = await ParkModel.find({});
            return res.status(200).json({ parks });
        } catch (err) {
            console.error('Error retrieving parks:', err);
            return res.status(500).json({ message: 'Error retrieving parks', error: err });
        }
    },

    /**
     * parkController.create()
     */
     addParks: async (req, res) => {
        // Check if all required fields are provided
        const { name, parkId } = req.body;
        if (!name || !parkId) {
            return res.status(400).json({ msg: "Please add all values in the request body" });
        }

        // Check if the park already exists
        let foundPark = await ParkModel.findOne({ parkId: req.body.parkId });
        if (foundPark) {
            return res.status(400).json({ msg: "Park ID already in use" });
        }

        // Create and save the new park
        const park = new ParkModel({ name, parkId });
        try {
            await park.save();
            return res.status(201).json(park);
        } catch (err) {
            return res.status(500).json({ message: 'Error when creating park', error: err });
        }
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
			park.parkId = req.body.parkId ? req.body.parkId : park.parkId;
			
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
