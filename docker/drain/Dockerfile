# syntax=docker/dockerfile:1
FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1
COPY drain-scala.zip .
RUN unzip drain-scala.zip
COPY application.conf /drain-scala/src/main/resources/
COPY file.log /drain-scala/src/main/resources/
RUN cd drain-scala
RUN sbt compile
WORKDIR /drain-scala
CMD ["sbt", "run"]