import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, GeoJSON } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import Navbar from './NavBar.jsx';
import "../styles/NavBar.css";
import "../styles/Parks.css";
import axios from "axios";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";

const Parks = () => {
    const [ws, setWs] = useState(null);
    const [parks, setParks] = useState([]);
    const [selectedPark, setSelectedPark] = useState(0);
    const [geoJsonData, setGeoJsonData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        date: '',
        description: ''
    });
    const navigate = useNavigate();

    useEffect(() => {
        const fetchParks = async () => {
            try {
                const response = await axios.get('http://172.211.85.100:3000/parks');
                setParks(response.data.parks);
                setSelectedPark(response.data.parks[0]?.parkId);
                setLoading(false);
            } catch (error) {
                setError('Error fetching parks data');
                setLoading(false);
                console.error('Error fetching parks data:', error);
            }
        };
        try{
        const response = await axios.post("http://172.211.85.100:3000/api/v1/register", formData);
         toast.success("Registration successfull");
         navigate("/login");
       }catch(err){
         toast.error(err.message);
       }
      }else{
        toast.error("Passwords don't match");
      }
    

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
        setFormData({ ...formData, [id]: value });
    };

    const handleEventCreation = async (e) => {
        e.preventDefault();
        const { name, date, description } = formData;
        if (name && date && description && selectedPark) {
            try {
                await axios.post('http://172.211.85.100:3000/events', {
                    name,
                    location: parks.find(park => park.parkId === selectedPark)?.name || '',
                    date,
                    description
                });
                toast.success("Event created successfully");
                navigate("/events"); // Redirect to events page after successful creation
            } catch (error) {
                toast.error("Error creating event: " + error.message);
            }
        } else {
            toast.error("Please fill all inputs");
        }
    };

    return (
        <div>
            {/* Your existing JSX content */}
            <div className="form-container">
                <form className="mt-4" onSubmit={handleEventCreation}>
                    <h1 className='form-title'>Let's clean together</h1>
                    <div className="row">
                        <label htmlFor="eventName">Event Name:</label>
                        <input type="text" className="form-control" id="name" value={formData.name} onChange={handleInputChange} placeholder="Enter event name" />
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
                        <input type="date" className="form-datepicker" id="date" value={formData.date} onChange={handleInputChange} />
                    </div>
                    <label htmlFor="eventDescription" id='details-label'>Event Details:</label>
                    <div className="form-group">
                        <textarea className="form-textarea" id="description" value={formData.description} onChange={handleInputChange} rows="4" />
                    </div>
                    <button type="submit" className="btn-send">Create Event</button>
                </form>
            </div>
        </div>
    );
};

export default Parks;
