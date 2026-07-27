pipeline {
    agent any

    environment {
        // 镜像名称和标签
        IMAGE_NAME = "my-app"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('拉取代码') {
            steps {
                echo '拉取 GitHub 代码...'
                checkout scm
            }
        }

        stage('编译打包') {
            steps {
                echo '开始 Maven 编译...'
                // 如果项目使用 Maven
                bat 'mvn clean package -DskipTests'
            }
        }

        stage('构建 Docker 镜像') {
            steps {
                echo '构建 Docker 镜像...'
                bat "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('启动容器') {
            steps {
                echo '停止并删除旧容器...'
                bat "docker stop ${IMAGE_NAME} || exit 0"
                bat "docker rm ${IMAGE_NAME} || exit 0"

                echo '启动新容器...'
                bat """
                    docker run -d \\
                        --name ${IMAGE_NAME} \\
                        -p 8082:8082 \\
                        --restart unless-stopped \\
                        ${IMAGE_NAME}:${IMAGE_TAG}
                """
            }
        }
    }

    post {
        success {
            echo '构建部署成功！'
        }
        failure {
            echo '构建部署失败！'
        }
    }
}
