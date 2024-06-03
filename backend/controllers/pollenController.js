var PollenModel = require('../models/pollenModel.js');

/**
 * pollenController.js
 *
 * @description :: Server-side logic for managing pollens.
 */
module.exports = {

    /**
     * pollenController.list()
     */
    list: function (req, res) {
        PollenModel.find(function (err, pollens) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting pollen.',
                    error: err
                });
            }

            return res.json(pollens);
        });
    },

    /**
     * pollenController.show()
     */
    show: function (req, res) {
        var id = req.params.id;

        PollenModel.findOne({_id: id}, function (err, pollen) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting pollen.',
                    error: err
                });
            }

            if (!pollen) {
                return res.status(404).json({
                    message: 'No such pollen'
                });
            }

            return res.json(pollen);
        });
    },

    /**
     * pollenController.create()
     */
    create: function (req, res) {
        var pollen = new PollenModel({
			type : req.body.type,
			value : req.body.value,
			timestamp : req.body.timestamp
        });

        pollen.save(function (err, pollen) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when creating pollen',
                    error: err
                });
            }

            return res.status(201).json(pollen);
        });
    },

    /**
     * pollenController.update()
     */
    update: function (req, res) {
        var id = req.params.id;

        PollenModel.findOne({_id: id}, function (err, pollen) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when getting pollen',
                    error: err
                });
            }

            if (!pollen) {
                return res.status(404).json({
                    message: 'No such pollen'
                });
            }

            pollen.type = req.body.type ? req.body.type : pollen.type;
			pollen.value = req.body.value ? req.body.value : pollen.value;
			pollen.timestamp = req.body.timestamp ? req.body.timestamp : pollen.timestamp;
			
            pollen.save(function (err, pollen) {
                if (err) {
                    return res.status(500).json({
                        message: 'Error when updating pollen.',
                        error: err
                    });
                }

                return res.json(pollen);
            });
        });
    },

    /**
     * pollenController.remove()
     */
    remove: function (req, res) {
        var id = req.params.id;

        PollenModel.findByIdAndRemove(id, function (err, pollen) {
            if (err) {
                return res.status(500).json({
                    message: 'Error when deleting the pollen.',
                    error: err
                });
            }

            return res.status(204).json();
        });
    }
};
