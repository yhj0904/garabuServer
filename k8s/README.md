# Garabu Server - AWS EKS 배포 가이드

## 📋 전제조건

1. **AWS 계정 및 권한**
   - EKS 클러스터 생성 권한
   - ECR 저장소 생성 권한
   - RDS, ElastiCache 생성 권한
   - IAM 역할 생성 권한

2. **로컬 도구 설치**
   ```bash
   # AWS CLI
   curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
   unzip awscliv2.zip
   sudo ./aws/install

   # kubectl
   curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
   chmod +x kubectl
   sudo mv kubectl /usr/local/bin/

   # eksctl
   curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
   sudo mv /tmp/eksctl /usr/local/bin

   # Helm
   curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
   ```

## 🚀 배포 단계

### 1. AWS 리소스 준비

#### RDS MySQL 생성
```bash
aws rds create-db-instance \
  --db-instance-identifier garabu-mysql \
  --db-instance-class db.t3.medium \
  --engine mysql \
  --engine-version 8.0.35 \
  --master-username admin \
  --master-user-password YOUR_PASSWORD \
  --allocated-storage 100 \
  --vpc-security-group-ids sg-xxxxxx \
  --db-subnet-group-name your-subnet-group
```

#### ElastiCache Redis 생성
```bash
aws elasticache create-replication-group \
  --replication-group-id garabu-redis \
  --replication-group-description "Garabu Redis Cluster" \
  --engine redis \
  --cache-node-type cache.t3.micro \
  --num-cache-clusters 2 \
  --automatic-failover-enabled \
  --cache-subnet-group-name your-cache-subnet-group
```

### 2. EKS 클러스터 생성

```bash
eksctl create cluster \
  --name garabu-eks \
  --region ap-northeast-2 \
  --version 1.28 \
  --nodegroup-name garabu-nodes \
  --node-type t3.medium \
  --nodes 3 \
  --nodes-min 3 \
  --nodes-max 10 \
  --managed \
  --alb-ingress-access \
  --asg-access \
  --full-ecr-access
```

### 3. ALB Ingress Controller 설치

```bash
# IAM OIDC 공급자 생성
eksctl utils associate-iam-oidc-provider \
  --region ap-northeast-2 \
  --cluster garabu-eks \
  --approve

# ALB Ingress Controller IAM 정책 생성
curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.6.2/docs/install/iam_policy.json
aws iam create-policy \
  --policy-name AWSLoadBalancerControllerIAMPolicy \
  --policy-document file://iam_policy.json

# IAM 서비스 계정 생성
eksctl create iamserviceaccount \
  --cluster=garabu-eks \
  --namespace=kube-system \
  --name=aws-load-balancer-controller \
  --attach-policy-arn=arn:aws:iam::YOUR_ACCOUNT_ID:policy/AWSLoadBalancerControllerIAMPolicy \
  --override-existing-serviceaccounts \
  --approve

# Helm으로 ALB Controller 설치
helm repo add eks https://aws.github.io/eks-charts
helm repo update
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system \
  --set clusterName=garabu-eks \
  --set serviceAccount.create=false \
  --set serviceAccount.name=aws-load-balancer-controller
```

### 4. ECR 저장소 생성 및 이미지 푸시

```bash
# ECR 저장소 생성
aws ecr create-repository \
  --repository-name garabu-server \
  --region ap-northeast-2

# Docker 이미지 빌드
cd /path/to/garabuserver
docker build -t garabu-server:latest .

# ECR 로그인
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com

# 이미지 태깅 및 푸시
docker tag garabu-server:latest YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/garabu-server:latest
docker push YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/garabu-server:latest
```

### 5. Secrets 및 ConfigMaps 설정

```bash
# Firebase Admin SDK JSON 파일을 base64로 인코딩
cat garabu-d95ea-firebase-adminsdk-fbsvc-435a13aff1.json | base64 -w 0 > firebase-admin-key.b64

# secret.env 파일 생성
cat > k8s/secret.env << EOF
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=your-db-password
SPRING_REDIS_PASSWORD=your-redis-password
JWT_SECRET=your-jwt-secret-base64
GOOGLE_CLIENT_SECRET=your-google-secret
NAVER_CLIENT_SECRET=your-naver-secret
APPLE_CLIENT_SECRET=your-apple-secret
EOF

# config.env 파일 생성
cat > k8s/config.env << EOF
SPRING_DATASOURCE_URL=jdbc:p6spy:mysql://your-rds-endpoint:3306/garabu?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
SPRING_REDIS_HOST=your-elasticache-endpoint
EOF
```

### 6. Kubernetes 리소스 배포

```bash
# 네임스페이스 생성
kubectl create namespace garabu

# Kustomize로 배포 (권장)
cd k8s
kustomize edit set image your-ecr-uri/garabu-server=YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/garabu-server:latest
kubectl apply -k .

# 또는 개별 파일로 배포
kubectl apply -f namespace.yaml
kubectl apply -f serviceaccount.yaml
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f firebase-secret.yaml
kubectl apply -f application-prod.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
kubectl apply -f ingress.yaml
kubectl apply -f hpa.yaml
kubectl apply -f pdb.yaml
kubectl apply -f networkpolicy.yaml
```

### 7. SSL 인증서 설정

```bash
# ACM에서 SSL 인증서 요청
aws acm request-certificate \
  --domain-name api.garabu.com \
  --validation-method DNS \
  --region ap-northeast-2

# Ingress 파일에서 certificate-arn 업데이트
# alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:ap-northeast-2:YOUR-ACCOUNT-ID:certificate/YOUR-CERT-ID
```

### 8. 배포 확인

```bash
# Pod 상태 확인
kubectl get pods -n garabu

# Service 확인
kubectl get svc -n garabu

# Ingress 확인
kubectl get ingress -n garabu

# 로그 확인
kubectl logs -f deployment/garabu-server -n garabu

# HPA 상태 확인
kubectl get hpa -n garabu
```

## 🔧 문제 해결

### Pod가 시작되지 않는 경우
```bash
# Pod 상세 정보 확인
kubectl describe pod <pod-name> -n garabu

# 이벤트 확인
kubectl get events -n garabu --sort-by='.lastTimestamp'
```

### 데이터베이스 연결 실패
- RDS 보안 그룹에서 EKS 노드 보안 그룹 허용 확인
- RDS endpoint가 올바른지 확인
- 데이터베이스 사용자 권한 확인

### Redis 연결 실패
- ElastiCache 보안 그룹 확인
- ElastiCache 클러스터 엔드포인트 확인
- Redis 암호 설정 확인

## 📊 모니터링

### Prometheus 설치
```bash
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm install prometheus prometheus-community/kube-prometheus-stack -n prometheus --create-namespace
```

### Grafana 대시보드 접근
```bash
kubectl port-forward -n prometheus svc/prometheus-grafana 3000:80
```

## 🔄 업데이트 절차

```bash
# 새 이미지 빌드 및 푸시
docker build -t garabu-server:v1.1.0 .
docker tag garabu-server:v1.1.0 YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/garabu-server:v1.1.0
docker push YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/garabu-server:v1.1.0

# Deployment 업데이트
kubectl set image deployment/garabu-server garabu-server=YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-2.amazonaws.com/garabu-server:v1.1.0 -n garabu

# 롤링 업데이트 상태 확인
kubectl rollout status deployment/garabu-server -n garabu
```

## 🧹 정리

```bash
# 전체 리소스 삭제
kubectl delete -k k8s/

# EKS 클러스터 삭제
eksctl delete cluster --name garabu-eks --region ap-northeast-2
```

## 📝 주의사항

1. **보안**: 실제 배포 시 모든 시크릿 값을 안전하게 관리하세요
2. **비용**: EKS, RDS, ElastiCache는 시간당 요금이 발생합니다
3. **백업**: RDS 자동 백업을 설정하세요
4. **모니터링**: CloudWatch 알람을 설정하여 이상 상황을 감지하세요
5. **네트워크**: VPC, 서브넷, 보안 그룹을 적절히 구성하세요