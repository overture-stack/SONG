#!/bin/bash
SCRIPT=`readlink -f ${BASH_SOURCE[0]}`
SCRIPT_DIR=$( dirname  "${SCRIPT}")
echo "SCRIPT_DIR = ${SCRIPT_DIR}"
SING_EXE="${SCRIPT_DIR}/sing"
echo "SING_EXE = ${SING_EXE}"

set -x 
uploadFile=${1:-sequencingRead.json}
u=`$SING_EXE upload -f $uploadFile`
echo "Got upload id '$u'"
${SING_EXE} status -u $u | jq -C .state 
sleep 1
${SING_EXE} status -u $u | jq -C .state 
a=`$SING_EXE save -u $u`
echo "Got analysis id '$a'"
${SING_EXE} status -u $u | jq -C .state 
${SING_EXE} manifest -a $a -f manifest.txt
cat manifest.txt
