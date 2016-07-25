FROM tomcat:7-jre7

ADD target/viss.war /tmp

RUN mv /tmp/viss.war /tmp/viss.zip && \
    unzip /tmp/viss.zip -d $CATALINA_HOME/webapps/viss && \
    rm /tmp/viss.zip

VOLUME /tmp/geoserver