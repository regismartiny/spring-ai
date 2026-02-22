mvn -Pnative spring-boot:build-image

docker run --rm -p 8080:8080 docker.io/library/demo:0.0.1-SNAPSHOT