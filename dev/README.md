#Dev demo and work flow:

This directory is to allow developers to run song locally (maven/IDE) while having all the dependencies it needs 
running through docker compose, it also contains an example to go through study and analysis lifecycle 

##To Run:
- start the infra (services needed by song: db, score, kafka etc) : `make start-infra`
- start song (not needed if through IDE) : `make start-song JDK_8_DIR=$JDK_8_HOME` 
where `$JDK_8_HOME` is an env variable pointing to my jdk8 dir or you could just paste the path directly
- to reset everything (clean volumes, dbs etc): `make nuke`

## Demo (publish study)
Then in a different terminal: 
- create study `make song-create-study`
- upload & validate analysis `make song-upload-payload`
output:
```json
{
  "status": "ok",
  "uploadId": "UP-911b259a-9578-46c4-9018-057f297fe58b"
}
```
- save analysis `make song-save-payload uploadId=<uploadId>` output:
```json
{
  "analysisId": "TESTANALYSIS",
  "status": "ok"
}
```
- upload analysis file to score `make score-upload`, will print "Upload completed" on success
- publish the analysis `make song-publish` outputs: `AnalysisId TESTANALYSIS successfully published`

Notes:
- regarding the auth server, it is needed by score to authenticate the scopes there is currently no way to disable that 
in score
- score bootstrap is needed to create the bucket for the object storage
