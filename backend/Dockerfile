
FROM node:14-alpine

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY package*.json ./
RUN npm install --development

COPY . .

EXPOSE 3000

CMD ["node", "server.js"]
