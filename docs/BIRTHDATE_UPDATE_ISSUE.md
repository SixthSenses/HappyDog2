# 마이페이지 반려동물 정보 수정 - 알려진 이슈

## 생년월일 수정 백엔드 오류 (500 Internal Server Error)

### 문제 상황
생년월일 수정 시 백엔드에서 500 오류 발생:
```
TypeError: ('Cannot convert to a Firestore Value', datetime.date(2023, 10, 15), 'Invalid type', <class 'datetime.date'>)
```

### 근본 원인
백엔드의 `pet_profile_service.py` (line 328)에서 Firestore에 `datetime.date` 객체를 직접 저장하려고 시도하고 있습니다. Firestore는 Python의 `datetime.date` 타입을 지원하지 않으며, `datetime.datetime` 또는 문자열만 허용합니다.

### 백엔드 수정 필요 사항

**파일**: `app/api/pets/services/pet_profile_service.py`

**현재 코드** (추정):
```python
# Line ~328
def update_pet_profile(self, pet_id: str, user_id: str, update_data: dict):
    # ...
    pet_ref.update(update_data)  # datetime.date 객체가 포함된 dict
```

**수정 방안 1 - 문자열로 변환**:
```python
def update_pet_profile(self, pet_id: str, user_id: str, update_data: dict):
    # birthdate를 문자열로 변환
    if 'birthdate' in update_data and isinstance(update_data['birthdate'], datetime.date):
        update_data['birthdate'] = update_data['birthdate'].isoformat()  # "YYYY-MM-DD"
    
    pet_ref.update(update_data)
```

**수정 방안 2 - datetime.datetime으로 변환**:
```python
def update_pet_profile(self, pet_id: str, user_id: str, update_data: dict):
    # birthdate를 datetime.datetime으로 변환
    if 'birthdate' in update_data and isinstance(update_data['birthdate'], datetime.date):
        update_data['birthdate'] = datetime.datetime.combine(
            update_data['birthdate'], 
            datetime.time.min
        )
    
    pet_ref.update(update_data)
```

**수정 방안 3 - Pydantic 스키마에서 처리** (권장):
```python
# schemas/pet_schemas.py
from pydantic import BaseModel, field_serializer
from datetime import date

class PetUpdateSchema(BaseModel):
    birthdate: Optional[date] = None
    
    @field_serializer('birthdate')
    def serialize_birthdate(self, value: date, _info) -> str:
        """Firestore 호환을 위해 날짜를 문자열로 직렬화"""
        return value.isoformat() if value else None
```

### 프론트엔드 측 대응 (이미 완료)

1. ✅ yyyy-MM-dd 형식의 문자열로 전송
2. ✅ 500 에러 시 사용자 친화적 메시지 표시
3. ✅ 로깅 추가로 디버깅 용이하게 개선

### 임시 해결책

백엔드 수정 전까지는 생년월일 수정 시 다음 메시지가 표시됩니다:
> "생년월일 수정 중 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요"

### 테스트 방법

백엔드 수정 후:
1. 마이페이지에서 생년월일 클릭
2. DatePicker에서 날짜 선택
3. "저장" 버튼 클릭
4. 성공 시 화면에 "YYYY.MM.DD" 형식으로 표시되어야 함

### 관련 파일
- 프론트엔드: `MyPageViewModel.kt` - `updateBirthDate()` 함수
- 프론트엔드: `EditBirthdateScreen.kt` - DatePicker UI
- 백엔드: `pet_profile_service.py` - `update_pet_profile()` 함수

---

**작성일**: 2025-10-13  
**상태**: 백엔드 수정 대기 중
