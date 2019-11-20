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
                    sh "./mvnw test package"
                }
            }
        }
        stage('Build & Publish Develop') {
            when {
                branch "develop"
            }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId:'OvertureDockerHub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker build --target=server --network=host -f Dockerfile . -t overture/song-server:edge -t overture/song-server:${commit}"
                    sh "docker build --target=client --network=host -f Dockerfile . -t overture/song-client:edge -t overture/song-client:${commit}"
                    sh "docker push overture/song-server:${commit}"
                    sh "docker push overture/song-server:edge"
                    sh "docker push overture/song-client:${commit}"
                    sh "docker push overture/song-client:edge"
                }
            }
        }
        stage('Release & tag') {
          when {
            branch "master"
          }
          steps {
                container('docker') {
                    withCredentials([usernamePassword(credentialsId: 'OvertureBioGithub', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                        sh "git tag ${version}"
                        sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/overture-stack/song --tags"
                    }
                    withCredentials([usernamePassword(credentialsId:'OvertureDockerHub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh 'docker login -u $USERNAME -p $PASSWORD'
                    }
                    sh "docker build --target=server --network=host -f Dockerfile . -t overture/song-server:latest -t overture/song-server:${version}"
                    sh "docker build --target=client --network=host -f Dockerfile . -t overture/song-client:latest -t overture/song-client:${version}"
                    sh "docker push overture/song-server:${version}"
                    sh "docker push overture/song-server:latest"
                    sh "docker push overture/song-client:${version}"
                    sh "docker push overture/song-client:latest"
                }
            }
        }

        stage('Deploy to Overture QA') {
            when {
                  branch "develop"
            }
            steps {
                container('helm') {
                    withCredentials([file(credentialsId:'4ed1e45c-b552-466b-8f86-729402993e3b', variable: 'KUBECONFIG')]) {
                        sh 'env'
                        sh 'helm init --client-only'
                        sh "helm ls --kubeconfig $KUBECONFIG"
                        sh "helm repo add overture https://overture-stack.github.io/charts-server/"
                        sh """
                            helm upgrade --kubeconfig $KUBECONFIG --install --namespace=overture-qa song-overture-qa \\
                            overture/song --reuse-values --set-string image.tag=${commit}
                           """
                    }
                }
            }
        }

        stage('Deploy to Overture Staging') {
            when {
                  branch "master"
            }
            steps {
                container('helm') {
                    withCredentials([file(credentialsId:'4ed1e45c-b552-466b-8f86-729402993e3b', variable: 'KUBECONFIG')]) {
                        sh 'env'
                        sh 'helm init --client-only'
                        sh "helm ls --kubeconfig $KUBECONFIG"
                        sh "helm repo add overture https://overture-stack.github.io/charts-server/"
                        sh """
                            helm upgrade --kubeconfig $KUBECONFIG --install --namespace=overture-staging song-overture-staging \\
                            overture/song --reuse-values --set-string image.tag=${version}
                           """
                    }
                }
            }
        }
        stage('Upload Artifact SNAPSHOT') {

	when { anyOf {
                  branch 'test-develop' 
                  branch 'develop' 
               }
             }
            steps {
                script {
                    repo = "dcc-snapshot/bio/overture"
                    client = "song-client"
                    clientName = "$client-$version-SNAPSHOT"
                    clientTarget = "$repo/$client/$version-SNAPSHOT/$clientName"

                    server = "song-server"
                    serverName = "$server-$version-SNAPSHOT"
                    serverTarget = "$repo/$server/$version-SNAPSHOT/$serverName"

                    core = "song-core"
                    coreName = "$core-$version-SNAPSHOT"
                    coreTarget = "$repo/$core/$version-SNAPSHOT/$coreName"

                    songTarget="$repo/song/$version-SNAPSHOT/song-$version-SNAPSHOT"
                    fileSet = [files:
                                       [      // song
                                              [pattern: "pom.xml", target: "${songTarget}.pom"],
                                              // song-client
                                              [pattern: "${client}/target/*.tar.gz",
                                               target : "${clientTarget}-dist.tar.gz"],
                                              [pattern: "${client}/target/*-exec.jar",
                                               target : "${clientTarget}-exec.jar"],
                                              [pattern        : "$client/target/*.jar",
                                               target         : "${clientTarget}.jar",
                                               excludePatterns: ["*-exec.jar"]
                                              ],
                                              [pattern: "${client}/pom.xml",
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
                                              [pattern: "${server}/pom.xml",
                                               target : "${serverTarget}.pom"
                                              ],

                                              // song-core
                                              [pattern        : "$core/target/*.jar",
                                               target         : "${coreTarget}.jar",
                                               excludePatterns: ["*-exec.jar"]
                                              ],
                                              [pattern: "$core/pom.xml",
                                               target : "${coreTarget}.pom"
                                              ]
                                       ]
                    ]
                    files = JsonOutput.toJson(fileSet)

                    print("Upload file specification=${files}")
                    print("Please work for me!")
                }
                rtUpload(serverId: 'artifactory',
                        spec: files
                )
            }
        }

        stage('Upload Artifact Release') {
            when { anyOf { 
		   branch 'master'
                   branch 'test-master'
		}} 
            steps {
                script {
                    repo = "dcc-release/bio/overture"
                    client = "song-client"
                    clientName = "$client-$version"
                    clientTarget = "$repo/$client/$version/$clientName"

                    server = "song-server"
                    serverName = "$server-$version"
                    serverTarget = "$repo/$server/$version/$serverName"

                    core = "song-core"
                    coreName = "$core-$version"
                    coreTarget = "$repo/$core/$version/$coreName"

                    songTarget="$repo/song/$version/song-$version"
                    fileSet = [files:
                                       [      // song
                                              [pattern: "pom.xml", target: "${songTarget}.pom"],
                                              // song-client
                                              [pattern: "${client}/target/*.tar.gz",
                                               target : "${clientTarget}-dist.tar.gz"],
                                              [pattern: "${client}/target/*-exec.jar",
                                               target : "${clientTarget}-exec.jar"],
                                              [pattern        : "$client/target/*.jar",
                                               target         : "${clientTarget}.jar",
                                               excludePatterns: ["*-exec.jar"]
                                              ],
                                              [pattern: "${client}/pom.xml",
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
                                              [pattern: "${server}/pom.xml",
                                               target : "${serverTarget}.pom"
                                              ],

                                              // song-core
                                              [pattern        : "$core/target/*.jar",
                                               target         : "${coreTarget}.jar",
                                               excludePatterns: ["*-exec.jar"]
                                              ],
                                              [pattern: "$core/pom.xml",
                                               target : "${coreTarget}.pom"
                                              ]
                                       ]
                    ]
                    files = JsonOutput.toJson(fileSet)

                    print("Upload file specification=${files}")
                }
                rtUpload(serverId: 'artifactory',
                        spec: files
                )
            }
        }
    }
}
