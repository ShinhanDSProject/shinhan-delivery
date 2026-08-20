# 코드 컨벤션 (Java / Layered Architecture)

> 대상 스코프(MVP): 회원·권한 / 운송수단·자원 / 예약·매칭 / 결제·정산
> 기준: Java 17+, Spring Boot, JPA
> 아키텍처: 전통적인 계층형 아키텍처(Controller → Service → Repository → Entity)

---

## 1. 기본 원칙

1. **계층은 위에서 아래로만 의존한다.** `Controller → Service → Repository`. 역방향 의존(Repository가 Service를 참조하는 등)은 금지.
2. **비즈니스 로직은 Service 계층에 모은다.** Controller는 요청을 받아 Service를 호출하고 결과를 응답으로 변환하는 역할만 한다. Entity는 데이터와 최소한의 자기 검증만 가진다.
3. **에러는 예외(Exception)로 처리한다.** 커스텀 예외를 던지고, `@RestControllerAdvice`(전역 예외 처리기)에서 HTTP 응답으로 변환한다.
4. Entity 및 모든 DTO/Java 클래스의 `Getter`, `Setter` 메서드는 직접 코드로 수동 작성하지 않고 **무조건 Lombok 라이브러리(`@Getter`, `@Setter`)를 100% 사용하여 개발**한다. 수동 `getXXX()`, `setXXX()` 메서드 작성은 엄격히 금지된다. 별도의 불변 객체(Value Object)나 `Result` 타입 같은 함수형 패턴은 쓰지 않는다.
5. **신규 기능은 설계 문서를 먼저 작성한다.** 코드를 작성하기 전에 `docs/design/기능명-design.md`(User Story·성공 기준, ERD 또는 아키텍처 흐름도, API/메시지 명세, 작업 분할(WBS))를 먼저 작성해 설계를 정리한 뒤 구현에 들어간다. 별도 설계 리뷰 승인까지는 필수가 아니며, 문서화 자체가 목적이다 (`docs/design-phase-guide.md` 참고).
6. **What과 How를 명확히 분리하고 단일 추상화 수준(SLAP)을 유지한다.** 
   - **Service 계층:** "무엇을 수행하는가(What)"에 집중하여 비즈니스 유스케이스 흐름을 선언적(Declarative)으로 작성합니다. DB 조작 기술, 복잡한 산출식, 문자열 파싱 등 "어떻게 처리하는가(How)"에 해당하는 세부 구현은 Entity, Repository, Helper로 위임합니다.
   - **Entity/Domain 계층:** 도메인 비즈니스 규칙과 상태 변이 로직을 캡슐화합니다.
   - **Repository 계층:** 데이터 영속화 및 기술적 조회 세부 사항(How)을 전담합니다.
   - **Controller 계층:** Presentation 단위 데이터 변환 및 HTTP 요청/응답 라우팅만 담당합니다.

---

## 2. 패키지 구조

**도메인을 최상위로 나누고, 그 안에서 레이어(controller/service/repository/entity/dto/exception, 필요 시 helper)를 둔다.** 같은 기능(도메인)에 관련된 파일들이 한 폴더 안에 모여있어서, 기능 단위로 찾고 수정하기 쉽다.

```
com.company.delivery
├── member
│   ├── entity
│   │   ├── Member.java
│   │   ├── MemberRole.java
│   │   └── MemberValidationField.java
│   ├── constant
│   │   └── MemberValidationConstants.java
│   ├── validator
│   │   └── MemberFieldValidator.java
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
│   ├── helper
│   │   └── DeliveryFeeCalculator.java
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
  - 예외: §4에서 규정하는, FK 필드를 쓰기의 유일한 진입점으로 유지하는 읽기 전용 `@ManyToOne(insertable = false, updatable = false)` Entity 간 연관관계는 이 금지 규칙의 대상이 아니다.
- **다른 도메인 객체 목록 조회 시 인덱스 수동 조작 금지:** 타 도메인의 Entity 리스트를 받아와 `list.get(0)`처럼 수동으로 인덱스를 꺼내지 않고, 해당 도메인의 Service/Repository에 대표 객체 전용 조회 메서드(예: `findFirstByMemberId`, `findPrimaryXxx`)를 캡슐화하여 호출한다.
- 도메인 간에 공통으로 쓰는 것(전역 예외 처리기, 설정 클래스 등)만 `common` 패키지에 둔다. `common`이 특정 도메인 전용 로직을 담는 곳이 되지 않도록 주의한다.
- 도메인 하나 안의 파일이 너무 많아지면(예: `delivery`가 계속 커지면) `delivery.request`, `delivery.matching`처럼 도메인을 더 잘게 쪼갠다.
- Repository는 Entity 1개당 1개씩 만든다 (`MemberRepository` ↔ `Member`, ...).
- Repository·Service 등 외부 협력 객체 의존 없이 순수 계산·변환만 하는 로직(복잡한 산출식, 좌표/거리 계산 등)이 무거워지면 `helper` 서브패키지로 분리한다 (§5.1 참고). 여러 도메인이 공유하는 계산 로직이면 `common.helper`에 둔다.

---

## 3. 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
|---|---|---|
| Entity | 명사, 도메인 용어 그대로 | `DeliveryRequest`, `Vehicle` |
| Entity의 getter/setter | Lombok `@Getter`/`@Setter`로 자동 생성 (직접 작성 금지) | `getStatus()`, `setStatus(...)` |
| Repository | `~Repository`, `JpaRepository` 상속 | `interface VehicleRepository extends JpaRepository<Vehicle, Long>` |
| Service | `~Service` | `VehicleService` |
| Service의 public 메서드 | 동사로 시작 | `registerVehicle(...)`, `matchCourier(...)` |
| Helper | 역할이 드러나는 이름(`~Helper`, `~Calculator` 등), 상태 없는 `@Component` | `DeliveryFeeCalculator` |
| Controller | `~Controller` | `DeliveryController` |
| 커스텀 예외 | `~Exception`, `RuntimeException` 상속 | `InvalidWeightException` |
| DTO | `{도메인}{동작}{Request\|Response}` | `DeliveryCreateRequest`, `DeliveryCreateResponse` |

---

## 4. Entity 컨벤션

- `@Entity` + Lombok `@Getter` `@Setter` `@NoArgsConstructor`를 기본으로 사용한다.
- 식별자는 `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Enum 필드는 `@Enumerated(EnumType.STRING)` (`ORDINAL` 금지 — 순서 바뀌면 데이터가 깨짐).
- **다른 도메인(테이블)을 참조하는 Entity는 참조 대상 엔티티 명칭 기반의 FK 필드(`Long xxxId`)를 통일되게 명명한다** — 예: `Address.memberId`, `Vehicle.memberId`, `DeliveryRequest.memberId`. 역할(Role) 표기 대신 참조 대상 엔티티 이름인 `memberId`(`member_id`)를 통일되게 사용하여 직관성과 일관성을 사수한다.
- **연관관계는 FK 값(`Long memberId`)을 항상 유지한다.** 생성·수정은 오직 이 FK 필드로만 하며(정적 팩토리·`updateBy` 등), 다른 도메인을 참조하는 FK마다 읽기 전용 `@ManyToOne(fetch = FetchType.LAZY)` 객체 연관관계를 `@JoinColumn(name = "...", insertable = false, updatable = false)`로 함께 둔다. FK 필드가 쓰기의 유일한 진입점이고, 객체 연관관계는 조인 조회 전용 뷰다.
- `@OneToMany` 컬렉션(부모 쪽에서 자식 목록을 들고 있는 방향)은 실제로 컬렉션 순회가 필요할 때만 추가한다 — 컬렉션은 캐스케이드·`orphanRemoval` 설정, N+1, 대량 데이터 시 메모리 문제까지 얽혀 있어 FK 하나당 `@ManyToOne` 하나를 추가하는 것보다 훨씬 신중해야 한다.

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

  @Column(name = "member_id", nullable = false)
  private Long memberId; // Member의 FK, 쓰기는 이 필드로만

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", insertable = false, updatable = false)
  private Member member; // 조인 조회 전용 읽기 전용 뷰

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
- 과거 주문 기반 재신청은 Entity나 결제 결과를 복제하지 않고, 소유권 검증 후 재사용 가능한 입력 필드만 전용 DTO로 반환한다. 신규 요금과 결제는 기존 견적·결제 유스케이스에서 다시 처리한다.

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

### 5.1 Helper 클래스 규칙 (Stateless Computation Helper Standard)

**원칙:** §1의 6번(SLAP) 원칙에 따라, Repository나 다른 Service 같은 외부 협력 객체에 의존하지 않는 순수 계산·변환 로직(복잡한 산출식, 좌표/거리 계산, 문자열 파싱 등)이 Service 메서드 안에서 무거워지면 도메인 패키지의 `helper` 서브패키지로 분리한다.

- **위치:** `{도메인}.helper.{이름}` (예: `delivery.helper.DeliveryFeeCalculator`). 여러 도메인이 공유하는 계산 로직이면 `common.helper`에 둔다.
- **이름:** 역할이 드러나는 이름을 쓴다 (`~Calculator`, `~Formatter`, `~Helper` 등).
- **상태를 갖지 않는다:** 인스턴스 필드로 Repository·Service·외부 자원을 주입받지 않는다. `static final` 상수만 둘 수 있다. 상태나 외부 협력이 필요해지는 순간 그건 Helper가 아니라 Service다.
- **Spring 빈으로 등록하되 트랜잭션은 없다:** `@Component`로 등록해 Service에 생성자 주입하지만, `@Transactional`은 붙이지 않는다 — 트랜잭션 경계는 Service의 책임이다.
- **Spring 컨텍스트 없이 단위 테스트 가능해야 한다:** `new`로 직접 생성해 순수 로직만 검증할 수 있어야 하고, Mockito 테스트에서는 mocking 없이 `@Spy`로 실제 인스턴스를 그대로 사용한다.

```java
@Component
public class DeliveryFeeCalculator {

    private static final BigDecimal BASE_FEE = BigDecimal.valueOf(3000);

    public DeliveryEstimateResponse calculateFee(double distanceKm, double weight, ItemSize itemSize) {
        // Repository/Service 의존 없는 순수 계산
        ...
    }
}
```

### 5.2 상태별 안내 문구 관리 및 단건 조회 반환 타입

- **상태값(Enum)마다 제목/본문 같은 정적 문구가 여러 개 딸려 있으면**, 대상마다 개별 `switch`를 반복하지 말고 상태 하나당 문구 세트 하나를 대응시키는 Enum(예: `NotificationCreateListener.DeliveryNotificationTemplate`)으로 모은다. 문구를 추가·수정할 때 한 곳만 보면 되게 하기 위함이다.
- **단건 조회의 반환 타입은 "없으면 실제 오류인가"로 정한다.** 존재가 불변식(있어야 정상)이면 `findXxxOrThrow(...)`로 커스텀 예외를 던진다(이 문서 전반의 기본 패턴). 반대로 "아직 없을 수 있는 게 정상 상태"(예: 아직 매칭되지 않은 배송 요청의 배송원 조회)라면 `Optional<T>`를 반환해 호출부가 `ifPresent`/`orElse`로 자연스럽게 처리하게 한다. 모든 단건 조회에 `Optional`을 일괄 강제하지는 않는다 — 존재가 불변식인 곳까지 `Optional`로 감싸면 호출부마다 불필요한 null 처리 부담만 늘어난다.

---

## 6. 예외 처리

### 6.1 최상위 비즈니스 예외(BusinessException) & 공통 EntityNotFoundException

프로젝트의 모든 커스텀 예외는 최상위 `BusinessException`(`RuntimeException` 상속)을 상속받고, `ErrorCode` Enum을 주입받아 예외 정보를 정밀하게 전달한다.

- **`BusinessException`**: 프로젝트 내 모든 비즈니스 예외의 최상위 부모 클래스
- **`EntityNotFoundException`**: 존재하지 않는 리소스 조회 시 사용하는 공통 예외. 도메인마다 `XxxNotFoundException` 클래스를 무분별하게 새로 만들지 않고, `new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND)` 형태로 조합하여 사용(KISS 및 클래스 폭발 방지 원칙 준수)
- **`ErrorCode`**: HTTP 상태 코드, 고유 에러 코드(`C001`, `M001` 등), 사용자 안내 메시지를 `common.exception` 패키지에서 통합 관리하는 Enum

```java
// 공통 리소스 미존재 예외 발생 시
Member member = memberRepository.findById(memberId)
    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

// 특수 도메인 비즈니스 예외 정의 시 (BusinessException 상속)
public class DuplicateMemberException extends BusinessException {
    public DuplicateMemberException(String email) {
        super(ErrorCode.DUPLICATE_EMAIL, "이미 가입된 이메일 주소입니다. (Email: " + email + ")");
    }
}
```

### 6.2 전역 예외 처리기 & 표준 에러 응답 DTO

모든 예외 → HTTP 응답 변환은 **전역 예외 처리기(`GlobalExceptionHandler`)**가 한곳에서 담당한다. Controller에 `try-catch`를 작성하지 않는다.

- **`ErrorResponse` DTO**: `status`, `code`, `message`, `timestamp`, `traceId`, `errors`(유효성 검사 세부 내역) 필드로 구성되며, `@JsonInclude(NON_EMPTY)`를 적용해 불필요한 null/빈 배열 출력을 직렬화에서 생략한다.
- **`GlobalExceptionHandler`**: `@RestControllerAdvice` 기반으로 `BusinessException`, `MethodArgumentNotValidException`(@Valid 실패), `HttpMessageNotReadableException`(JSON 파싱 실패), `HttpRequestMethodNotSupportedException`, 및 `Exception`(500 스택트레이스 은폐)을 수집 포착한다.

### 6.3 예외 → HTTP 상태코드 & 에러코드 매핑 기준

| 예외 유형 | HTTP 상태 | ErrorCode 예시 | 응답 메시지 예시 |
|---|---|---|---|
| 입력 값 검증/포맷 오류 | `400 Bad Request` | `INVALID_INPUT_VALUE` (`C001`) | 유효하지 않은 입력값입니다. / 요청 본문의 JSON 형식이 올바르지 않습니다. |
| 리소스 미존재 | `404 Not Found` | `MEMBER_NOT_FOUND` (`M001`), `VEHICLE_NOT_FOUND` (`V001`) | 존재하지 않는 회원입니다. |
| 중복/충돌 | `409 Conflict` | `DUPLICATE_EMAIL` (`M002`) | 이미 가입된 이메일 주소입니다. |
| 잔액 부족 등 비즈니스 규칙 위반 | `400 Bad Request` | `INSUFFICIENT_BALANCE` (`P002`) | 포인트 잔액이 부족합니다. |
| 미처 포착하지 못한 서버 내부 오류 | `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` (`C003`) | 서버 내부 오류가 발생했습니다. (스택트레이스 서버 로깅 후 은폐) |

### 6.4 관측가능성: Trace ID (`MdcLoggingFilter`)

모든 HTTP 요청은 `Security` 필터 체인의 `MdcLoggingFilter`를 가장 먼저 통과한다. 이 필터가 요청 헤더 `X-Trace-Id`(없으면 신규 UUID)를 SLF4J MDC에 `traceId`로 주입하고, 요청 종료 시 `MDC.clear()`로 정리한다.

- 그 결과 해당 요청 처리 중 찍히는 모든 로그 라인(`logback-spring.xml` 패턴에 `[%X{traceId}]` 반영)과 `ErrorResponse.traceId`가 자동으로 같은 값을 갖는다.
- 개발자는 로그를 남기거나 예외를 던질 때 traceId를 직접 다루는 코드를 작성할 필요가 없다 — 필터가 이미 MDC에 넣어둔 값을 로깅 프레임워크와 `ErrorResponse`가 알아서 읽는다.

### 6.5 예외 메시지 하드코딩 금지 및 ErrorCode Enum 일관성 수칙 (Strict Rule)

1. **표준 예외 및 하드코딩 메시지 사용 금지**:
   - `throw new IllegalArgumentException("존재하지 않는 회원입니다.")` 처럼 Java 표준 예외(`IllegalArgumentException`, `RuntimeException`)를 직접 인스턴스화하거나 예외 메시지 문자열을 하드코딩하여 던지는 구문을 100% 금지한다.
2. **`ErrorCode` Enum 필수 사용**:
   - 모든 비즈니스 예외 발생 시 `common.exception.ErrorCode` Enum에 정의된 고유 코드를 주입하여 `throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND)` 또는 `throw new EntityNotFoundException(ErrorCode.DELIVERY_NOT_FOUND)` 형태로 발화한다.
3. **신규 에러 발생 상황 처리**:
   - 신규 예외 상황 발생 시 임의의 String 파라미터를 하드코딩하지 말고, `ErrorCode` Enum에 도메인별 고유 에러 코드(`C008`, `M007`, `V005` 등) 및 명확한 한국어 안내 메시지를 추가한 후 사용한다.

- 장애 문의 시 클라이언트가 알려준 `traceId`로 로그를 검색하면 해당 요청의 전체 처리 과정을 즉시 역추적할 수 있다.

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

## 8. 명명 및 포맷팅 규칙

### 8.1 식별자 명명 규칙 (Naming Conventions)

- **메서드명 (비즈니스/테스트 포함 모든 메서드):** 무조건 **`lowerCamelCase`**로 작성합니다 (예: `getMember`, `registerVehicle`, `updateBy`, `createAddressSuccess`). 메서드명에 언더스코어(`_`)나 한글 사용을 엄격히 금지합니다.
- **상수 (`static final` 필드 및 Enum 상수):** 무조건 **`UPPER_SNAKE_CASE`**로 작성합니다 (예: `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE`, `MEMBER_NOT_FOUND`).
- **클래스 및 인터페이스명:** 무조건 **`UpperCamelCase` (PascalCase)**로 작성합니다 (예: `MemberService`, `DeliveryRequest`, `VehicleType`).
- **변수 및 필드명:** 무조건 **`lowerCamelCase`**로 작성합니다 (예: `memberId`, `deliveryRequestRepository`).

### 8.2 단일 도메인 서비스 메서드명 명명 규칙 (Service Method Naming Standard)

- **원칙:** 단일 도메인 서비스(`AddressService`, `MemberService`, `VehicleService`, `PaymentService`, `NoticeService` 등) 내부의 CRUD 및 조회 메서드는 서비스 클래스명에 이미 도메인이 포함되어 있으므로 **중복 명사를 제거하고 명확한 동사/조회 패턴(`create`, `getById`, `list`, `listByIds`, `update`, `delete`)을 적용**합니다.
- **목적:** `addressService.getAddresses(...)` ❌ 대신 `addressService.list(...)` ⭕, `memberService.getMember(1L)` ❌ 대신 `memberService.getById(1L)` ⭕ 처럼 명명 직관성과 통일성을 사수합니다.
- **표준 메서드 명명 매핑 규칙:**
  1. **생성 (Create):** `create(...)`
  2. **단건 ID 조회 (Read Single by ID):** `getById(...)` (예: `getById(Long id)`)
  3. **다건/목록 조회 (Read List / Page):** `list(...)` (예: `list()`, `list(Pageable pageable)`, `list(Long memberId)`)
  4. **ID 목록 조회 (Read List by IDs):** `listByIds(...)` (예: `listByIds(List<Long> ids)`)
  5. **수정 (Update):** `update(...)`
  6. **삭제 (Delete):** `delete(...)`
  7. **도메인 특화 비즈니스 동작:** 의미 있는 도메인 어휘 선호 (`login`, `updatePassword`, `updateRole`, `chargePoint`, `usePoint` 등)
  8. **다중 도메인 복합 서비스 (`DeliveryService` 등):** 문맥 구분이 필요하므로 특화 메서드명 유지 (`requestDelivery`, `estimateFee`, `confirmPickup`, `completeDelivery` 등)

### 8.3 메서드 파라미터 계층 정렬 규칙 (Parameter Hierarchy Ordering Standard)

- **원칙:** 메서드의 파라미터(인자) 순서는 **도메인 계층 구조(Domain Hierarchy)상 큰 개념(상위 도메인 / 소유자 ID)부터 작은 개념(하위 리소스 ID), 조건/필터, DTO/Payload 순으로 배치**합니다.
- **목적:** "누가(Member) 어떤 리소스(Resource)를 무엇(Request)으로 변경하는가"의 비즈니스 문맥과 RESTful URL 경로(`/members/{memberId}/addresses/{addressId}`) 순서를 100% 동기화하여 위치 착오 버그를 방지합니다.
- **표준 파라미터 배치 순서:**
  1. **상위 도메인 식별자 (Parent / Aggregate Root / Owner ID):** 예) `memberId`
  2. **하위 도메인 식별자 (Child Domain / Resource ID):** 예) `addressId`, `notificationId`
  3. **조건 및 옵션 (Filter / Condition / Pageable):** 예) `category`, `pageable`
  4. **수정/생성 데이터 패킷 (Request DTO / Command Payload):** 예) `request`
- **적용 예시:**
  ```java
  // ⭕ 올바른 순서: memberId (상위) -> addressId (하위) -> request (DTO)
  public Address update(Long memberId, Long id, AddressUpdateRequest request)
  public void delete(Long memberId, Long id)
  public Notification markAsRead(Long memberId, Long notificationId)

  // ❌ 잘못된 순서 (하위 리소스 ID가 상위 소유자 ID보다 앞에 옴)
  public Address update(Long id, Long memberId, AddressUpdateRequest request)
  public Notification markAsRead(Long notificationId, Long memberId)
  ```

### 8.4 Enhanced Switch 문법 사용 의무화 수칙 (Java 14+ Modern Switch Rule)

- **원칙:** `switch` 구문 작성 시 기존 `case A: ... break;` 스타일 대신 Java 14+부터 표준 도입된 **Enhanced Switch 문법 (`switch (x) { case A -> ...; }` 또는 Switch Expression)**을 100% 사용하여 개발해야 합니다.
- **목적:**
  1. `break;` 누락으로 인한 의도치 않은 **fall-through 버그 발생을 근본적으로 차단**합니다.
  2. `return switch (x) { case A -> val; ... };` 형태의 표현식을 활용하여 가독성 및 표현력을 최상으로 유지합니다.
  3. **Enum 타입의 Switch Expression 시 불필요한 `default` 문 작성 금지**: Enum의 모든 상수가 커버된 경우 `default` 구문을 생략함으로써, 향후 신규 Enum 상수가 추가되었을 때 컴파일러가 이를 자동 감지(Compile-time Exhaustiveness Check)하도록 안전성을 극대화합니다.
- **적용 예시:**
  ```java
  // ⭕ 올바른 예시: Enhanced Switch Expression (Enum 전체 커버 시 default 생략)
  public MemberFieldValidateResponse validate(MemberFieldValidateRequest request) {
    return switch (request.getField()) {
      case EMAIL -> validateEmail(value);
      case PHONE_NUMBER -> validatePhoneNumber(value);
      case PASSWORD -> validatePassword(value);
    };
  }

  // ❌ 금지 예시: 전통적인 case A: ... break; 구문 및 불필요한 default 작성
  switch (request.getField()) {
    case EMAIL:
      return validateEmail(value);
      break;
    ...
  }
  ```

### 8.5 포맷팅 및 코드 스타일 규칙

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

- **테스트 메서드명:** 메서드명은 무조건 **`camelCase` 영문(English)**으로 작성합니다 (예: `registerVehicleShouldThrowExceptionWhenWeightIsZeroOrLess`, `createAddressSuccess`). 언더스코어(`_`)나 한글 메서드명 사용을 금지합니다.
- **테스트 시나리오 설명 (`@DisplayName`):** 테스트 케이스에 대한 상세한 한글 설명은 JUnit 5 `@DisplayName("한글 시나리오 설명")` 어노테이션을 필수 부여하여 작성합니다.

```java
@Test
@DisplayName("무게가 0 이하이면 InvalidWeightException을 던진다")
void registerVehicleShouldThrowExceptionWhenWeightIsZeroOrLess() {
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

## 10.1 객체 생성 규칙: 정적 팩토리 메서드 (Static Factory Method) 및 DTO 전달 수용

- **원칙:** Service, Controller, DTO, Entity 등 모든 계층에서 `new` 키워드나 외부 `Builder` 수동 조립을 금지하며, **객체 내부에 명확한 의미를 가진 정적 팩토리 메서드(Static Factory Method)를 작성하여 생성**합니다.
- **실용적 DTO 전달 허용:** 엔티티 생성 시 Request DTO의 파라미터를 개별적으로 일일이 나열하지 않고, **Request DTO 자체를 정적 팩토리 메서드의 인자로 전달받는 패턴(`Entity.from(request, ...)` 또는 `Entity.of(request, ...)` )을 적극 권장**합니다. 
- **이유:**
  1. Service 계층에서 파라미터 조립 코드를 제거하여 핵심 비즈니스 유스케이스 흐름을 극단적으로 단순화합니다.
  2. Request DTO에 필드가 추가되어도 Service 메서드 시그니처 수정 없이 Entity 팩토리 내부 수정만으로 대응 가능합니다.
  3. 같은 도메인 패키지 안에서의 참조이므로 레이어 아키텍처 오염을 일으키지 않습니다.
- **표준 명명 규칙:**
  1. `from(request)` / `from(id, request)`: Request DTO 기반으로 엔티티를 변환/생성할 때 사용 (예: `Address.from(memberId, request)`, `Member.from(request, encodedPassword)`)
  2. `of(request, extraField1, ...)`: Request DTO 및 추가 계산값(거리, 요금 등)을 수용하여 생성할 때 사용 (예: `DeliveryRequest.of(request, distanceKm, feePoint)`)
  3. `createDefault(...)` / `createEmpty(...)`: 초기 기본 상태(Status=AVAILABLE, Balance=0 등)를 자동 세팅하여 신규 객체를 생성할 때 사용 (예: `PointWallet.createEmpty(memberId)`)

---

## 10.2 코드 내 전체 패키지명(FQCN) 직접 작성 금지

- **원칙:** 코드 본문(클래스 선언, 메서드 시그니처, 변수 타입, 예외 생성 등)에 `com.example.shinhandelivery.xxx`와 같은 인라인 패키지 전체 경로(Fully-Qualified Class Name)를 직접 작성하는 것을 엄격히 금지합니다.
- **지침:**
  1. 모든 외부/타 패키지 클래스는 파일 상단의 `import` 구문으로 선언하여 사용합니다.
  2. 동일한 클래스명이 존재하여 충돌하는 특수한 경우를 제외하고는 코드 본문에 패키지명을 나열하지 않습니다.
  3. 이를 통해 코드 가독성을 확보하고 패키지 리팩토링 시 변경 요소를 최소화합니다.

---

## 10.3 엔티티 도메인 메서드: DTO 수용 및 `this` 반환 (Fluent Update)

- **원칙:** 엔티티의 상태 변경 메서드는 외부에서 필드를 개별로 주입받지 않고, **Request DTO 또는 의미 있는 도메인 단위 값을 통째로 수용**하며, `return this`를 통해 자기 자신을 반환합니다.
- **목적:**
  - Service 계층에서 인라인 체이닝 또는 단일 표현식으로 처리할 수 있어 코드 라인 수를 줄이고 가독성을 높입니다.
  - `How`(필드를 일일이 꺼내 대입)를 Entity 내부로 완전히 캡슐화합니다.
  - 반환값 사용은 선택 사항입니다 — `void`처럼 호출해도 JPA Dirty Checking은 정상 동작합니다.
- **명명 규칙:**
  - 단순 필드 일괄 수정: `updateBy(XxxUpdateRequest request)` 형식
  - 도메인 의미 있는 상태 전이: `pickUp()`, `complete(request)`, `cancel()` 등 도메인 어휘 사용
  - 단일 상태값 변경: `changeXxx(value)` 형식 (예: `changeRole(role)`, `changePassword(encoded)`)

```java
// Entity — updateBy + this 반환
public Address updateBy(AddressUpdateRequest request) {
    this.alias = request.getAlias();
    this.address = request.getAddress();
    this.detailAddress = request.getDetailAddress();
    this.pickupGuide = request.getPickupGuide();
    return this;
}

// Service — 단일 표현식으로 처리, 반환값 활용
return findAddressOrThrow(id, memberId).updateBy(request);

// Service — 반환값 미사용(void처럼), JPA Dirty Checking은 그대로 동작
findAddressOrThrow(id, memberId).updateBy(request);
```

---

## 10.4 불필요한 단발성 임시 변수 인라인화 (Inline Temporary Variable)

- **원칙:** 메서드 생성/조회/수정 흐름에서 1회성으로 생성되어 즉시 `return`되거나 단 1회만 파라미터로 전달되어 소비되는 불필요한 단발성 임시 변수는 선언을 생략하고 즉시 **인라인(Inline)** 처리합니다.
- **목적:**
  1. 단발성 중간 임시 변수 생성을 억제하여 변수 스코프 오염을 줄입니다.
  2. `return repository.save(Entity.from(request));` 처럼 객체 생성과 데이터 저장을 단일 선언적 표현식(Declarative Expression)으로 연결하여 코드 직관성과 읽기 편의성을 높입니다.

```java
// BAD: 1회만 참조되어 즉시 return되는 불필요한 단발성 임시 변수 선언
Address address = Address.from(memberId, request);
return addressRepository.save(address);

// GOOD: 단일 선언적 표현식으로 즉시 인라인 처리
return addressRepository.save(Address.from(memberId, request));
```

---

## 10.5 검증 책임 분리 규칙 (Validation Responsibility Separation Standard)

- **원칙:** 입력값 유효성 검증과 도메인 불변성 검증을 각 레이어의 본래 책임에 맞게 명확히 분리하며, Service 계층에 수동 필드 검증 코드가 흩어지는 것을 차단합니다.
- **목적:** Service 계층은 유스케이스 흐름 오케스트레이션에만 집중시키고, 입력 파라미터의 형태 검증은 DTO Bean Validation 어노테이션이, 도메인 생성 및 상태 변경 시의 비즈니스 불변 규칙 검증은 Entity 내부에서 전담하도록 아키텍처 결합도를 낮춥니다.
- **레이어별 검증 분리 표준:**
  1. **1차 입력값 검증 (DTO Layer):**
     - HTTP 요청 파라미터의 필수 여부, 범위, 형식 검증(예: 음수 방지, 필수값 등)은 **DTO 필드에 Bean Validation 어노테이션 (`@NotNull`, `@NotBlank`, `@DecimalMin`, `@Positive` 등)**을 선언하여 Controller 레벨 `@Valid`로 1차 처리합니다.
     - Service 계층에 `validateWeight(weight)`와 같은 필드 레벨 수동 `if` 검증 프라이빗 메서드를 작성하는 것을 금지합니다.
  2. **2차 도메인 불변성 검증 (Domain Entity Layer):**
     - 도메인 객체 생성 및 상태 변경 시 지켜져야 하는 핵심 불변성 규칙(Domain Invariants)은 **Entity의 정적 팩토리 메서드 (`Entity.of(...)`) 또는 도메인 비즈니스 메서드** 내부에서 검증하여 도메인 캡슐화를 사수합니다.
  3. **Service 계층의 역할 (Usecase Orchestration):**
     - Service 계층은 복수 도메인 서비스 조율, 도메인 생성 위임, 저장소(`Repository`) 호출, 이벤트 발행 등 유스케이스의 실행 흐름 오케스트레이션에만 집중합니다.

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

## 12.1 동시성 제어

고객 취소처럼 하나의 유스케이스에서 배송 상태와 여러 회원 지갑을 함께 변경할 때는 `DeliveryRequest → Matching → Vehicle → 고객 PointWallet → 배송원 PointWallet` 순서로 비관적 락을 획득하고 단일 트랜잭션으로 처리한다. 일부 자원만 사용하는 흐름도 자신이 사용하는 락을 이 순서와 반대로 획득하지 않는다. 상세 근거와 검증 기준은 [ADR-0004](docs/adr/0004-고객-취소-원자적-포인트-정산.md)를 따른다.

동시 요청으로 정확성이 깨지면 안 되는 자원(포인트 잔액, 차량 배정, 배송 매칭 등)은 **비관적 락(Pessimistic Lock)**으로 보호한다. 상세 원리·데드락 회피·테스트 작성법은 `docs/concurrency-control-guide.md`를 참고하고, 여기서는 코딩 전에 알아야 할 핵심만 정리한다.

- Repository에 `findByIdForUpdate`라는 이름으로 비관적 쓰기 락 조회 메서드를 추가한다.

  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select w from PointWallet w where w.id = :id")
  Optional<PointWallet> findByIdForUpdate(@Param("id") Long id);
  ```

- 값을 **변경**하는 Service 메서드(충전/차감, 배정 등)만 `findByIdForUpdate`를 쓴다. 단순 조회(GET)에는 락을 걸지 않고 기존 `findById`를 그대로 쓴다.
- 한 트랜잭션에서 여러 리소스에 락을 걸어야 하면(예: 배송 요청 → 차량) **항상 같은 순서**로 락을 획득해 데드락을 방지한다.
- 새로운 동시성 민감 로직을 추가하면 `ExecutorService` + `CountDownLatch`(스레드 풀 크기는 반드시 동시 요청 수 이상) 패턴으로 최소 100개 동시 요청 테스트를 작성하고, 최종 데이터 정합성(잔액이 음수가 아닌지, 유실 없이 정확한지)을 assert한다.

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

스키마 변경은 Hibernate가 아니라 **Flyway**로만 관리한다 (`spring.jpa.hibernate.ddl-auto: validate`). 상세 작동 원리·IDE 활용법은 `docs/architecture/Flyway-마이그레이션-가이드.md`를 참고하고, 여기서는 커밋 전 반드시 지켜야 할 핵심만 정리한다.

- **파일 위치/네이밍**: `src/main/resources/db/migration/V<버전번호>__<설명>.sql` (언더스코어 2개 필수). 예: `V7__add_location_to_vehicle.sql`
- **이미 반영된 마이그레이션 파일은 절대 수정하지 않는다.** 로컬/서버 DB에 한 번이라도 적용된 파일을 고치면 Checksum 불일치로 다음 구동이 실패한다. 수정이 필요하면 버전을 올린 새 파일을 추가한다.
- Entity에 필드를 추가/변경했다면, **같은 PR 안에 대응하는 마이그레이션 파일을 함께 커밋**한다. Entity만 바꾸고 마이그레이션을 빠뜨리면 `ddl-auto: validate`에 의해 애플리케이션이 기동 실패한다.
- 여러 테이블을 함께 바꿔야 하면 파일 하나에 `ALTER TABLE` 여러 개를 순서대로 넣어도 되고, 논리적으로 성격이 다르면 파일을 나눠도 된다 — 팀 판단에 맡긴다.
- **Entity를 새로 추가하거나 기존 Entity의 필드·FK·연관관계를 변경했다면, 같은 PR 안에 프로젝트 ERD 문서(`docs/architecture/ERD-데이터베이스-연관관계도.md`)도 함께 갱신한다.** ERD 문서는 전체 테이블의 컬럼과 FK 기반 연관관계도를 한눈에 파악할 수 있는 단일 원본(SSOT) 문서로 유지하며, 코드(Entity)와 ERD가 어긋나지 않도록 항상 동기화한다.

---

## 16. Git 커밋 / 브랜치 / PR 컨벤션

브랜치 전략·PR 규칙의 전체 내용은 `docs/harness/Git-Flow-및-커밋-컨벤션.md`가 기준 문서이며, 여기서는 코딩 전에 알아야 할 핵심만 요약한다.

### 16.1 브랜치 전략

`main` 단일 브랜치 전략을 사용한다(과거 `develop` 통합 브랜치를 시도했으나 실제 PR이 계속 `main`에 직접 병합되며 방치되어, 이슈 #200을 계기로 `main` 단일 브랜치 전략으로 공식 전환했다).

| 브랜치 유형 | 용도 | 이름 규칙 | 대상 상위 브랜치 |
|---|---|---|---|
| `main` | 배포 가능한 가장 안정적인 브랜치이자 모든 작업의 기준 브랜치 | `main` | - |
| `feature` | 신규 기능/버그 수정 작업 브랜치 | `feat/도메인-내용` (예: `feat/delivery-fee-calculation`) | `main` |
| `hotfix` | 배포된 `main`의 긴급 장애 패치 | `hotfix/<이슈번호>-<요약>` | `main` |

- 작업 시작 전 `main`을 최신화한 뒤 그 위에서 브랜치를 분기한다.
- 머지 후 로컬 작업 브랜치는 삭제한다.

### 16.2 커밋 메시지

- 최소 형식: `type: 설명` (`feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `style`)
  - 예: `feat: 배송 요금 산정 로직 추가`
- 변경 배경까지 남기고 싶으면 `docs/harness/Git-Flow-및-커밋-컨벤션.md`의 확장 형식(`type(scope): subject` + 본문 + 푸터)을 써도 된다. 최소 형식과 상충하지 않는, 필요할 때만 쓰는 선택 사항이다.
- 큰 단위로 한 번에 커밋하지 않고, DTO/Entity/Service/Controller/Test 등 레이어(서브태스크) 단위로 작업이 완성될 때마다 커밋 프리뷰를 제시하고 개발자가 `commit`(또는 `/commit`) 명령을 입력하면 `./scripts/verify.sh` 검증 통과 후 즉시 마이크로 커밋을 집행한다 (`AGENTS.md` Principle 9, `docs/onboarding/AI-기반-페어-프로그래밍-가이드.md` 참고).

### 16.3 PR 규칙

- 리뷰어를 최소 1명 이상 지정한다.
- 연관 이슈가 있으면 본문에 명시해 자동으로 닫히게 한다 (예: `Resolves: #45`).
- `.github/pull_request_template.md`의 항목(요약 / 주요 변경 사항 / 리뷰 포인트 / 스크린샷·테스트 결과)을 빠짐없이 채운다 — UI 작업은 스크린샷, API 작업은 테스트 로그나 호출 결과를 첨부한다.
- PR 단위는 도메인 하나 + 기능 하나 정도로 작게 유지하며, `Squash and Merge`로 병합한다.
- 신규 기술 도입 및 핵심 아키텍처 결정 시 `docs/adr/` 규격에 따라 **ADR(Architecture Decision Record)**을 100% 필수 작성하며, PR 제출 시 `docs/harness/PR-리뷰어-3분-족보-가이드.md` 양식에 따라 **[리뷰어 3분 족보 가이드]** 및 **[Files changed 탭 핀포인트 인라인 댓글]**을 필수 작성/부착한다.

### 16.4 GitHub 이슈 규격

- `[카테고리] 요약 설명` 형식 (`[Feature]`, `[Security]`, `[Concurrency]`, `[Testing]`, `[Observability]`, `[Ops]`, `[Bug]`, `[Docs]`)
  - 예시: `[Security] Spring Security 및 JWT 기반 REST API 인증/인가 체계 구축`
- `.github/ISSUE_TEMPLATE/` 템플릿(개요, 세부 요구사항, 완료 정의) 사용을 의무화한다.

---

## 17. 체크리스트 (PR 리뷰 시 확인)

- [ ] Controller가 Entity를 직접 반환하지 않고 DTO로 변환하는가?
- [ ] 비즈니스 로직이 Controller가 아니라 Service에 있는가?
- [ ] 예상 가능한 실패가 커스텀 예외로 표현되고, `GlobalExceptionHandler`에 매핑이 추가되어 있는가?
- [ ] `@Transactional`이 Service 계층에만 붙어 있는가?
- [ ] 잔액/배정처럼 동시 요청에 취약한 로직을 변경했다면 `findByIdForUpdate`(비관적 락)와 동시성 테스트(`docs/architecture/동시성-제어-가이드.md`)를 추가했는가?
- [ ] `@Autowired` 필드 주입이 아니라 생성자 주입을 쓰는가?
- [ ] Repository가 Entity 1개당 1개씩 대응되는가?
- [ ] Service에서 Repository/Service 의존 없는 순수 계산·변환 로직을 새로 추출했다면 `helper` 서브패키지의 상태 없는 `@Component`로 분리했는가? (§5.1)
- [ ] 다른 도메인의 Repository/Entity를 직접 참조하지 않고, 필요하면 그 도메인의 Service를 거쳤는가? (§4의 읽기 전용 `@ManyToOne` 연관관계는 예외)
- [ ] 다른 도메인 객체 목록을 조회할 때 `list.get(0)` 등 인덱스 수동 조작 대신 해당 도메인 Service/Repository의 캡슐화된 전용 조회 메서드를 활용했는가? (§2)
- [ ] Enum 필드에 `@Enumerated(EnumType.STRING)`을 썼는가? (`ORDINAL` 금지)
- [ ] 새로 추가한 서비스 로직에 단위 테스트가 있는가? (영문 메서드명 + `@DisplayName` 사용)
- [ ] ArchUnit 테스트(레이어 의존성 규칙)가 깨지지 않는가?
- [ ] Spotless 포맷팅 검사를 통과하는가?
- [ ] Entity 필드를 추가/변경했다면 대응하는 Flyway 마이그레이션 파일을 새로 추가했는가? (기존 마이그레이션 파일을 수정하지 않았는가?)
- [ ] 다른 도메인을 참조하는 Entity에 FK 필드(`Long xxxId`)가 포함되어 있는가?
- [ ] Entity를 추가/변경했다면 프로젝트 ERD 문서(`docs/architecture/ERD-데이터베이스-연관관계도.md`)의 컬럼·연관관계도도 함께 갱신했는가?
- [ ] `main`을 대상으로 브랜치를 분기·PR 했는가?
- [ ] 리뷰어를 지정하고, PR 템플릿의 요약/변경사항/리뷰 포인트/테스트 결과를 모두 작성했는가?
- [ ] UI 개발 시 `/css/design-system.css` 토큰 및 `templates/fragments/components.html` Thymeleaf 프래그먼트를 100% 준수하였는가?
- [ ] 모든 HTML 화면 파일이 `src/main/resources/templates/` 하위에 위치하며, Spring MVC `@Controller` 및 Thymeleaf SSR(`Model` 바인딩 & `th:*` 태그) 기반으로 개발되었는가?
- [ ] 신규 화면이나 기능 추가 시 `docs/architecture/전체-유저-플로우-가이드.md`에 E2E 유저 플로우 및 화면 매핑을 필수 업데이트하였는가?

---

## 18. 프론트엔드 & UI 개발 컨벤션 (Design System & Thymeleaf Rules)

1. **공통 디자인 시스템 100% 준수 (Design Token First):**
   - 프로젝트 내 모든 HTML 및 Thymeleaf UI 개발 시 임의의 인라인 CSS 작성이나 별도의 커스텀 색상 지정을 엄격히 금지합니다.
   - 모든 화면은 반드시 `/css/design-system.css`를 포함하고, 정의된 CSS 변수(`var(--color-blue-100)`, `var(--color-grey-100)` 등)와 공통 유틸리티 클래스(`.btn-primary`, `.input-field`, `.card-box`, `.badge` 등)를 사용해야 합니다.
2. **Thymeleaf Component Fragment 우선 사용:**
   - 반복되는 UI 요소(버튼, 입력창, 카드, 배지 등)는 `src/main/resources/templates/fragments/components.html`에 정의된 Thymeleaf Fragment를 100% 활용하여 구축해야 합니다.
3. **표준 기술 스택 준수 (Thymeleaf Core Stack):**
   - 본 프로젝트의 프론트엔드는 `HTML5 + Vanilla CSS + Vanilla JS + Thymeleaf` 기반의 서버 사이드 템플릿 아키텍처를 프로젝트 표준 스택으로 채택하여 개발합니다.
4. **Thymeleaf 템플릿 디렉토리 표준 및 static HTML 배치 금지 (Templates Directory Rule):**
   - 모든 UI 화면 HTML 파일(`*.html`)은 반드시 `src/main/resources/templates/` 하위에 위치시켜야 합니다.
   - `src/main/resources/static/` 하위에는 CSS, JS, 이미지 등 순수 정적 자산(Static Assets)만 허용하며, HTML 화면 파일을 `static/`에 배치하고 JavaScript `fetch()` 비동기 통신으로 화면 껍데기를 조립하는 CSR 방식 개발을 엄격히 금지합니다.
5. **서버 사이드 렌더링(SSR) 및 Web Controller 구축 필수 (SSR First & Web Controller Rule):**
   - 공지사항, 카테고리 목록, 홈 화면 등 초기 데이터 조회가 필요한 모든 화면은 백엔드 Spring MVC `@Controller` (Web Controller)를 신설하고 `Model`에 DTO를 추가하여 Thymeleaf SSR(`th:each`, `th:text`, `th:if`, `th:replace`) 형태로 완성된 HTML을 서버에서 렌더링하여 응답해야 합니다.
   - 초기 진입 시 FOUC(Flash of Unstyled Content)나 스켈레톤 지연 없이 완성된 DOM을 사용자에게 즉시 노출해야 합니다.
6. **Thymeleaf 표현식 Null-Safety 수칙:**
   - 템플릿 내 날짜/시간 포맷팅이나 데이터 바인딩 시 `th:text="${notice.createdAt != null ? #temporals.format(notice.createdAt, 'yyyy.MM.dd') : ''}"`와 같이 방어적 Null-Safety 표현식을 적용해야 합니다.
7. **문서 및 라이브 가이드 세트 최신화:**
   - 신규 UI 요소 추가나 디자인 스펙 수정 시 반드시 `docs/architecture/UI-공통-디자인-시스템.md` 가이드북과 `http://localhost:8080/style-guide.html` 라이브 가이드 페이지를 소스 코드와 세트로 최신화해야 합니다.
8. **전체 유저 플로우 가이드 동기화 필수:**
   - 신규 화면(`*.html`)이나 새로운 기능/유스케이스가 추가될 때, 개발자는 반드시 단일 원본 문서인 `docs/architecture/전체-유저-플로우-가이드.md`에 해당 화면과 유저 여정, 화면-서비스 매핑표를 필수로 동기화 반영해야 합니다.
9. **주소 퀵 칩 표준:**
   - 저장 주소 선택 UI는 공통 `addressQuickChipSelector` 프래그먼트와 디자인 시스템 클래스를 사용하며, 상세 동작은 `docs/design/address-quick-chips-design.md`를 단일 원본으로 참조합니다.
10. **인라인 style/script 미분리 금지 및 정적 파일 추출 규격 (Static Asset Extraction Rule):**
   - Thymeleaf HTML 템플릿(`src/main/resources/templates/*.html`) 내부에 인라인 `<style>` 및 `<script>` 블록을 대용량으로 직접 작성하는 행위를 엄격히 금지합니다.
   - 페이지 전용 스타일과 스크립트는 반드시 `src/main/resources/static/css/pages/{page-name}.css` 및 `src/main/resources/static/js/pages/{page-name}.js` 전용 자산으로 분리하여 HTTP 브라우저 캐싱(`Cache-Control`, `ETag`) 및 관심사 분리(SoC)를 달성해야 합니다.
   - HTML 템플릿 내부에서는 Thymeleaf 표준 URL 바인딩 문법인 `<link rel="stylesheet" th:href="@{/css/pages/{page-name}.css}">` 및 `<script th:src="@{/js/pages/{page-name}.js}"></script>`를 사용하여 연동합니다.
11. **CSS 중복 제거 및 3단계 모듈화 수칙 (CSS Deduplication & 3-Tier Rule):**
   - 모바일 셸 레이아웃(`.page`), 뒤로가기 버튼(`.back-button`), 탭 바(`.tabs`), 상태 UI(`.empty-state`, `.loading-state`) 등 2개 이상의 화면에서 중복 등장하는 CSS 규칙은 페이지 전용 CSS 파일에 중복 정의하지 않고 `src/main/resources/static/css/design-system.css` 공통 클래스로 전격 통합 모듈화해야 합니다.
   - 페이지 전용 CSS (`static/css/pages/*.css`)에는 해당 화면 고유의 레이아웃 규칙만 경량화하여 작성합니다.
12. **공통 JavaScript 유틸리티 모듈화 규격 (JS Utility Module Rule):**
   - 인증 토큰 추출(`authHeader()`), HTML XSS 방어(`escapeHtml()`), 날짜/상대시간 포맷팅(`formatDate()`, `formatRelativeTime()`), 토스트 UI(`showToast()`) 등 복수 스크립트에 중복 작성되던 함수는 개별 스크립트에 재정의하지 않고 `src/main/resources/static/js/utils/` (`auth.js`, `format.js`, `ui.js`) 공통 모듈로 통합해야 합니다.
   - 각 HTML 템플릿에서는 필요한 공통 유틸리티 모듈을 스크립트 상단에 `<script src="/js/utils/*.js"></script>` 형태로 로드하여 중복 코드를 제거하고 재사용성을 100% 사수해야 합니다.
13. **비즈니스 유효성 검사 중앙집중화 및 백엔드 단일 원본(SSOT) 수칙 (BE-Driven Validation & Debounce Rule):**
   - 이메일 중복 체크, 전화번호 포맷, 비밀번호 규칙, 요금 계산 등 모든 핵심 비즈니스 유효성 검증(Business Validation) 로직은 백엔드(BE)를 **단일 원본(SSOT: Single Source of Truth)**으로 삼아 중앙집중식으로 처리해야 합니다.
   - 프론트엔드(FE)에 비즈니스 규칙 정규식이나 검증 로직을 중복 구현하는 행위를 엄격히 금지하며, FE는 숫자 입력 이외 제한/실시간 자동 하이픈 마스킹 등 UX 조작에만 집중합니다.
   - 실시간 필드 검증이 필요한 폼은 백엔드 필드 검증 API(`POST /api/v1/members/validate`)와 300ms 디바운스(Debounce) 유틸리티(`format.js` 내 `debounce`)를 활용하여 비동기로 호출하고 백엔드가 응답한 메시지 및 결과를 화면에 렌더링해야 합니다.
