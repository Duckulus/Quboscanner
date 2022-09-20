FROM maven:3.8.3-openjdk-17

WORKDIR /app

COPY . .

RUN mvn clean compile assembly:single

CMD ["./run.sh"]

