.ONESHELL:
STUDY_ID := test
ACCESS_TOKEN := ad83ebde-a55c-11e7-abc4-cec278b6b50a
SONG_HOST := http://localhost:8080
SCORE_HOST := http://localhost:8087
ANALYSIS_ID := TESTANALYSIS

init:
	sudo apt-get install jq

start-song:
	# we need this param JDK_8_DIR in case we have different jdk set as default.
	export JAVA_HOME="$(JDK_8_DIR)"
	cd ../song-server
	../mvnw spring-boot:run -Dspring-boot.run.profiles=dev,kafka

start-infra:
	docker-compose -f docker-compose.yml up -d

stop:
	docker-compose -f docker-compose.yml down

nuke:
	docker-compose -f docker-compose.yml down -v

nuke-db:
	docker exec -it dev_song-db_1 psql -U postgres -c "DROP TABLE IF EXISTS Study CASCADE;DROP TABLE IF EXISTS Donor CASCADE;DROP TABLE IF EXISTS Specimen CASCADE;DROP TABLE IF EXISTS Sample CASCADE;DROP TABLE IF EXISTS Analysis CASCADE;DROP TABLE IF EXISTS File CASCADE;DROP TABLE IF EXISTS SampleSet CASCADE;DROP TABLE IF EXISTS SequencingRead CASCADE;DROP TABLE IF EXISTS VariantCall CASCADE;DROP TABLE IF EXISTS Upload CASCADE;DROP TABLE IF EXISTS Info;"
	echo "NOW RESTART SONG"

song-create-study:
	curl -X POST --header 'Content-Type: application/json' --header 'Accept: */*' \
	--header 'Authorization: Bearer $(ACCESS_TOKEN)' \
	-d '{"description": "study description","name": "$(STUDY_ID)","organization": "organization name","studyId": "$(STUDY_ID)"}' \
	'$(SONG_HOST)/studies/$(STUDY_ID)/'
	echo

song-show-studies:
	curl -X GET --header 'Accept: application/json' '$(SONG_HOST)/studies/all'
	echo

song-upload-payload:
	curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' \
	--header 'Authorization: Bearer $(ACCESS_TOKEN)' \
	-d @example/exampleVariantCall.json \
	'$(SONG_HOST)/upload/$(STUDY_ID)' | jq .
	echo

# this needs uploadId from previous command 'song-upload-payload'
song-save-payload:
	curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' \
	--header 'Authorization: Bearer $(ACCESS_TOKEN)' \
	'$(SONG_HOST)/upload/$(STUDY_ID)/save/$(uploadId)?ignoreAnalysisIdCollisions=false' | jq .
	echo

score-upload:
	docker run -it --rm --network host --env SPRING_PROFILES=docker \
	--env ACCESSTOKEN=$(ACCESS_TOKEN) --env STORAGE_URL=$(SCORE_HOST) \
	--env METADATA_URL=$(SONG_HOST) -v `pwd`/example:/data overture/score:1.6.1 bin/score-client upload \
	--manifest /data/manifest.txt
	echo

song-publish:
	curl -X PUT --header 'Content-Type: application/json' --header 'Accept: application/json' \
	--header 'Authorization: Bearer $(ACCESS_TOKEN)' \
	'$(SONG_HOST)/studies/$(STUDY_ID)/analysis/publish/$(ANALYSIS_ID)'
	echo

song-show-published:
	curl -X GET --header 'Content-Type: application/json' --header 'Accept: application/json' \
	--header 'Authorization: Bearer $(ACCESS_TOKEN)' \
	'$(SONG_HOST)/studies/$(STUDY_ID)/analysis?analysisStates=PUBLISHED'
	echo