#!/bin/bash

# Stop current docker container
sudo docker stop backend-server
sudo docker rm backend-server

sudo docker stop frontend-client
sudo docker rm frontend-client

# Pull newest server-side image
sudo docker pull natasadju/server-side:latest
sudo docker pull natasadju/frontend:latest

# Run the container again
# sudo docker run -d --name backend-server natasadju/server-side:latest
sudo docker compose up -d
