var express = require('express');
var router = express.Router();
// var userController = require('../controllers/userController.js');

const { login, register, dashboard, getAllUsers } = require("../controllers/userController.js");
const authMiddleware = require('../middleware/authError')

router.route("/login").post(login);
router.route("/register").post(register);
router.route("/dashboard").get(authMiddleware, dashboard);
router.route("/users").get(getAllUsers);

module.exports = router;
