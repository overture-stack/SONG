#!/bin/bash
set -x 
u=`sing upload -f metadata.json`
echo "Got upload id '$u'"
sing status -u $u | jq -C .state 
sleep 1
sing status -u $u | jq -C .state 
a=`sing save -u $u`
echo "Got analysis id '$a'"
sing status -u $u | jq -C .state 
sing manifest -a $a -f manifest.txt
cat manifest.txt
