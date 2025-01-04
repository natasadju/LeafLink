const express = require("express");
const multer = require("multer");
const { PythonShell } = require("python-shell");
const path = require("path");

const router = express.Router();

// Configure multer for file uploads
const upload = multer({
    dest: "uploads/", // Directory where uploaded files are temporarily stored
    limits: { fileSize: 5 * 1024 * 1024 }, // 5MB limit
    fileFilter: (req, file, cb) => {
        if (file.mimetype.startsWith("image/")) {
            cb(null, true);
        } else {
            cb(new Error("Only image files are allowed!"), false);
        }
    },
});

// Endpoint for uploading and processing the image
router.post("/upload", upload.single("file"), async (req, res) => {
    try {
        const filePath = req.file.path;

        // Run Python script to process the image
        const options = {
            mode: "text",
            pythonOptions: ["-u"], // Unbuffered output
            scriptPath: path.join(__dirname, "../scripts"), // Path to Python scripts
            args: [filePath], // Pass the file path as an argument to the Python script
        };

        PythonShell.run("model.py", options, (err, results) => {
            if (err) {
                console.error("Error running Python script:", err);
                return res.status(500).json({ error: "Error processing the image" });
            }

            // Results from the Python script
            const output = results ? results[0] : null;
            console.log("Python script output:", output);

            res.json({ result: output });
        });
    } catch (error) {
        console.error("Error handling image upload:", error);
        res.status(500).json({ error: "Error uploading the image" });
    }
});

module.exports = router;
