const EventModel = require('../models/eventModel.js');
const ParkModel = require('../models/parkModel.js');

/**
 * eventController.js
 *
 * @description :: Server-side logic for managing events.
 */
module.exports = {

    /**
     * eventController.list()
     */
    list: async (req, res) => {
        try {
            const events = await EventModel.find().populate('date'); 
            res.json(events);
        } catch (err) {
            res.status(500).json({
                message: 'Error when getting events.',
                error: err.message
            });
        }
    },

    /**
     * eventController.show()
     */
    show: async (req, res) => {
        const id = req.params.id;

        try {
            const event = await EventModel.findById(id);
            if (!event) {
                return res.status(404).json({ message: 'No such event' });
            }
            res.json(event);
        } catch (err) {
            res.status(500).json({
                message: 'Error when getting event.',
                error: err.message
            });
        }
    },

    /**
     * eventController.create()
     */
    create: async (req, res) => {
        try {
            const park = await ParkModel.findById(req.body.location); 
            if (!park) {
                return res.status(404).json({ message: 'Park not found' });
            }

            const event = new EventModel({
                name: req.body.name,
                parkId: req.body.location, 
                date: req.body.date,
                description: req.body.description
                // organizer: req.body.organizer
                
            });

            const savedEvent = await event.save();
            res.status(201).json(savedEvent);
        } catch (err) {
            res.status(500).json({
                message: 'Error when creating event',
                error: err.message
            });
        }
    },
    /**
     * eventController.update()
     */
    update: function(req, res){
        // Add validation rules
        body('name').optional().notEmpty().withMessage('Name is required'),
        body('location').optional().notEmpty().withMessage('Location is required'),
        body('date').optional().notEmpty().withMessage('Date is required'),
        body('description').optional().notEmpty().withMessage('Description is required'),

        async (req, res) => {
            const id = req.params.id;

            // Handle validation errors
            const errors = validationResult(req);
            if (!errors.isEmpty()) {
                return res.status(400).json({ errors: errors.array() });
            }

            try {
                const event = await EventModel.findById(id);
                if (!event) {
                    return res.status(404).json({ message: 'No such event' });
                }

                event.name = req.body.name || event.name;
                event.location = req.body.location || event.location;
                event.date = req.body.date || event.date;
                event.description = req.body.description || event.description;
                event.organizer = req.body.organizer || event.organizer;

                const updatedEvent = await event.save();
                res.json(updatedEvent);
            } catch (err) {
                res.status(500).json({
                    message: 'Error when updating event.',
                    error: err.message
                });
            }
        }
    },

    /**
     * eventController.remove()
     */
    remove: async (req, res) => {
        const id = req.params.id;

        try {
            await EventModel.findByIdAndRemove(id);
            res.status(204).json();
        } catch (err) {
            res.status(500).json({
                message: 'Error when deleting the event.',
                error: err.message
            });
        }
    },
};
