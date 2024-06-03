import React, { useState } from 'react';
import '../styles/PollenAndAir.css';
import Navbar from './NavBar';

function PollenAndAir() {
    const [isPollenOpen, setIsPollenOpen] = useState(false);
    const [isAirQualityOpen, setIsAirQualityOpen] = useState(false);
  
    const togglePollen = () => {
      setIsPollenOpen(!isPollenOpen);
      setIsAirQualityOpen(false);
    };
  
    const toggleAirQuality = () => {
      setIsAirQualityOpen(!isAirQualityOpen);
      setIsPollenOpen(false);
    };

  return (
    <div>
        <Navbar />
        <div className="container-data">
      <div className={`column ${isPollenOpen ? 'open' : ''}`} onClick={togglePollen}>
        <div className="header">
          <span>{isPollenOpen ? '▾' : '▸'}</span>
          <h2>Pollen</h2>
        </div>
        {isPollenOpen && (
          <div className="content">
            <p>Tree Pollen: High</p>
            <p>Grass Pollen: Moderate</p>
            <p>Weed Pollen: Low</p>
          </div>
        )}
      </div>
      <div className={`column ${isAirQualityOpen ? 'open' : ''}`} onClick={toggleAirQuality}>
        <div className="header">
          <span>{isAirQualityOpen ? '▾' : '▸'}</span>
          <h2>Air Quality</h2>
        </div>
        {isAirQualityOpen && (
          <div className="content">
            <p>PM2.5: 35 µg/m³ (Moderate)</p>
            <p>PM10: 45 µg/m³ (Moderate)</p>
            <p>Ozone: 50 ppb (Good)</p>
          </div>
        )}
      </div>
    </div>
    </div>
  );
}

export default PollenAndAir;
