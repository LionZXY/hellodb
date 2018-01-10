FROM ubuntu:16.04

RUN \
  sed -i 's/# \(.*multiverse$\)/\1/g' /etc/apt/sources.list && \
  apt-get update && \
  apt-get -y upgrade && \
  apt-get install -y build-essential && \
  apt-get install -y software-properties-common && \
  apt-get install -y byobu curl git htop man unzip vim wget && \
  rm -rf /var/lib/apt/lists/*

ENV POSTGRESV 9.5
ENV DEBIAN_FRONTEND noninteractive

RUN apt-get -y -qq update &&\
    apt-get install -y -qq apt-utils &&\
    apt-get install -y -qq postgresql-$POSTGRESV

RUN \
  echo oracle-java9-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java9-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk9-installer

ENV JAVA_HOME /usr/lib/jvm/java-9-oracle

USER postgres

RUN /etc/init.d/postgresql start &&\
    psql --command "CREATE USER lionzxy WITH SUPERUSER PASSWORD '123456789';" &&\
    createdb -E UTF8 -T template0 -O lionzxy hellodb &&\
    /etc/init.d/postgresql stop

RUN echo "host all  all    0.0.0.0/0  trust" >> /etc/postgresql/$POSTGRESV/main/pg_hba.conf

EXPOSE 5432

VOLUME  ["/etc/postgresql", "/var/log/postgresql", "/var/lib/postgresql"]

USER root

RUN echo "Europe/Moscow" > /etc/timezone

EXPOSE 5000
ENV TZ Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV WORKDIR ./

COPY . .

RUN ./gradlew clean build

CMD service postgresql start &&\
    java -jar ./build/libs/technopark-db-0.1.0.jar

