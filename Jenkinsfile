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
