**About SONG**

- SONG is our metadata server that tracks metadata associated with the data files uploaded to a data storage server
- SONG is implemented as a series of REST endpoints using a Spring Boot Java framework

**Running your own SONG server**
- To run your own SONG server to track metadata you've uploaded to a storage repository,  you need:
  - For data security, an Oath2 authorization server (we use ...) 
  - An id token generation server (we use the ... protocol) 
  - Access to a storage repository 
    - A Java API for that storage repository (we support S3 and native storage,
	but it should be easy to extend to other APIs; all you have to do is
        show SONG how to ask the storage repository if a file exists or not) 
    - A Java interface to access that storage repository from SONG, so that the SONG server can verify that a given file was successfully uploaded to the straoge repository (we currently have an interface for the S3 storage repository API; but it should be easy to create interfaces to check the contents of other storage APIs, as well)
  - A server to run SONG on, with Java 8, maven, and PostgresSQL 

**About SING**

- SING is our client program to upload, verify, and query metadata stored on a SONG metadata server; which references a corresponding data upload server which
stores the actual data files. 
- The metadata files you upload must be written according to the JSON specification given at <INSERT FILE PATH HERE> 
- Each JSON file must contain the following fields:
  - study _The study id that the SONG server administrator gave you when (s)he added your study to the SONG server, and gave you your access token to access it_    
**Metadata Upload Rules**
- Every upload that you save creates a new analysis, with it's own unique
analysis id. If you upload and save the same metadata six times, you will create six distinct analyses, with six distinct analysis ids.
- Every field in the analysis metadata has to be included with every upload; no partial uploads are allowed.
- When you save an upload, if you change the content of previous metadata that you have provided, (such as specifying a different gender for a given donor with a given donor id), we will update the metadata with your changes, and inform you about what has changed in the reply we send you. 
- You cannot change any existing sample/specimen/donor parent-child relationships when you publish an analysis; and we will return an error if you try.

