pipeline {
    environment {
      JAVA_TOOL_OPTIONS = '-Duser.home=/root'
    }
    agent any
    stages {
        stage('package') {
           agent {
                docker {
                    image 'maven:3.9.6-amazoncorretto-21'
                    args '-u jenkins -v /var/jenkins_home:/root'
                }
            }
            steps {
                sh 'mvn clean verify -U'
            }
        }
    }
}