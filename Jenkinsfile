pipeline {
    agent {
        label 'ecs'
    }

    tools {
        maven "M3"
        dockerTool "Docker"
        terraform "Terraform"
    } //tools

    options {
        ansiColor('xterm')
        buildDiscarder(logRotator(artifactNumToKeepStr: '10'))
    }

    stages {

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
                                -Dsonar.projectKey=${params.SONARQUBE_PROJECT_KEY} \
                                -Dsonar.branch.name=${params.SONARQUBE_BRANCH}"
                        }
                    }
                }
            }
        }
    }

}