import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import axios from 'axios';

const Parks = () => {
    const [ws, setWs] = useState(null);
    const [parks, setParks] = useState([]);
    const [selectedPark, setSelectedPark] = useState(0);
    const [geoJsonData, setGeoJsonData] = useState(null);

    useEffect(() => {
        // Fetch parks data from the API
        const fetchParks = async () => {
            try {
                const response = await axios.get('http://172.211.85.100:3000/parks');
                setParks(response.data.parks); // Assuming response.data.parks contains the parks array
                setSelectedPark(response.data.parks[0]?.parkId); // Set the initial selected park
            } catch (error) {
                console.error('Error fetching parks data:', error);
            }
        };

        fetchParks();
    }, []);

    useEffect(() => {
        const socket = new WebSocket('ws://172.211.85.100:3000');

        socket.addEventListener('open', () => {
            console.log('Connected to WebSocket server');
            setWs(socket);
        });

        socket.addEventListener('message', (event) => {
            console.log('Message from server', event.data);
            const parsedData = JSON.parse(event.data);
            setGeoJsonData(parsedData);
        });

        socket.addEventListener('error', (error) => {
            console.error('WebSocket error:', error);
        });

        return () => {
            socket.close();
        };
    }, []);

    useEffect(() => {
        // Log changes to geoJsonData
        console.log('geoJsonData changed:', geoJsonData);
    }, [geoJsonData]);

    const handleParkChange = (e) => {
        setSelectedPark(Number(e.target.value));
    };

    const sendMessage = () => {
        if (ws && ws.readyState === WebSocket.OPEN && selectedPark) {
            ws.send(JSON.stringify(selectedPark));
            console.log('Sent Park ID to WebSocket:', selectedPark);
        }
    };

    return (
        <div>
            <h1>OpenStreetMap with Multiple OSM Features</h1>
            <select value={selectedPark} onChange={handleParkChange}>
                {parks.map((park) => (
                    <option key={park.parkId} value={park.parkId}>
                        {park.name}
                    </option>
                ))}
            </select>
            <button onClick={sendMessage}>Send Park ID to WebSocket</button>
            <MapContainer center={[46.5491, 15.6459]} zoom={13} style={{ height: '500px', width: '100%' }}>
                <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                />
                {geoJsonData && <GeoJSON key={JSON.stringify(geoJsonData)} data={geoJsonData} />}
            </MapContainer>
        </div>
    );
};

export default Parks;
