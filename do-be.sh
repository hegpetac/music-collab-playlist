cd music-collab-playlist-be
mvn clean install -DskipTests
docker build -t music-collab-playlist-be .
cd ..