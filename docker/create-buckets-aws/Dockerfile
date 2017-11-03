FROM song_base-ubuntu:latest

ENV DCC_SCRIPTS $DCC_HOME/scripts
RUN  pip3 install boto3

CMD python3 $DCC_SCRIPTS/bucket-setup.py; \
	    FOR_100_YEARS=$((100*365*24*60*60));while true;do sleep $FOR_100_YEARS;done


