# poker-rating

## Requirements
- JDK 17. 

## How to check
```shell
./gradlew clean check
```

## How to build
```shell
./gradlew build
```

## How to run rating REST API
```shell
./gradlew :poker-rating:rule-engine:bootRun
```

## How to publish rule engine
```shell
./gradlew clean :poker-rating:rule-engine:build \
    -P registryUser="$STEF_JFROG_USER" \
    -P registryPassword="$STEF_JFROG_KEY" \
    -Ppublish
```

## Check dependency updates
```shell
./gradlew dependencyUpdates -Drevision=release -DoutputFormatter=html
```

## MongoDB run
```shell
docker run -d --name mongo-5 \
    -p 27017:27017 --memory=1024m \
	-e MONGO_INITDB_ROOT_USERNAME=root \
	-e MONGO_INITDB_ROOT_PASSWORD=passw0rdTest \
	mongo:5
```

```shell
docker exec -it mongo-5 bash
mongo --host some-mongo \
    -u mongoadmin \
    -p secret \
    --authenticationDatabase admin \
    poker
    
 db.getCollection("playerRatings").find().pretty()
 db.getCollection("playerRatings").deleteOne({"_id": "urn:applicationId:app1:userId:11"})
```
