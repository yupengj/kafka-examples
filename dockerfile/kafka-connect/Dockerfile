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

FROM confluentinc/cp-kafka-connect-base:5.3.0

MAINTAINER jiangyp
LABEL io.confluent.docker=true
ARG COMMIT_ID=unknown
LABEL io.confluent.docker.git.id=$COMMIT_ID
ARG BUILD_NUMBER=-1
LABEL io.confluent.docker.build.number=$BUILD_NUMBER

ENV COMPONENT=kafka-connect

# 安装 Elasticsearch 有问题
#RUN echo "===> Installing Elasticsearch ..." \
#    && apt-get -qq update \
#    && apt-get install -y \
#        confluent-kafka-connect-elasticsearch=${CONFLUENT_VERSION}${CONFLUENT_PLATFORM_LABEL}-${CONFLUENT_DEB_VERSION} \
#    && echo "===> Cleaning up ..."  \
#    && apt-get clean && rm -rf /tmp/* /var/lib/apt/lists/*

RUN echo "===> Installing Postgres-connector ..."
RUN curl -k -SL "https://search.maven.org/remotecontent?filepath=io/debezium/debezium-connector-postgres/0.9.5.Final/debezium-connector-postgres-0.9.5.Final-plugin.tar.gz" \
     | tar -xzf - -C /usr/share/java/kafka/ --strip-components=1 debezium-connector-postgres/postgresql-42.2.5.jar

RUN echo "===> Installing GCS Sink Connector ..."
RUN confluent-hub install confluentinc/kafka-connect-gcs:latest --no-prompt