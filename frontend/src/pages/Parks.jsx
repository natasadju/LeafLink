import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import axios from 'axios';
import Navbar from './NavBar.jsx';
import "../styles/NavBar.css";
import "../styles/Parks.css";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";

const Parks = () => {
    const [ws, setWs] = useState(null);
    const [parks, setParks] = useState([]);
    const [selectedPark, setSelectedPark] = useState(0);
    const [geoJsonData, setGeoJsonData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [date, setDate] = useState('');
    const navigate = useNavigate();
    

    useEffect(() => {
        const fetchParks = async () => {
            try {
                const response = await axios.get('http://172.211.85.100:3000/parks');
                setParks(response.data.parks); // Assuming response.data.parks contains the parks array
                setSelectedPark(response.data.parks[0]?.parkId); // Set the initial selected park
                setLoading(false);
            } catch (error) {
                setError('Error fetching parks data');
                setLoading(false);
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

        socket.addEventListener('close', () => {
            console.log('WebSocket connection closed. Reconnecting...');
        });

        return () => {
            socket.close();
        };
    }, []);

    const handleParkChange = (e) => {
        setSelectedPark(Number(e.target.value));
    };

    const handleInputChange = (e) => {
        const { id, value } = e.target;
        if(id === "name"){
            setName(value);
        }
        else if(id === "description"){
            setDescription(value);
        }
        else if(id === "date"){
            setDate(value);
        }
    };

    const handleEventCreation = async (e) => {
        e.preventDefault();
        if (name && selectedPark && date) {
            try {
                await axios.post('http://172.211.85.100:3000/events', {
                    name,
                    location: parks.find(park => park.parkId === selectedPark)?._id, // Use the _id of the selected park
                    description,
                    date
                });
                toast.success("Event created successfully");
                navigate("/dashboard"); // Redirect to events page after successful creation
            } catch (error) {
                toast.error("Error creating event: " + error.message);
            }
        } else {
            toast.error("Please fill all inputs");
        }
    };

    const sendMessage = () => {
        if (ws && ws.readyState === WebSocket.OPEN && selectedPark) {
            ws.send(JSON.stringify(selectedPark));
        }
    };

    return (
        <div>
            <Navbar />
            {loading ? (
                <p>Loading...</p>
            ) : error ? (
                <p>{error}</p>
            ) : (
                <div className="container mt-4">
                    <div className="map-container">
                    <div className="map-chooser">
                        <label htmlFor="parkSelect" className="form-label">Select Park: </label>
                        <select 
                            className="demo-simple-select-label"
                            value={selectedPark} 
                            onChange={handleParkChange}
                        >
                            {parks.map((park) => (
                                <option key={park.parkId} value={park.parkId}>
                                    {park.name}
                                </option>
                            ))}
                        </select>
                        <button className="map-button" onClick={sendMessage}>Find this green space </button>
                    </div>
                        <MapContainer center={[46.5491, 15.6459]} zoom={13} style={{ height: '500px', width: '100%' }}>
                            <TileLayer
                                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                            />
                            {geoJsonData && <GeoJSON key={JSON.stringify(geoJsonData)} data={geoJsonData} />}
                        </MapContainer>
                    </div>
                    
                    <div className="form-container">
                        <form className="mt-4" onSubmit={handleEventCreation}>
                            <h1 className='form-title'>Let's clean together</h1>
                            <div className="row">
                                <label htmlFor="eventName">Event Name:</label>
                                <input type="text" className="form-control" id="name" value={name} onChange={handleInputChange} placeholder="Enter event name" />
                            </div>
                            <label htmlFor="location">Location:</label>
                            <input
                                type="text"
                                className="form-control"
                                id="location"
                                value={parks.find(park => park.parkId === selectedPark)?.name || ''}
                                readOnly
                            />
                            <div className="form-group">
                                <label htmlFor="eventDate">Event Date:</label>
                                <input type="date" className="form-datepicker" id="date" value={date} onChange={handleInputChange} />
                            </div>
                            <label htmlFor="eventDescription" id='details-label'>Event Details:</label>
                            <div className="form-group">
                                <textarea className="form-textarea" id="description" value={description} onChange={handleInputChange} rows="4" />
                            </div>
                            <button type="submit" className="btn-send">Create Event</button>
                        </form>
                    </div>
                </div>

            )}
        </div>
    );
};

export default Parks;
