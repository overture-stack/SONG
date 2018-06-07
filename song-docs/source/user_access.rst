===================
User Access
===================

DACO Authentication
=====================

SONG servers use the `auth.icgc.org <https://auth.icgc.org>`_ OAuth2 authorization service to authorize secure API requests.
In order to create the neccessary access tokens to interact with the song-python-sdk and the SONG server,
the user **must** have DACO access. For more information about obtaining DACO access, please visit the instructions for
`DACO Cloud Access <http://docs.icgc.org/cloud/guide/#daco-cloud-access>`_.


OAuth2 Authorization
======================

With proper DACO access, the user can create an access token, using
the `Access Tokens <http://docs.icgc.org/cloud/guide/#access-tokens>`_
and `Token Manager <http://docs.icgc.org/cloud/guide/#token-manager>`_ instructions.

For each cloud environment, there is a specific authorization scope that is needed:

* For the **Collaboratory - Toronto** SONG Server (https://song.cancercollaboratory.org), the required authorization scope needed is **collab.upload**.
* For the **AWS - Virginia** SONG Server (https://virginia.song.icgc.org), the required authorization scope needed is **aws.upload**.


