#!/bin/bash

# User input
SERVER_URL=$1
STUDY_ID=$2
AUTH_TOKEN=$3

# Internal variables
max_repeats=100
seconds_until_retry=5


is_alive(){
	curl ${SERVER_URL}/isAlive
}

n=0
until [ $n -ge $max_repeats ]
do
  is_alive && break  # substitute your command here
  n=$[$n+1]
  echo "Try $n/$max_repeats, retrying in $seconds_until_retry seconds..."
  sleep $seconds_until_retry
done

value=$(curl ${SERVER_URL}/studies/${STUDY_ID} | jq .errorId | xargs echo)
echo "VALUE: ${value}"
if [ "${value}" == "study.id.does.not.exist" ];then
	curl -XPOST --header 'Accept: application/json' --header 'Content-Type: application/json' --header "Authorization: Bearer ${AUTH_TOKEN}" -d "{\"studyId\":\"${STUDY_ID}\"}"  ${SERVER_URL}/studies/${STUDY_ID}/
else 
	echo "The study \"${STUDY_ID}\" already exists. doing nothing"
fi
