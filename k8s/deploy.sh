#!/bin/bash

# Garabu Server EKS 배포 스크립트

set -e

# 설정
AWS_REGION="ap-northeast-2"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REPOSITORY="garabu-server"
ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY}"
CLUSTER_NAME="garabu-eks"
NAMESPACE="garabu"

# 색상 설정
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 함수: 성공 메시지
success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# 함수: 에러 메시지
error() {
    echo -e "${RED}❌ $1${NC}"
    exit 1
}

# 함수: 정보 메시지
info() {
    echo -e "${YELLOW}ℹ️  $1${NC}"
}

# 1. AWS 인증 확인
info "AWS 인증 확인 중..."
aws sts get-caller-identity || error "AWS 인증 실패. aws configure를 실행하세요."
success "AWS 인증 성공"

# 2. ECR 로그인
info "ECR 로그인 중..."
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_URI}
success "ECR 로그인 성공"

# 3. Docker 이미지 빌드
info "Docker 이미지 빌드 중..."
cd ..
docker build -f k8s/Dockerfile.prod -t ${ECR_REPOSITORY}:latest .
success "Docker 이미지 빌드 성공"

# 4. 이미지 태깅
VERSION=$(date +%Y%m%d%H%M%S)
docker tag ${ECR_REPOSITORY}:latest ${ECR_URI}:latest
docker tag ${ECR_REPOSITORY}:latest ${ECR_URI}:${VERSION}
success "이미지 태깅 완료 (latest, ${VERSION})"

# 5. ECR로 푸시
info "ECR로 이미지 푸시 중..."
docker push ${ECR_URI}:latest
docker push ${ECR_URI}:${VERSION}
success "이미지 푸시 성공"

# 6. kubectl 컨텍스트 확인
info "kubectl 컨텍스트 확인 중..."
kubectl config current-context | grep -q ${CLUSTER_NAME} || error "kubectl이 ${CLUSTER_NAME}에 연결되지 않았습니다."
success "kubectl 컨텍스트 확인 완료"

# 7. 네임스페이스 생성 (없는 경우)
if ! kubectl get namespace ${NAMESPACE} &> /dev/null; then
    info "네임스페이스 생성 중..."
    kubectl create namespace ${NAMESPACE}
    success "네임스페이스 생성 완료"
else
    info "네임스페이스가 이미 존재합니다"
fi

# 8. Secrets 확인
info "필수 Secrets 확인 중..."
cd k8s

# secret.env 파일 확인
if [ ! -f "secret.env" ]; then
    error "secret.env 파일이 없습니다. README.md를 참고하여 생성하세요."
fi

# config.env 파일 확인
if [ ! -f "config.env" ]; then
    error "config.env 파일이 없습니다. README.md를 참고하여 생성하세요."
fi

# Firebase Admin SDK 파일 확인
if [ ! -f "../garabu-d95ea-firebase-adminsdk-fbsvc-435a13aff1.json" ]; then
    error "Firebase Admin SDK JSON 파일이 없습니다."
fi

# 9. Firebase Secret 생성
info "Firebase Secret 생성 중..."
FIREBASE_B64=$(cat ../garabu-d95ea-firebase-adminsdk-fbsvc-435a13aff1.json | base64 -w 0)
cat > firebase-secret-temp.yaml << EOF
apiVersion: v1
kind: Secret
metadata:
  name: firebase-admin-key
  namespace: ${NAMESPACE}
type: Opaque
data:
  firebase-adminsdk.json: ${FIREBASE_B64}
EOF
kubectl apply -f firebase-secret-temp.yaml
rm -f firebase-secret-temp.yaml
success "Firebase Secret 생성 완료"

# 10. Kustomization 업데이트
info "Kustomization 이미지 업데이트 중..."
cat > kustomization-temp.yaml << EOF
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: ${NAMESPACE}

resources:
  - namespace.yaml
  - serviceaccount.yaml
  - configmap.yaml
  - secret.yaml
  - application-prod.yaml
  - deployment.yaml
  - service.yaml
  - ingress.yaml
  - hpa.yaml
  - pdb.yaml
  - networkpolicy.yaml

images:
  - name: your-ecr-uri/garabu-server
    newName: ${ECR_URI}
    newTag: ${VERSION}

configMapGenerator:
  - name: garabu-config
    behavior: merge
    envs:
      - config.env

secretGenerator:
  - name: garabu-secret
    behavior: merge
    envs:
      - secret.env
EOF

# 11. 배포 실행
info "Kubernetes 리소스 배포 중..."
kubectl apply -k .
success "배포 시작됨"

# 12. 배포 상태 확인
info "배포 상태 확인 중..."
kubectl rollout status deployment/garabu-server -n ${NAMESPACE} --timeout=5m
success "배포 완료!"

# 13. 서비스 정보 출력
echo ""
info "=== 배포 정보 ==="
echo "네임스페이스: ${NAMESPACE}"
echo "이미지: ${ECR_URI}:${VERSION}"
echo ""

# Pod 상태
kubectl get pods -n ${NAMESPACE} -l app=garabu-server

# Service 정보
echo ""
kubectl get svc -n ${NAMESPACE}

# Ingress 정보
echo ""
kubectl get ingress -n ${NAMESPACE}

# ALB URL 가져오기
ALB_URL=$(kubectl get ingress garabu-server-ingress -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "대기 중...")
echo ""
if [ "${ALB_URL}" != "대기 중..." ]; then
    success "ALB URL: http://${ALB_URL}"
else
    info "ALB가 프로비저닝 중입니다. 몇 분 후에 다시 확인하세요."
fi

# 정리
rm -f kustomization-temp.yaml

echo ""
success "배포가 성공적으로 완료되었습니다! 🎉"