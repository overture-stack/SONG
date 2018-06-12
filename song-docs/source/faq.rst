============================
Frequently Asked Questions
============================

How can I find the latest official release via command line?
================================================================

Official releases can alternatively be found via command line as follows:

.. todo::
    Once SONG-292 - Create simple /version endpoint (https://github.com/overture-stack/SONG/issues/292) is closed, can replace the following 2 points, with a simple call to /version

1. Execute an **unauthenticated** github request (rate limited to 60 requests/hour) using ``curl`` and  ``jq``

.. code-block:: bash

    curl --silent "https://api.github.com/repos/overture-stack/SONG/releases" | jq '.[].tag_name | match("^song-\\d+\\.\\d+\\.\\d+$") | .string' | head -1 | xargs echo

**OR**

2. Execute an **authenticated** github request (rate limited to 5000 requests/hour) using ``curl`` and  ``jq``

.. code-block:: bash

    curl --silent -H "Authorization: Bearer $MY_GITHUB_OAUTH_TOKEN" "https://api.github.com/repos/overture-stack/SONG/releases" | jq '.[].tag_name | match("^song-\\d+\\.\\d+\\.\\d+$") | .string' | head -1 | xargs echo

..
    - how do i create a mirrored song server?
    - how can i sync between 2 song servers?
    - when syncing why cant i just dump the song server database, and restore it on the song server mirror database?  (because of publish state you cant)
    - why do i have to upload and then save? doesnt it make sense to merge them into one?
            - explain the purpose of validation for uploading, and that save == commit to db
    - (python) im getting a "dataclasses" error? why?
        - you are using python < 3.6
    - (server) im getting an duplicate analysis  attempt? what is that?
        - explain reason 
    - (server) im getting an analysis.id.collision error? what is that and how do i solve it?
    - why cant i create specimens, donors, samples, and files from their respective apis?
        - purpose is consistency, and to maintain that assumption, need to restrict user submission through upload service
    - why is a central id server used? whats the purpose
    - 

