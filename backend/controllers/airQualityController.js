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
            station: req.body.station,
            timestamp: req.body.timestamp,
            so2: req.body.so2,
            co: req.body.co,
            pm10: req.body.pm10,
            pm25: req.body.pm25,
            o3: req.body.o3,
            no2: req.body.no2,
            benzene: req.body.benzene,
            isFake: req.body.isFake
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

            airQuality.station = req.body.station ? req.body.station : airQuality.station;
            airQuality.pm10 = req.body.pm10 ? req.body.pm10 : airQuality.pm10;
            airQuality.pm25 = req.body.pm25 ? req.body.pm25 : airQuality.pm25;
            airQuality.so2 = req.body.so2 ? req.body.so2 : airQuality.so2;
            airQuality.co = req.body.co ? req.body.co : airQuality.co;
            airQuality.o3 = req.body.o3 ? req.body.o3 : airQuality.o3;
            airQuality.no2 = req.body.no2 ? req.body.no2 : airQuality.no2;
            airQuality.benzene = req.body.benzene ? req.body.benzene : airQuality.benzene;
            airQuality.isFake = req.body.isFake !== undefined ? req.body.isFake : airQuality.isFake;
            airQuality.timestamp = req.body.timestamp ? req.body.timestamp : airQuality.timestamp;

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

        AirqualityModel.findByIdAndRemove(id, function (err) {
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
