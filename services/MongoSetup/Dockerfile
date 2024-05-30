# Use the official MongoDB image from Docker Hub
FROM mongo:latest

WORKDIR /usr/src/configs

COPY seeddb.sh ./seeddb.sh
RUN chmod +x seeddb.sh

ADD sink_connector /tutorials/sink_connector
ADD utils /usr/local/bin
RUN chmod +x /usr/local/bin/cx
RUN chmod +x /usr/local/bin/del
RUN chmod +x /usr/local/bin/kc
RUN chmod +x /usr/local/bin/status

RUN apt-get -y update
RUN apt-get -y upgrade
RUN apt-get install -y --no-install-recommends apt-utils
RUN apt-get install -y curl
RUN apt-get install -y dos2unix
RUN apt-get install -y kafkacat
RUN apt-get install -y bsdmainutils

RUN dos2unix /usr/local/bin/*
RUN dos2unix ./seeddb.sh

COPY priceTableSeedNew.csv ./data/priceTableSeedNew.csv
COPY sampleClientHoldings.csv ./data/sampleClientHoldings.csv
COPY marketValueSeed.csv ./data/marketValueSeed.csv
COPY stockInventory.csv ./data/stockInventory.csv

# Run the seed script when the container starts
CMD ["./seeddb.sh"]
