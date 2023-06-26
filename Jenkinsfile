import groovy.json.JsonOutput

def repo = 'UNKNOWN'

def pom(path, target) {
    return [pattern: "${path}/pom.xml", target: "${target}.pom"]
}

def jar(path, target) {
    return [pattern: "${path}/target/*.jar",
            target         : "${target}.jar",
            excludePatterns: ['*-exec.jar']
            ]
}

def tar(path, target) {
    return [pattern: "${path}/target/*.tar.gz",
            target : "${target}-dist.tar.gz"]
}

def runjar(path, target) {
    return [pattern: "${path}/target/*-exec.jar",
            target : "${target}-exec.jar"]
}

String podSpec = '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jdk
    tty: true
    image: adoptopenjdk/openjdk11:jdk-11.0.6_10-alpine
    env:
      - name: DOCKER_HOST
        value: tcp://localhost:2375
  - name: dind-daemon
    image: docker:18.06-dind
    securityContext:
        privileged: true
        runAsUser: 0
    volumeMounts:
      - name: docker-graph-storage
        mountPath: /var/lib/docker
  - name: docker
    image: docker:18-git
    tty: true
    env:
    - name: DOCKER_HOST
      value: tcp://localhost:2375
    - name: HOME
      value: /home/jenkins/agent
  - name: curl
    image: curlimages/curl
    command:
    - cat
    tty: true
  securityContext:
    runAsUser: 1000
  volumes:
  - name: docker-graph-storage
    emptyDir: {}
'''

pipeline {
    agent {
        kubernetes {
            yaml podSpec
        }
    }

    environment {
        dockerHub = 'overture/song'
        gitHubRegistry = 'ghcr.io'
        gitHubRepo = 'overture-stack/song'
        githubPackages = "${gitHubRegistry}/${gitHubRepo}"

        commit = sh(
            returnStdout: true,
            script: 'git describe --always'
        ).trim()

        version = readMavenPom().getVersion()
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }

    stages {
        stage('Test') {
            steps {
                container('jdk') {
                    sh './mvnw test package'
                }
            }
        }

        stage('Build images') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'master'
                    branch 'test'
                }
            }
            steps {
                container('docker') {
                    sh "docker build --target=server --network=host -f Dockerfile . -t server:${commit}"
                    sh "docker build --target=client --network=host -f Dockerfile . -t client:${commit}"
                }
            }
        }

        stage('Publish images') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'master'
                    branch 'test'
                }
            }
            parallel {
                stage('...to dockerhub') {
                    steps {
                        container('docker') {
                            withCredentials([usernamePassword(
                                credentialsId:'OvertureDockerHub',
                                passwordVariable: 'PASSWORD',
                                usernameVariable: 'USERNAME'
                            )]) {
                                sh "docker login -u $USERNAME -p $PASSWORD"

                                script {
                                    if (env.BRANCH_NAME ==~ /(main|master)/) { // push latest and version tags
                                        sh "docker tag server:${commit} ${dockerHub}-server:${version}"
                                        sh "docker push ${dockerHub}-server:${version}"

                                        sh "docker tag server:${commit} ${dockerHub}-server:latest"
                                        sh "docker push ${dockerHub}-server:latest"

                                        sh "docker tag client:${commit} ${dockerHub}-client:edge"
                                        sh "docker push ${dockerHub}-client:${version}"

                                        sh "docker tag client:${commit} ${dockerHub}-client:edge"
                                        sh "docker push ${dockerHub}-client:latest"
                                    } else { // push commit tags
                                        sh "docker tag server:${commit} ${dockerHub}-server:${commit}"
                                        sh "docker push ${dockerHub}-server:${commit}"

                                        sh "docker tag client:${commit} ${dockerHub}-client:${commit}"
                                        sh "docker push ${dockerHub}-client:${commit}"
                                    }

                                    if (env.BRANCH_NAME ==~ /(develop)/) { // push edge tags
                                        sh "docker tag server:${commit} ${dockerHub}-server:edge"
                                        sh "docker push ${dockerHub}-server:edge"

                                        sh "docker tag client:${commit} ${dockerHub}-client:edge"
                                        sh "docker push ${dockerHub}-client:edge"
                                    }
                                }
                            }
                        }
                    }
                }

                stage('...to github') {
                    steps {
                        container('docker') {
                            withCredentials([usernamePassword(
                                credentialsId:'OvertureBioGithub',
                                passwordVariable: 'PASSWORD',
                                usernameVariable: 'USERNAME'
                            )]) {
                                sh "docker login ${gitHubRegistry} -u $USERNAME -p $PASSWORD"

                                script {
                                    if (env.BRANCH_NAME ==~ /(main|master)/) { // push latest and version tags
                                        sh "docker tag server:${commit} ${githubPackages}-server:${version}"
                                        sh "docker push ${githubPackages}-server:${version}"

                                        sh "docker tag server:${commit} ${githubPackages}-server:latest"
                                        sh "docker push ${githubPackages}-server:latest"

                                        sh "docker tag client:${commit} ${githubPackages}-client:edge"
                                        sh "docker push ${githubPackages}-client:${version}"

                                        sh "docker tag client:${commit} ${githubPackages}-client:edge"
                                        sh "docker push ${githubPackages}-client:latest"
                                    } else { // push commit tags
                                        sh "docker tag server:${commit} ${githubPackages}-server:${commit}"
                                        sh "docker push ${githubPackages}-server:${commit}"

                                        sh "docker tag client:${commit} ${githubPackages}-client:${commit}"
                                        sh "docker push ${githubPackages}-client:${commit}"
                                    }

                                    if (env.BRANCH_NAME ==~ /(develop)/) { // push edge tags
                                        sh "docker tag server:${commit} ${githubPackages}-server:edge"
                                        sh "docker push ${githubPackages}-server:edge"

                                        sh "docker tag client:${commit} ${githubPackages}-client:edge"
                                        sh "docker push ${githubPackages}-client:edge"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Release & tag') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                container('docker') {
                    withCredentials([usernamePassword(
                        credentialsId: 'OvertureBioGithub',
                        passwordVariable: 'GIT_PASSWORD',
                        usernameVariable: 'GIT_USERNAME'
                    )]) {
                        sh "git tag ${version}"
                        sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/${gitHubRepo} --tags"
                    }
                }
            }
        }

        stage('Destination SNAPSHOT') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'test-develop'
                }
            }
            steps {
                script {
                    repo = 'dcc-snapshot/bio/overture'
                }
            }
        }

        stage('Destination release') {
            when {
                anyOf {
                    branch 'master'
                    branch 'test-master'
                }
            }
            steps {
                script {
                    repo = 'dcc-release/bio/overture'
                }
            }
        }

        stage('Upload Artifacts') {
            when {
                anyOf {
                    branch 'master'
                    branch 'test-master'
                    branch 'develop'
                    branch 'test-develop'
                }
            }
            steps {
                script {
                    project = 'song'
                    versionName = "$version"
                    subProjects = ['client', 'core', 'server', 'java-sdk']

                    files = []
                    files.add([pattern: 'pom.xml', target: "$repo/$project/$versionName/$project-${versionName}.pom"])

                    for (s in subProjects) {
                        name = "${project}-$s"
                        target = "$repo/$name/$versionName/$name-$versionName"
                        files.add(pom(name, target))
                        files.add(jar(name, target))

                        if (s in ['client', 'server']) {
                            files.add(runjar(name, target))
                            files.add(tar(name, target))
                        }
                    }

                    fileSet = JsonOutput.toJson([files: files])
                    pretty = JsonOutput.prettyPrint(fileSet)
                    print("Uploading files=${pretty}")
                }

                rtUpload(serverId: 'artifactory', spec: fileSet)
            }
        }

        stage('Deploy to Overture QA') {
            when {
                branch 'develop'
            }
            steps {
                build(job: '/Overture.bio/provision/DeployWithHelm', parameters: [
                        [$class: 'StringParameterValue', name: 'OVERTURE_ENV', value: 'qa' ],
                        [$class: 'StringParameterValue', name: 'OVERTURE_CHART_NAME', value: 'song'],
                        [$class: 'StringParameterValue', name: 'OVERTURE_RELEASE_NAME', value: 'song'],
                        [$class: 'StringParameterValue', name: 'OVERTURE_HELM_CHART_VERSION', value: ''], // use latest
                        [$class: 'StringParameterValue', name: 'OVERTURE_HELM_REPO_URL', value: 'https://overture-stack.github.io/charts-server/'],
                        [$class: 'StringParameterValue', name: 'OVERTURE_HELM_REUSE_VALUES', value: 'true' ],
                        [$class: 'StringParameterValue', name: 'OVERTURE_ARGS_LINE', value: "--set-string image.tag=${commit}" ]
                ])
            }
        }

        stage('Deploy to Overture Staging') {
            when {
                branch 'master'
            }
            steps {
                build(job: '/Overture.bio/provision/DeployWithHelm', parameters: [
                        [$class: 'StringParameterValue', name: 'OVERTURE_ENV', value: 'staging' ],
                        [$class: 'StringParameterValue', name: 'OVERTURE_CHART_NAME', value: 'song'],
                        [$class: 'StringParameterValue', name: 'OVERTURE_RELEASE_NAME', value: 'song'],
                        [$class: 'StringParameterValue', name: 'OVERTURE_HELM_CHART_VERSION', value: ''], // use latest
                        [$class: 'StringParameterValue', name: 'OVERTURE_HELM_REPO_URL', value: 'https://overture-stack.github.io/charts-server/'],
                        [$class: 'StringParameterValue', name: 'OVERTURE_HELM_REUSE_VALUES', value: 'true' ],
                        [$class: 'StringParameterValue', name: 'OVERTURE_ARGS_LINE', value: "--set-string image.tag=${version}" ]
                ])
            }
        }
    }

    post {
        fixed {
            withCredentials([string(
                credentialsId: 'OvertureSlackJenkinsWebhookURL',
                variable: 'fixed_slackChannelURL'
            )]) {
                container('curl') {
                    script {
                        if (env.BRANCH_NAME ==~ /(develop|main|master)/) {
                            sh "curl \
                                -X POST \
                                -H 'Content-type: application/json' \
                                --data '{ \
                                    \"text\":\"Build Fixed: ${env.JOB_NAME} [Build ${env.BUILD_NUMBER}](${env.BUILD_URL}) \" \
                                }' \
                                ${fixed_slackChannelURL}"
                        }
                    }
                }
            }
        }

        success {
            withCredentials([string(
                credentialsId: 'OvertureSlackJenkinsWebhookURL',
                variable: 'success_slackChannelURL'
            )]) {
                container('curl') {
                    script {
                        if (env.BRANCH_NAME ==~ /(main|master)/) {
                            sh "curl \
                                -X POST \
                                -H 'Content-type: application/json' \
                                --data '{ \
                                    \"text\":\"New Song published succesfully: v.${version} [Build ${env.BUILD_NUMBER}](${env.BUILD_URL}) \" \
                                }' \
                                ${success_slackChannelURL}"
                        }
                    }
                }
            }
        }

        unsuccessful {
            withCredentials([string(
                credentialsId: 'OvertureSlackJenkinsWebhookURL',
                variable: 'failed_slackChannelURL'
            )]) {
                container('curl') {
                    script {
                        if (env.BRANCH_NAME ==~ /(develop|main|master)/) {
                            sh "curl \
                                -X POST \
                                -H 'Content-type: application/json' \
                                --data '{ \
                                    \"text\":\"Build Failed: ${env.JOB_NAME} [Build ${env.BUILD_NUMBER}](${env.BUILD_URL}) \" \
                                }' \
                                ${failed_slackChannelURL}"
                        }
                    }
                }
            }
        }
    }
}
