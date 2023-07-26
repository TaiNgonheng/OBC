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
                        env.SONARQUBE_LOGIN = credentials('SonarqubeNonProd')
                    }else if (params.WORKSPACE == "uat") {
                        env.SERVER_IP_ADDRESS = "10.202.38.40"
                        env.SERVER_IP_ADDRESS_SECOND = "10.202.38.41"
                        env.SONARQUBE_LOGIN = credentials('SonarqubeNonProd')
                    }else if (params.WORKSPACE == "production") {
                        env.SERVER_IP_ADDRESS = "TBD"
                        env.SONARQUBE_LOGIN = credentials('SonarqubeNonProd')
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
                script {
                    withSonarQubeEnv('SonarqubeNonProd') {
                        script {
                            sh "echo 'Starting Sonarqube Scan...' "
                            sh "mvn -am sonar:sonar \
                                -Dsonar.projectKey=cdrb-obc \
                                -Dsonar.host.url=https://sonarqube.intranet.rhbgroup.com \
                                -Dsonar.branch.name=${params.SONARQUBE_BRANCH}" \ 
                                -Dsonar.login=${SONARQUBE_LOGIN}

                        }
                    }
                }
            }
        }
        stage('Deployment') {
            steps {
                script {
                    sh "echo 'Starting Deployment...' "
                    // sh "mv target/obc-1.0.jar target/obc-1.0_${date +%Y%m%d_%H%M}.jar"
                    // sh "scp target/obc-1.0_$(date +%Y%m%d_%H%M).jar" root@${SERVER_IP_ADDRESS}:/opt/app/versions/"
                }
            }
        }
    }
}