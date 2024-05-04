# LeafLink

LeafLink is a digital twin cross-platform project aimed at promoting community engagement and environmental awareness in Maribor. The app allows users to discover and participate in events focused on green spaces, such as park clean-ups, tree planting initiatives, and other environmental activities.

## Features

### 1. Account Creation and Green Area Map Viewing

- **Account Creation**: Users can sign up for an account to access the full features of the application.
- **Green Area Map**: Users can view a detailed map of Maribor with marked green areas, providing them with insights into the city's natural spaces.

### 2. Event Organization

- **Event Creation**: Users can organize events, such as park clean-ups, through the platform. This feature enables individuals and organizations to mobilize the community for environmental causes.

### 3. Event Notifications

- **Proximity Notifications**: Users receive notifications about events happening in their vicinity, ensuring they stay informed about opportunities to participate in local initiatives.

### 4. Event Attendance

- **Event RSVP**: Users can choose to attend specific events, indicating their interest and commitment to participating in community activities.

### 5. Collaboration and Social Networking

- **Friend Management**: Users can add friends and collaborate with them on event organization tasks, fostering a sense of community and teamwork.
- **User Profiles**: Users can view profiles and statistics of other users, facilitating networking and community building within the platform.

### 6. Event Media Sharing

- **Image Sharing**: Users can post pictures from events they attended, creating a shared archive of images for each event. Additionally, users can view pictures posted by other attendees, promoting community engagement and documentation of activities.

### 7. Air Quality Data Integration

- **Air Quality Information**: The app integrates with a digital twin to scrape air quality data in Maribor. Users can access real-time air quality information within the app, helping them make informed decisions about outdoor activities.

## Technologies Used

LeafLink is built using the following technologies:

- **Frontend**: React
- **Backend**: Node.js, Express.js
- **Database**: MongoDB
- **Maps Integration**: Google Maps API
- **Authentication**: JSON Web Tokens (JWT)

# Installation

1. Install backend dependencies:
```
npm install
```

2. Install frontend dependencies:

```
cd frontend
npm install
```

3. Configure MongoDB and JWT:
Visit MongoDB website, create account, database and take connection string.
After that generate 256 bits random key and add it to .env file.
Create the .env file in the root directory with the following contents:
```
JWT_SECRET=your_jwt_secret
```

4. Run the application:
Start the backend server:
```
cd backend
node app.js
```

5. In a new terminal, start the frontend:
```
cd client
npm run dev
```
