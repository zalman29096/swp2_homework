pipeline {
  agent any
  environment {
    DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials-id')
    DOCKER_IMAGE = "${env.DOCKERHUB_CREDENTIALS_USR}/swp2_homework:latest"
  }
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }
    stage('Build') {
      tools { jdk 'jdk17' }
      steps {
        sh 'mvn clean install'
      }
    }
    stage('Build Docker Image') {
      when { branch 'main' }
      steps {
        sh 'docker build -t ${DOCKER_IMAGE} .'
      }
    }
    stage('Push Docker Image') {
      when { branch 'main' }
      steps {
        withEnv(["DOCKER_CLI_EXPERIMENTAL=enabled"]) {
          sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
          sh 'docker push ${DOCKER_IMAGE}'
        }
      }
    }
  }
}
