FROM minio/minio:latest
#  RUN mkdir -p /opt/dcc/data/oicr.icgc/data && \
#      touch /opt/dcc/data/oicr.icgc/data/heliograph
#  CMD ["minio"] 

ENV SHELL /bin/sh
RUN echo "SHELL=$SHELL" > robshell.txt
CMD mkdir -p /opt/dcc/data/oicr.icgc/data && \
    touch /opt/dcc/data/oicr.icgc/data/heliograph && \
    minio

