# 인증·매물·체크 결과 확장 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refresh Token 기반 인증을 완성하고 매물·메모·매물 체크리스트·결과·진행률 API를 명시적 JDBC와 엄격한 레이어 구조로 구현한다.

**Architecture:** 기능별 수직 슬라이스로 엔티티, 저장소 인터페이스와 JDBC 구현, 서비스 정책·검증, 컨트롤러 DTO를 추가한다. 복합 변경은 서비스 트랜잭션으로 묶고 모든 소유 자원 SQL에 회원 ID를 포함한다.

**Tech Stack:** Java 21, Spring Boot 4.1, Spring MVC, Spring JDBC, MySQL 8, Flyway, Nimbus JWT, JUnit 5, Testcontainers

## Global Constraints

- Spring Data JDBC와 JPA를 사용하지 않는다.
- SQL은 JDBC 저장소의 해당 메소드 안에 둔다.
- DTO가 계층 사이 변환을 담당한다.
- Lombok은 `@Getter`, `@Slf4j`만 허용한다.
- 서비스 테스트는 MySQL Testcontainers 통합 테스트로 작성한다.
- 기존 `AuthConfig`의 커밋되지 않은 사용자 변경은 보존한다.
- 기능 단위로 전체 테스트를 통과한 뒤 한국어 커밋한다.

---

### Task 1: Refresh Token과 회원 인증 완성

**Files:**
- Create: `src/main/java/com/jachwisunbae/auth/token/RefreshToken.java`
- Create: `src/main/java/com/jachwisunbae/auth/token/RefreshTokenGenerator.java`
- Create: `src/main/java/com/jachwisunbae/auth/token/RefreshTokenHasher.java`
- Create: `src/main/java/com/jachwisunbae/auth/repository/RefreshTokenRepository.java`
- Create: `src/main/java/com/jachwisunbae/auth/repository/JdbcRefreshTokenRepository.java`
- Create: `src/main/java/com/jachwisunbae/auth/controller/dto/RotateTokenRequest.java`
- Create: `src/main/java/com/jachwisunbae/auth/controller/dto/LogoutRequest.java`
- Create: `src/main/java/com/jachwisunbae/member/controller/MemberController.java`
- Create: `src/main/java/com/jachwisunbae/member/controller/dto/MemberResponse.java`
- Create: `src/main/java/com/jachwisunbae/member/service/MemberService.java`
- Create: `src/main/java/com/jachwisunbae/auth/web/AuthenticationErrorWriter.java`
- Modify: `src/main/java/com/jachwisunbae/auth/service/AuthService.java`
- Modify: `src/main/java/com/jachwisunbae/auth/controller/AuthController.java`
- Modify: `src/main/java/com/jachwisunbae/auth/controller/dto/LoginResponse.java`
- Modify: `src/main/java/com/jachwisunbae/auth/web/JwtAuthenticationFilter.java`
- Modify: `src/main/java/com/jachwisunbae/auth/config/AuthConfig.java`
- Modify: `src/main/java/com/jachwisunbae/common/exception/DomainErrorCode.java`
- Modify: `src/main/java/com/jachwisunbae/common/web/error/DomainErrorHttpMapper.java`
- Modify: `src/main/resources/application.properties`
- Test: `src/test/java/com/jachwisunbae/auth/repository/JdbcRefreshTokenRepositoryTest.java`
- Test: `src/test/java/com/jachwisunbae/auth/service/AuthServiceTest.java`
- Test: `src/test/java/com/jachwisunbae/auth/controller/AuthControllerTest.java`
- Test: `src/test/java/com/jachwisunbae/member/service/MemberServiceTest.java`
- Test: `src/test/java/com/jachwisunbae/auth/web/JwtAuthenticationFilterTest.java`

**Interfaces:**
- Produces: `RefreshTokenRepository.findByHashForUpdate(String)`, `save(RefreshToken)`, `revoke(Long, LocalDateTime)`, `revokeAllActiveByMemberId(Long, LocalDateTime)`
- Produces: `AuthService.rotate(String)`, `AuthService.logout(Long, String)`, `MemberService.getMe(Long)`

- [ ] **Step 1: Write failing repository and service integration tests**

Cover storing only a 64-character hash, successful rotation, expiry rejection, reused token revoking every active member token, logout revoking only the supplied token, and member lookup.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `./gradlew test --tests '*JdbcRefreshTokenRepositoryTest' --tests '*AuthServiceTest' --tests '*MemberServiceTest'`

Expected: compilation or assertion failure because Refresh Token and member service APIs do not exist.

- [ ] **Step 3: Implement the token domain, JDBC repository, services and DTO conversion**

Use `SecureRandom` for 32 random bytes and Base64 URL encoding without padding. Hash UTF-8 token bytes with SHA-256 and encode lowercase hexadecimal. Lock token lookup with `SELECT ... FOR UPDATE`.

- [ ] **Step 4: Write and run failing filter/controller tests**

Cover public route bypass, missing/invalid/expired Bearer token common JSON 401, rotate response, logout 204, and `/api/members/me`.

- [ ] **Step 5: Implement protected route policy and common authentication error writer**

Public paths are `/api/auth/**` and `/api/check-items`; all other `/api/**` paths require a valid Access Token. Error payload uses the shared error DTO and never includes token text.

- [ ] **Step 6: Run authentication tests and full regression tests**

Run: `./gradlew test --tests 'com.jachwisunbae.auth.*' --tests 'com.jachwisunbae.member.*'`

Run: `./gradlew test`

- [ ] **Step 7: Commit**

```bash
git add src/main src/test
git commit -m "Refresh Token 회전과 회원 인증을 완성한다"
```

### Task 2: 매물 CRUD, 검색·정렬·페이징

**Files:**
- Create: `src/main/java/com/jachwisunbae/property/entity/Property.java`
- Create: `src/main/java/com/jachwisunbae/property/repository/PropertyRepository.java`
- Create: `src/main/java/com/jachwisunbae/property/repository/JdbcPropertyRepository.java`
- Create: `src/main/java/com/jachwisunbae/property/service/PropertyService.java`
- Create: `src/main/java/com/jachwisunbae/property/service/policy/PropertyPolicy.java`
- Create: `src/main/java/com/jachwisunbae/property/service/validation/PropertyValidator.java`
- Create: `src/main/java/com/jachwisunbae/property/service/dto/*.java`
- Create: `src/main/java/com/jachwisunbae/property/controller/PropertyController.java`
- Create: `src/main/java/com/jachwisunbae/property/controller/dto/*.java`
- Create: `src/main/java/com/jachwisunbae/common/web/PageResponse.java`
- Modify: `src/main/java/com/jachwisunbae/member/repository/MemberRepository.java`
- Modify: `src/main/java/com/jachwisunbae/member/repository/JdbcMemberRepository.java`
- Modify: `src/main/java/com/jachwisunbae/common/exception/DomainErrorCode.java`
- Modify: `src/main/java/com/jachwisunbae/common/web/error/DomainErrorHttpMapper.java`
- Test: `src/test/java/com/jachwisunbae/property/repository/JdbcPropertyRepositoryTest.java`
- Test: `src/test/java/com/jachwisunbae/property/service/PropertyServiceTest.java`
- Test: `src/test/java/com/jachwisunbae/property/controller/PropertyControllerTest.java`

**Interfaces:**
- Produces: `PropertyRepository.save`, `findPageByMemberId`, `countByMemberId`, `findDetailByIdAndMemberId`, `update`, `deleteByIdAndMemberId`, `touch`
- Produces: `PropertyService.create`, `findAll`, `findOne`, `update`, `delete`

- [ ] **Step 1: Write failing entity, repository and service tests**

Cover normalization, nullable versus zero amounts, stable activity/id sorting, escaped partial-name search, paging totals, ownership hiding, and the 51st property rejection.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `./gradlew test --tests 'com.jachwisunbae.property.*'`

Expected: missing production types or failed behavior assertions.

- [ ] **Step 3: Implement property domain, validation/policy and JDBC repository**

Lock the member row before counting and inserting. Use count plus page queries, `LIMIT ? OFFSET ?`, and checklist aggregate joins for progress without N+1.

- [ ] **Step 4: Implement DTOs and controller**

Create returns 201 and Location. List returns `PageResponse`. PATCH uses a Jackson-aware field wrapper so omitted and explicit-null values differ.

- [ ] **Step 5: Run focused and full tests**

Run: `./gradlew test --tests 'com.jachwisunbae.property.*'`

Run: `./gradlew test`

- [ ] **Step 6: Commit**

```bash
git add src/main src/test
git commit -m "매물 CRUD와 검색 페이징을 구현한다"
```

### Task 3: 매물 메모 전체 교체

**Files:**
- Create: `src/main/java/com/jachwisunbae/property/memo/entity/PropertyMemo.java`
- Create: `src/main/java/com/jachwisunbae/property/memo/entity/PropertyMemoItem.java`
- Create: `src/main/java/com/jachwisunbae/property/memo/repository/PropertyMemoRepository.java`
- Create: `src/main/java/com/jachwisunbae/property/memo/repository/JdbcPropertyMemoRepository.java`
- Create: `src/main/java/com/jachwisunbae/property/memo/service/PropertyMemoService.java`
- Create: `src/main/java/com/jachwisunbae/property/memo/service/validation/PropertyMemoValidator.java`
- Create: `src/main/java/com/jachwisunbae/property/memo/controller/PropertyMemoController.java`
- Create: `src/main/java/com/jachwisunbae/property/memo/controller/dto/*.java`
- Test: `src/test/java/com/jachwisunbae/property/memo/repository/JdbcPropertyMemoRepositoryTest.java`
- Test: `src/test/java/com/jachwisunbae/property/memo/service/PropertyMemoServiceTest.java`
- Test: `src/test/java/com/jachwisunbae/property/memo/controller/PropertyMemoControllerTest.java`

**Interfaces:**
- Produces: `PropertyMemoRepository.findByPropertyId`, `saveRoot`, `deleteItems`, `insertItems`
- Produces: `PropertyMemoService.get(Long memberId, Long propertyId)`, `replace(Long memberId, Long propertyId, ReplacePropertyMemoCommand)`

- [ ] **Step 1: Write failing integration tests**

Cover empty memo response, ordered replacement, label/content/free memo boundaries, ownership hiding, activity touch, and rollback when an item insert fails.

- [ ] **Step 2: Run tests and verify RED**

Run: `./gradlew test --tests 'com.jachwisunbae.property.memo.*'`

- [ ] **Step 3: Implement repository, validator, transactional service and DTOs**

Upsert the memo root, delete items, batch insert new ordered items, and touch the property inside one transaction.

- [ ] **Step 4: Run focused and full tests**

Run: `./gradlew test --tests 'com.jachwisunbae.property.memo.*'`

Run: `./gradlew test`

- [ ] **Step 5: Commit**

```bash
git add src/main src/test
git commit -m "매물 메모 전체 교체를 구현한다"
```

### Task 4: 매물 체크리스트 적용과 교체

**Files:**
- Create: `src/main/java/com/jachwisunbae/property/checklist/entity/PropertyChecklist.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/entity/PropertyChecklistItem.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/type/CheckStatus.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/repository/PropertyChecklistRepository.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/repository/JdbcPropertyChecklistRepository.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/service/PropertyChecklistService.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/service/validation/PropertyChecklistValidator.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/controller/PropertyChecklistController.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/controller/dto/*.java`
- Extend: `src/main/java/com/jachwisunbae/checklist/repository/UserChecklistRepository.java`
- Extend: `src/main/java/com/jachwisunbae/checklist/repository/JdbcUserChecklistRepository.java`
- Test: `src/test/java/com/jachwisunbae/property/checklist/repository/JdbcPropertyChecklistRepositoryTest.java`
- Test: `src/test/java/com/jachwisunbae/property/checklist/service/PropertyChecklistServiceTest.java`
- Test: `src/test/java/com/jachwisunbae/property/checklist/controller/PropertyChecklistControllerTest.java`

**Interfaces:**
- Produces: `PropertyChecklistRepository.findByPropertyAndStageForUpdate`, `saveChecklist`, `findItems`, `deleteItems`, `insertItems`, `findSummaries`, `findDetail`
- Produces: `PropertyChecklistService.findAll`, `applyOrReplace`, `findOne`

- [ ] **Step 1: Write failing snapshot and replacement integration tests**

Cover first application, immutable snapshots, one checklist per stage, ownership, stage mismatch, deleted source rejection, same-system-item result inheritance, new IDs for every replacement item, removed result deletion, and transaction rollback.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `./gradlew test --tests 'com.jachwisunbae.property.checklist.*'`

- [ ] **Step 3: Implement entities, repository and transactional replacement flow**

Load the owned property and active owned source checklist. Capture previous results by source system item ID, upsert the stage root, delete old items, then insert the new snapshot with inherited or default result values.

- [ ] **Step 4: Implement list/detail DTOs and controller**

List returns all three stages with an applied flag and progress. Detail returns snapshot metadata, ordered items and stage progress.

- [ ] **Step 5: Run focused and full tests**

Run: `./gradlew test --tests 'com.jachwisunbae.property.checklist.*'`

Run: `./gradlew test`

- [ ] **Step 6: Commit**

```bash
git add src/main src/test
git commit -m "매물 체크리스트 적용과 결과 승계를 구현한다"
```

### Task 5: 체크 상태·메모와 진행률

**Files:**
- Modify: `src/main/java/com/jachwisunbae/property/checklist/repository/PropertyChecklistRepository.java`
- Modify: `src/main/java/com/jachwisunbae/property/checklist/repository/JdbcPropertyChecklistRepository.java`
- Modify: `src/main/java/com/jachwisunbae/property/checklist/service/PropertyChecklistService.java`
- Modify: `src/main/java/com/jachwisunbae/property/checklist/controller/PropertyChecklistController.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/controller/dto/UpdateCheckStatusRequest.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/controller/dto/UpdateCheckMemoRequest.java`
- Create: `src/main/java/com/jachwisunbae/property/checklist/service/dto/CheckProgressResult.java`
- Test: `src/test/java/com/jachwisunbae/property/checklist/service/PropertyChecklistResultServiceTest.java`
- Test: `src/test/java/com/jachwisunbae/property/checklist/controller/PropertyChecklistResultControllerTest.java`

**Interfaces:**
- Produces: `updateStatus(..., CheckStatus)`, `updateMemo(..., String)`, `aggregateProgress(Long memberId, Long propertyId)`

- [ ] **Step 1: Write failing independent update and aggregation tests**

Cover status preserving memo, memo preserving status, empty memo deletion, 500-character boundary, stale item ID 404, each status count, stage/overall consistency and zero-item 0 percent.

- [ ] **Step 2: Run focused tests and verify RED**

Run: `./gradlew test --tests '*PropertyChecklistResult*'`

- [ ] **Step 3: Implement narrow update SQL and aggregate queries**

Each update statement joins or exists-checks ownership using member, property, property checklist and item IDs. Progress uses conditional aggregate expressions and integer division guarded by `CASE WHEN COUNT(*) = 0`.

- [ ] **Step 4: Run focused and full tests**

Run: `./gradlew test --tests '*PropertyChecklistResult*'`

Run: `./gradlew test`

- [ ] **Step 5: Commit**

```bash
git add src/main src/test
git commit -m "체크 결과 자동 저장과 진행률 집계를 구현한다"
```

### Task 6: 마이그레이션·문서·최종 회귀 검증

**Files:**
- Create when required: `src/main/resources/db/migration/V4__*.sql`
- Modify: `README.md`
- Modify: `src/test/java/com/jachwisunbae/FlywayMigrationTests.java`

**Interfaces:**
- Consumes: Tasks 1–5 public API and database schema.
- Produces: accurate implementation checklist and repeatable full verification.

- [ ] **Step 1: Add a failing migration assertion only if schema changes are required**

Do not edit V1–V3. Any new column/index/constraint goes into V4 or later and is asserted through MySQL Flyway integration tests.

- [ ] **Step 2: Update README implementation status and API notes**

Mark only implemented behavior complete and keep photos/S3 explicitly pending.

- [ ] **Step 3: Run formatting and diff checks**

Run: `git diff --check`

- [ ] **Step 4: Run clean full test suite**

Run: `./gradlew clean test`

Expected: `BUILD SUCCESSFUL`, zero failed tests.

- [ ] **Step 5: Inspect final working tree and commit documentation/migrations**

```bash
git status --short
git add README.md src/main/resources/db/migration src/test/java/com/jachwisunbae/FlywayMigrationTests.java docs/superpowers/plans/2026-08-17-auth-property-check-result-plan.md
git commit -m "구현 상태와 데이터베이스 검증을 정리한다"
```
