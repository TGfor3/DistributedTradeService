# Use official Node.js image as base
FROM node:latest

# Set working directory inside the container
WORKDIR /usr/src/app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy local code to the container
COPY . .

# Command to run your script
CMD ["node", "--env-file=.env", "index.js"]
