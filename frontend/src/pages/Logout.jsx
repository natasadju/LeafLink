import React, { useEffect } from 'react'
import "../styles/Logout.css";
import { useNavigate } from 'react-router-dom';

const Logout = () => {

    const navigate = useNavigate();
    
    useEffect(() => {
        localStorage.removeItem("auth");
        setTimeout(() => {
            navigate("/");
        }, 3000);
    }, []);

  return (
    <div className='logout-main'>
    <h1>Thank you for saving enviorment!</h1>
    <p>You logged out! You will be redirected to page where you can login or register</p>
  </div>
  )
}

export default Logout