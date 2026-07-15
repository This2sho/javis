# AWS Terraform

이 디렉터리는 `learn-hub` 를 거의 무료에 가깝게 올리는 것을 목표로 한 최소 AWS 구성입니다.

## 구성

- VPC
- Public subnet 1개
- EC2 1대
- Elastic IP 1개
- Docker
- MySQL 컨테이너 1개
- Spring Boot 컨테이너 1개
- Caddy 컨테이너 1개
- SSM 접속용 IAM Role

즉, 앱과 DB를 같은 EC2 안에서 같이 띄웁니다.
ALB, ECS, RDS, NAT Gateway 같은 별도 과금 포인트는 넣지 않았습니다.

## 왜 이렇게 했는가

혼자 쓰는 프로젝트 기준에서는 이 구성이 가장 단순하고 비용도 가장 낮습니다.

- ALB 비용 없음
- ECS/Fargate 비용 없음
- RDS 비용 없음
- ECR 없이도 Docker Hub 이미지로 바로 배포 가능
- ALB 없이 Let's Encrypt 기반 HTTPS 가능

대신 운영 안정성은 낮아집니다.

- 인스턴스 1대 장애 시 전체 다운
- DB 백업/복구 자동화 없음
- 수평 확장 없음

혼자 쓰는 서비스라면 이 정도 타협이 가장 현실적입니다.

## 권장 인스턴스 타입

- 가장 저렴하게: `t3.micro`
- 메모리 여유가 더 필요하면: `t3.small`

현재 앱은 Spring Boot + MySQL 을 같은 인스턴스에서 돌리기 때문에,
`t3.micro` 에서는 메모리가 빠듯할 수 있어서 swap 을 기본으로 켭니다.
불안정하면 `t3.small` 로 올리는 편이 낫습니다.

## 사전 준비

- `terraform`
- AWS 자격증명

SSH 는 기본적으로 열지 않습니다.
대신 SSM Session Manager 로 붙을 수 있게 IAM Role 을 넣었습니다.

## 사용 방법

1. 예시 파일 복사

```bash
cd terraform/aws
cp terraform.tfvars.example terraform.tfvars
```

2. `terraform.tfvars` 채우기

- `db_password`
- `jwt_secret_key`
- `kakao_client_id`
- `kakao_client_secret`
- `kakao_redirect_uri`
- `gemini_api_key`

3. 적용

```bash
terraform init
terraform apply
```

4. 접속 주소 확인

```bash
terraform output app_url
```

5. DNS 연결

도메인 제공업체에서 `learn-hub.kr` 의 A 레코드를 `terraform output public_ip` 값으로 연결하면 됩니다.
`www.learn-hub.kr` 은 같은 IP로 A 레코드를 넣거나 CNAME 으로 `learn-hub.kr` 에 연결하면 됩니다.

## 이미지 교체

기본값은 현재 저장소의 `docker-compose.yml` 에 이미 적혀 있던 프로덕션 이미지를 사용합니다.

```hcl
container_image = "leegeonho/learn-hub-backend:0.1.6"
```

나중에 직접 빌드한 이미지를 쓰고 싶으면 `container_image` 만 바꾸면 됩니다.
Docker Hub 이미지든, ECR 이미지든 모두 가능합니다.

ECR private 이미지를 쓰는 경우를 위해 인스턴스에는 읽기 권한도 넣어두었습니다.

## EC2 내부에서 하는 일

초기 부팅 시 user data 가 아래를 수행합니다.

- Docker 설치
- swap 생성
- MySQL 컨테이너 실행
- Spring Boot 컨테이너 실행
- Caddy 리버스 프록시 실행
- Caddy 가 `80/443` 을 받고 앱 컨테이너로 프록시

DB 는 외부에 열지 않습니다.
애플리케이션은 Caddy 를 통해 80/443 포트로 외부 공개됩니다.
Elastic IP 를 붙이므로 인스턴스를 재시작해도 서비스 IP 는 유지됩니다.

## 접속 방법

SSM:

```bash
terraform output ssm_start_session_command
```

SSH 를 쓰고 싶으면 `key_name` 과 `allowed_ssh_cidrs` 를 같이 넣으세요.

## 주의 사항

- 이 구성은 백업이 없습니다.
- DB 는 컨테이너 볼륨에만 저장됩니다.
- 인스턴스를 교체하면 데이터가 사라질 수 있습니다.
- HTTPS 는 DNS 가 이 서버 IP 로 연결된 뒤 Caddy 가 자동 발급합니다.

혼자 쓰는 장난감/개인 프로젝트 용도에 맞춘 구성입니다.
