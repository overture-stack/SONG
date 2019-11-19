def commit = "UNKNOWN"
def version = "UNKNOWN"

pipeline {
    agent: any
    tools {
       maven 'MVN3'
    }
    stages {
        stage('Prepare') {
            steps {
                script {
                    commit = sh(returnStdout: true, script: 'git describe --always').trim()
                }
                script {
                    version = readMavenPom().getVersion()
                }
            }
        }
        stage('Upload Artifacts to Artifactory') {
           steps {
               rtMavenResolver (
                       id: 'resolver-unique-id',
                       serverId: 'artifactory',
                       releaseRepo: 'dcc-release',
                       snapshotRepo: 'dcc-snapshot'
               )

               rtMavenDeployer (
                       id: 'deployer-unique-id',
                       serverId: 'artifactory',
                       releaseRepo: 'dcc-release',
                       snapshotRepo: 'dcc-snapshot'
               )
               rtMavenRun (
                       tool: 'MVN3',
                       pom: 'song/pom.xml',
                       goals: 'clean install',
                       // Maven options.
                       opts: '-Dmaven.test.skip=true -Xms1024m -Xmx4096m',
                       resolverId: 'resolver-unique-id',
                       deployerId: 'deployer-unique-id',
               )
          }
       }
    }
}
