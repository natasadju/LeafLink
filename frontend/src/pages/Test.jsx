import React, { useState, useEffect } from 'react';

const Test = () => {
    const [users, setUsers] = useState([]);

    useEffect(() => {
        const getUsers = async () => {
            try {
                const response = await fetch('http://172.211.85.100:3000/api/v1/getusers');
                if (response.ok) {
                    const data = await response.json();
                    setUsers(data.users);
                } else {
                    throw new Error('Failed to fetch users');
                }
            } catch (error) {
                console.error('Error fetching users:', error);
            }
        };

        getUsers();
    }, []);

    return (
        <div>
            <p>This is a test</p>
            <ul>
                {users.map(user => (
                    <li key={user._id}>{user.username}</li>
                ))}
            </ul>
        </div>
    );
};

export default Test;
