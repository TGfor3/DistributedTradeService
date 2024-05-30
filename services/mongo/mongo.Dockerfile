# Use the official MongoDB image from Docker Hub
FROM mongo:latest


ENV MONGO_INITDB_ROOT_USERNAME=isigutt
ENV MONGO_INITDB_ROOT_PASSWORD=isi

# Set the working directory in the container
WORKDIR /usr/src/configs

# Copy initialization scripts (if you have any)
COPY ./mongo-init /docker-entrypoint-initdb.d

# Make the seed script executable
RUN chmod +x /docker-entrypoint-initdb.d/seeddb.sh

RUN apt-get -y update
RUN apt-get -y upgrade

RUN apt-get install -y dos2unix

RUN dos2unix /docker-entrypoint-initdb.d/seeddb.sh

# Expose ports (default MongoDB port is 27017)
EXPOSE 27017

CMD ["mongod"]
