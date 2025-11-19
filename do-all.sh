set -a
. ./.env
set +a

cd music-collab-playlist-be
mvn clean install -DskipTests
docker build  -t music-collab-playlist-be .
cd ..

cd music-collab-playlist-fe
ng build
docker build --build-arg GITHUB_TOKEN="$NPM_TOKEN" -t music-collab-playlist-fe .
cd ..