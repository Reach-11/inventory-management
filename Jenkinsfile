pipeline {
    // Run on the dedicated build agent (JDK 25 + Git + Docker CLI + Compose), NOT on the controller.
    agent { label 'build-agent' }

    options {
        timestamps()                                   // timestamp on every log line
        disableConcurrentBuilds()                      // never deploy two builds at once
        timeout(time: 30, unit: 'MINUTES')             // kill hung builds
        buildDiscarder(logRotator(numToKeepStr: '10')) // keep only the last 10 builds
    }

    triggers {
        githubPush()   // start when GitHub sends a push webhook
    }

    environment {
        JAVA_HOME        = '/opt/java/jdk-25'          // JDK 25 installed in the agent image
        PATH             = "${JAVA_HOME}/bin:${PATH}"
        // Secure (TLS) connection to the Docker Engine running in the "docker" container:
        DOCKER_HOST      = 'tcp://docker:2376'
        DOCKER_CERT_PATH = '/certs/client'
        DOCKER_TLS_VERIFY = '1'
        // The deployed app runs inside that engine, so we reach it via the "docker" hostname.
        DEPLOY_HOST      = 'docker'
        POSTGRES_DB      = 'inventory'                 // not a secret
        APP_VERSION      = "${env.BUILD_NUMBER}"       // image tag = build number
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm            // fetch the commit that triggered this build from GitHub
                sh 'git log -1 --oneline'
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew --no-daemon clean classes'
            }
        }

        stage('Test') {
            steps {
                // A failing test makes this command exit non-zero -> pipeline stops here.
                sh './gradlew --no-daemon test'
            }
            post {
                always { junit 'build/test-results/test/*.xml' }   // show results in Jenkins
            }
        }

        stage('Package') {
            steps {
                sh './gradlew --no-daemon bootJar -x test'
                archiveArtifacts artifacts: 'build/libs/app.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'inventory-db',
                        usernameVariable: 'POSTGRES_USER', passwordVariable: 'POSTGRES_PASSWORD')]) {
                    sh 'docker compose build app'
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'inventory-db',
                        usernameVariable: 'POSTGRES_USER', passwordVariable: 'POSTGRES_PASSWORD')]) {
                    sh 'docker compose up -d --remove-orphans'
                    sh 'docker compose ps'
                }
            }
        }

        stage('Verify Health') {
            steps {
                sh '''
                    for i in $(seq 1 30); do
                      if curl -fsS "http://$DEPLOY_HOST:8081/actuator/health" | grep -q '"status":"UP"'; then
                        echo "Application is healthy."
                        exit 0
                      fi
                      echo "Waiting for application ($i/30)..."
                      sleep 5
                    done
                    echo "Application did not become healthy in time."
                    exit 1
                '''
            }
        }
    }

    post {
        success { echo "SUCCESS: build #${env.BUILD_NUMBER} deployed and healthy." }
        failure {
            echo "FAILURE: build #${env.BUILD_NUMBER} failed. Showing container logs (if any)."
            withCredentials([usernamePassword(credentialsId: 'inventory-db',
                    usernameVariable: 'POSTGRES_USER', passwordVariable: 'POSTGRES_PASSWORD')]) {
                sh 'docker compose logs --tail=100 || true'
            }
        }
        always {
            sh 'docker image prune -f || true'   // remove dangling images
        }
    }
}
