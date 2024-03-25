pipeline {
    environment {
      JAVA_TOOL_OPTIONS = '-Duser.home=/root'
    }
  agent {
        docker {
            image 'maven:3.9.6-amazoncorretto-21'
      //     args '-v /var/jenkins_home:/root'
        }
    }
    stages {
        stage('package') {
            steps {
                sh 'mkdir -p /root/.m2'
                sh 'mvn clean verify -U'
            }
        }
    }
}