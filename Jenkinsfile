pipeline {
    environment {
      JAVA_TOOL_OPTIONS = '-Duser.home=/root'
    }
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
          agent {
                docker {
                    image 'maven:3.9.6-amazoncorretto-21'
                    args '-u root -v /var/jenkins_home:/root'
                }
            }
             steps {
                 sh "./mvnw package -DskipTests"
             }
        }
    }
}