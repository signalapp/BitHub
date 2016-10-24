FROM maven:3-jdk-8-onbuild

# RUN apt-get update
# RUN apt-get install -y maven

WORKDIR /usr/src/app

COPY . .

RUN mvn package -Dmaven.test.skip=true

EXPOSE 8080
EXPOSE 8081
CMD ["java", "-Djetty.host=0.0.0.0", "-jar", "target/BitHub-0.1.jar", "server", "config/production.yml"]
