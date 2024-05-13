FROM amazoncorretto:17.0.10
VOLUME /tmp
RUN ls -la > /ls_output.txt && cat /ls_output.txt
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} buy-and-sell.jar
EXPOSE 8080
ENTRYPOINT [ "sh", \
             "-c", \
             "exec java -Xmx768m -Xms256m -jar /buy-and-sell.jar"]