var ExtremeModel = require('../models/extremeModel.js');

/**
 * extremeController.js
 *
 * @description :: Server-side logic for managing extreme events.
 */
module.exports = {

    /**
     * extremeController.list()
     */
    list: function (req, res) {
        ExtremeModel.find(function (err, events) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting extreme events.',
                    error: err
                });
            }

            return res.json(events);
        });
    },

    /**
     * extremeController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        ExtremeModel.findOne({ _id: id }, function (err, event) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting extreme event.',
                    error: err
                });
            }

            if (!event) {
                return res.status(404).json({
                    message: 'No such extreme event'
                });
            }

            return res.json(event);
        });
    },

    /**
     * extremeController.create()
     */
    create: function (req, res) {
        var event = new ExtremeModel({
            message: req.body.message,
            location: req.body.location,
            date: req.body.date,
            category: req.body.category
        });

        event.save(function (err, event) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating extreme event',
                    error: err
                });
            }

            return res.status(201).json(event);
        });
    },

    /**
     * extremeController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        ExtremeModel.findOne({ _id: id }, function (err, event) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting extreme event',
                    error: err
                });
            }

            if (!event) {
                return res.status(404).json({
                    message: 'No such extreme event'
                });
            }

            event.message = req.body.message ? req.body.message : event.message;
            event.location = req.body.location ? req.body.location : event.location;
            event.date = req.body.date ? req.body.date : event.date;
            event.category = req.body.category ? req.body.category : event.category;

            event.save(function (err, event) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating extreme event.',
                        error: err
                    });
                }

                return res.json(event);
            });
        });
    },

    /**
     * extremeController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        ExtremeModel.findByIdAndRemove(id, function (err) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the extreme event.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
