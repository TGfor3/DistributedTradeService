#!/bin/bash
# MongoDB credentials and connection parameters
MONGO_HOST="localhost"  # Set to your MongoDB host address
MONGO_PORT="27017"      # Set to your MongoDB port
USERNAME="isigutt"
PASSWORD="isi"
AUTH_DB="admin"

# MongoDB databases and collection
DATABASE_COMMAND="CommandDB"
DATABASE_QUERY="QueryDB"
COLLECTION="stockInventory"
STAGE_COLLECTION="stageStockInventory"
CSV_FILE_PATH="/docker-entrypoint-initdb.d/stockInventory.csv"

# MongoDB command to create or modify collection with validation
VALIDATION_RULE='{
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["quantity"],
         properties: {
            quantity: {
               bsonType: "int",
               minimum: 0,
               description: "must be an integer and cannot be negative"
            }
         }
      }
   }
}'

# Function to setup collection with validation
setup_collection_validation() {
    db=$1
    echo "Setting up validation for $db.$COLLECTION..."
    mongosh --host $MONGO_HOST --port $MONGO_PORT -u $USERNAME -p $PASSWORD --authenticationDatabase $AUTH_DB --eval "db = db.getSiblingDB('$db'); db.createCollection('$COLLECTION', $VALIDATION_RULE);" || mongo --host $MONGO_HOST --port $MONGO_PORT -u $USERNAME -p $PASSWORD --authenticationDatabase $AUTH_DB --eval "db = db.getSiblingDB('$db'); db.runCommand({collMod: '$COLLECTION', $VALIDATION_RULE});"
}


# Set up validation rules for both databases
setup_collection_validation $DATABASE_COMMAND
setup_collection_validation $DATABASE_QUERY

# Import stock inventory data from CSV file to CommandDB
mongoimport --username=$USERNAME --password=$PASSWORD --authenticationDatabase=$AUTH_DB --db=$DATABASE_COMMAND --collection=$COLLECTION --type=csv --file=$CSV_FILE_PATH --headerline

# Import stock inventory data from CSV file to QueryDB
mongoimport --username=$USERNAME --password=$PASSWORD --authenticationDatabase=$AUTH_DB --db=$DATABASE_QUERY --collection=$COLLECTION --type=csv --file=$CSV_FILE_PATH --headerline



echo "Data import completed."
