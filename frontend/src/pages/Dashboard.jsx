import React, { useEffect, useState } from 'react';
import "../styles/Dashboard.css";
import { toast } from 'react-toastify';
import axios from 'axios';
import Navbar from "./NavBar.jsx";
import Parks from './Parks.jsx';
import Events from './Events.jsx';


const Dashboard = () => {
  const [token, setToken] = useState(JSON.parse(localStorage.getItem("auth")) || "");
  const [data, setData] = useState({});
  
  const fetchToken = async () => {
    let axiosConfig = {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    };

    try {
      const response = await axios.get("http://172.211.85.100:3000/api/v1/dashboard", axiosConfig);
      setData({ msg: response.data.msg, luckyNumber: response.data.secret });
    } catch (error) {
      toast.error(error.message);
    }
  }

  useEffect(() => {
    fetchToken();
  }, []);

  return (
    <div>
      <Navbar />
      <div className='dashboard-main'>
        <h1>Welcome to LeafLink!</h1>
        <Events />
      </div>
    </div>
  );
}

export default Dashboard;
