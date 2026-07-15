# Problem Import

운영 서버에 문제를 한 번에 넣을 때는 JSON 배열을 만들고 `/admin/api/problems` 로 올리면 된다.

## 준비

- 운영 계정이 `ADMIN` 이어야 한다.
- `JWT_SECRET_KEY` 값을 알고 있어야 스크립트로 바로 업로드할 수 있다.

## 업로드

```bash
JWT_SECRET_KEY='<your-jwt-secret>' \
/Users/hoy/IdeaProjects/javis/scripts/import_problems_to_remote.sh \
  https://learn-hub.kr \
  1 \
  /Users/hoy/IdeaProjects/javis/docs/problem-imports/culture-fit-english-precall.json
```

인자 의미:

- `https://learn-hub.kr`: 운영 서버 주소
- `1`: 문제를 등록할 멤버 ID
- JSON 파일: `ProblemCreateRequest[]` 형식 문제 목록

## 관리자 화면

브라우저에서 `/admin/problem-insert` 로 들어가 JSON 파일을 직접 올릴 수도 있다.
이 경로는 로그인한 계정의 role 이 `ADMIN` 일 때만 접근된다.
