FROM confluentinc/cp-kafka:latest

EXPOSE 9092

# Taken from https://github.com/confluentinc/kafka-images/tree/master/examples/kafka-single-node
# "`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-
# An important note about accessing Kafka from clients on other machines: 
# -----------------------------------------------------------------------
#
# The config used here exposes port 9092 for _external_ connections to the broker
# i.e. those from _outside_ the docker network. This could be from the host machine
# running docker, or maybe further afield if you've got a more complicated setup. 
# If the latter is true, you will need to change the value 'localhost' in 
# KAFKA_ADVERTISED_LISTENERS to one that is resolvable to the docker host from those 
# remote clients
#
# For connections _internal_ to the docker network, such as from other services
# and components, use kafka:29092.
#
# See https://rmoff.net/2018/08/02/kafka-listeners-explained/ for details
# "`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-'"`-._,-
#
ENV KAFKA_BROKER_ID=1
ENV KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
# Make sure this is an env variable in the docker compose
# ENV KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:29092,PLAINTEXT_HOST://192.168.8.220:9092
ENV KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
ENV KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT
ENV KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1

COPY ./kafka-init.sh ./kafka-init.sh

CMD [ "./kafka-init.sh" ]


