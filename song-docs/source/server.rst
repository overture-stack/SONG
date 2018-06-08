=======================================
Deploying a SONG Server in Production
=======================================

The following section describes how to install, configure and run the SONG server in production.


.. _server_prereq:

Prerequisites
==================

The following software dependencies are required in order to run the server:

- Bash Shell
- Java 8 or higher

.. _server_official_releases:

Official Releases
==================

Official SONG releases can be found `here <https://github.com/overture-stack/SONG/releases>`_. The releases follow the `symantic versioning specification <https://semver.org/>`_ and contain notes with a description of the bug fixes, new features or enhancements and breaking changes, as well as links to downloads and change logs. All official song releases are tagged in the format ``$COMPONENT-$VERSION``, where the ``$COMPONENT`` portion follows the regex ``^[a-z-]+$`` and the ``$VERSION`` component follows ``^\d+\.\d+\.\d+$`` . For the SONG server, the tag format has the regex: ``^song-\d+\.\d+\.\d+$``. For example ``song-1.0.0``.

Alternatively, official releases can be found via command line as follows:

1. Execute an **unauthenticated** github request (rate limited to 60 requests/hour) using ``curl`` and  ``jq``

.. code-block:: bash

    curl --silent "https://api.github.com/repos/overture-stack/SONG/releases" | jq '.[].tag_name | match("^song-\\d+\\.\\d+\\.\\d+$") | .string' | head -1 | xargs echo

**OR**


2. Execute an **authenticated** github request (rate limited to 5000 requests/hour) using ``curl`` and  ``jq``

.. code-block:: bash

    curl --silent -H "Authorization: Bearer $MY_GITHUB_OAUTH_TOKEN" "https://api.github.com/repos/overture-stack/SONG/releases" | jq '.[].tag_name | match("^song-\\d+\\.\\d+\\.\\d+$") | .string' | head -1 | xargs echo


Installation
===============================

Once the desired release tag and therefore ``$VERSION`` are known, the corresponding distibution can be downloaded using the command:

.. code-block:: bash
    
   curl "https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-server/$VERSION/song-server-$VERSION-dist.tar.gz" -Ls -o song-server-$VERSION-dist.tar.gz

This distibution contains the default configuration and jars for running the server. To unarchive, run the command:

.. code-block:: bash

    tar zxvf song-server-$VERSION-dist.tar.gz


Configuration
===============================
By default, the SONG server distibution is configured to run in secure production mode. The server can easily be configured by creating the file ``./conf/application-secure.properties`` with the following contents:

.. code-block:: bash

    ################################
    #     OAuth2 Server Config     #
    ################################

    # Scope prefix used to authorize requests to the SONG server.
    auth.server.prefix=collab

    # Endpoint to validate OAuth2 tokens
    auth.server.url=https://auth.icgc.org/oauth/check_token

    auth.server.clientId=<auth-client-id>
    auth.server.clientSecret=<auth-client-secret>


    ################################
    #       ID Server Config       #
    ################################

    # URL of the ID server
    id.idUrl=https://id.icgc.org

    # ID server auth token, which has id.create scope
    id.authToken=<id-server-auth-token>

    # Enabled to use an ID server. If false, will use 
    # and in-memory id server (use only for testing)
    id.realIds=true

    ################################
    #   Postgres Database Config   #
    ################################

    spring.datasource.url=jdbc:postgresql://localhost:5432/song?stringtype=unspecified
    spring.datasource.username=<my-db-username>
    spring.datasource.password=<my-db-password>

    ################################
    # SCORE Storage Server Config  #
    ################################

    # URL used to ensure files exist in the storage server
    # Note: The same SONG auth token will be used for requests sent 
    #       to the SCORE server. This means the same scope must be 
    #       authorized to access the SCORE storage service.
    dcc-storage.url=https://storage.cancercollaboratory.org


The example file above configures the server to use the ``id.icgc.org`` id service, ``auth.icgc.org`` auth service, and the ``storage.cancercollaboratory.org`` SCORE storage service with a local Postgres database, however any similar service can be used. For example, the :ref:`Docker for SONG Microservice Architecture <docker_microservice_architecture>` uses a different implementation of an OAuth2 server.


Running as a Service
===============================

Although the SONG server distribution could be run as a **standalone** application, it must be manually started or stopped by the user. 
For a long-running server, sudden power loss or a hard reboot would mean the standalone application would need to be restarted manually. 
However, if the SONG server distribution is run as a **service**, the OS would be responsible for automatically restarting the service upon reboot.
For this reason, the distibution should be configured as a service that is always started on boot. 

Linux
--------

Assuming the directory path of the distribution is ``$SONG_SERVER_HOME``, the following steps will register the SONG server 
as a SysV service on any Linux host supporting SysV and the :ref:`Prerequisites<server_prereq>`, and configure it to start on boot.

.. code-block:: bash

  # Register the SONG service
  sudo ln -s $SONG_SERVER_HOME/bin/song-server /etc/init.d/song-server

  # Start on boot (defaults)
  sudo update-rc.d song-server defaults
    
It can also be manually managed using serveral commands:

.. code-block:: bash

    # Start the service
    sudo service song-server start
    
    # Stop the service
    sudo service song-server stop

    # Restart the service
    sudo service song-server restart

.. todo::

    Example SSL Termination with NGINX
    ====================================


    Installing NGINX
    -----------------

    sdfsdf

    LetsEncrypt
    --------------

    sdf

    Configuring NGINX
    -------------------
    sdfsd

    Running NGINX as a Service
    ---------------------------
    sdfsd


