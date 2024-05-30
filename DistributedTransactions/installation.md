# Deployment Instructions

## Requirements
- Every container must be running in a docker container on the same network

## Setup Instructions

1. **Download the deployment folder** in the T2Deploy folder in the services directory

2. **Download the infrastructure folder** in the Infrastructures Deploy folder in the services directory
    * Take note of the IP address of the device running the infrastructure containers

3. **Configure Environment Variables**:
   - In the T2 deploy .env file edit change the IP addresses of the following variables to match the IP of the device running the infrastructure containers. Take care to ensure that the ports are unchanged
        - `KAFKA_BOOTSTRAP_SERVERS`
        - `pikaURL`
        - `RABBITMQ_URI` 
        - `MONGO_URL`
        - `RABBITMQ_HOST`

4. **Start and Stop Services**:
   - In the `Infrastructure Deploy` folder:
     - To start the service, run:
       ```sh
       docker compose up --build
       ```
     - To shut down the service, run:
       ```sh
       docker compose down
       ```
        * Alternatively you can ctrl+c in the terminal that you called docker compose up from to end the task
        * Further you can call ```docker compose up``` to redploy a turned off service that will maintain its previous state

   - In the `T2 Deploy` folder:
     - To start the service, run:
       ```sh
       docker compose up --build
       ```
     - To shut down the service, run:
       ```sh
       docker compose down
       ```
        * Alternatively you can ctrl+c in the terminal that you called docker compose up from
