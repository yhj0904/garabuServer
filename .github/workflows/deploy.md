name: CI/CD to AWS EC2
on:
  push:
    branches: [main]

concurrency: deploy-main

permissions:
  contents: read
  packages: write

env:
  REGISTRY: ${{ secrets.DOCKER_USERNAME }}/spring-app
  IMAGE_TAG: ${{ github.sha }}

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      # 1. 소스 체크아웃
      - uses: actions/checkout@v4

      # 2. Gradle 캐시 + 빌드 (멀티스테이지 대신 Dockerfile 내부 빌드 시 이 단계 생략 가능)
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Gradle Build
        run: ./gradlew build -x test --no-daemon --stacktrace

      # 3. buildx + Docker Hub 로그인 + 이미지 빌드/푸시
      - uses: docker/setup-buildx-action@v3

      - uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}

      - name: Build & Push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ${{ env.REGISTRY }}:${{ env.IMAGE_TAG }}
            ${{ env.REGISTRY }}:latest

      # 4. 날짜 출력 (Slack용)
      - name: Set current date
        id: now
        run: echo "date=$(date '+%Y-%m-%d %H:%M:%S')" >> $GITHUB_OUTPUT

      # 5. EC2 SSH 배포
      - uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ${{ secrets.EC2_USER }}
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            cd ~/deploy
            git pull origin main
            docker-compose pull spring-app
            docker-compose up -d --no-deps spring-app
            docker image prune -f --filter "until=24h"

      # 6. Health Check
      - name: Health Check
        env:
          EC2_HOST: ${{ secrets.EC2_HOST }}
        run: curl -fsS --retry 3 https://$EC2_HOST/actuator/health

      # 7. Slack 알림 (성공 시)
      - name: Slack Notify (success)
        if: success()
        uses: slackapi/slack-github-action@v1.24.0
        with:
          payload: |
            {
              "channel": "deploy-alerts",
              "attachments": [
                {
                  "color": "good",
                  "title": "✅ 배포 성공",
                  "fields": [
                    { "title": "브랜치", "value": "${{ github.ref_name }}", "short": true },
                    { "title": "커밋", "value": "${{ env.IMAGE_TAG }}", "short": true },
                    { "title": "시간", "value": "${{ steps.now.outputs.date }}", "short": true }
                  ]
                }
              ]
            }

      # 8. Slack 알림 (실패 시)
      - name: Slack Notify (failure)
        if: failure()
        uses: slackapi/slack-github-action@v1.24.0
        with:
          payload: |
            {
              "channel": "deploy-alerts",
              "attachments": [
                {
                  "color": "danger",
                  "title": "❌ 배포 실패",
                  "text": "워크플로 로그 확인 필요"
                }
              ]
            }
