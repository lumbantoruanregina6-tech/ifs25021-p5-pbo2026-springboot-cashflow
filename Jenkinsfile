pipeline {
    agent any
    stages {
        stage('Build & Test') {
            steps { sh './mvnw clean verify' }
        }
        stage('Jacoco 100%') {
            steps { sh './mvnw jacoco:check' }
        }
        stage('Trivy Scan') {
            steps { sh 'trivy fs --exit-code 0 .' }
        }
        stage('Package') {
            steps { sh './mvnw clean package -Pproduction -DskipTests' }
        }
        stage('Publish') {
            steps { echo 'TODO: publish artifact / docker image' }
        }
        stage('Deploy') {
            steps { echo 'TODO: deploy ke server' }
        }
    }
}
