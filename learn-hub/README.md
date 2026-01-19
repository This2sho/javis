# Learn-Hub – Spring API Server

Learn-Hub는 인터뷰 연습 플랫폼의 Spring 기반 API 서버입니다. 

사용자, 인터뷰, 문제 흐름을 관리하며 외부 Python 채점 서버로 평가 요청을 전달하는 역할을 담당합니다. 

이 문서는 Spring 서버 단독 기준으로 작성되었으며, 전체 프로젝트 구조 및 실행 방법은 상위 README를 참고합니다.


## Responsibilities
해당 시스템 내에서 다음 책임을 가집니다.
- 사용자 인증 및 인가 (JWT 기반)
- 인터뷰 세션 생성 및 상태(질문 흐름 제어) 관리
- 문제(Problem) 생성 및 추천
- 사용자 답변을 받아 Python 채점 서버로 채점 요청 전달 및 채점 결과 저장 및 점수 반영

## Tech Stack
- Java 17
- Spring Boot 3.5
- Spring Data JPA
- MySQL 8+
- Thymeleaf

## Spring Server Architecture
도메인 중심 패키지 구조를 기반으로 설계되었습니다.
기술 계층이 아닌 도메인 단위로 패키지를 분리하고,
각 도메인 내부에서 역할별 계층을 나눕니다.

Package Structure

프로젝트 최상위에는 도메인 패키지가 위치합니다.
- answer : 인터뷰 답변 채점 및 저장을 위한 도메인
- category : 문제에서 사용되는 카테고리 조회 및 생성을 위한 도메인
- interview : 인터뷰 진행 및 질문(Question: 인터뷰에서 사용한 Problem의 스냅샷) 관리하는 도메인
- member : 사용자 관련 도메인
- problem : 문제(카테고리, 연관 문제, 난이도, 작성자, 노출(visibility), 내용)와 문제 채점관련 정보(ProblemScoringInfo: 채점에 필요한 모범 답안, 핵심 키워드) 관리하는 도메인
- review : 개인이 작성한 문제를 public으로 변경하기 위한 도메인 
- score : 사용자 개인의 카테고리 별 점수를 나타내는 도메인
---
그 외 최상위 패키지
- admin : 관리자용 API용 (사용자 리뷰 조회, 처리, 공통 문제 작성)
- event : 이벤트 정의 및 관리 (양방향 의존 제거를 위해 패키지 분리)
- support : 공통 커서 페이징 및 예외, 페이지 컨트롤러 포함한 패키지

각 도메인은 동일한 내부 구조를 가집니다.
- domain
- service
- presentation
- infrastructure

### Domain Layer

domain 패키지는 핵심 비즈니스 규칙을 담당합니다.

구성 요소는 다음과 같습니다.

- Entity / Domain Object : 도메인의 핵심 개념과 상태, 행위 정의
- Repository : Spring Data JPA 기반 영속성 접근
- Domain Service : 도메인 로직을 역할별로 분리하여 구성
  - Reader : 단일 엔티티의 단순 조회 (Read-only)
  - Finder : 여러 엔티티를 조합한 조회 및 탐색 (Read-only)
  - Recommender : 문제, 카테고리 등 도메인 특성에 맞는 추천 로직 (Read-only)
  - Processor : 엔티티 생성 및 상태 변경, 인터뷰 진행 중 발생하는 상태 전이를 포함 (Write)

조회 책임과 변경 책임을 분리하여
도메인 로직의 역할을 명확히 합니다.

### Service Layer

service 패키지는 유스케이스 조립 계층입니다.
- API 단위의 비즈니스 흐름 구성
- 여러 도메인 컴포넌트 간 협력 조정 
- 트랜잭션 경계 관리

### Presentation Layer

presentation 패키지는 REST API 컨트롤러를 담당합니다.

- HTTP 요청/응답 처리
- 요청 DTO → 유스케이스 호출

### Infrastructure Layer

infrastructure 패키지는 외부 시스템 및 기술 의존 영역을 담당합니다.

- PythonEvaluator (외부 채점 서버 연동)
- JPAConverter (영속성 변환)
- JwtUtil (JWT 처리)
- KakaoAuthProvider (OAuth 인증)

외부 의존성을 이 계층에 한정하여
도메인 계층과의 결합도를 최소화합니다.


## Related Documentation
👉 [전체 프로젝트 개요 및 실행 방법](../README.md)

👉 [Python 채점 서버 설명](../evaluation/README.md)
