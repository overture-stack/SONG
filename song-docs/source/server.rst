.. _server_ref:

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
- Postgres database

.. note::
    Only a postgres database can be used, since postgres-specific features are used in the SONG server

.. _server_official_releases:

Official Releases
==================


Official SONG releases can be found `here <https://github.com/overture-stack/SONG/releases>`_. The releases follow the `symantic versioning specification <https://semver.org/>`_ and contain notes with a description of the bug fixes, new features or enhancements and breaking changes, as well as links to downloads and change logs. All official song releases are tagged in the format ``$COMPONENT-$VERSION``, where the ``$COMPONENT`` portion follows the regex ``^[a-z-]+$`` and the ``$VERSION`` component follows ``^\d+\.\d+\.\d+$`` . For the SONG server, the tag format has the regex: ``^song-\d+\.\d+\.\d+$``. For example ``song-1.0.0``.




Installation
===============================

Once the desired release tag and therefore ``$VERSION`` are known, the corresponding distibution can be downloaded using the command:

.. code-block:: bash

   curl "https://artifacts.oicr.on.ca/artifactory/dcc-release/bio/overture/song-server/$VERSION/song-server-$VERSION-dist.tar.gz" -Ls -o song-server-$VERSION-dist.tar.gz

This distribution contains the default configuration and jars for running the server. To unarchive, run the command:

.. code-block:: bash

    tar zxvf song-server-$VERSION-dist.tar.gz


Configuration
===============================

Server
---------------
By default, the SONG server distribution is configured to run in secure production mode. The server can easily be configured by creating the file ``./conf/application-secure.properties`` with the following contents:

.. code-block:: bash


   ################################
   #     SONG Server Config       #
   ################################

   server.port=8080

   ################################
   #     OAuth2 Server Config     #
   ################################

   # Scope prefix used to authorize requests to the SONG server.
   # For example, using the configurations below, the User-Agent's
   # access token would need to have collab.upload scope in order to
   # complete an authorized request
   auth.server.prefix=collab
   auth.server.suffix=upload

   # Endpoint to validate OAuth2 tokens
   auth.server.url=https://auth.icgc.org/oauth/check_token

   auth.server.clientId=<auth-client-id>
   auth.server.clientSecret=<auth-client-secret>


   ################################
   #       ID Server Config       #
   ################################

   # URL of the ID server
   id.idUrl=https://id.icgc.org

   # Application level access token used to interact with the ID server. 
   # The access token must have id.create scope
   id.authToken=<id-server-access-token>

   # Enabled to use an ID server. If false, will use
   # and in-memory id server (use only for testing)
   id.realIds=true

   ################################
   #   Postgres Database Config   #
   ################################

   spring.datasource.url=jdbc:postgresql://localhost:5432/song?stringtype=unspecified
   spring.datasource.username=<my-db-username>
   spring.datasource.password=<my-db-password>

   # Enable flyway to manage database migrations automatically
   spring.flyway.enabled=true
   spring.flyway.locations=classpath:db/migration

   ################################
   # SCORe Server Config  #
   ################################

   # URL used to ensure files exist in the score server
   score.url=https://storage.cancercollaboratory.org

   # Application level access token used internally by the SONG server to download
   # additional file metadata from the SCORe server. This access token must have the 
   # correct download scope inorder to download from SCORe. In the case of collab,
   # it would be collab.download
   score.accessToken=<score-access-token-with-download-scope>



The example file above configures the server to use the ``id.icgc.org`` id service, ``auth.icgc.org`` auth service, and the ``storage.cancercollaboratory.org`` SCORe service with a local Postgres database, however any similar service can be used. For example, the :ref:`Docker for SONG Microservice Architecture <docker_microservice_architecture>` uses a different implementation of an OAuth2 server.

Database
----------------
If the user chooses to host their own song server database, it can easily be initialized with a few commands. As of ``song-1.5.0``, SONG server database migrations are managed by `flyway <https://flywaydb.org/getstarted>`_. 
When upgrading the SONG server version, a flyway migration must be run. 

The following steps show how to create an empty database, and migrate a new or exising database using flyway.

Migrating a Database
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
This scenario is relevant to users installing a SONG server for the first time, or for those upgrading the SONG server to a newer version.

If the database doesnt exist yet, a flyway migration can easily be run on a newly created postgres database using the example commands below where, for example, the database user is ``postgres``, password is ``password``, database name is ``song`` and database url is ``http://localhost:8082``

**1. Create an empty database with password and user**

Skip this step and move to step 2 if the database already exists.

.. code-block:: bash

   # Create an empty database called "song" with user "postgres"
   sudo -u postgres psql -c "createdb song"

   # Create the password "myNewPassword" for the user "postgres"
   sudo -u postgres psql postgres -c ‘ALTER USER postgres WITH PASSWORD ‘myNewPassword’;

**2. Run a flyway migration on the empty or existing database for a particular SONG server version.**

This step should be run initially on an empty database or when upgrading the SONG server version. In either case, the same commands should be executed:

.. code-block:: bash

   # Clone the SONG repository for version "song-X.X.X"
   git clone --branch song-X.X.X https://github.com/overture-stack/song

   # Run the migration on the empty database "song" for version "song-X.X.X"
   cd song
   ./mvnw -pl song-server flyway:migrate \
      -Dflyway.url=jdbc:postgresql://localhost:8082/song?stringtype=unspecified \
      -Dflyway.user=postgres \
      -Dflyway.password=password \
      -Dflyway.locations=db/migration
..
   1. Create the ``song`` db as the user ``postgres``.

   .. code-block:: bash

       sudo -u postgres psql -c "createdb song"

   2. Create the password for the postgres user.

   .. code-block:: bash

       sudo -u postgres psql postgres -c ‘ALTER USER postgres WITH PASSWORD ‘myNewPassword’;

   3. Download the desired release's song-server jar archive. Refer to :ref:`Official Releases<server_official_releases>` for more information.

   .. code-block:: bash

       wget ‘https://artifacts.oicr.on.ca/artifactory/dcc-release/bio/overture/song-server/$VERSION/song-server-$VERSION.jar’ -O /tmp/song-server.jar


   4. Extract the schema.sql from the song-server jar archive.

   .. code-block:: bash

       unzip -p /tmp/song-server.jar  schema.sql > /tmp/schema.sql

   5. Load the schema.sql into the ``song`` db.

   .. code-block:: bash

       sudo -u postgres psql song < /tmp/schema.sql


Running as a Service
===============================

Although the SONG server distribution could be run as a **standalone** application, it must be manually started or stopped by the user.
For a long-running server, sudden power loss or a hard reboot would mean the standalone application would need to be restarted manually.
However, if the SONG server distribution is run as a **service**, the OS would be responsible for automatically restarting the service upon reboot.
For this reason, the distribution should be configured as a service that is always started on boot.

Linux (SysV)
-------------

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
