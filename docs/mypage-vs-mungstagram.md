# MyPage vs. 멍스타그램 프로필

목적과 데이터 소스가 다른 두 화면을 명확히 구분하기 위한 안내입니다.

## MyPage (마이페이지)
- 목적: 내 계정/반려동물 관리, 설정, 프로필 이미지 변경(비공개 영역)
- 헤더 데이터: Users API — GET `/api/users/{user_id}`의 `nickname`, `profile_image_url`, `post_count`
- 반려견 데이터: Pet API — GET `/api/pets/{pet_id}` (소유자 전용)
- 업로드 그리드(선택): 내 게시물 목록을 Posts API에서 가져와 타일로 표시
  - GET `/api/posts/users/{author_id}/posts?limit&cursor` (jwt_optional)
  - 매핑: 각 `post.image_urls[0]`를 썸네일로 사용
- 인증: 일반적으로 jwt 필요(마이페이지는 비공개)

## 멍스타그램 프로필 (공개 작성자 페이지)
- 목적: 특정 작성자의 공개 프로필 + 게시물 피드(공개 영역, jwt_optional)
- 헤더 데이터: Users API — GET `/api/users/{user_id}` (공개 프로필)
- 게시물 데이터: Posts API — GET `/api/posts/users/{author_id}/posts`
- 인증: jwt_optional (로그인 여부에 따라 is_liked 등 달라질 수 있음)

## 혼동 포인트 정리
- `users.profile_image_url`는 헤더(프로필 원형 이미지)용이며, 그리드 이미지는 `posts.image_urls[]`에서 가져옵니다.
- MyPage의 헤더/설정은 Users/Pets에서, 그리드는 Posts에서 데이터를 가져옵니다.
- 403/404 처리: 보호 API(예: 반려동물 소유자 전용)는 403/404 수신 시 selected_pet_id를 무효화하고 등록 플로우로 유도합니다.

## 페이징/에러 가이드
- 페이징: `limit`/`cursor`를 이용, `next_cursor`로 추가 로드
- 에러: 401은 Authenticator가 처리, 403/404/기타는 SafeApiCall로 표준화 후 UI에서 메시지 표시

## 구현 체크리스트(요약)
- [ ] Posts API/DTO/DI 추가
- [ ] PostRepository/UseCases 구현(커서 페이징)
- [ ] MyPageViewModel: getMyPosts로 `uploadedImageUrls` 채우기
- [ ] 멍스타그램 프로필 화면: 헤더(users) + 그리드(posts)
