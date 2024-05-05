var AirqualityModel = require('../models/airQualityModel.js');

/**
 * airQualityController.js
 *
 * @description :: Server-side logic for managing airQualitys.
 */
module.exports = {

    /**
     * airQualityController.list()
     */
    list: function (req, res) {
        AirqualityModel.find(function (err, airQualitys) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting airQuality.',
                    error: err
                });
            }

            return res.json(airQualitys);
        });
    },

    /**
     * airQualityController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        AirqualityModel.findOne({_id: id}, function (err, airQuality) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting airQuality.',
                    error: err
                });
            }

            if (!airQuality) {
                return res.status(404).json({
                    message: 'No such airQuality'
                });
            }

            return res.json(airQuality);
        });
    },

    /**
     * airQualityController.create()
     */
    create: function (req, res) {
        var airQuality = new AirqualityModel({
			location : req.body.location,
			timestamp : req.body.timestamp,
			pm10 : req.body.pm10,
			pm25 : req.body.pm25,
			benzene : req.body.benzene
        });

        airQuality.save(function (err, airQuality) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating airQuality',
                    error: err
                });
            }

            return res.status(201).json(airQuality);
        });
    },

    /**
     * airQualityController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        AirqualityModel.findOne({_id: id}, function (err, airQuality) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting airQuality',
                    error: err
                });
            }

            if (!airQuality) {
                return res.status(404).json({
                    message: 'No such airQuality'
                });
            }

            airQuality.location = req.body.location ? req.body.location : airQuality.location;
			airQuality.timestamp = req.body.timestamp ? req.body.timestamp : airQuality.timestamp;
			airQuality.pm10 = req.body.pm10 ? req.body.pm10 : airQuality.pm10;
			airQuality.pm25 = req.body.pm25 ? req.body.pm25 : airQuality.pm25;
			airQuality.benzene = req.body.benzene ? req.body.benzene : airQuality.benzene;
			
            airQuality.save(function (err, airQuality) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating airQuality.',
                        error: err
                    });
                }

                return res.json(airQuality);
            });
        });
    },

    /**
     * airQualityController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        AirqualityModel.findByIdAndRemove(id, function (err, airQuality) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the airQuality.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
