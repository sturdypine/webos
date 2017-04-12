############################################
# Version 9.4.0b
# desc : jdk8,webos
############################################
# 
#FROM centos/jdk8:latest
FROM alpine/jre8:latest
#
MAINTAINER spc (sturdypine@icloud.com)

ENV TZ=Asia/Shanghai
ENV WEBOS_HOME /webos

#
#RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN mkdir /webos && \
    mkdir /webos/logs && \
    mkdir /webos/apps
ADD lib /webos/lib
ADD bin /webos/bin
RUN chmod -R 755 /webos/bin

VOLUME  ["/webos/logs"]
