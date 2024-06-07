# Stage 1: Build
FROM node:18 AS build
WORKDIR /usr/src/app

WORKDIR /usr/src/app

# Copy package.json and package-lock.json (if available)
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY . .

# Build the application
RUN npm run build

# Stage 2: Serve
FROM node:18-alpine AS serve
WORKDIR /usr/src/app

# Copy only the build output from the previous stage
COPY --from=build /usr/src/app/dist ./dist

# Install a simple HTTP server to serve the build output
RUN npm install -g serve

# Expose the port the app runs on
EXPOSE 5173

CMD ["npm", "run", "dev"]
# Command to run the app
CMD ["serve", "-s", "dist", "-l", "5173"]
