**About SONG**

- SONG is our metadata server that tracks metadata associated with the data files uploaded to a data storage server
- SONG is implemented as a series of REST endpoints using a Spring Boot Java framework

**Running your own SONG server**
- To run your own SONG server to track metadata you've uploaded to a storage repository,  you need:
  - A server to run SONG on, with Java 8, maven, and PostgresSQL 
  - An id server 
  - For data security, an Oath2 authorization server [optional] 
  - Access to a storage repository 
  - A Java interface to access that storage repository from SONG, so that the SONG server can verify that a given file was successfully uploaded to the straoge repository (we currently have an interface for the S3 storage repository API; but it should be easy to create interfaces to check the contents of other storage APIs, as well)

If you have Docker installed and running on your machine, you can just run 'docker-compose build;docker-compose up' in the docker directory to get started with SONG

**About SING**

- SING is our client program to upload, verify, and query metadata stored on a SONG metadata server; which references a corresponding data upload server which
stores the actual data files. 
- The metadata files you upload must be written according to the JSON schemas located in the directory song-server/main/resources/schemas 

**Metadata Upload Rules**
- If you don't include a unique "analysisSubmitterId" in your JSON file, we will create a new upload id (and new analysis) for each submitter. 
- Every field in the analysis metadata has to be included with every upload; no partial uploads are allowed.
- When you upload metadata with the same analysisSubmitterId as a previous upload, we will replace the metadata with the new version, and the reply message will include a warning message and the previous version of the metadata. 
- You cannot change any existing sample/specimen/donor parent-child relationships when you publish an analysis; and we will return an error if you try.

