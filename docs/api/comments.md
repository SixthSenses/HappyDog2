# Comments 도메인 명세

댓글 생성/조회/삭제 및 좋아요.

기본 정보
- 베이스 URL: `/api`
- 태그: `comments`
- 인증: Bearer 필요
- 멱등성: `POST /api/comments/posts/{post_id}/comments`에 적용

스키마
- `CommentCreateSchema`, `CommentResponseSchema`, `CommentListResponseSchema`, `CommentLikeToggleResponseSchema`

Endpoints
1) POST `/api/comments/posts/{post_id}/comments`
- 설명: 댓글 생성(알림 트리거)
- 요청: `CommentCreateSchema`
- 응답: 201(JSON `CommentResponseSchema`)
- 멱등성: 적용

2) GET `/api/comments/posts/{post_id}/comments`
- 설명: 댓글 목록 페이지네이션
- 응답: 200(JSON `CommentListResponseSchema`)

3) DELETE `/api/comments/comments/{comment_id}`
- 설명: 댓글 삭제(작성자만)
- 응답: 204(No Content)

4) POST `/api/comments/comments/{comment_id}/like`
- 설명: 댓글 좋아요 토글
- 요청: `EmptyRequestSchema`
- 응답: 200(JSON `CommentLikeToggleResponseSchema`)
