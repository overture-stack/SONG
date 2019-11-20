def commit = "UNKNOWN"
def version = "UNKNOWN"
import groovy.json.JsonOutput

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

        stage('Upload Artifact SNAPSHOT') {
           steps {
                script {
                    repo = "dcc-snapshot/bio/overture"
                    client="song-client"
                    server="song-server"
                    clientName = "${client}-${version}"
                    serverName = "${server}-${version}"
                    fileSet=[files:
                                     [
                                             [pattern: "${client}/target/*.tar.gz",
                                              target: "${repo}/${client}/${version}/${clientName}-dist.tar.gz"],
                                             [pattern: "${client}/target/*-exec.jar",
                                              target:"${repo}/${client}/${version}/${clientName}-exec.jar"],
                                             [pattern: "${server}/target/*.tar.gz",
                                              target:  "${repo}/${server}/${version}/${serverName}-dist.tar.gz"],
                                             [pattern: "${server}/target/*-exec.jar",
                                              target:  "${repo}/${server}/${version}/${serverName}-exec.jar"]
                                     ]
                    ]
                    files=JsonOutput.toJson(fileSet)

                    print("Upload file specification=${files}")
                }
             rtUpload( serverId: 'artifactory',
                        spec: files
             )
          }
       }
    }
}
