import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';

const parks = [
    { name: 'Mestni park', id: 10473149 },
    { name: 'Magdalenski park', id: 171810350 }
];

const Parks = () => {
    const [ws, setWs] = useState(null);
    const [selectedPark, setSelectedPark] = useState(parks[0].id);
    const [geoJsonData, setGeoJsonData] = useState(null);

    useEffect(() => {
        const socket = new WebSocket('ws://localhost:3000');

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
                {parks.map((park, index) => (
                    <option key={index} value={park.id}>
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
