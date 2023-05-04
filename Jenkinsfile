pipeline {
    agent any
    environment {
        DOCKER_REPO = 'qkyrie/defitrack'
    }
    stages {
        stage('compile') {
            steps {
                sh './mvnw -T 1.5C clean compile '
            }
        }
        stage('Test') {
            steps {
                sh './mvnw -T 1.5C test'
             }
         }
        stage('Package') {
             steps {
                 echo "-=- packaging project -=-"
                 sh "./mvnw -T 1.5C package -DskipTests"
             }
        }
        stage('Docker Package') {
            when {
                allOf {
                    branch 'main'
                }
            }
            steps {
                sh "bash ci/package.sh"
            }
        }
    }
}