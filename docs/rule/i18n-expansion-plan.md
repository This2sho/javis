# I18N Expansion Plan

## 목표

현재 한국어 중심으로 동작하는 Learn-Hub를 한국어와 영어 모두 지원하도록 확장한다.
이때 단순 UI 번역이 아니라 아래 두 층을 분리해서 설계한다.

- UI Locale
  버튼, 안내 문구, 오류 메시지, 페이지 제목 같은 시스템 문구의 언어
- Content Language
  문제 내용, 모범 답안, 질문 스냅샷, 사용자 답변, AI 채점 프롬프트가 따라야 하는 언어

이 분리를 하지 않으면 "영문 UI에서 한국어 문제를 푸는 사용자"와 "영문 문제를 영문으로 푸는 사용자"를 동시에 지원하기 어렵다.

## 현재 상태 진단

### 1. View 계층이 한국어 하드코딩 상태

다음 위치에 한국어 문구가 직접 박혀 있다.

- `learn-hub/src/main/resources/templates/*.html`
- `learn-hub/src/main/resources/static/js/*.js`
- 일부 `static/css` 주석

영향:

- 페이지 제목, 버튼, 로딩 문구, 알림, 인터뷰 진행 메시지 모두 locale 전환이 불가능하다.

### 2. 서비스/도메인 계층에도 표시 문구가 섞여 있다

예시:

- `PageController`
  웰컴 메시지, 샘플 카테고리명
- `InterviewHistoryResponse`
  `MainCategory -> 한글명` 변환
- `InterviewStartPolicy`
  사용 제한 오류 메시지와 카테고리 표시명
- `NotFoundException`, `UnauthorizedException`, 여러 `IllegalStateException`

영향:

- 다국어 적용 시 템플릿만 바꿔서는 안 되고, 예외와 응답 메시지 체계도 손봐야 한다.

### 3. AI 채점 프롬프트가 한국어 고정

`GeminiEvaluationClient`의 `SYSTEM_PROMPT`와 `USER_PROMPT`가 한국어 기반이다.

영향:

- 영어 문제/답변을 기술적으로 처리할 수는 있어도, 평가 일관성과 피드백 품질을 보장하기 어렵다.
- 출력 피드백이 어떤 언어로 나올지 제어가 불분명하다.

### 4. 콘텐츠 언어 메타데이터가 없다

현재 아래 데이터에는 언어 컬럼이 없다.

- `Problem.content`
- `ProblemScoringInfo.referenceAnswer`
- `Question.message`
- `Answer.message`
- `EvaluationResult.feedback`

영향:

- 공개 문제 풀에 한국어/영어 문제가 함께 들어오면 추천과 재사용 기준이 모호해진다.
- 어떤 언어로 질문을 생성했고 어떤 프롬프트를 써야 하는지 추적할 수 없다.

### 5. 식별자 계층은 오히려 다국어에 유리하다

- `MainCategory`는 `computer_science`, `backend` 같은 slug를 가진다.
- `Category.path`도 영어 slug 기반이다.
- `Grade`는 영어 enum 코드다.

이 값들은 번역 대상이 아니라 공통 코드값으로 유지해야 한다.

## 설계 원칙

### 1. 코드값과 표시값을 분리

다음은 번역하지 않는다.

- enum 이름
- DB에 저장되는 category path
- API 내부 분기용 상태값
- AI 응답 파싱용 grade 코드

번역 대상은 별도 message key 또는 localization layer로 분리한다.

### 2. UI Locale과 Content Language를 분리

예시:

- 사용자의 브라우저는 `en-US`
- 인터뷰 문제는 `KO`
- 시스템 버튼은 영어
- 문제와 AI 피드백은 한국어

이 조합이 가능해야 한다.

### 3. 도메인 모델에는 콘텐츠 언어를 명시

UI locale은 요청/세션/쿠키 수준에서 처리하고, 콘텐츠 언어는 엔티티와 인터뷰 흐름에서 관리한다.

### 4. 예외 메시지는 코드 기반으로 전환

도메인/서비스에서 사용자 노출 문장을 직접 만들지 않고, 오류 코드와 파라미터를 던진 뒤 `GlobalExceptionHandler`에서 locale에 맞춰 메시지를 조합한다.

## 목표 구조

### UI Locale 처리

권장 구조:

- `CookieLocaleResolver` 또는 `AcceptHeaderLocaleResolver`
- 기본 locale: `ko-KR`
- 지원 locale: `ko-KR`, `en-US`
- 메시지 번들:
  - `messages.properties`
  - `messages_ko.properties`
  - `messages_en.properties`

권장 선택:

- SSR 페이지가 있으므로 `CookieLocaleResolver`를 기본으로 두고
- 최초 진입 시 `Accept-Language`를 fallback으로 반영

### Content Language 처리

신규 enum 제안:

```java
public enum ContentLanguage {
    KO,
    EN
}
```

1차 도입 권장 컬럼:

- `problem.content_language`
- `question.content_language`
- `interview.content_language`

선택 사항:

- `evaluation.feedback_language`
- `answer.detected_language`

설계 이유:

- `Problem`에 언어가 있어야 추천 풀을 분리할 수 있다.
- `Question`에 언어가 있어야 인터뷰 중 스냅샷이 안정적이다.
- `Interview`에 언어가 있어야 재접속 시 같은 언어 흐름을 유지할 수 있다.

`ProblemScoringInfo`는 별도 언어 컬럼 없이 `Problem.contentLanguage`를 따르도록 시작하는 것이 단순하다.
문제 내용과 모범 답안이 다른 언어를 허용해야 한다면 그때 분리한다.

## 권장 구현안

### 1단계: UI 문구 다국어화

범위:

- Thymeleaf 템플릿 문구
- 인라인 JS 문구
- alert, loading, placeholder, 페이지 제목

구현:

- 템플릿은 `th:text="#{...}"`로 치환
- JS는 두 방식 중 하나를 택한다.

옵션 A:
페이지마다 번역 문자열을 `window.messages = {...}`로 주입

옵션 B:
공용 `/api/i18n/messages?scope=interview` 엔드포인트 제공

현재 구조에서는 옵션 A가 변경 범위가 더 작다.
템플릿 기반 페이지가 이미 존재하므로 서버가 필요한 메시지를 함께 주입하기 쉽다.

추가 작업:

- `PageController`의 웰컴 메시지 직접 생성 제거
- 카테고리 표시명 직접 switch 제거

### 2단계: 표시명/오류 메시지 중앙화

새 컴포넌트 제안:

- `MessageKeys`
- `LocalizedMessageService`
- `BusinessException` + `ErrorCode`

예시:

```java
public enum ErrorCode {
    MEMBER_NOT_FOUND,
    INTERVIEW_DAILY_LIMIT_EXCEEDED,
    CATEGORY_NOT_FOUND
}
```

도메인/서비스:

- `throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);`
- `throw new BusinessException(ErrorCode.INTERVIEW_DAILY_LIMIT_EXCEEDED, mainCategory.name());`

예외 핸들러:

- locale 기반 message resolution
- API 응답에는 `code`, `message` 함께 노출

이 단계가 필요한 이유:

- 지금은 예외 메시지가 곧 한국어 사용자 문구다.
- 이 상태에서는 다국어 외에도 클라이언트 처리 일관성이 깨진다.

### 3단계: 콘텐츠 언어 모델링

### API 변경

문제 생성/수정 요청에 언어 필드 추가:

```json
{
  "problem": "...",
  "referenceAnswer": "...",
  "difficulty": "EASY",
  "category": "computer_science:network",
  "contentLanguage": "EN"
}
```

인터뷰 시작도 콘텐츠 언어를 명시해야 한다.

권장안:

- `POST /api/interviews/start/{mainCategory}?contentLanguage=EN`

또는

- body 기반 요청 DTO로 전환

현재 API 스타일을 덜 깨려면 query parameter 추가가 더 안전하다.

### 도메인 변경

- `Problem`
  콘텐츠 언어 추가
- `Question`
  문제 스냅샷 언어 추가
- `Interview`
  해당 인터뷰의 콘텐츠 언어 추가
- `ProblemRecommender`
  추천 시 언어 필터 추가
- `InterviewFlowService`
  재개 시 언어 일관성 유지

### 데이터 마이그레이션

- 기존 데이터는 모두 `KO`로 backfill
- 신규 인덱스 고려:
  `problem(visibility, content_language, category_id, id)`

### 4단계: AI 채점 다국어 지원

### 프롬프트 구조

현재의 한국어 상수 문자열을 다음처럼 분리한다.

- `EvaluationPromptFactory`
- `evaluation-system-prompt-ko.txt`
- `evaluation-system-prompt-en.txt`
- `evaluation-user-prompt-ko.txt`
- `evaluation-user-prompt-en.txt`

선택 기준:

- `Question.contentLanguage`
- 또는 `Interview.contentLanguage`

원칙:

- `grade` 코드는 계속 영어 enum 사용
- `feedback`은 콘텐츠 언어와 동일하게 생성
- 질문/기준답변/사용자답변이 모두 영어면 영어 피드백

추가 가드:

- 입력 언어와 `contentLanguage`가 크게 다르면 경고 로그 남김
- 필요 시 언어 감지 단계를 추가하되, 1차 버전에서는 명시 언어를 신뢰

### 5단계: 공개 문제 풀과 운영 정책 정리

영어 지원이 시작되면 운영 정책이 필요하다.

- 공개 문제는 언어별로 리뷰한다.
- 추천은 인터뷰 언어와 동일한 문제만 대상으로 한다.
- 관리자 대량 등록 JSON에도 `contentLanguage`를 포함한다.
- 리뷰 목록/문제 상세에도 언어 배지를 노출한다.

없으면 생기는 문제:

- 영어 인터뷰에 한국어 꼬리 질문이 섞일 수 있다.
- 공개 문제 검색/검수 기준이 모호해진다.

## 번역 책임 분리

계층별로 번역 책임을 아래처럼 고정하는 것이 좋다.

- Domain
  코드값, 상태 전이, 언어 정보 보유. 번역 문구 직접 생성 금지
- Service/Application
  오류 코드와 파라미터 결정. 번역 조합은 최소화
- Presentation
  locale에 맞는 시스템 문구 조합
- Infrastructure
  Gemini 프롬프트, message bundle, locale resolver

## 권장 배포 순서
 
### Phase 1

- message bundle 도입
- 템플릿/JS 문구 치환
- 카테고리 표시명 중앙화

효과:

- UI는 바로 한영 전환 가능
- 데이터 모델 변경 없음

### Phase 2

- `BusinessException`, `ErrorCode`, localized error response 도입

효과:

- API/화면 오류 메시지 일관성 확보

### Phase 3

- `ContentLanguage` 도입
- 문제/질문/인터뷰에 언어 컬럼 추가
- 인터뷰 시작/추천/재개 언어 연동

효과:

- 실제 영어 문제 풀이 흐름 가능

### Phase 4

- Gemini 프롬프트 언어 분리
- 관리자 등록/리뷰 화면 언어 지원
- 운영 데이터 분리 정책 정착

효과:

- 영어 콘텐츠 품질 안정화

## 테스트 계획

### 단위 테스트

- `MainCategory`, `ContentLanguage` 파싱
- `ProblemRecommender` 언어 필터
- `InterviewFlowService` 재개 시 언어 유지
- `EvaluationPromptFactory` 언어별 프롬프트 선택
- `GlobalExceptionHandler` locale별 message resolution

### 통합 테스트

- `Accept-Language: en-US` 요청 시 영어 페이지/오류 문구 반환
- 영어 문제 생성 후 영어 인터뷰 시작 가능
- 영어 답변 채점 후 영어 피드백 생성
- 한국어 인터뷰 재접속 시 한국어 질문 유지

### 회귀 테스트 포인트

- 기존 한국어 데이터가 모두 정상 조회되는지
- 공개 문제 추천 수가 언어 필터 때문에 비정상 감소하지 않는지
- WebSocket 재접속 시 locale과 content language가 뒤섞이지 않는지

## 바로 실행 가능한 작업 목록

우선순위 기준으로 정리하면 다음이 가장 현실적이다.

1. message bundle과 locale resolver를 먼저 넣는다.
2. `PageController`, 템플릿, JS의 한국어 하드코딩을 message key로 바꾼다.
3. `MainCategory`와 점수/이력 응답의 표시명 변환을 중앙화한다.
4. `BusinessException` 체계로 예외 문구를 분리한다.
5. `ContentLanguage` 컬럼과 마이그레이션을 추가한다.
6. 인터뷰 시작/추천/재개/채점 프롬프트를 콘텐츠 언어 기준으로 연결한다.

## 이 구조가 현재 코드와 잘 맞는 이유

- 이미 category path와 grade가 안정적인 코드값이라 번역 계층을 덧씌우기 쉽다.
- 문제 추천, 채점, 웹소켓 흐름이 서비스 경계로 나뉘어 있어 언어 정보 전달 경로를 추가하기 쉽다.
- `Question`이 스냅샷이므로 인터뷰 중 언어 일관성을 보존하기 좋다.

핵심은 "UI 번역"과 "영어 콘텐츠 인터뷰"를 같은 일로 보지 않는 것이다.
현재 구조에서는 이 둘을 분리해서 단계적으로 적용해야 리스크가 가장 낮다.
