import React, { useEffect, useRef } from 'react';
import * as d3 from 'd3';
import '../styles/AirQualityGraph.css'; // Add your CSS file for styling

function AirQualityGraph({ data }) {
  const svgRefs = useRef([]);

  useEffect(() => {
    if (data && data.length > 0) {
      const margin = { top: 50, right: 30, bottom: 100, left: 60 }; // Increased bottom margin for x-axis label
      const width = 600 - margin.left - margin.right; // Increased width
      const height = 450 - margin.top - margin.bottom; // Increased height

      const parseTime = d3.timeParse('%Y-%m-%dT%H:%M:%S.%LZ');

      // Filter data for the year 2024
      const filteredData = data.filter(d => new Date(d.timestamp).getFullYear() === 2024);

      // Get unique stations
      const stations = [...new Set(filteredData.map(d => d.station))];

      stations.forEach((station, index) => {
        const stationData = filteredData.filter(d => d.station === station);
        if (stationData.length > 0) {
          createGraph(stationData, svgRefs.current[index], station, width, height, margin, parseTime);
        }
      });
    }
  }, [data]);

  const createGraph = (data, svgRef, stationName, width, height, margin, parseTime) => {
    const svg = d3.select(svgRef)
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    const x = d3.scaleTime()
      .domain(d3.extent(data, d => parseTime(d.timestamp)))
      .range([0, width]);

    const y = d3.scaleLinear()
      .domain([0, d3.max(data, d => Math.max(d.pm10, d.pm25, d.ozon, d.no2, d.benzen))])
      .nice()
      .range([height, 0]);

    const xAxis = d3.axisBottom(x).tickFormat(d3.timeFormat('%Y-%m-%d')); // Format date as 'YYYY-MM-DD'
    const yAxis = d3.axisLeft(y);

    // Add gridlines
    const xAxisGrid = d3.axisBottom(x)
      .tickSize(-height)
      .tickFormat('');

    const yAxisGrid = d3.axisLeft(y)
      .tickSize(-width)
      .tickFormat('');

    svg.append('g')
      .attr('class', 'x axis')
      .attr('transform', `translate(0,${height})`)
      .call(xAxis)
      .selectAll('text')
      .style('text-anchor', 'end')
      .attr('transform', 'rotate(-45)')
      .attr('dy', '0.35em')
      .attr('dx', '-0.8em')
      .style('font-size', '10px');

    svg.append('g')
      .attr('class', 'y axis')
      .call(yAxis);

    // Append gridlines
    svg.append('g')
      .attr('class', 'x grid')
      .attr('transform', `translate(0,${height})`)
      .call(xAxisGrid);

    svg.append('g')
      .attr('class', 'y grid')
      .call(yAxisGrid)
      .style('opacity', 0.1); // Make grid lines fainter

    const parameters = ['pm10', 'pm25', 'ozon', 'no2', 'benzen'];
    const colors = ['#1f77b4', '#ff7f0e', '#2ca02c', '#d62728', '#9467bd']; // Different colors for each parameter

    parameters.forEach((parameter, index) => {
      const line = d3.line()
        .x(d => x(parseTime(d.timestamp)))
        .y(d => y(d[parameter]));

      svg.append('path')
        .datum(data)
        .attr('fill', 'none')
        .attr('stroke', colors[index]) // Use a different color for each parameter
        .attr('stroke-width', 1.5)
        .attr('d', line);
    });

    // Add title
    svg.append('text')
      .attr('x', width / 2)
      .attr('y', 0 - (margin.top / 2))
      .attr('text-anchor', 'middle')
      .style('font-size', '16px')
      .style('text-decoration', 'underline')
      .text(stationName);

    // Add legend
    const legend = svg.append('g')
      .attr('class', 'legend')
      .attr('transform', `translate(${width + 40},20)`);

    parameters.forEach((parameter, index) => {
      legend.append('rect')
        .attr('x', 0)
        .attr('y', index * 20)
        .attr('width', 10)
        .attr('height', 10)
        .attr('fill', colors[index]);

      legend.append('text')
        .attr('x', 20)
        .attr('y', index * 20 + 9)
        .attr('dy', '0.35em')
        .text(parameter);
    });
  };

  // Render one SVG per station
  const stations = [...new Set(data.map(d => d.station))];

  return (
    <div className="airQualityGraph">
      {stations.map((station, index) => (
        <svg key={station} ref={el => svgRefs.current[index] = el}></svg>
      ))}
    </div>
  );
}

export default AirQualityGraph;
