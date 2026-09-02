# Next Session Guide

## 목적

이 문서는 다음 작업일에 Codex에게 바로 이어서 일을 시키기 위한 인수인계 문서다.
새 세션이 열리면 이 문서를 먼저 읽고, 아래 템플릿 중 하나로 작업을 지시하면 된다.

## 다음 세션에 이렇게 명령하면 된다

가장 안전한 시작 문구:

```text
/docs/rule/next-session-guide.md, /docs/rule/architecture-code-style.md, /docs/rule/i18n-expansion-plan.md 먼저 읽고
현재 다국어 작업을 이어서 진행해.
이번에는 Phase 1부터 실제 코드로 구현해.
```

구현 범위를 좁혀서 시키는 문구:

```text
/docs/rule/*.md 먼저 읽고 이어서 작업해.
이번에는 UI 문구 다국어화만 구현해.
Spring MessageSource, LocaleResolver, messages_ko/messages_en,
Thymeleaf/JS 치환까지 하고 정리해.
```

검토 중심으로 시키는 문구:

```text
/docs/rule/*.md 먼저 읽고
현재 i18n 구현안을 기준으로 실제 코드 변경 순서를 더 구체화해줘.
필요하면 바로 구현까지 진행해.
```

## Codex가 다음 세션 시작 직후 기억해야 할 정보

### 현재 상태

- 이번 턴에서는 구현이 아니라 문서화만 완료했다.
- 작성된 기준 문서는 아래 두 개다.
  - `docs/rule/architecture-code-style.md`
  - `docs/rule/i18n-expansion-plan.md`
- 아직 실제 i18n 코드 변경은 시작하지 않았다.

### 프로젝트 핵심 구조

- 실서비스 코드는 `learn-hub`에 모여 있다.
- 스택은 `Spring Boot 3.5`, `Java 17`, `JPA`, `Thymeleaf`, `WebSocket`, `Spring AI Gemini`다.
- 패키지 구조는 도메인 중심이다.
- 주요 도메인:
  `answer`, `category`, `evaluation`, `interview`, `member`, `problem`, `review`, `score`, `support`, `admin`

### 현재 코드 스타일 규칙

- 컨트롤러는 얇게 유지한다.
- 유스케이스 조립과 트랜잭션 경계는 `service` 또는 `application` 계층에 둔다.
- 도메인 서비스는 `Reader`, `Finder`, `Processor`, `Recommender` 패턴을 따른다.
- DTO는 `record`를 많이 사용한다.
- 엔티티는 setter보다 명시적 상태 전이 메서드를 사용한다.
- JPA 직접 연관 대신 `Association<T>`로 ID 참조를 감싼 구조가 많다.

### i18n 설계 핵심

- 단순 번역이 아니라 `UI Locale`과 `Content Language`를 분리해야 한다.
- 지금 한국어 하드코딩은 템플릿, JS, 예외 메시지, 카테고리 표시명, Gemini 프롬프트에 퍼져 있다.
- `MainCategory.path`, `Category.path`, `Grade`는 번역 대상이 아니라 코드값으로 유지해야 한다.
- 장기적으로는 `ContentLanguage`를 `Problem`, `Question`, `Interview`에 넣는 방향이 맞다.

## 내일 바로 할 우선순위

### 추천 시작점

Phase 1부터 구현한다.

1. Spring `MessageSource`와 `LocaleResolver` 추가
2. `messages_ko.properties`, `messages_en.properties` 추가
3. Thymeleaf 템플릿 하드코딩 문구를 message key로 치환
4. JS에 필요한 문구를 서버에서 주입하는 방식으로 정리
5. 카테고리 표시명 하드코딩 제거 시작

### Phase 1에서 건드릴 가능성이 높은 파일

- `learn-hub/src/main/resources/templates/*.html`
- `learn-hub/src/main/resources/static/js/*.js`
- `learn-hub/src/main/java/com/javis/learn_hub/support/presentation/PageController.java`
- `learn-hub/src/main/java/com/javis/learn_hub/support/config/WebMvcConfig.java`
- `learn-hub/src/main/resources/application.yml`

필요 시 새 파일 추가:

- `learn-hub/src/main/resources/messages_ko.properties`
- `learn-hub/src/main/resources/messages_en.properties`
- locale 설정용 config 클래스

## 다음 세션에서 주의할 점

- `docs/rule` 문서를 먼저 읽고 구현 방향을 맞춘다.
- 기존 사용자 변경사항은 되돌리지 않는다.
- 현재 `.gitignore`는 `docs/**`를 기본 무시하고, `docs/rule` 내 일부 문서만 예외로 열어둔 상태다.
- `PageController`, `InterviewHistoryResponse`, `InterviewStartPolicy`, `GeminiEvaluationClient`는 i18n 영향도가 높은 파일이다.
- 작업 완료 직전에 `scripts/codex_notify_done.sh`를 실행해서 macOS 알림을 보낸다.

예시:

```bash
./scripts/codex_notify_done.sh "Codex" "작업이 완료되었습니다." "javis"
```

## 다음 세션 완료 기준

최소 완료 기준은 아래다.

- 한국어/영어 message bundle이 존재한다.
- 주요 SSR 페이지가 locale에 따라 문구를 바꿔 렌더링한다.
- JS alert, placeholder, loading 문구도 locale 전환 가능하다.
- 다음 단계인 `BusinessException + ContentLanguage` 도입으로 자연스럽게 이어질 수 있게 구조가 정리된다.
