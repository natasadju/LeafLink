FROM node:18

WORKDIR /usr/src/app

RUN apt-get update && apt-get install -y python3 python3-pip

COPY requirements.txt .
RUN pip3 install --break-system-packages -r requirements.txt

RUN pip3 install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu

COPY package*.json ./

RUN npm install

COPY . .

EXPOSE 3000
CMD [ "node", "app.js" ]