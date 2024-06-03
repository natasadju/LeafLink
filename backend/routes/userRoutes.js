var express = require('express');
var router = express.Router();
// var userController = require('../controllers/userController.js');

const {login, register, dashboard, getAllUsers} = require("../controllers/userController.js");
const {addParks} = require("../controllers/parkController.js");
const authMiddleware = require('../middleware/authError')
const {update} = require("../controllers/userController");

router.route("/login").post(login);

router.get('/getusers', async (req, res) => {
    try {
        let users = await getAllUsers(req, res); // Call the getAllUsers function
        return res.status(200).json({users});
    } catch (error) {
        return res.status(500).json({error: 'Internal Server Error'});
    }
});

router.post('/register', async (req, res) => {
    try {
        let result = await register(req, res);
        return res.status(200).json({result});
    } catch (error) {
        return res.status(500).json({error: 'Internal Server Error'});
    }
});

// router.route('/register').post(register);
router.route("/dashboard").get(authMiddleware, dashboard);
router.route("/users").get(getAllUsers);
router.route("/users/:id").put(update);

module.exports = router;
