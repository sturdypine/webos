############################################
# Version 9.4.0b
# desc : jdk8,webos
############################################
# 
FROM centos/jdk8:latest
#
MAINTAINER spc (sturdypine@icloud.com)

#
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#
RUN mkdir /webos
RUN mkdir /webos/logs
ADD lib /webos/lib
RUN mkdir /webos/apps
ADD bin /webos/bin
RUN chmod -R 755 /webos/bin

VOLUME  ["/webos/logs"]
ENV WEBOS_HOME /webos
