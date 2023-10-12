pipeline {
    agent any
    stages {
        stage('compile') {
            steps {
                sh './mvnw -T 1.5C clean compile -U'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw -T 1.5C test'
             }
         }
        stage('Package') {
             steps {
                 sh "./mvnw -T 1.5C package -DskipTests"
             }
        }
    }
}