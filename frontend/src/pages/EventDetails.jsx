import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import Navbar from "./NavBar.jsx";
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import '../styles/EventDetails.css';
import { toast } from 'react-toastify';

const EventDetails = () => {
    const { eventId } = useParams();
    const [event, setEvent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [photo, setPhoto] = useState(null);
    const [images, setImages] = useState([]);

    useEffect(() => {
        const fetchEvent = async () => {
            try {
                const response = await axios.get(`http://172.211.85.100:3000/events/${eventId}`);
                const eventData = response.data;

                const parkResponse = await axios.get(`http://172.211.85.100:3000/parks/${eventData.parkId}`);
                const parkData = parkResponse.data;

                setEvent({
                    ...eventData,
                    parkName: parkData.name,
                    parkLocation: [parkData.lat, parkData.long],
                });

                setLoading(false);
            } catch (error) {
                setError('Error fetching event data');
                setLoading(false);
                console.error('Error fetching event data:', error);
            }
        };

        fetchEvent();
    }, [eventId]);

    useEffect(() => {
        const fetchImages = async () => {
            try {
                const response = await axios.get(`http://localhost:3000/images`);
                setImages(response.data.filter(image => image.event === eventId));
            } catch (error) {
                console.error('Error fetching images:', error);
            }
        };

        fetchImages();
    }, [eventId]);

    const handleImageUpload = async (e) => {
        e.preventDefault();
        if (!photo) {
            toast.error('No photo selected.');
            return;
        }

        try {
            const formData = new FormData();
            formData.append('file', photo);
            formData.append('event', eventId);

            // Log formData content
            for (let [key, value] of formData.entries()) { 
                console.log(`${key}:`, value);
        }

            // Send image data to the server
            const response = await axios.post(`http://localhost:3000/images`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });

            // Handle response from the server, if needed
            toast.success('Image uploaded successfully:', response.data);
            window.location.reload();
        } catch (error) {
            toast.error('Error uploading image:', error);
        }
    };

    if (loading) return <p>Loading...</p>;
    if (error) return <p>{error}</p>;

    return (
        <div>
            <Navbar />
            <div className='card-container'>
                <div className="map-container">
                    <MapContainer center={event.parkLocation} zoom={16} scrollWheelZoom={true} style={{ height: "400px", width: "100%" }}>
                        <TileLayer
                            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        />
                        <Marker position={event.parkLocation} >
                            <Popup>
                                <div>
                                    <h2 style={{"color": "#4b7f52"}}>{event.name}</h2>
                                    <p><i className="fa-regular fa-calendar"></i> {new Date(event.date).toLocaleDateString()}</p>
                                    <p><i className="fa-regular fa-clock"></i> {new Date(event.date).toLocaleTimeString([], { hour12: false, timeZone: 'UTC' })}</p>
                                    <p><i className="fa-solid fa-location-dot"></i> {event.parkName}</p>
                                </div>
                            </Popup>
                        </Marker>
                    </MapContainer>
                </div>
                <div className='details-box'>
                    <h1>{event.name}</h1>
                    <div className='details'>
                        <div className='details-info'>
                          <p><i className="fa-regular fa-calendar"></i> {new Date(event.date).toLocaleDateString()}</p>
                          <p><i className="fa-regular fa-clock"></i> {new Date(event.date).toLocaleTimeString([], { hour12: false, timeZone: 'UTC' })}</p>
                          <p><i className="fa-solid fa-location-dot"></i>  {event.parkName}</p>
                          <form onSubmit={handleImageUpload} className='upload-form'>
                                <label htmlFor="photo-upload" className='upload-label'>Select a photo:</label>
                                <input type="file" accept="image/*" id="photo-upload" onChange={(e) => setPhoto(e.target.files[0])} className='upload-input' />
                                <button type="submit" className='upload-button'>Upload Photo</button>
                          </form>
                        </div>
                        <div className='details-description'>
                            <p><i className="fa-regular fa-comment"></i><b>Event description:</b></p>
                            <p>{event.description}</p>
                        </div>
                    </div>
                </div>
                <div class="gallery">
                    <input type="checkbox"/>
                    {images.map((image, index) => (
                        <img key={index} src={`http://localhost:3000/${image.imageUrl}`} alt={`Event image ${index + 1}`} />
                    ))}    
                </div>

            </div>
        </div>
    );
};

export default EventDetails;
