build:
    mvn clean package -DskipTests

run:
    mvn spring-boot:run

up broker="rabbitmq" strategy="orchestration":
    docker compose \
        --env SPRING_PROFILES_ACTIVE={{broker}},{{strategy}} \
        up --build --detach

down:
    docker compose down