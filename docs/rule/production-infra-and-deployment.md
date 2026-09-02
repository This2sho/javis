# Production Infra And Deployment

이 문서는 `learn-hub` 운영 환경의 현재 구조와 실제 배포 절차를 정리한 문서입니다.

기준 시점:
- 2026-07-26

## 1. 현재 운영 인프라 구조

운영은 AWS 단일 EC2 인스턴스 위에서 Docker 컨테이너 3개를 함께 실행하는 구조입니다.

- AWS Region: `ap-northeast-2`
- EC2 Instance ID: `i-04ad46dd307699b01`
- EC2 Name tag: `learn-hub-prod`
- Instance type: `t4g.micro`
- Public IP: `43.200.28.205`
- Security Group: `sg-036deed347580eb96` (`learn-hub-prod-sg`)
- IAM Instance Profile: `learn-hub-prod-profile`

인스턴스 내부 구성:
- `learn-hub-app`: Spring Boot 애플리케이션 컨테이너
- `learn-hub-mysql`: MySQL 8.4 컨테이너
- `learn-hub-caddy`: Caddy 2.10 리버스 프록시 컨테이너
- Docker network: `learn-hub-net`

외부 공개 포트:
- `80/tcp`: 전체 공개
- `443/tcp`: 전체 공개

기본적으로 닫혀 있는 포트:
- `22/tcp`: 기본 비활성

즉, 운영은 ALB/ECS/RDS 없이 앱, DB, 프록시를 모두 한 EC2에 올린 단일 서버 구조입니다.

## 2. Terraform의 역할

`terraform/aws`는 운영의 기반 인프라를 만드는 용도입니다.

Terraform이 담당하는 것:
- VPC / Subnet
- Security Group
- EC2
- Elastic IP
- SSM 접속용 IAM Role
- 초기 Docker / Caddy / MySQL / 앱 실행 user data

중요:
- 일상적인 애플리케이션 배포는 보통 `terraform apply`로 하지 않습니다.
- Terraform은 인프라 변경이나 초기 프로비저닝이 필요할 때 사용합니다.

예:
- EC2 타입 변경
- 보안그룹 변경
- user data 변경
- env 구조 변경
- 새 인스턴스 재구성

## 3. 현재 운영 배포 방식

현재 운영 배포는 아래 순서로 진행합니다.

1. 로컬 최신 소스로 Docker 이미지 빌드
2. Docker Hub에 태그 푸시
3. SSM으로 운영 EC2에 명령 전송
4. 운영 서버에서 새 이미지 pull
5. `learn-hub-app` 컨테이너만 교체
6. 헬스 체크 확인

즉, 현재 운영 배포의 핵심은:
- 이미지 배포: Docker Hub
- 서버 명령 실행: AWS SSM
- 앱 교체 방식: `docker rm -f learn-hub-app` 후 `docker run`

## 4. 운영 서버 파일/런타임 위치

운영 EC2 내부:
- 앱 env: `/opt/learn-hub/app.env`
- MySQL env: `/opt/learn-hub/mysql.env`
- Caddy 설정: `/opt/learn-hub/Caddyfile`

컨테이너 이름:
- 앱: `learn-hub-app`
- DB: `learn-hub-mysql`
- 프록시: `learn-hub-caddy`

## 5. 표준 배포 절차

### 5-1. 로컬에서 이미지 빌드

프로젝트 루트 기준:

```bash
docker build -t leegeonho/learn-hub-backend:<tag> /Users/hoy/IdeaProjects/javis/learn-hub
```

예:

```bash
docker build -t leegeonho/learn-hub-backend:0.1.7-some-hotfix /Users/hoy/IdeaProjects/javis/learn-hub
```

이 Docker build는 내부에서 `./gradlew build`를 수행합니다.

### 5-2. Docker Hub 푸시

```bash
docker push leegeonho/learn-hub-backend:<tag>
```

### 5-3. 운영 EC2 배포

SSM으로 운영 인스턴스에 아래 흐름을 보냅니다.

```bash
AWS_DEFAULT_REGION=ap-northeast-2 aws ssm send-command \
  --instance-ids i-04ad46dd307699b01 \
  --document-name AWS-RunShellScript \
  --comment 'deploy learn-hub app' \
  --parameters '{
    "commands":[
      "set -e",
      "docker pull leegeonho/learn-hub-backend:<tag>",
      "docker rm -f learn-hub-app || true",
      "docker run -d --name learn-hub-app --restart unless-stopped --network learn-hub-net --env-file /opt/learn-hub/app.env leegeonho/learn-hub-backend:<tag>",
      "sleep 10",
      "docker ps --format \"table {{.Names}}\\t{{.Image}}\\t{{.Status}}\"",
      "docker logs --tail 80 learn-hub-app"
    ]
  }' \
  --query 'Command.CommandId' \
  --output text
```

그 다음 결과를 확인합니다.

```bash
AWS_DEFAULT_REGION=ap-northeast-2 aws ssm get-command-invocation \
  --command-id <command-id> \
  --instance-id i-04ad46dd307699b01
```

### 5-4. 외부 헬스 체크

```bash
curl -i https://learn-hub.kr/actuator/health
```

정상 예시:

```text
HTTP/2 200
{"status":"UP"}
```

## 6. 언제 Terraform으로 배포해야 하는가

다음 같은 경우에는 Docker Hub + SSM만으로 끝내지 말고 Terraform 변경 여부를 같이 검토합니다.

- EC2 스펙 변경
- 새 보안그룹 규칙 적용
- EIP/VPC/Subnet 구조 변경
- user data 변경
- Caddy 기본 실행 구조 변경
- 컨테이너 최초 기동 방식 변경

반대로 아래는 보통 Terraform 대상이 아닙니다.

- Spring 코드 수정
- 템플릿/메시지 수정
- 비즈니스 로직 수정
- 단순 핫픽스 배포

## 7. 운영 점검 체크리스트

배포 전:
- 로컬 변경이 맞는지 확인
- 이미지 태그를 새 값으로 생성
- 필요하면 `./gradlew testClasses` 또는 대상 테스트 실행

배포 후:
- `learn-hub-app`이 새 이미지로 떠 있는지 확인
- `docker logs --tail 80 learn-hub-app` 확인
- `https://learn-hub.kr/actuator/health` 확인
- 필요한 경우 실제 화면/API 동작 확인

## 8. 주의 사항

- 운영 DB도 같은 EC2에 있으므로 인스턴스 장애 시 앱과 DB가 함께 영향받습니다.
- SSH는 기본적으로 닫혀 있으므로 운영 작업은 SSM 기준으로 생각하는 것이 맞습니다.
- 임시 SSH 오픈은 예외적인 복구 작업일 때만 사용하고, 끝나면 다시 닫는 것이 원칙입니다.
- 애플리케이션 핫픽스 배포와 인프라 변경 배포를 혼동하지 않는 것이 중요합니다.
