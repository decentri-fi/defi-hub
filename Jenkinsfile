pipeline {
    agent any
    stages {
        stage('compile') {
            steps {
                sh './mvnw clean compile'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw test'
             }
         }
        stage('Package') {
             steps {
                 echo "-=- packaging project -=-"
                 sh "./mvnw package -DskipTests"
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