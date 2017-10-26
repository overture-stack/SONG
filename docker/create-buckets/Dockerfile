FROM song_base-ubuntu:latest

ADD scripts $DCC_HOME/scripts
RUN pip install boto3

CMD python3 $DCC_HOME/scripts/create-buckets.py

