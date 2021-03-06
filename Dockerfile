FROM openjdk:8

COPY target/universal/scapig-publisher-*.tgz .
COPY start-docker.sh .
RUN chmod +x start-docker.sh
RUN tar xvf scapig-publisher-*.tgz

EXPOSE 9019

CMD ["sh", "start-docker.sh"]