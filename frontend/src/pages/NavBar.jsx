import React, { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';
import '@fortawesome/fontawesome-free/css/all.css';
import "../styles/NavBar.css";

const Navbar = () => {
  const location = useLocation();
  const [activeTab, setActiveTab] = useState('');

  useEffect(() => {
    const path = location.pathname.split("/").pop();
    setActiveTab(path || 'dashboard');
  }, [location.pathname]);

  const handleNavItemClick = (tab) => {
    setActiveTab(tab);
  };

  return (
    <nav className="navbar navbar-expand-custom navbar-mainbg">
      <a className="navbar-brand navbar-logo" href="/dashboard"><i className="fa-brands fa-pagelines fa-lg"></i>LeafLink</a>
      <div className="collapse navbar-collapse" id="navbarSupportedContent">
        <ul className="navbar-nav ml-auto">
          <li className={`nav-item ${activeTab === 'dashboard' ? 'active' : ''}`}>
            <NavLink className="nav-link" to="/dashboard" onClick={() => handleNavItemClick('dashboard')}><i className="fa-solid fa-house"></i>Dashboard</NavLink>
          </li>
          <li className={`nav-item ${activeTab === 'parks' ? 'active' : ''}`}>
            <NavLink className="nav-link" to="/parks" onClick={() => handleNavItemClick('parks')}><i className="fa-solid fa-tree"></i>Green spaces</NavLink>
          </li>
          <li className={`nav-item ${activeTab === 'components' ? 'active' : ''}`}>
            <NavLink className="nav-link" to="#" onClick={() => handleNavItemClick('components')}><i className="far fa-clone"></i>Components</NavLink>
          </li>
          <li className={`nav-item ${activeTab === 'logout' ? 'active' : ''}`}>
            <NavLink className="nav-link" to="/logout" onClick={() => handleNavItemClick('logout')}><i className="fa-solid fa-right-from-bracket"></i>Logout</NavLink>
          </li>
        </ul>
      </div>
    </nav>
  );
};

export default Navbar;
