FROM tomcat:7-jre7

ADD target/om-conversion-service.war /tmp

RUN mv /tmp/om-conversion-service.war /tmp/omcs.zip && \
    unzip /tmp/omcs.zip -d $CATALINA_HOME/webapps/omcs && \
    rm /tmp/omcs.zip