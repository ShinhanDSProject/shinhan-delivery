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

- **`ErrorResponse` DTO**: `status`, `code`, `message`, `timestamp`, `errors`(유효성 검사 세부 내역) 필드로 구성되며, `@JsonInclude(NON_EMPTY)`를 적용해 불필요한 null/빈 배열 출력을 직렬화에서 생략한다.
- **`GlobalExceptionHandler`**: `@RestControllerAdvice` 기반으로 `BusinessException`, `MethodArgumentNotValidException`(@Valid 실패), `HttpMessageNotReadableException`(JSON 파싱 실패), `HttpRequestMethodNotSupportedException`, 및 `Exception`(500 스택트레이스 은폐)을 수집 포착한다.

### 6.3 예외 → HTTP 상태코드 & 에러코드 매핑 기준

| 예외 유형 | HTTP 상태 | ErrorCode 예시 | 응답 메시지 예시 |
|---|---|---|---|
| 입력 값 검증/포맷 오류 | `400 Bad Request` | `INVALID_INPUT_VALUE` (`C001`) | 유효하지 않은 입력값입니다. / 요청 본문의 JSON 형식이 올바르지 않습니다. |
| 리소스 미존재 | `404 Not Found` | `MEMBER_NOT_FOUND` (`M001`), `VEHICLE_NOT_FOUND` (`V001`) | 존재하지 않는 회원입니다. |
| 중복/충돌 | `409 Conflict` | `DUPLICATE_EMAIL` (`M002`) | 이미 가입된 이메일 주소입니다. |
| 잔액 부족 등 비즈니스 규칙 위반 | `400 Bad Request` | `INSUFFICIENT_BALANCE` (`P002`) | 포인트 잔액이 부족합니다. |
| 미처 포착하지 못한 서버 내부 오류 | `500 Internal Server Error` | `INTERNAL_SERVER_ERROR` (`C003`) | 서버 내부 오류가 발생했습니다. (스택트레이스 서버 로깅 후 은폐) |

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

## 15. Git 커밋 / 이슈 / 브랜치 컨벤션

- **GitHub 이슈 규격:** `[카테고리] 요약 설명` 형식 (`[Feature]`, `[Security]`, `[Concurrency]`, `[Testing]`, `[Observability]`, `[Ops]`, `[Bug]`, `[Docs]`)
  - 예시: `[Security] Spring Security 및 JWT 기반 REST API 인증/인가 체계 구축`
  - `.github/ISSUE_TEMPLATE/` 템플릿(개요, 세부 요구사항, 완료 정의) 사용 의무화
- **커밋 메시지:** `type: 설명` 형식 (Conventional Commits 기반)
  - `feat`, `fix`, `refactor`, `test`, `docs`, `chore`
  - 예시: `feat: 배송 요금 산정 로직 추가`
- **브랜치명:** `type/도메인-내용` (예: `feat/delivery-fee-calculation`)
- **PR 단위:** 도메인 하나 + 기능 하나 정도로 작게 유지하며 `Squash and Merge` 병합. PR 제출 시 리뷰어의 검토 소요 시간을 단축하기 위해 `docs/pr-review-guide.md` 양식에 따라 **[리뷰어 3분 족보 가이드]** 및 **[Files changed 탭 핀포인트 인라인 댓글]**을 100% 필수 작성/부착한다.

---

## 16. 체크리스트 (PR 리뷰 시 확인)

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