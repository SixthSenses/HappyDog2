# Posts 도메인 명세

게시글 생성/조회/수정/삭제 및 좋아요.

기본 정보
- 베이스 URL: `/api`
- 태그: `posts`
- 인증: 대부분 Bearer 필요(피드 조회는 상황 따라 공개/비공개 정책 적용)
- 멱등성: `POST /api/posts/`에 적용

스키마
- `PostCreateSchema`, `PostResponseSchema`, `PostUpdateSchema`, `PostsFeedResponseSchema`, `PostLikeToggleResponseSchema`

Endpoints
1) POST `/api/posts/`
- 설명: 게시글 생성(텍스트/파일 경로)
- 요청: `PostCreateSchema`
- 응답: 201(JSON `PostResponseSchema`)
- 멱등성: 적용

2) GET `/api/posts/`
- 설명: 피드 목록 커서 기반 페이지네이션(`limit`, `cursor`)
- 응답: 200(JSON `PostsFeedResponseSchema`)

3) GET `/api/posts/{post_id}`
- 설명: 게시글 상세(로그인 사용자는 `is_liked` 포함)
- 응답: 200(JSON `PostResponseSchema`)

4) PATCH `/api/posts/{post_id}`
- 설명: 게시글 수정(작성자만)
- 요청: `PostUpdateSchema`
- 응답: 200(JSON `PostResponseSchema`)

5) DELETE `/api/posts/{post_id}`
- 설명: 게시글 삭제(작성자만)
- 응답: 204(No Content)

6) POST `/api/posts/{post_id}/like`
- 설명: 좋아요 토글
- 요청: `EmptyRequestSchema`
- 응답: 200(JSON `PostLikeToggleResponseSchema`)
