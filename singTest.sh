#!/bin/bash
SCRIPT=`readlink -f ${BASH_SOURCE[0]}`
SCRIPT_DIR=$( dirname  "${SCRIPT}")
echo "SCRIPT_DIR = ${SCRIPT_DIR}"
SING_EXE="${SCRIPT_DIR}/sing"
echo "SING_EXE = ${SING_EXE}"

set +x 

function header {
    message=$@
    topLine="********************************************************************************************"
    echo " "
    echo " "
    echo "$topLine"
    echo "   $message"
    echo "$topLine"
}

header "Uploading file"
uploadFile=${1:-sequencingRead.json}
set -x 
u=`$SING_EXE upload -f $uploadFile | jq -r -C '.uploadId' `
set +x 

header "Checking Status of upload"
set -x 
${SING_EXE} status -u $u | jq -r -C .state
set +x 

header "Sleeping for 1 sec, then checking upload status again"
sleep 1
set -x 
${SING_EXE} status -u $u | jq -r .state
set +x 

header "Saving uploaded analysis data"
set -x 
a=`$SING_EXE save -u $u | jq -r .analysisId`
set +x 

header "Checking status of upload"
set -x 
${SING_EXE} status -u $u | jq -r .state
set +x 

header "Fetching manifest for our analysis" 
set -x 
${SING_EXE} manifest -a $a -f manifest.txt
set +x 
cat manifest.txt

non_existant_upload_id="fake_uploadId"
header "[ERROR_TEST] Checking status of non-existant uploadId $non_existant_upload_id"
set -x 
${SING_EXE} status -u $non_existant_upload_id | jq -C .state 
set +x 

header "[ERROR_TEST] Saving non-existant uploadId $non_existant_upload_id"
set -x 
bad_a=`$SING_EXE save -u $non_existant_upload_id`
set +x 

non_existant_analysis_id="fake_analysisId"
header "[ERROR_TEST] Uploading manifect for non-existant analysisId $non_existant_analysis_id"
set -x 
${SING_EXE} manifest -a $non_existant_analysis_id -f manifest.txt
set +x 
