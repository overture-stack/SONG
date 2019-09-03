=================
Design
=================

Motivation
===============
- historically, what were the options (GNOS)
- with so much data, need a more reliable and minimally mutable method of tracking metadata other than excel spreadsheets
- what is the challenge with metadata tracking?
    - avoiding duplicates (hibernate manages the defined relationships)
    - preventing corruptive mal-formatted data (this is what validation fixes)
    - accumulation of useless data (hanging data or orphanes are considered useless)
    - 

.. should explain the _introduction_features in detail

Objectives
============
- robust
- consistency over performance , hence SQL backend (accurate)
- strict data structure/opinionated, however allow the user to upload custom data (info fields)
- use centralized id service for generating and recording ids.
- secure (oauth2)
- talk to score for object data existence
- scalable

Architecture
===============

show image of arcitecture

Data Model
=============

show data model as UML. Point is to show the many-to-many relationships

Rules
=============

- cannot create donors, specimens, samples through there endpoints. Must use the upload endpoint, as it contains the logic for deciding to update or create
- no orphaned entities:
    - samples: a sample without a parent specimen is useless and should be deleted, however a sample can have 0 files
    - specimens: a specimen without a parent donor is useless and should be deleted.
    - donors: a donor without a parent study is useless and should be deleted.
    - files: a file without a parent analysis is uselss and should be deleted. 
    - analyses: an analysis wihtout a parent study is useless and should be deleted. Also if an analysis has 0 samples, it is useless and should be deleted.
- analysis ids are unique and cononical UUIDs throughout all song servers that use the same id server.
- submitter id are relative to the study and can be configured/chosen by the user, however the unique cononical entity ids are managed by the SONG server
- cannot have duplicate entities
- all required fields must be uploaded (refer to json schema). no partial uploads allowed
- when uploading metadata with the same analysisId as a previous upload, the metadata will be replaced with the new version, 
  and the reply message will include a warning with the previous version of the metadata

- following fields are enumerated. They are defined in the jsonSchema and validated by the upload endpoint
    - donor.donorGender
    - sample.sampleType
    - specimen.specimenType
    - specimen.specimenClass
    - file.fileAccess
    - file.fileType

- all entities must follow the following entity relationships
    - show diagram with all relationships


Central ID Service
===================
explain importance of the id service
