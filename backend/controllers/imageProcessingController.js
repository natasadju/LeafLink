module.exports = {
    show: async (req, res) => {
        try {
            // Example data or operation
            const event = { message: "hello" }; // Replace this with your actual logic if needed

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
};
