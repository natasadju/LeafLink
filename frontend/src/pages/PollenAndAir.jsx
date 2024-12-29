import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../styles/PollenAndAir.css';
import Navbar from './NavBar';
import AirQualityTable from './AirQualityTable'; 
import AirQualityGraph from './AirQualityGraph'; 
import PollenData from './PollenData';


function PollenAndAir() {
  const [isPollenOpen, setIsPollenOpen] = useState(false);
  const [isAirQualityOpen, setIsAirQualityOpen] = useState(false);
  const [pollenData, setPollenData] = useState([]);
  const [airQualityData, setAirQualityData] = useState([]);
  const [showFakeAirQuality, setShowFakeAirQuality] = useState(false);
  const [graphKey, setGraphKey] = useState(0); // Key for forcing re-render of the graphs

  const togglePollen = () => {
    setIsPollenOpen(!isPollenOpen);
  };

  const toggleAirQuality = () => {
    setIsAirQualityOpen(!isAirQualityOpen);
  };

  useEffect(() => {
    const fetchPollenData = async () => {
      try {
        const response = await axios.get('http://172.211.85.100:3000/pollen');
        const sortedPollenData = response.data.sort((a, b) => a.type.localeCompare(b.type));
        setPollenData(sortedPollenData);
      } catch (error) {
        console.error('Error fetching pollen data:', error);
      }
    };

    const fetchAirQualityData = async () => {
      try {
        const response = await axios.get('http://172.211.85.100:3000/air');
        let filteredAirQualityData = response.data;
        if (showFakeAirQuality) {
          filteredAirQualityData = filteredAirQualityData.filter(d => !d.isFake); // Filter out fake data if showFakeAirQuality is true
        }
        const sortedAirQualityData = filteredAirQualityData.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));
        setAirQualityData(sortedAirQualityData);
      } catch (error) {
        console.error('Error fetching air quality data:', error);
      }
    };

    fetchPollenData();
    fetchAirQualityData();
  }, [showFakeAirQuality]); // Include showFakeAirQuality in dependency array

  const handleToggleFakeAirQuality = () => {
    setShowFakeAirQuality(!showFakeAirQuality); // Toggle the state of showFakeAirQuality
    setGraphKey(prevKey => prevKey + 1); // Update the key to force re-render of the graphs
  };

  return (
    <div>
      <Navbar />
      <div className="container-data">
        <div className={`column ${isPollenOpen ? 'open' : ''}`}>
          <div className="header" onClick={togglePollen}>
            <span>{isPollenOpen ? <i className="fa-solid fa-chevron-down"></i> : <i className="fa-solid fa-angle-right"></i>}</span>
            <h2 className="headerText"><i className="fas fa-seedling"></i>Pollen</h2>
          </div>
          {isPollenOpen && (
            <div className="content">
              <PollenData data={pollenData} />
            </div>
          )}
        </div>
        <div className={`column ${isAirQualityOpen ? 'open' : ''}`}>
          <div className="header" onClick={toggleAirQuality}>
            <span>{isAirQualityOpen ? <i className="fa-solid fa-chevron-down"></i> : <i className="fa-solid fa-angle-right"></i>}</span>
            <h2 className="headerText"><i className="fa-solid fa-wind"></i>Air Quality</h2>
          </div>
          {isAirQualityOpen && (
            <div className="content">
              <div className="filterFakeAirQuality">
                <label> 
                  <input type="checkbox" onChange={handleToggleFakeAirQuality} checked={showFakeAirQuality} /> Remove fake data for air quality
                </label>
              </div>
              <div className="airQualityTablesAndGraphs">
                <div className="airQualityTables">
                  <AirQualityTable station="MB Vrbanski" data={airQualityData.filter(d => d.station === "MB Vrbanski")} />
                  <AirQualityTable station="MB Titova" data={airQualityData.filter(d => d.station === "MB Titova")} />
                </div>
                <div className="airQualityGraphs">
                  <AirQualityGraph key={graphKey} data={airQualityData.filter(d => d.station === "MB Vrbanski" && (!showFakeAirQuality || !d.isFake))} />
                  <AirQualityGraph key={graphKey + 1} data={airQualityData.filter(d => d.station === "MB Titova" && (!showFakeAirQuality || !d.isFake))} />
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default PollenAndAir;
