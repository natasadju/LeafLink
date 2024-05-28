import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import '../styles/Events.css';

const EventsPage = () => {
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [mapCenter, setMapCenter] = useState([46.5491, 15.6459]); // Default center of the map

    

    useEffect(() => {
        const fetchEvents = async () => {
            try {
                const response = await axios.get('http://172.211.85.100:3000/events');
                const eventsWithParkData = await Promise.all(response.data.map(async event => {
                    // Fetch park data based on parkId
                    const parkResponse = await axios.get(`http://172.211.85.100:3000/parks/${event.parkId}`);
                    const parkData = parkResponse.data;
                    return {
                        ...event,
                        parkName: parkData.name,
                        parkLocation: [parkData.lat, parkData.lon] // Assuming park data includes latitude and longitude
                    };
                }));
                setEvents(eventsWithParkData);
                setLoading(false);
            } catch (error) {
                setError('Error fetching events data');
                setLoading(false);
                console.error('Error fetching events data:', error);
            }
        };
        

        fetchEvents();
    }, []);

    return (
        <div>
            <h3>All Events</h3>
            {loading ? (
                <p>Loading...</p>
            ) : error ? (
                <p>{error}</p>
            ) : (
                <div>
                    <MapContainer center={mapCenter} zoom={13} style={{ height: '500px', width: '100%' }}>
                        <TileLayer
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        />
                        {events.map(event => (
                            <Marker position={event.position || mapCenter} key={event._id}>
                                <Popup>
                                    <div>
                                        <h3>{event.name}</h3>
                                        <p>Date: {new Date(event.date).toLocaleDateString()}</p>
                                        <p>Description: {event.description}</p>
                                        <p>Park: {event.parkName}</p> {/* Display park name */}
                                    </div>
                                </Popup>
                            </Marker>
                        ))}
                    </MapContainer>
                    <div className="cards-container">
                        {events.map(event => (
                            <div key={event._id} className="card">
                                <h2>{event.name}</h2>
                                <p>Date: {new Date(event.date).toLocaleDateString()}</p>
                                <p>Description: {event.description}</p>
                                <p>Park: {event.parkName}</p> {/* Display park name */}
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default EventsPage;
