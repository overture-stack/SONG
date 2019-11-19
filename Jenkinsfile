def commit = "UNKNOWN"
def version = "UNKNOWN"

pipeline {
    agent any 
    tools {
       jdk   'OpenJDK 11'
       maven 'MVN3'
    }
    stages {
        stage('Upload Artifacts to Artifactory') {
           steps {
              rtMavenDeployer (
                       id: 'deployer-unique-id',
                       serverId: 'artifactory',
                       releaseRepo: 'dcc-release',
                       snapshotRepo: 'dcc-snapshot'
               )
               rtMavenRun (
                       tool: 'MVN3',
                       pom: 'pom.xml',
                       goals: 'clean install -Dmaven.repo.local=.m2',
                       // Maven options.
                       opts: '-Dmaven.test.skip=true -Xms1024m -Xmx4096m',
                       deployerId: 'deployer-unique-id',
               )
          }
       }
    }
}
