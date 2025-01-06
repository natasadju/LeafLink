const express = require("express");
const multer = require("multer");
const path = require("path");
const { spawn } = require("child_process");
const fs = require("fs");
const imageProcessingController = require("../controllers/imageProcessingController");

const router = express.Router();

// Configure multer for file uploads
const upload = multer({
    dest: "uploads/", // Directory where uploaded files are temporarily stored
    limits: { fileSize: 5 * 1024 * 1024 }, // 5MB limit
    fileFilter: (req, file, cb) => {
        console.log("Checking file type...");
        if (file.mimetype.startsWith("image/")) {
            console.log("File is an image. Proceeding...");
            cb(null, true);
        } else {
            console.error("File is not an image. Rejecting...");
            cb(new Error("Only image files are allowed!"), false);
        }
    },
});

// Usage in your route
router.post("/upload", upload.single("file"), async (req, res) => {
    try {
        console.log("File upload request received.");
        const filePath = req.file.path;
        console.log(`File saved temporarily at: ${filePath}`);

        const pythonScript = path.join(__dirname, "../scripts", "model.py");

        // Spawn a Python process
        const pythonProcess = spawn("python3", [pythonScript, filePath]);

        let pythonOutput = ""; // Variable to capture Python output

        // Capture the standard output from Python script
        pythonProcess.stdout.on("data", (data) => {
            console.log(`Python stdout: ${data}`);
            pythonOutput += data.toString(); // Append the output
        });

        // Capture any error messages from Python script
        pythonProcess.stderr.on("data", (data) => {
            console.error(`Python stderr: ${data}`);
        });

        // Handle Python process exit
        pythonProcess.on("close", (code) => {
            if (code !== 0) {
                console.error(`Python script exited with code ${code}`);
                return res.status(500).json({ error: "Error processing the image" });
            }

            console.log("Python script executed successfully.");
            console.log("Python output:", pythonOutput);

            const cleanedOutput = pythonOutput.replace(/\r\n|\r|\n/g, "");

            // Delete the uploaded image after processing
            fs.unlink(filePath, (err) => {
                if (err) {
                    console.error("Error deleting the uploaded image:", err);
                } else {
                    console.log("Uploaded image deleted successfully.");
                }
            });

            // Return the result from the Python script as the response
            res.json({ result: cleanedOutput });
        });
    } catch (error) {
        console.error("Error handling image upload:", error);
        res.status(500).json({ error: "Error uploading the image" });
    }
});

// Health check endpoint
router.get('/check', (req, res) => {
    console.log("Health check endpoint called.");
    imageProcessingController.show(req, res);
});

module.exports = router;
