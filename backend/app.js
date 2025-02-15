require("dotenv").config();
require('express-async-errors');
var bodyParser = require('body-parser');
const connectDB = require("./db/connect");
const express = require("express");
const cors = require('cors');
var path = require('path');
// Added the websocket
const http = require('http');
const WebSocket = require('ws');
const osmtogeojson = require('osmtogeojson');
const axios = require('axios'); // Import Axios here
// .
const app = express();
const mainRouter = require("./routes/userRoutes");

// Middleware to parse the request body
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}))

const allowedOrigins = [
    'http://localhost:5173',
    'http://localhost:3000',
    'http://172.211.85.100:5173',
    'http://172.211.85.100:3000'
];

app.use(express.json());
app.use(cors());
app.use("/api/v1", mainRouter);

// added a parkRouter
const parkRouter = require("./routes/parkRoutes");
app.use("/parks", parkRouter);

const airQualityRouter = require("./routes/airQualityRoutes");
app.use('/air', airQualityRouter);

const pollenRouter = require("./routes/pollenRoutes");
app.use('/pollen', pollenRouter);

app.use(express.static(path.join(__dirname, 'public')));
var imageRouter = require("./routes/imageRoutes");
app.use("/images", imageRouter);

var eventRouter = require('./routes/eventRoutes');
app.use('/events', eventRouter);

const imageProcessRouter = require("./routes/scanImageRoutes");
app.use('/processImages', imageProcessRouter);

const extremeRouter = require('./routes/extremeRoutes');
app.use('/extreme', extremeRouter);

const port = process.env.PORT || 3000;

// Created an HTTP server
const server = http.createServer(app);

// Initialized the WebSocket server instance
const wss = new WebSocket.Server({server});

// WebSocket connection handling
wss.on('connection', (ws) => {
    console.log('Client connected');

    ws.on('message', async (message) => {
        console.log('Received:', message);

        try {
            const selectedParkId = JSON.parse(message);

            // Fetch data from OpenStreetMap using the selectedParkId
            const osmData = await fetchDataFromOpenStreetMap(selectedParkId);

            // Convert OpenStreetMap data to GeoJSON
            const geoJsonData = osmtogeojson(osmData);

            // Send the GeoJSON data back to the client
            ws.send(JSON.stringify(geoJsonData));
        } catch (error) {
            console.error('Error processing message:', error);
            ws.send(JSON.stringify({error: 'Error processing request'}));
        }
    });

    ws.on('close', () => {
        console.log('Client disconnected');
    });
});

// Function to fetch data from OpenStreetMap
async function fetchDataFromOpenStreetMap(parkId) {
    console.log(parkId)
    try {
        const response = await axios.get(`https://overpass-api.de/api/interpreter?data=[out:json];way(${parkId});out body;>;out skel qt;`);
        return response.data; // Assuming the response contains the desired data from OpenStreetMap
    } catch (error) {
        console.error('Error fetching data from OpenStreetMap:', error);
        throw error; // Handle the error appropriately in your application
    }
}

const start = async () => {
    try {
        await connectDB(process.env.MONGO_URI);
        server.listen(port, () => {
            console.log(`Server is listening on port ${port}`);
        });
    } catch (error) {
        console.log(error);
    }
};

start();
module.exports = app;
