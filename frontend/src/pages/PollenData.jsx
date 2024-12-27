import React, { useState, useEffect } from 'react';
import * as d3 from 'd3';
import '../styles/PollenData.css'; // Import the CSS file

const PollenData = ({ data }) => {
  const [selectedType, setSelectedType] = useState('');
  const [hideFakeData, setHideFakeData] = useState(false);

  // Extract unique pollen types from data and exclude fake data
  const pollenTypes = [...new Set(data.filter(item => !item.isFake).map(item => item.type))];

  const filteredData = selectedType ? 
  data.filter(item => item.type === selectedType && (!hideFakeData || !item.isFake)) : [];

  const chartData = filteredData.map(item => ({
    date: new Date(item.timestamp),
    time: new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    value: item.value
  }));

  // Function to draw the chart
  const drawChart = () => {
    const margin = { top: 20, right: 30, bottom: 50, left: 40 };
    const width = 650 - margin.left - margin.right;
    const height = 400 - margin.top - margin.bottom;

    // Remove existing chart if any
    d3.select('#pollen-chart').selectAll('*').remove();

    // Append SVG to the chart container
    const svg = d3.select('#pollen-chart')
      .append('svg')
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    // Define gradient
    const defs = svg.append('defs');
    const gradient = defs.append('linearGradient')
      .attr('id', 'area-gradient')
      .attr('x1', '0%')
      .attr('y1', '0%')
      .attr('x2', '0%')
      .attr('y2', '100%');
    
    gradient.append('stop')
      .attr('offset', '0%')
      .attr('stop-color', 'green')
      .attr('stop-opacity', 0.8);
    
    gradient.append('stop')
      .attr('offset', '100%')
      .attr('stop-color', 'green')
      .attr('stop-opacity', 0);

    // X scale
    const x = d3.scaleTime().domain(d3.extent(chartData, d => d.date)).range([0, width]);
    const xAxis = d3.axisBottom(x).tickFormat(d3.timeFormat("%Y-%m-%d"));
    svg.append('g')
      .attr('class', 'x-axis')
      .attr('transform', `translate(0,${height})`)
      .call(xAxis);

    // Y scale
    const y = d3.scaleLinear().domain([0, d3.max(chartData, d => d.value)]).range([height, 0]);
    svg.append('g').call(d3.axisLeft(y));

    // Area
    const area = d3.area()
      .x(d => x(d.date))
      .y0(height)
      .y1(d => y(d.value));

    svg.append('path')
      .datum(chartData)
      .attr('fill', 'url(#area-gradient)')
      .attr('stroke', 'green')
      .attr('stroke-width', 1.5)
      .attr('d', area);
  };

  // Dropdown change handler
  const handleTypeChange = event => {
    setSelectedType(event.target.value);
  };

  const handleCheckboxChange = event => {
    setHideFakeData(event.target.checked);
  };

  // Redraw chart on selected type or filtered data change
  useEffect(() => {
    if (filteredData.length > 0) {
      drawChart();
    }
  }, [filteredData]);

  return (
    <div className="pollen-container">
      <div className="pollen-chart">
        <h3>Pollen Data</h3>
        <select className="pollen-dropdown" onChange={handleTypeChange} value={selectedType}>
          <option value="">Select Pollen Type</option>
          {pollenTypes.map((type, index) => (
            <option key={index} value={type}>
              {type}
            </option>
          ))}
        </select>
        <div className="checkbox-container">
          <label>
            <input type="checkbox" onChange={handleCheckboxChange} checked={hideFakeData} /> Hide Fake Data
          </label>
        </div>
        {filteredData.length > 0 && (
          <div className="data-table">
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Time</th> {/* New column for time */}
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                {chartData.map((d, index) => (
                  <tr key={index}>
                    <td>{d3.timeFormat('%Y-%m-%d')(d.date)}</td>
                    <td>{d.time}</td> {/* New column for time */}
                    <td>{d.value}</td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      )}
        <div id="pollen-chart"></div>
      </div>
      
    </div>
  );
};

export default PollenData;
