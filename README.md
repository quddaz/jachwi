# 자취선배 백엔드

후보 매물을 저장하고, 사진·메모·단계별 체크리스트를 이용해 집을 체계적으로 확인하는 서비스의 백엔드입니다.

## 기술 스택

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Security / OAuth2 Resource Server
- Spring JDBC
- MySQL
- Flyway
- AWS S3 SDK
- Testcontainers
- springdoc-openapi / Swagger UI

## 현재 구현 상태

- [x] Spring Boot 프로젝트 구성
- [x] MySQL 드라이버 구성
- [x] Flyway 및 MySQL 전용 모듈 구성
- [x] `V1__create_schema.sql` 초기 스키마 작성
- [x] 회원, 토큰, 매물, 사진, 메모, 체크리스트 관련 11개 테이블 생성
- [x] 외래 키, UNIQUE, CHECK 제약조건과 조회용 인덱스 구성
- [x] Testcontainers 기반 Flyway 마이그레이션 테스트
- [x] 환경변수 기반 데이터베이스 연결 설정
- [ ] 도메인 기능 및 API 구현

## 구현 범위

### 1. 공통 기반

- [ ] 공통 성공 응답 형식 구현
- [ ] 공통 오류 응답 형식과 필드 검증 오류 구현
- [ ] 도메인별 오류 코드 정의
- [ ] 전역 예외 처리기 구현
- [ ] Access Token에서 현재 회원을 식별하는 인증 컨텍스트 구현
- [ ] 다른 회원의 자원 접근 시 `404 Not Found`로 응답하도록 소유권 검증 구현
- [ ] 페이지 번호와 크기 검증 및 공통 페이지 응답 구현
- [ ] UTC 기반 날짜·시간 직렬화 설정
- [ ] 요청 ID, API 경로, 상태 코드, 처리 시간을 남기는 구조화 로그 구현
- [ ] 토큰, Google 인증 정보, 메모 원문, 사진 저장 키 등 민감정보 로그 차단
- [ ] OpenAPI JSON 및 Swagger UI 구성

### 2. 회원 및 인증

- [ ] Google Authorization Code + PKCE 로그인 구현
- [ ] Google ID Token의 서명, issuer, audience, nonce 검증
- [ ] Google `subject`를 기준으로 최초 로그인 시 회원 자동 생성
- [ ] 기존 회원 재로그인 시 이름, 이메일, 마지막 로그인 시각 갱신
- [ ] Access Token 발급 구현
- [ ] 회전 가능한 Refresh Token 발급 구현
- [ ] Refresh Token 원문 대신 SHA-256 해시만 저장
- [ ] Refresh Token 만료·폐기·재사용 검증
- [ ] 토큰 재발급 시 기존 Refresh Token 폐기 및 신규 토큰 생성
- [ ] 현재 기기의 Refresh Token을 폐기하는 로그아웃 구현
- [ ] 현재 로그인 회원 정보 조회 구현
- [ ] 공개 API와 인증 필요 API에 대한 SecurityFilterChain 설정

구현 API:

- [ ] `POST /api/auth/google`
- [ ] `POST /api/auth/tokens`
- [ ] `POST /api/auth/logout`
- [ ] `GET /api/members/me`

### 3. 후보 매물

- [ ] 매물 생성 구현
- [ ] 회원당 최대 50개 제한 구현
- [ ] 이름 trim 및 1~50자 검증
- [ ] 보증금·월세·관리비의 `null`과 0원 구분
- [ ] 금액이 0 이상의 정수인지 검증
- [ ] 주소와 발견 경로의 최대 500자 검증
- [ ] 매물 이름 부분 일치 검색 구현
- [ ] 회원 소유 매물만 조회되도록 쿼리 구현
- [ ] `lastActivityAt DESC, propertyId DESC` 정렬 구현
- [ ] 기본 20개, 최대 50개의 페이지네이션 구현
- [ ] 첫 번째 사진 미리보기와 전체 체크 진행 현황을 포함한 목록 조회 구현
- [ ] 사진 수, 메모, 단계별 체크 요약과 삭제 영향을 포함한 상세 조회 구현
- [ ] 전달된 필드만 변경하는 부분 수정 구현
- [ ] 선택 필드에 전달된 명시적 `null` 처리 구현
- [ ] 매물 삭제 전 외부 사진 객체 삭제 확인
- [ ] 매물 삭제 시 사진 메타데이터·메모·체크 기록 연쇄 삭제
- [ ] 매물 변경 시 `last_activity_at` 갱신 정책 구현

구현 API:

- [ ] `POST /api/properties`
- [ ] `GET /api/properties`
- [ ] `GET /api/properties/{propertyId}`
- [ ] `PATCH /api/properties/{propertyId}`
- [ ] `DELETE /api/properties/{propertyId}`

> `detailAddress`와 `properties.detail_address`는 현재 범위에 포함하지 않습니다.

### 4. 매물 사진

- [ ] S3 비공개 버킷 연동
- [ ] 사진 한 장 단위 업로드 구현
- [ ] 매물당 최대 30장 제한 구현
- [ ] 사진 한 장당 최대 10MiB 제한 구현
- [ ] JPEG, PNG, WebP 형식 제한 구현
- [ ] 선언 MIME 타입 검증
- [ ] 파일 시그니처 검증
- [ ] 이미지 디코딩 가능 여부 검증
- [ ] 외부 객체 저장 후 DB 저장 실패 시 보상 삭제 구현
- [ ] 업로드 완료 시각과 ID 기준 사진 순서 보장
- [ ] 첫 번째 사진을 미리보기로 선택
- [ ] 사진 원본을 인증된 요청으로만 제공
- [ ] 사진 응답에 공개 캐시가 남지 않도록 헤더 설정
- [ ] 사진 객체가 삭제됐거나 이미 없을 때만 DB 메타데이터 삭제
- [ ] 내부 `storage_key`를 API와 로그에 노출하지 않도록 처리

구현 API:

- [ ] `GET /api/properties/{propertyId}/photos`
- [ ] `POST /api/properties/{propertyId}/photos`
- [ ] `GET /api/properties/{propertyId}/photos/{photoId}/content`
- [ ] `DELETE /api/properties/{propertyId}/photos/{photoId}`

### 5. 매물 메모

- [ ] 매물별 메모 루트 조회 구현
- [ ] 동적 구조화 메모와 자유 메모 동시 조회 구현
- [ ] 구조화 메모 최대 20개 검증
- [ ] 항목명 trim 및 1~30자 검증
- [ ] 항목 내용 최대 200자 검증
- [ ] 자유 메모 최대 2,000자 검증
- [ ] 요청 배열 순서를 1부터 시작하는 `display_order`로 저장
- [ ] 기존 구조화 메모를 요청 상태로 전체 교체
- [ ] 구조화 메모와 자유 메모 전체 저장을 하나의 트랜잭션으로 처리
- [ ] 저장 실패 시 마지막 정상 저장 상태 유지

구현 API:

- [ ] `GET /api/properties/{propertyId}/memo`
- [ ] `PUT /api/properties/{propertyId}/memo`

### 6. 시스템 체크 항목

- [ ] 체크 단계 열거형 구현: `ONLINE_PHONE`, `ON_SITE`, `PRE_CONTRACT`
- [ ] 항목 유형 열거형 구현: `CORE`, `OPTIONAL`
- [ ] 활성 시스템 체크 항목 공개 조회 구현
- [ ] 단계 필수 필터 구현
- [ ] 유형, 검색어, 페이지 필터 구현
- [ ] 핵심 항목 우선 정렬 구현
- [ ] 비활성 항목의 신규 선택 차단
- [ ] 시스템 체크 항목 초기 데이터 마이그레이션 작성
- [ ] 새로운 활성 핵심 항목을 기존의 삭제되지 않은 사용자 체크리스트에 추가하는 관리 작업 구현
- [ ] 핵심 항목 자동 추가가 기존 매물 체크 스냅샷에는 영향을 주지 않도록 처리

구현 API:

- [ ] `GET /api/check-items`

### 7. 사용자 체크리스트

- [ ] 체크리스트 생성 구현
- [ ] 이름 trim 및 1~50자 검증
- [ ] 생성 시 같은 단계의 활성 핵심 항목 자동 포함
- [ ] 같은 단계의 선택 항목 추가 구현
- [ ] 다른 단계 항목 추가 차단
- [ ] 사용자 직접 질문 생성 차단
- [ ] 핵심 항목 제거 차단
- [ ] 항목 최소 1개, 최대 100개 검증
- [ ] 동일 시스템 항목 중복 방지
- [ ] 항목 표시 순서 저장 및 변경 구현
- [ ] 체크리스트 목록·상세 조회 구현
- [ ] 생성 후 단계 변경 차단
- [ ] 이름과 전체 항목 순서의 전체 교체 구현
- [ ] 비활성 항목은 기존 체크리스트에서만 유지·재정렬 가능하도록 처리
- [ ] `deleted_at`을 이용한 소프트 삭제 구현
- [ ] 삭제된 체크리스트를 조회·수정·신규 적용 대상에서 제외
- [ ] 체크리스트 삭제 후 기존 매물 체크 스냅샷 유지

구현 API:

- [ ] `POST /api/checklists`
- [ ] `GET /api/checklists`
- [ ] `GET /api/checklists/{checklistId}`
- [ ] `PUT /api/checklists/{checklistId}`
- [ ] `DELETE /api/checklists/{checklistId}`

### 8. 매물 체크리스트 적용 및 교체

- [ ] 매물의 단계별 체크리스트 적용 현황 조회 구현
- [ ] 한 매물의 같은 단계에 체크리스트를 최대 하나만 적용
- [ ] 적용 시 체크리스트 이름, 단계, 시스템 항목 ID, 질문, 안내, 순서를 스냅샷으로 복사
- [ ] 신규 스냅샷 항목을 `UNCONFIRMED`와 빈 메모로 생성
- [ ] 원본 체크리스트 수정이 기존 매물 스냅샷에 반영되지 않도록 처리
- [ ] 같은 단계의 다른 체크리스트로 교체 구현
- [ ] 시스템 항목 ID가 같은 항목의 상태와 메모 승계
- [ ] 질문 문구만 같은 서로 다른 시스템 항목은 별개로 처리
- [ ] 교체 후 모든 매물 체크 항목에 새 ID 발급
- [ ] 새 체크리스트에만 있는 항목을 기본 상태로 추가
- [ ] 새 체크리스트에 없는 기존 항목과 결과 삭제
- [ ] 체크리스트 교체 전체를 하나의 트랜잭션으로 처리
- [ ] 교체 실패 시 기존 체크 기록 전체 유지

구현 API:

- [ ] `GET /api/properties/{propertyId}/checklists`
- [ ] `PUT /api/properties/{propertyId}/checklists/{stage}`
- [ ] `GET /api/properties/{propertyId}/checklists/{propertyChecklistId}`

### 9. 체크 상태와 항목 메모 자동 저장

- [ ] 체크 상태 열거형 구현: `UNCONFIRMED`, `GOOD`, `CAUTION`
- [ ] 상태만 변경하는 저장 쿼리 구현
- [ ] 메모만 변경하는 저장 쿼리 구현
- [ ] 상태 저장 시 메모가 변경되지 않도록 보장
- [ ] 메모 저장 시 상태가 변경되지 않도록 보장
- [ ] 항목 메모 최대 500자 검증
- [ ] 빈 문자열을 이용한 항목 메모 전체 삭제 지원
- [ ] 교체 전 항목 ID로 들어온 자동 저장 요청을 `404`로 무효화
- [ ] 저장 실패 시 클라이언트가 미반영 입력을 유지할 수 있는 오류 응답 제공

구현 API:

- [ ] `PATCH /api/properties/{propertyId}/checklists/{propertyChecklistId}/items/{itemId}/status`
- [ ] `PATCH /api/properties/{propertyId}/checklists/{propertyChecklistId}/items/{itemId}/memo`

### 10. 진행 현황

- [ ] `GOOD`과 `CAUTION`을 확인 완료로 집계
- [ ] `UNCONFIRMED`를 미확인으로 집계
- [ ] 단계별 전체·완료·괜찮음·주의·미확인 개수 계산
- [ ] 매물 전체 단계의 진행 현황 계산
- [ ] `완료 개수 / 전체 개수 * 100` 정수 진행률 계산
- [ ] 전체 항목이 없을 때 진행률 0 반환
- [ ] 목록 조회에서 N+1 없이 진행 현황 집계

## 트랜잭션 경계

다음 작업은 반드시 하나의 트랜잭션으로 처리해야 합니다.

- [ ] 매물 메모 전체 교체
- [ ] 사용자 체크리스트 생성·수정·삭제
- [ ] 체크리스트의 매물 최초 적용
- [ ] 적용 체크리스트 교체와 결과 승계
- [ ] 사진 객체 처리를 제외한 매물 종속 DB 데이터 삭제

사진 업로드·삭제는 S3와 DB를 하나의 ACID 트랜잭션으로 묶을 수 없으므로 보상 작업을 사용해야 합니다.

## 테스트해야 할 핵심 시나리오

### 인증 및 소유권

- [ ] 최초 Google 로그인 시 회원이 생성된다.
- [ ] 재로그인 시 기존 데이터가 유지된다.
- [ ] Refresh Token이 회전되고 이전 토큰이 폐기된다.
- [ ] 로그아웃한 Refresh Token을 다시 사용할 수 없다.
- [ ] 인증 없이 보호 API를 호출하면 `401`을 반환한다.
- [ ] 다른 회원의 자원에 접근하면 `404`를 반환한다.

### 매물 및 사진

- [ ] 미입력 금액과 0원이 구분된다.
- [ ] 회원당 51번째 매물 등록이 거부된다.
- [ ] 첫 사진 삭제 후 다음 사진이 미리보기가 된다.
- [ ] 잘못된 MIME 또는 파일 시그니처의 사진이 거부된다.
- [ ] S3 저장 후 DB 저장 실패 시 S3 객체가 삭제된다.
- [ ] 사진 객체 삭제 실패 시 매물 DB 데이터가 유지된다.

### 메모 및 체크리스트

- [ ] 메모 전체 교체 실패 시 기존 메모가 유지된다.
- [ ] 체크리스트 생성 시 활성 핵심 항목이 모두 포함된다.
- [ ] 핵심 항목 제거 요청이 거부된다.
- [ ] 삭제된 체크리스트를 신규 매물에 적용할 수 없다.
- [ ] 원본 체크리스트 변경 후에도 기존 매물 질문과 순서가 유지된다.
- [ ] 체크리스트 교체 시 공통 시스템 항목의 상태와 메모가 유지된다.
- [ ] 교체 후 이전 항목 ID의 자동 저장 요청이 거부된다.
- [ ] 상태 저장이 메모를 변경하지 않는다.
- [ ] 메모 저장이 상태를 변경하지 않는다.

### 진행 현황

- [ ] `GOOD`과 `CAUTION`만 완료 개수에 포함된다.
- [ ] 전체 항목이 없으면 진행률이 0이다.
- [ ] 단계별 집계의 합이 전체 집계와 일치한다.

## 권장 구현 순서

1. 공통 응답, 오류 처리, 인증 컨텍스트
2. Google 로그인, JWT, Refresh Token
3. 회원 및 매물 CRUD
4. S3 사진 관리
5. 매물 메모
6. 시스템 체크 항목과 초기 데이터
7. 사용자 체크리스트
8. 매물 체크리스트 적용·교체
9. 상태·메모 자동 저장
10. 진행 현황 집계
11. OpenAPI 문서와 통합 테스트 보강

## 로컬 실행

MySQL 데이터베이스를 생성한 뒤 환경변수를 설정합니다.

```bash
export DB_URL='jdbc:mysql://localhost:3306/jachwisunbae?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC'
export DB_USERNAME='root'
export DB_PASSWORD='your-password'
```

애플리케이션 실행:

```bash
bash gradlew bootRun
```

테스트 실행:

```bash
bash gradlew test
```

테스트는 MySQL Testcontainers를 사용하므로 Docker 실행 환경이 필요합니다.

## Flyway 규칙

- 운영 환경에서 적용된 마이그레이션 파일은 수정하지 않습니다.
- 스키마 변경은 새로운 `V{version}__{description}.sql` 파일로 추가합니다.
- Spring SQL 초기화 스크립트와 Flyway를 함께 사용하지 않습니다.
- baseline은 빈 DB 기준으로 비활성화합니다.
- Flyway clean은 비활성화 상태를 유지합니다.
- 애플리케이션 배포 전에 실제 MySQL 기반 마이그레이션 테스트를 실행합니다.

현재 마이그레이션:

- `V1__create_schema.sql`: MVP 초기 스키마

## MVP 제외 범위

- 사용자 간 체크리스트 공유
- 사용자의 새로운 체크 질문 직접 생성
- 방문 기록 및 방문 완료
- 체크 기록 리비전과 교체 전 기록 복구
- 여러 매물 비교, 추천 및 점수
- 사진 순서 변경과 대표 사진 직접 지정
- 관리자 전용 화면
- 회원 탈퇴 및 삭제 데이터 복구
