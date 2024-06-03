const jwt = require("jsonwebtoken");
const User = require("../models/userModel");

const login = async (req, res) => {
    const {email, password} = req.body;

    if (!email || !password) {
        return res.status(400).json({
            msg: "Bad request. Please add email and password in the request body",
        });
    }

    let foundUser = await User.findOne({email: req.body.email});
    if (foundUser) {
        const isMatch = await foundUser.comparePassword(password);

        if (isMatch) {
            const token = jwt.sign(
                {id: foundUser._id, name: foundUser.name},
                process.env.JWT_SECRET,
                {
                    expiresIn: "30d",
                }
            );

            return res.status(200).json({msg: "user logged in", token});
        } else {
            return res.status(400).json({msg: "Bad password"});
        }
    } else {
        return res.status(400).json({msg: "Bad credentails"});
    }
};

const dashboard = async (req, res) => {

  res.status(200).json({
    msg: `Hello, ${req.user.name}`
  });
};

const getAllUsers = async (req, res) => {
    let users = await User.find({});
    return res.status(200).json({users});
};

const register = async (req, res) => {
    let foundUser = await User.findOne({email: req.body.email});
    if (foundUser === null) {
        let {username, email, password} = req.body;
        if (username.length && email.length && password.length) {
            const person = new User({
                name: username,
                email: email,
                password: password,
            });
            await person.save();
            return res.status(200).json({person});
        } else {
            return res.status(400).json({msg: "Please add all values in the request body"});
        }
    } else {
        return res.status(400).json({msg: "Email already in use"});
    }
};

const update = async (req, res) => {
    const id = req.params.id;

    try {
        let user = await User.findOne({_id: id});
        if (!user) {
            return res.status(404).json({msg: "No such user"});
        }

        user.name = req.body.name ? req.body.name : user.name;
        user.email = req.body.email ? req.body.email : user.email;
        user.password = req.body.password ? req.body.password : user.password;

        await user.save();
        return res.json(user);
    } catch (err) {
        return res.status(500).json({
            msg: 'Error when updating user.',
            error: err
        });
    }
};




module.exports = {
    login,
    register,
    dashboard,
    getAllUsers,
    update
};
