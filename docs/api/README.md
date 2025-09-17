# HappyDog API 도메인 명세 인덱스

OpenAPI(`docs/openapi_pretty.json`)와 멱등성/딥링크 가이드(`docs/DEEPLINKS_AND_IDEMPOTENCY.md`)를 기반으로 도메인별 명세를 정리했습니다.

도메인 문서
- [auth.md](auth.md)
- [users.md](users.md)
- [breeds.md](breeds.md)
- [pets.md](pets.md)
- [pet-care.md](pet-care.md)
- [posts.md](posts.md)
- [comments.md](comments.md)
- [notifications.md](notifications.md)
- [uploads.md](uploads.md)
- [cartoon-jobs.md](cartoon-jobs.md)
- [health.md](health.md)

공통 규칙
- 인증: 대부분 Bearer 토큰 필요. 예외는 인증 시작/토큰 교환 등 일부 엔드포인트.
- 멱등성: 쓰기 메서드에 `X-Idempotency-Key` 적용 범위는 각 도메인 문서의 Endpoints 섹션에 표기.
- 에러: 공통 오류 응답(VALIDATION_ERROR, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, INTERNAL_ERROR 등)과 도메인 전용 오류는 각 문서에 요약.

서버
- 개발: `http://localhost:5000/api`
- 프로덕션: `https://api.happydog.com/api`