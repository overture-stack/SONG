def commit = "UNKNOWN"
def version = "UNKNOWN"

pipeline {
    agent {
        kubernetes {
            label 'song-executor'
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jdk
    tty: true
    image: openjdk:11
    env: 
      - name: DOCKER_HOST 
        value: tcp://localhost:2375 
  - name: dind-daemon 
    image: docker:18.06-dind
    securityContext: 
        privileged: true 
    volumeMounts: 
      - name: docker-graph-storage 
        mountPath: /var/lib/docker 
  - name: helm
    image: alpine/helm:2.12.3
    command:
    - cat
    tty: true
  - name: docker
    image: docker:18-git
    tty: true
    volumeMounts:
    - mountPath: /var/run/docker.sock
      name: docker-sock
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
      type: File
  - name: docker-graph-storage 
    emptyDir: {}
"""
        }
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
        stage('Test') {
            steps {
                container('jdk') {
                    sh "./mvnw package -DskipTests "
                }
            }
        }

        stage('Test Artifactory') {
           steps {
             rtUpload ( 
		serverId: 'artifactory-test', 
		spec: '''{
          		"files": [
            			{
              			"pattern": "song-client/target/*.tar.gz",
              			"target": "dcc-release/bio/overture/song-client/$version-SNAPSHOT/song-client-$version-dist.tar.gz"
            			},
                                {
                                "pattern": "song-client/target/*-exec.jar",
                                "target": "dcc-release/bio-overture/song-client/$version-SNAPSHOT/song-client-$version.jar"
                                }
         		]
    		}'''
             )
          }
       }
    }
}
