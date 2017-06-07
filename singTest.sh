#!/bin/bash
u=`sing upload -f metadata.json`
sing status -u $u | jq .status
a=`sing save -u $u`
sing manifest -a $a -f manifest.txt
