#
# Copyright 2016 Confluent Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Builds a docker image for Kafka Connect

FROM debezium/connect-base:0.9

LABEL maintainer="Debezium Community"

ENV DEBEZIUM_VERSION="0.9.5.Final" \
    MAVEN_REPO_CORE="https://repo1.maven.org/maven2" \
    MAVEN_DEP_DESTINATION=$KAFKA_CONNECT_PLUGINS_DIR \
    MONGODB_MD5=a9bde9c4173d4c4f3c0adc9d97a795ee \
    MYSQL_MD5=720b1396358fbdc59bce953f47d3c53f \
    POSTGRES_MD5=a838ae664b7d8d050419d3e638e64273 \
    SQLSERVER_MD5=f21ef11e7b3e34736d428eb1d6ce5450 \
    ORACLE_MD5=1b93c90502d5d369e5a7173759de2504

RUN docker-maven-download debezium postgres "$DEBEZIUM_VERSION" "$POSTGRES_MD5"

#RUN docker-maven-download debezium postgres "$DEBEZIUM_VERSION" "$MONGODB_MD5" && \
#    docker-maven-download debezium mysql "$DEBEZIUM_VERSION" "$MYSQL_MD5" && \
#    docker-maven-download debezium postgres "$DEBEZIUM_VERSION" "$POSTGRES_MD5" && \
#    docker-maven-download debezium sqlserver "$DEBEZIUM_VERSION" "$SQLSERVER_MD5" && \
#    docker-maven-download debezium oracle "$DEBEZIUM_VERSION" "$ORACLE_MD5"