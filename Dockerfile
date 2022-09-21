FROM maven:3.8.3-openjdk-17

WORKDIR /app

COPY . .

RUN mvn clean compile assembly:single

RUN chmod +x ./run.sh

CMD ["./run.sh"]

