import React, { useEffect, useState } from 'react';
import axios from "axios";
import { toast } from "react-toastify";
import {useNavigate} from "react-router-dom";
import Image from "../assets/image.png";

const AddPark = () => {
    const [ token, setToken ] = useState(JSON.parse(localStorage.getItem("auth")) || "");
    const navigate = useNavigate();


    const handleParkSubmit = async (e) => {
        e.preventDefault();
        let name = e.target.name.value;
        let parkId = e.target.parkId.value;

        if (name.length > 0 && parkId > 0) {
            const formData = {
                name,
                parkId,
            };
            try {
                const response = await axios.post("http://localhost:3000/parks/addParks", formData);
                console.log(response.data);
                toast.success("Successfully added the park!");
                navigate("/parks");
            } catch (err) {
                console.log(err);
                toast.error(err.message);
            }

        } else {
            toast.error("Please fill all inputs");
        }
    };
    useEffect(() => {
        if(token === ""){
            toast.error("Please login!");
            navigate("/login");
        }
    }, []);
    return (
        <div className="login-main">
            <div className="login-left">
                <img src={Image} alt="" />
            </div>
            <div className="login-right">
                <div className="login-right-container">
                    <div className="login-center">
                        <h2>Welcome to updating LeafLink!</h2>
                        <p>Please enter the details of the Park you want to add!</p>
                        <form onSubmit={handleParkSubmit}>
                            <input type="text" placeholder="Name" name="name" />
                            <input type="text" placeholder="Park ID" name="parkId" required={true} />

                            <div className="login-center-buttons">
                                <button type="submit">Add Park</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AddPark;