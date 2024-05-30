# Use the official PostgreSQL image from Docker Hub
FROM postgres:latest

# Set environment variables for PostgreSQL
ENV POSTGRES_DB=tradeserver
ENV POSTGRES_USER=myuser
ENV POSTGRES_PASSWORD=mypassword

# Copy the SQL file into the Docker container
COPY postgressetup.sql /docker-entrypoint-initdb.d/
COPY output.csv /data/fakeevents.csv

# Expose the PostgreSQL port
EXPOSE 5432
