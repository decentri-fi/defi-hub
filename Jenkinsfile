pipeline {
    agent any
    stages {
        stage('compile') {
            steps {
                sh './mvnw clean compile -U'
            }
        }
        stage('Test') {
            steps {
                sh './mvnw test'
             }
         }
        stage('Package') {
             steps {
                 sh "./mvnw package -DskipTests"
             }
        }
    }
}