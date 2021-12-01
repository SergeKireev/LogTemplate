# syntax=docker/dockerfile:1
FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1
ARG branch=master
WORKDIR /logtemplate
RUN git clone --recurse-submodules https://github.com/SergeKireev/LogTemplate.git
WORKDIR LogTemplate
RUN git checkout $branch
RUN ["sbt", "assembly"]
CMD ["java", "-jar", "target/scala-2.13/log-template-assembly-0.1.jar"]