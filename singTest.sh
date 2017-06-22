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

header "Uploading File"
uploadFile=${1:-sequencingRead.json}
set -x 
u=`$SING_EXE upload -f $uploadFile`
set +x 

header "Checking Status of upload id '$u'"
set -x 
${SING_EXE} status -u $u | jq -C .state 
set +x 


header "Sleeping for 1 sec, then checking upload status "
sleep 1
set -x 
${SING_EXE} status -u $u | jq -C .state 
set +x 


header "Saving upload id to get analysis id"
set -x 
a=`$SING_EXE save -u $u`
set +x 
echo "Got analysis id '$a'"


header "Checking status if upload id"
set -x 
${SING_EXE} status -u $u | jq -C .state 
set +x 


header "Uploading manifest using analysis id $a"
set -x 
${SING_EXE} manifest -a $a -f manifest.txt
set +x 
cat manifest.txt


non_existant_upload_id="fake$u"
header "[ERROR_TEST] Checking status of non-existant uploadId $non_existant_upload_id"
set -x 
${SING_EXE} status -u $non_existant_upload_id | jq -C .state 
set +x 


header "[ERROR_TEST] Saving non-existant uploadId $non_existant_upload_id"
set -x 
bad_a=`$SING_EXE save -u $non_existant_upload_id`
set +x 


non_existant_analysis_id="fake$a"
header "[ERROR_TEST] Uploading manifect for non-existant analysisId $non_existant_analysis_id"
set -x 
${SING_EXE} manifest -a $non_existant_analysis_id -f manifest.txt
set +x 
