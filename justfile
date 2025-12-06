build:
    mvn clean package -DskipTests

run:
    mvn spring-boot:run

up broker="rabbitmq" strategy="orchestration":
    SPRING_PROFILES_ACTIVE={{broker}},{{strategy}} \
        docker compose up --build --detach

down:
    docker compose down