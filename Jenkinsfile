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
                sh """cat > fake.pom <<-EOT                
<dependency>
    <groupId>bio.overture</groupId>
    <artifactId>song</artifactId>
    <version>$version</version>
    <type>pom</type>
</dependency> 
EOT 
"""
                sh "echo 'contents of our fake pomfile';cat fake.pom"
                script {
                    repo = "dcc-snapshot/bio/overture"
                    client = "song-client"
                    clientName = "$client-$version-SNAPSHOT"
                    clientTarget = "$repo/$client/$version/$clientName"

                    server = "song-server"
                    serverName = "$server-$version-SNAPSHOT"
                    serverTarget = "$repo/$server/$version/$serverName"

                    core = "song-core"
                    coreName = "$core-$version-SNAPSHOT"
                    coreTarget = "$repo/$core/$version/$coreName"
                    fileSet = [files:
                                       [      // song
                                              [pattern: "fake.pom", target: "${repo}/song/song-${version}.pom"],
                                              // song-client
                                              [pattern: "${client}/target/*.tar.gz",
                                               target : "${clientTarget}-dist.tar.gz"],
                                              [pattern: "${client}/target/*-exec.jar",
                                               target : "${clientTarget}-exec.jar"],
                                              [pattern        : "$client/target/*.jar",
                                               target         : "${clientTarget}.jar",
                                               excludePatterns: ["*-exec.jar"]
                                              ],
                                              [pattern: "fake.pom",
                                               target : "${clientTarget}.pom"
                                              ],

                                              // song-server
                                              [pattern: "${server}/target/*.tar.gz",
                                               target : "${serverTarget}-dist.tar.gz"],
                                              [pattern: "${server}/target/*-exec.jar",
                                               target : "${serverTarget}-exec.jar"],
                                              [pattern        : "$server/target/*.jar",
                                               target         : "${serverTarget}.jar",
                                               excludePatterns: ["*-exec.jar"]
                                              ],
                                              [pattern: "fake.pom",
                                               target : "${serverTarget}.pom"
                                              ],

                                              // song-core
                                              [pattern        : "$core/target/*.jar",
                                               target         : "${coreTarget}.jar",
                                               excludePatterns: ["*-exec.jar"]
                                              ],
                                              [pattern: "fake.pom",
                                               target : "${coreTarget}.pom"
                                              ]
                                       ]
                    ]
                    files = JsonOutput.toJson(fileSet)

                    print("Upload file specification=${files}")
                    print("Please work! I would appreciate it greatly!")
                }
                rtUpload(serverId: 'artifactory',
                        spec: files
                )
            }
        }
    }
}
