# 백엔드 수정 요청: 알림 메시지에 반려견 이름 표시

## 📌 요청 사항
알림 메시지에서 **사용자 닉네임 대신 대표 반려견 이름**을 표시하도록 수정 요청

## 🎯 목적
멍스타그램은 반려견 중심의 SNS이므로, 알림에서도 사용자보다 **반려견 이름**이 표시되는 것이 더 자연스러움

## 📱 현재 동작 vs 원하는 동작

### 현재 동작
```
"철수님이 회원님의 게시물을 좋아합니다"
"영희님이 댓글을 남겼습니다: 안녕하세요"
```

### 원하는 동작
```
"보리님이 회원님의 게시물을 좋아합니다"  // 보리 = 철수의 대표 반려견
"콩이님이 댓글을 남겼습니다: 안녕하세요"  // 콩이 = 영희의 대표 반려견
```

## 🔧 제안 구현 방식

### 방식 1: 메시지 생성 시 반려견 이름 사용 (권장)

**수정 위치**: `app/services/notification_service.py` - `create_notification()` 메서드

```python
# Step 3: 발신자 정보 구성
if sender_id == "system":
    sender_data = {
        "user_id": "system",
        "nickname": "HappyDog",
        "profile_image_url": None
    }
else:
    sender_doc = self.users_ref.document(sender_id).get()
    sender_info = sender_doc.to_dict()
    
    # ✅ 대표 반려견 이름 조회
    primary_pet_name = None
    primary_pet_id = sender_info.get('primary_pet_id')
    if primary_pet_id:
        pet_doc = self.pets_ref.document(primary_pet_id).get()
        if pet_doc.exists:
            primary_pet_name = pet_doc.to_dict().get('name')
    
    # ✅ 반려견 이름이 없으면 닉네임 사용 (fallback)
    display_name = primary_pet_name if primary_pet_name else sender_info.get('nickname')
    
    sender_data = {
        "user_id": sender_info.get('user_id'),
        "nickname": display_name,  # 반려견 이름 또는 닉네임
        "profile_image_url": sender_info.get('profile_image_url')
    }
```

### 방식 2: 알림 DTO에 pet_name 필드 추가 (대안)

프론트엔드에서 선택적으로 사용할 수 있도록 필드 추가

**수정 위치**: `app/models/notification.py` + `NotificationItemDto`

```python
# Notification 응답에 추가
{
  "notification_id": "noti456",
  "type": "COMMENT",
  "sender": {
    "user_id": "user123",
    "nickname": "철수",
    "pet_name": "보리",  // ✅ 추가
    "profile_image_url": "https://..."
  },
  "message": "보리님이 댓글을 남겼습니다: 안녕하세요",  // ✅ pet_name 사용
  ...
}
```

## 📝 수정 필요 파일

### 방식 1 (권장)
1. `app/services/notification_service.py`
   - `create_notification()` 메서드의 발신자 정보 구성 로직
   
2. 테스트 필요
   - 대표 반려견이 있는 경우: 반려견 이름 표시
   - 대표 반려견이 없는 경우: 닉네임으로 fallback
   - 시스템 알림: 기존대로 "HappyDog" 사용

### 방식 2 (대안)
1. `app/models/notification.py`
   - `Notification` 클래스에 `sender.pet_name` 필드 추가
   
2. `app/services/notification_service.py`
   - sender_data에 `pet_name` 포함
   
3. `app/api/notifications/handlers.py`
   - 메시지 템플릿에서 `pet_name` 우선 사용

## ✅ 기대 효과
- 멍스타그램의 반려견 중심 UX 강화
- 사용자 혼동 감소 (닉네임보다 반려견 이름이 더 친숙)
- 알림의 일관성 향상 (게시물/댓글에서도 반려견 이름 사용)

## 🚀 우선순위
**HIGH** - UX 개선에 직접적인 영향

## 📅 예상 작업 시간
- 방식 1: 1-2시간 (코드 수정 + 테스트)
- 방식 2: 2-3시간 (DTO 수정 + 마이그레이션 + 테스트)

## 📞 연락처
프론트엔드 팀 - 추가 논의 필요 시 연락 바랍니다.
