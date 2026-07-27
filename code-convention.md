# 코드 컨벤션 (Java / Layered Architecture)

> 대상 스코프(MVP): 회원·권한 / 운송수단·자원 / 예약·매칭 / 결제·정산
> 기준: Java 17+, Spring Boot, JPA
> 아키텍처: 전통적인 계층형 아키텍처(Controller → Service → Repository → Entity)

---

## 1. 기본 원칙

1. **계층은 위에서 아래로만 의존한다.** `Controller → Service → Repository`. 역방향 의존(Repository가 Service를 참조하는 등)은 금지.
2. **비즈니스 로직은 Service 계층에 모은다.** Controller는 요청을 받아 Service를 호출하고 결과를 응답으로 변환하는 역할만 한다. Entity는 데이터와 최소한의 자기 검증만 가진다.
3. **에러는 예외(Exception)로 처리한다.** 커스텀 예외를 던지고, `@RestControllerAdvice`(전역 예외 처리기)에서 HTTP 응답으로 변환한다.
4. Entity는 JPA 표준 방식(`@Entity`, Lombok `@Getter`/`@Setter`)을 따른다. 별도의 불변 객체(Value Object)나 `Result` 타입 같은 함수형 패턴은 쓰지 않는다.

---

## 2. 패키지 구조

**도메인을 최상위로 나누고, 그 안에서 레이어(controller/service/repository/entity/dto/exception)를 둔다.** 같은 기능(도메인)에 관련된 파일들이 한 폴더 안에 모여있어서, 기능 단위로 찾고 수정하기 쉽다.

```
com.company.delivery
├── member
│   ├── entity
│   │   ├── Member.java
│   │   └── Role.java
│   ├── repository
│   │   └── MemberRepository.java
│   ├── service
│   │   └── MemberService.java
│   ├── controller
│   │   └── MemberController.java
│   ├── dto
│   │   ├── request
│   │   │   └── MemberCreateRequest.java
│   │   └── response
│   │       └── MemberCreateResponse.java
│   └── exception
│       ├── DuplicateMemberException.java
│       └── MemberNotFoundException.java
├── vehicle
│   ├── entity
│   │   ├── Vehicle.java
│   │   └── VehicleType.java
│   ├── repository
│   │   └── VehicleRepository.java
│   ├── service
│   │   └── VehicleService.java
│   ├── controller
│   │   └── VehicleController.java
│   ├── dto
│   │   ├── request/VehicleCreateRequest.java
│   │   └── response/VehicleCreateResponse.java
│   └── exception
│       ├── InvalidWeightException.java
│       └── OverMaxDistanceException.java
├── delivery
│   ├── entity
│   │   ├── DeliveryRequest.java
│   │   ├── DeliveryStatus.java
│   │   ├── Matching.java
│   │   └── MatchingStatus.java
│   ├── repository
│   │   ├── DeliveryRequestRepository.java
│   │   └── MatchingRepository.java
│   ├── service
│   │   ├── DeliveryService.java
│   │   └── MatchingService.java
│   ├── controller
│   │   └── DeliveryController.java
│   ├── dto
│   │   ├── request/DeliveryCreateRequest.java
│   │   └── response/DeliveryCreateResponse.java
│   └── exception
│       ├── NoAvailableCourierException.java
│       └── AlreadyMatchedException.java
├── payment
│   ├── entity
│   │   └── PointWallet.java
│   ├── repository
│   │   └── PaymentRepository.java
│   ├── service
│   │   └── PaymentService.java
│   ├── controller
│   │   └── PaymentController.java
│   └── exception
│       └── InsufficientPointException.java
└── common
    ├── exception
    │   ├── ErrorResponse.java
    │   └── GlobalExceptionHandler.java   // @RestControllerAdvice, 도메인 전체 예외를 여기서 HTTP로 변환
    └── config
        └── (JPA/Web/Security 등 설정 클래스)
```

**규칙**
- 패키지명 = 도메인명 (`member`, `vehicle`, `delivery`, `payment`). 각 도메인 패키지 안에서만 `entity/repository/service/controller/dto/exception`으로 다시 나눈다.
- **다른 도메인의 Repository나 Entity를 직접 참조하지 않는다.** 다른 도메인의 데이터가 필요하면 그 도메인의 Service를 호출한다.
  - 예: `DeliveryService`가 결제를 처리해야 하면 `PaymentRepository`를 직접 호출하지 않고 `PaymentService.charge(...)`를 호출한다.
- 도메인 간에 공통으로 쓰는 것(전역 예외 처리기, 설정 클래스 등)만 `common` 패키지에 둔다. `common`이 특정 도메인 전용 로직을 담는 곳이 되지 않도록 주의한다.
- 도메인 하나 안의 파일이 너무 많아지면(예: `delivery`가 계속 커지면) `delivery.request`, `delivery.matching`처럼 도메인을 더 잘게 쪼갠다.
- Repository는 Entity 1개당 1개씩 만든다 (`MemberRepository` ↔ `Member`, ...).

---

## 3. 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
|---|---|---|
| Entity | 명사, 도메인 용어 그대로 | `DeliveryRequest`, `Vehicle` |
| Entity의 getter/setter | Lombok `@Getter`/`@Setter`로 자동 생성 (직접 작성 금지) | `getStatus()`, `setStatus(...)` |
| Repository | `~Repository`, `JpaRepository` 상속 | `interface VehicleRepository extends JpaRepository<Vehicle, Long>` |
| Service | `~Service` | `VehicleService` |
| Service의 public 메서드 | 동사로 시작 | `registerVehicle(...)`, `matchCourier(...)` |
| Controller | `~Controller` | `DeliveryController` |
| 커스텀 예외 | `~Exception`, `RuntimeException` 상속 | `InvalidWeightException` |
| DTO | `{도메인}{동작}{Request\|Response}` | `DeliveryCreateRequest`, `DeliveryCreateResponse` |

---

## 4. Entity 컨벤션

- `@Entity` + Lombok `@Getter` `@Setter` `@NoArgsConstructor`를 기본으로 사용한다.
- 식별자는 `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Enum 필드는 `@Enumerated(EnumType.STRING)` (`ORDINAL` 금지 — 순서 바뀌면 데이터가 깨짐).
- 연관관계는 우선 FK 값(`Long memberId`)으로 두고, `@ManyToOne`/`@OneToMany` 같은 객체 연관관계는 실제로 조인 조회가 필요할 때만 추가한다 (N+1 문제 예방).

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId; // Member의 FK

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    private double maxWeight;
    private double maxDistance;
}
```

- 최소한의 자기 검증(예: 음수 방지)이 필요하면 Entity 안에 넣어도 되지만, **비즈니스 규칙 검증은 원칙적으로 Service에서 한다.** Entity에 검증 로직이 많아지면 Service로 옮긴다.

---

## 5. Service 계층 규칙

- 비즈니스 로직, 트랜잭션, 예외 발생이 전부 여기서 일어난다.
- 생성자 주입(Lombok `@RequiredArgsConstructor`)을 쓴다. 필드 주입(`@Autowired` on field)은 금지.
- 하나의 public 메서드 = 하나의 유스케이스. 메서드 안에서 검증 → Entity 생성/조회 → Repository 호출 순서로 작성한다.

```java
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public Vehicle registerVehicle(VehicleCreateRequest request) {
        if (request.getMaxWeight() <= 0) {
            throw new InvalidWeightException(request.getMaxWeight());
        }
        if (request.getMaxDistance() <= 0) {
            throw new OverMaxDistanceException(request.getMaxDistance());
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setOwnerId(request.getOwnerId());
        vehicle.setType(request.getType());
        vehicle.setMaxWeight(request.getMaxWeight());
        vehicle.setMaxDistance(request.getMaxDistance());

        return vehicleRepository.save(vehicle);
    }
}
```

- Service는 다른 Service를 호출할 수 있다 (예: `DeliveryService`가 `PaymentService`를 호출). 단, **순환 호출**(A가 B를 부르고 B가 다시 A를 부르는 구조)은 금지 — 순환이 필요해 보이면 로직을 상위 UseCase성 메서드로 합치거나 이벤트로 분리한다.

---

## 6. 예외 처리

### 6.1 커스텀 예외

도메인별로 의미 있는 예외를 만든다. 전부 `RuntimeException`을 상속한다 (checked exception 금지 — 메서드 시그니처가 지저분해지고 람다와도 안 맞음). 각 예외는 해당 도메인 패키지의 `exception` 하위(예: `vehicle.exception.InvalidWeightException`)에 둔다. `ErrorResponse`와 `GlobalExceptionHandler`만 도메인 전체가 공유하므로 `common.exception`에 둔다.

```java
public class InvalidWeightException extends RuntimeException {
    public InvalidWeightException(double weight) {
        super("유효하지 않은 무게입니다: " + weight);
    }
}

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(Long memberId) {
        super("존재하지 않는 회원입니다: " + memberId);
    }
}

public class AlreadyMatchedException extends RuntimeException {
    public AlreadyMatchedException(Long deliveryRequestId) {
        super("이미 매칭된 배송 요청입니다: " + deliveryRequestId);
    }
}
```

### 6.2 전역 예외 처리기

모든 예외 → HTTP 응답 변환을 **한 곳**(`GlobalExceptionHandler`)에서 담당한다. Controller마다 `try-catch`를 흩어놓지 않는다.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidWeightException.class, OverMaxDistanceException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(MemberNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DuplicateMemberException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DuplicateMemberException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler({NoAvailableCourierException.class, AlreadyMatchedException.class,
            InsufficientPointException.class})
    public ResponseEntity<ErrorResponse> handleUnprocessable(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        // 예상 못한 예외는 500으로, 원인은 로그로만 남기고 사용자에겐 상세 노출 안 함
        return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류가 발생했습니다."));
    }
}
```

```java
public record ErrorResponse(String message) {}
```

### 6.3 예외 → HTTP 상태코드 매핑 기준

| 예외 유형 | HTTP 상태 | 예시 |
|---|---|---|
| 입력 값 자체가 유효하지 않음 | `400 Bad Request` | `InvalidWeightException`, `OverMaxDistanceException` |
| 리소스 없음 | `404 Not Found` | `MemberNotFoundException` |
| 중복/충돌 | `409 Conflict` | `DuplicateMemberException` |
| 비즈니스 규칙상 처리 불가 | `422 Unprocessable Entity` | `NoAvailableCourierException`, `AlreadyMatchedException`, `InsufficientPointException` |
| 예상 못한 서버 오류 | `500 Internal Server Error` | 그 외 모든 예외 |

---

## 7. 적용 예시 요약 (도메인별)

| 도메인 | Entity | Repository | Service | 대표 예외 |
|---|---|---|---|---|
| 회원·권한 | `Member` | `MemberRepository` | `MemberService` | `DuplicateMemberException`, `MemberNotFoundException` |
| 운송수단·자원 | `Vehicle` | `VehicleRepository` | `VehicleService` | `InvalidWeightException`, `OverMaxDistanceException` |
| 예약·매칭 | `DeliveryRequest` | `DeliveryRequestRepository` | `DeliveryService` | `NoAvailableCourierException` |
| 예약·매칭 | `Matching` | `MatchingRepository` | `MatchingService` | `AlreadyMatchedException` |
| 결제·정산 | `PointWallet` | `PaymentRepository` | `PaymentService` | `InsufficientPointException` |

---

## 8. 포맷팅 규칙

- 들여쓰기: 스페이스 2칸 (탭 금지)
- 한 줄 최대 길이: 100자
- import 순서: `java.*` → `javax.*`/`jakarta.*` → 외부 라이브러리 → `com.company.*` (그룹 사이 빈 줄 1개), wildcard import(`import java.util.*`) 금지
- 도구로 강제: **Spotless + google-java-format**
  - 이 값들은 google-java-format이 강제하는 고정값이며 별도 설정 옵션이 아니다.
  - `./gradlew spotlessCheck`를 CI에 포함, 로컬에는 pre-commit hook으로 `spotlessApply` 실행
  - IntelliJ에 google-java-format 플러그인을 설치하면 IDE 자동 들여쓰기도 이 규칙을 따르게 된다.
- 어노테이션은 필드/메서드 선언 바로 위 줄에, 한 줄에 하나씩.

---

## 9. 테스트 컨벤션

- 테스트 메서드명: `given_when_then` 스타일 (한글 허용)

```java
@Test
void 무게가_0이하면_InvalidWeightException을_던진다() {
    VehicleCreateRequest request = new VehicleCreateRequest(1L, VehicleType.CAR, -5, 10);

    assertThatThrownBy(() -> vehicleService.registerVehicle(request))
        .isInstanceOf(InvalidWeightException.class);
}
```

- 예외 검증은 `assertThatThrownBy(...).isInstanceOf(...)` 또는 `assertThrows(...)`로 한다. 메시지 문자열까지 검증하고 싶으면 `.hasMessageContaining(...)` 추가.
- 레이어별 테스트 전략:

| 레이어 | 테스트 종류 | 도구 |
|---|---|---|
| `service` | 단위 테스트 (Repository는 mock) | JUnit5 + Mockito |
| `repository` | 통합 테스트 (실제 DB) | `@DataJpaTest` + Testcontainers |
| `controller` | API 테스트 (HTTP 요청/응답 검증) | `@SpringBootTest` + MockMvc |
| `entity` | 필요 시에만 (검증 로직이 있는 경우) | JUnit5 + AssertJ |

- 테스트 커버리지 강제 라인은 팀 논의 후 확정 (우선 `service` 레이어 70% 이상을 제안).

---

## 10. DTO 컨벤션

- Entity를 Controller 밖으로 직접 노출하지 않는다. 요청은 `~Request`, 응답은 `~Response`로 변환한다.
- 변환은 Service 계층에서 하거나, Controller에서 간단히 해도 된다 (DDD처럼 레이어를 엄격히 분리하지 않으므로 유연하게 판단).
- 변환 방식: 수동 매핑(생성자/정적 팩토리) 우선, 필드가 많아지면 MapStruct 도입 검토.
- **DTO 네이밍**: `{도메인}{동작}{Request|Response}` 형식 (예: `DeliveryCreateRequest`, `DeliveryCreateResponse`). Entity 이름(`DeliveryRequest`)을 DTO 이름에 그대로 붙이지 않는다 — 의미 중복으로 헷갈림.

```java
public record DeliveryCreateResponse(Long id, String status, long feePoint) {
    public static DeliveryCreateResponse from(DeliveryRequest entity) {
        return new DeliveryCreateResponse(
            entity.getId(),
            entity.getStatus().name(),
            entity.getFeePoint()
        );
    }
}
```

---

## 11. Controller 규칙

- Controller는 DTO만 다루고, Service를 호출한 뒤 결과를 응답으로 변환하는 역할만 한다. 비즈니스 로직(if 분기, 계산)을 Controller에 넣지 않는다.
- 예외는 Controller에서 잡지 않는다 — Service에서 던진 예외가 `GlobalExceptionHandler`까지 그대로 전파되게 둔다.

```java
@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryCreateResponse> requestDelivery(
            @RequestBody DeliveryCreateRequest request) {
        DeliveryRequest created = deliveryService.requestDelivery(request);
        return ResponseEntity.ok(DeliveryCreateResponse.from(created));
    }
}
```

---

## 12. 트랜잭션 경계

- `@Transactional`은 **Service 계층에만** 붙인다. Controller, Repository에는 붙이지 않는다.
- 하나의 Service public 메서드 = 하나의 트랜잭션 단위가 원칙.
- 외부 API 호출(알림 발송 등)은 트랜잭션 안에 포함하지 않는다 — 트랜잭션 커밋 이후 이벤트(`@TransactionalEventListener` 등)로 분리한다.
- 조회만 하는 메서드는 `@Transactional(readOnly = true)`를 붙여 성능을 최적화한다.

---

## 13. 로깅 규칙

- `Service` 계층에서 예외를 던지기 직전, 또는 `GlobalExceptionHandler`에서 로그를 남긴다. 같은 예외를 여러 군데서 중복 로깅하지 않는다.
- 로그 레벨 기준:

| 상황 | 레벨 |
|---|---|
| 사용자 입력 오류성 예외 (400계열) | `INFO` 또는 로그 생략 (너무 빈번함) |
| 비즈니스 규칙 실패 (422계열) | `WARN` |
| 예상 못한 서버 오류 (500) | `ERROR` + 스택트레이스 포함 |

- 민감정보(비밀번호, 결제수단 등)는 로그에 절대 포함하지 않는다.

---

## 14. 레이어 의존성 강제

사람 리뷰만으로는 "Controller가 Repository를 직접 호출하면 안 된다" 같은 규칙이 지켜지기 어려우므로 **ArchUnit**으로 빌드 시 강제한다.

```java
@Test
void controller는_repository를_직접_호출하지_않는다() {
    noClasses().that().resideInAPackage("..controller..")
        .should().dependOnClassesThat().resideInAPackage("..repository..")
        .check(importedClasses);
}

@Test
void repository는_service나_controller에_의존하지_않는다() {
    noClasses().that().resideInAPackage("..repository..")
        .should().dependOnClassesThat().resideInAnyPackage("..service..", "..controller..")
        .check(importedClasses);
}

@Test
void 도메인은_다른_도메인의_repository를_직접_참조하지_않는다() {
    // 예: vehicle 패키지의 어떤 클래스도 member 패키지의 repository를 직접 호출하면 안 된다.
    // (member 데이터가 필요하면 member.service의 MemberService를 거쳐야 한다)
    noClasses().that().resideInAPackage("..vehicle..")
        .should().dependOnClassesThat().resideInAPackage("..member.repository..")
        .check(importedClasses);
    // 실제로는 도메인 개수만큼(member/vehicle/delivery/payment 상호 조합) 반복하거나,
    // ArchUnit의 slice 기반 규칙(사이클 검사)으로 일반화하는 것을 검토한다.
}
```

최소 강제 규칙 3가지:
1. `controller` → `repository` 직접 의존 금지 (반드시 `service`를 거칠 것)
2. `repository` → `service`, `controller` 의존 금지 (역방향 금지)
3. 도메인 패키지(`member`, `vehicle`, `delivery`, `payment`)는 서로 다른 도메인의 `repository`/`entity`를 직접 의존하지 않는다 — 반드시 그 도메인의 `service`를 거칠 것

---

## 15. 데이터베이스 마이그레이션 규칙

스키마 변경은 Hibernate가 아니라 **Flyway**로만 관리한다 (`spring.jpa.hibernate.ddl-auto: validate`). 상세 작동 원리·IDE 활용법은 `docs/flyway-guide.md`를 참고하고, 여기서는 커밋 전 반드시 지켜야 할 핵심만 정리한다.

- **파일 위치/네이밍**: `src/main/resources/db/migration/V<버전번호>__<설명>.sql` (언더스코어 2개 필수). 예: `V7__add_location_to_vehicle.sql`
- **이미 반영된 마이그레이션 파일은 절대 수정하지 않는다.** 로컬/서버 DB에 한 번이라도 적용된 파일을 고치면 Checksum 불일치로 다음 구동이 실패한다. 수정이 필요하면 버전을 올린 새 파일을 추가한다.
- Entity에 필드를 추가/변경했다면, **같은 PR 안에 대응하는 마이그레이션 파일을 함께 커밋**한다. Entity만 바꾸고 마이그레이션을 빠뜨리면 `ddl-auto: validate`에 의해 애플리케이션이 기동 실패한다.
- 여러 테이블을 함께 바꿔야 하면 파일 하나에 `ALTER TABLE` 여러 개를 순서대로 넣어도 되고, 논리적으로 성격이 다르면 파일을 나눠도 된다 — 팀 판단에 맡긴다.

---

## 16. Git 커밋 / 브랜치 / PR 컨벤션

브랜치 전략·PR 규칙의 전체 내용은 `docs/git-flow-guide.md`가 기준 문서이며, 여기서는 코딩 전에 알아야 할 핵심만 요약한다.

### 16.1 브랜치 전략

| 브랜치 유형 | 용도 | 이름 규칙 | 대상 상위 브랜치 |
|---|---|---|---|
| `main` | 배포 가능한 가장 안정적인 브랜치 | `main` | - |
| `develop` | 다음 버전을 위한 기능이 모이는 통합 브랜치 | `develop` | `main` |
| `feature` | 신규 기능/버그 수정 작업 브랜치 | `feat/도메인-내용` (예: `feat/delivery-fee-calculation`) | `develop` |
| `release` | 배포 준비 및 최종 QA | `release/<버전>` | `develop` |
| `hotfix` | 배포된 `main`의 긴급 장애 패치 | `hotfix/<이슈번호>-<요약>` | `main` |

- 작업 시작 전 `develop`을 최신화한 뒤 그 위에서 브랜치를 분기한다. `hotfix`만 예외적으로 `main`에서 분기해 `main`으로 PR한다.
- 머지 후 로컬 작업 브랜치는 삭제한다.

### 16.2 커밋 메시지

- 최소 형식: `type: 설명` (`feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `style`)
  - 예: `feat: 배송 요금 산정 로직 추가`
- 변경 배경까지 남기고 싶으면 `docs/git-flow-guide.md`의 확장 형식(`type(scope): subject` + 본문 + 푸터)을 써도 된다. 최소 형식과 상충하지 않는, 필요할 때만 쓰는 선택 사항이다.

### 16.3 PR 규칙

- 리뷰어를 최소 1명 이상 지정한다.
- 연관 이슈가 있으면 본문에 명시해 자동으로 닫히게 한다 (예: `Resolves: #45`).
- `.github/pull_request_template.md`의 항목(요약 / 주요 변경 사항 / 리뷰 포인트 / 스크린샷·테스트 결과)을 빠짐없이 채운다 — UI 작업은 스크린샷, API 작업은 테스트 로그나 호출 결과를 첨부한다.
- PR 단위는 도메인 하나 + 기능 하나 정도로 작게 유지한다.

---

## 17. 체크리스트 (PR 리뷰 시 확인)

- [ ] Controller가 Entity를 직접 반환하지 않고 DTO로 변환하는가?
- [ ] 비즈니스 로직이 Controller가 아니라 Service에 있는가?
- [ ] 예상 가능한 실패가 커스텀 예외로 표현되고, `GlobalExceptionHandler`에 매핑이 추가되어 있는가?
- [ ] `@Transactional`이 Service 계층에만 붙어 있는가?
- [ ] `@Autowired` 필드 주입이 아니라 생성자 주입을 쓰는가?
- [ ] Repository가 Entity 1개당 1개씩 대응되는가?
- [ ] 다른 도메인의 Repository/Entity를 직접 참조하지 않고, 필요하면 그 도메인의 Service를 거쳤는가?
- [ ] Enum 필드에 `@Enumerated(EnumType.STRING)`을 썼는가? (`ORDINAL` 금지)
- [ ] 새로 추가한 서비스 로직에 단위 테스트가 있는가? (given_when_then 네이밍)
- [ ] ArchUnit 테스트(레이어 의존성 규칙)가 깨지지 않는가?
- [ ] Spotless 포맷팅 검사를 통과하는가?
- [ ] Entity 필드를 추가/변경했다면 대응하는 Flyway 마이그레이션 파일을 새로 추가했는가? (기존 마이그레이션 파일을 수정하지 않았는가?)
- [ ] `develop`을 대상으로 브랜치를 분기·PR 했는가? (`hotfix`는 `main` 예외)
- [ ] 리뷰어를 지정하고, PR 템플릿의 요약/변경사항/리뷰 포인트/테스트 결과를 모두 작성했는가?