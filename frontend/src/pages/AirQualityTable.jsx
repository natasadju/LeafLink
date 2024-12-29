import React from 'react';

function AirQualityTable({ station, data }) {
  // Threshold values
  const thresholds = {
    pm10: 20,
    pm25: 15,
    ozon: 50,
    no2: 20,
    benzen: 0.5
  };

  // Function to determine CSS class based on value and threshold
  const getColorClass = (value, threshold) => {
    if (value >= threshold) {
      return { backgroundColor: '#aaffaa' }; // Light green
    }
    return {};
  };

  // Function to determine if the air is clean for the entire column
  const isColumnClean = (columnName) => {
    for (const item of data) {
      if (item[columnName] > thresholds[columnName]) {
        return false;
      }
    }
    return true;
  };

  return (
    <div className="airQualityTable">
      <h3>{station}</h3>
      <table>
        <thead>
          <tr>
            <th>Timestamp</th>
            <th style={isColumnClean('pm10') ? { backgroundColor: '#aaffaa' } : {}}>PM10</th>
            <th style={isColumnClean('pm25') ? { backgroundColor: '#aaffaa' } : {}}>PM25</th>
            <th style={isColumnClean('ozon') ? { backgroundColor: '#aaffaa' } : {}}>Ozone</th>
            <th style={isColumnClean('no2') ? { backgroundColor: '#aaffaa' } : {}}>NO2</th>
            <th style={isColumnClean('benzen') ? { backgroundColor: '#aaffaa' } : {}}>Benzene</th>
          </tr>
        </thead>
        <tbody>
          {data.map((item, index) => (
            <tr key={index}>
              <td>{new Date(item.timestamp).toLocaleString()}</td>
              <td style={getColorClass(item.pm10, thresholds.pm10)}>{item.pm10}</td>
              <td style={getColorClass(item.pm25, thresholds.pm25)}>{item.pm25}</td>
              <td style={getColorClass(item.ozon, thresholds.ozon)}>{item.ozon}</td>
              <td style={getColorClass(item.no2, thresholds.no2)}>{item.no2}</td>
              <td style={getColorClass(item.benzen, thresholds.benzen)}>{item.benzen}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default AirQualityTable;
