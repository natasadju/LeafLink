import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { useNavigate } from 'react-router-dom';
import '../styles/Events.css';

const EventsPage = () => {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [mapCenter, setMapCenter] = useState([51.505, -0.09]);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchEvents = async () => {
            try {
                const response = await axios.get('http://172.211.85.100:3000/events');
                const eventsWithParkData = await Promise.all(response.data.map(async event => {
                    const parkResponse = await axios.get(`http://172.211.85.100:3000/parks/${event.parkId}`);
                    const parkData = parkResponse.data;
                    return {
                        ...event,
                        parkName: parkData.name,
                        parkLocation: [parkData.lat, parkData.long],
                    };
                }));
                setEvents(eventsWithParkData);
                setLoading(false);

                if (eventsWithParkData.length > 0) {
                    setMapCenter(eventsWithParkData[0].parkLocation);
                }
            } catch (error) {
                setError('Error fetching events data');
                setLoading(false);
                console.error('Error fetching events data:', error);
            }
        };

        fetchEvents();
    }, []);

    const showEventDetails = (eventId) => {
        navigate(`/events/${eventId}`);
    };

    return (
        <div className='container'>
            {loading ? (
                <p>Loading...</p>
            ) : error ? (
                <p>{error}</p>
            ) : (
                <div className='box'>
                    <MapContainer center={mapCenter} zoom={13} style={{ height: '500px', width: '100%' }}>
                        <TileLayer
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        />
                        {events.map(event => (
                            <Marker position={event.parkLocation} key={event._id}>
                                <Popup>
                                    <div>
                                        <h2 style={{"color": "#4b7f52"}}>{event.name}</h2>
                                        <p><i className="fa-regular fa-calendar"></i> {new Date(event.date).toLocaleDateString()}</p>
                                        <p><i className="fa-regular fa-clock"></i> {new Date(event.date).toLocaleTimeString([], { hour12: false, timeZone: 'UTC' })}</p>
                                        <p><i className="fa-solid fa-location-dot"></i> {event.parkName}</p>
                                    </div>
                                </Popup>
                            </Marker>
                        ))}
                    </MapContainer>
                    <div className="cards-container">
                        {events.map(event => (
                            <div key={event._id} className="card">
                                <div className="event-details">
                                    <h3>{event.name}</h3>
                                    <p><i className="fa-regular fa-calendar"></i>Date: {new Date(event.date).toLocaleDateString()}</p>
                                    <p><i className="fa-regular fa-clock"></i>Time: {new Date(event.date).toLocaleTimeString([], { hour12: false, timeZone: 'UTC' })}</p>
                                    <p><i className="fa-solid fa-location-dot"></i>Park: {event.parkName}</p>
                                </div>
                                <div className='btn-container'>
                                    <button onClick={() => showEventDetails(event._id)} className='btn-view'>View details</button>
                                </div>
                            </div>
                        ))}
                    </div> 
                </div>
            )}
        </div>
    );
};

export default EventsPage;
