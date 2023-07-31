pipeline {
    agent any

    tools {
        maven "M3"
    }

    stages {
        stage("Set deployment PARAM") {
            steps {
                script {
                    if (params.WORKSPACE == "sit") {
                        env.SERVER_IP_ADDRESS = "10.202.38.39"
                    }else if (params.WORKSPACE == "uat") {
                        env.SERVER_IP_ADDRESS = "10.202.38.40"
                        env.SERVER_IP_ADDRESS_SECOND = "10.202.38.41"
                    }else if (params.WORKSPACE == "production") {
                        env.SERVER_IP_ADDRESS = "TBD"
                    }
                }
            }
        }
        stage('Maven Build') {
            steps {
                script {
                    sh "echo 'Starting Maven Build...' "
                    sh "mvn clean package"
                }
            }
        }

        stage('Sonarqube Analysis') {
            steps {
                withSonarQubeEnv('SonarqubeNonProd') {
                    script {
                        sh "echo 'Starting Sonarqube Scan...' "
                        sh "mvn -am sonar:sonar \
                            -Dsonar.branch.name='${params.BRANCH}' \
                            -Dsonar.host.url=https://sonarqube.intranet.rhbgroup.com \
                            -Dsonar.projectKey=cdrb-obc"
                    }
                }
            }
        }

        stage('FileProcessing') {
            steps {
                script {
                    sh "mv target/obc-1.0.jar target/obc-1.0_`date +%Y%m%d_%H%M`.jar"
                    sh "scp target/obc-1.0_`date +%Y%m%d_%H%M`.jar root@${SERVER_IP_ADDRESS}:/opt/app/versions/"
                }
            }
        }

        stage('Deployment') {
            steps {
                script {
                    sh "echo 'Call remote deployment scirpt' "
                }
            }
        }
    }
}