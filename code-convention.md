# 코드 컨벤션 (Java / DDD + FP + Railway-oriented Programming)

> 대상 스코프(MVP): 회원·권한 / 운송수단·자원 / 예약·매칭 / 결제·정산
> 기준: Java 17+ (record, sealed interface, pattern matching switch 사용)

---

## 1. 기본 원칙

1. **도메인 주도 개발(DDD)**: 코드 구조는 기술 레이어가 아니라 비즈니스 도메인 개념을 중심으로 나눈다.
2. **함수형 프로그래밍(FP)**: 도메인 로직은 부수효과 없는 순수 함수 / 불변 객체로 표현한다.
3. **Railway 지향 프로그래밍**: 예상 가능한 실패는 예외(throw)가 아니라 `Result` 타입으로 표현하고 체이닝한다.

세 원칙은 다음 순서로 결합된다.

```
DDD로 도메인 경계·모델 정의
   → FP로 도메인 로직을 순수 함수로 구현
      → Railway로 그 함수들의 실패를 Result 타입으로 명시/체이닝
```

---

## 2. 패키지 구조

도메인 단위(Bounded Context)로 최상위 패키지를 나눈다. MVP 스코프 기준 4개 도메인.

```
com.company.delivery
├── domain
│   ├── member/            // 회원·권한
│   │   ├── Member.java            // Aggregate Root (abstract)
│   │   ├── Customer.java
│   │   ├── Courier.java
│   │   ├── Admin.java
│   │   ├── MemberId.java          // Value Object (record)
│   │   ├── Email.java             // Value Object (record)
│   │   └── MemberRepository.java  // interface
│   ├── vehicle/            // 운송수단·자원
│   │   ├── Vehicle.java            // Aggregate Root
│   │   ├── VehicleType.java        // enum (DRONE, MOTORCYCLE, CAR)
│   │   ├── MaxWeight.java          // Value Object
│   │   ├── MaxDistance.java        // Value Object
│   │   └── VehicleRepository.java
│   ├── delivery/            // 예약·매칭
│   │   ├── DeliveryRequest.java           // Aggregate Root
│   │   ├── DeliveryRequestId.java         // Value Object (record)
│   │   ├── DeliveryRequestRepository.java // interface
│   │   ├── Matching.java                  // Aggregate Root (독립적인 생성/동시성 경계)
│   │   ├── MatchingRepository.java        // interface
│   │   ├── Location.java                  // Value Object
│   │   ├── Route.java                     // Value Object (출발지-도착지)
│   │   └── PackageInfo.java               // Value Object (배송 물품 정보)
│   └── payment/            // 결제·정산
│       ├── Fee.java                // Value Object
│       ├── PointWallet.java        // Aggregate Root
│       └── PaymentRepository.java
├── application
│   ├── member/RegisterMemberUseCase.java
│   ├── vehicle/RegisterVehicleUseCase.java
│   ├── delivery/RequestDeliveryUseCase.java
│   ├── delivery/MatchCourierUseCase.java
│   ├── delivery/DeliveryCreateRequest.java   // 요청 DTO (11번 참고)
│   ├── delivery/DeliveryCreateResponse.java  // 응답 DTO (10번 참고). UseCase와 같은 패키지에 둔다
│   └── payment/ChargeFeeUseCase.java
├── infrastructure
│   └── persistence/
│       ├── MemberRepositoryImpl.java
│       ├── VehicleRepositoryImpl.java
│       ├── DeliveryRequestRepositoryImpl.java
│       ├── MatchingRepositoryImpl.java
│       └── PaymentRepositoryImpl.java
├── presentation
│   └── (Controller 등 — 도메인 타입을 직접 노출하지 않고 DTO로 변환)
└── common
    ├── result/
    │   └── Result.java              // Railway 타입
    ├── error/
    │   └── DomainError.java         // sealed interface
    └── http/
        └── DomainErrorHttpMapper.java  // DomainError → HTTP 상태코드 매핑 (11번 참고)
```

**규칙**
- `domain` 패키지는 Spring 등 프레임워크 의존성을 가지지 않는다. (`@Entity`, `@Service` 등 금지)
- Aggregate는 다른 Aggregate를 참조할 때 객체 참조가 아니라 `~Id` 값으로만 참조한다.
  - 예: `DeliveryRequest`는 `Courier` 전체가 아니라 `MemberId courierId`만 가진다.
- **Repository는 Aggregate Root 1개당 1개씩 만든다.** 한 패키지(Bounded Context) 안에 Aggregate Root가 여러 개 있다면(예: `delivery` 패키지의 `DeliveryRequest`, `Matching`) Repository도 그 개수만큼 나눈다. 패키지 이름을 따서 Repository 하나로 뭉치지 않는다.
  - Aggregate Root 판단 기준: 독립적으로 생성/조회되는가? 자기만의 트랜잭션·동시성 경계가 필요한가? 둘 중 하나라도 해당하면 별도 Aggregate Root + 별도 Repository로 분리한다.
  - 반대로 항상 상위 Aggregate와 함께 생성·조회되고 단독으로 조회될 일이 없다면, 별도 Aggregate가 아니라 상위 Aggregate 내부의 하위 Entity로 편입하고 Repository도 만들지 않는다.

---

## 3. 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
|---|---|---|
| Aggregate Root / Entity | 명사, 도메인 용어 그대로 | `DeliveryRequest`, `Vehicle` |
| Entity의 getter | `get` 접두사 사용 (일반 class이므로 record 스타일 아님) | `getStatus()`, `getId()` |
| Value Object | 명사, `record` | `MaxWeight`, `Location`, `Fee` |
| Repository 인터페이스 | `~Repository` | `VehicleRepository` |
| UseCase (Application) | 동사+명사+`UseCase` | `MatchCourierUseCase` |
| 도메인 순수 함수 | 동사로 시작, 부수효과 없음을 암시 | `calculateFee`, `validateWeight` |
| 부수효과가 있는 메서드 | 명확히 드러나는 동사 | `save`, `notify`, `charge` |
| 실패를 반환하는 함수 | 반환 타입에 `Result<S, F>` 명시 | `Result<Vehicle, DomainError> register(...)` |
| Domain 에러 | `sealed interface DomainError`의 하위 `record` | `DomainError.InvalidWeight` |

---

## 4. Value Object 컨벤션 (불변성 + 유효성 검증)

- 생성자를 직접 노출하지 않고 **정적 팩토리 메서드(`of`)로만 생성**, 실패는 `Result`로 반환한다.
- `record`를 사용해 불변성을 보장한다.

```java
public record MaxWeight(double kg) {

    // canonical constructor를 private으로 선언해서 `new MaxWeight(...)`를
    // 클래스 외부에서 직접 호출하지 못하게 막는다. 생성은 반드시 of()를 통해서만.
    private MaxWeight {}

    public static Result<MaxWeight, DomainError> of(double kg) {
        if (kg <= 0) {
            return Result.failure(new DomainError.InvalidWeight(kg));
        }
        return Result.success(new MaxWeight(kg));
    }

    public boolean canCarry(double packageWeightKg) {
        return packageWeightKg <= kg;
    }
}
```

> `private MaxWeight {}` 없이 `record MaxWeight(double kg)`만 쓰면 canonical constructor가 자동으로 `public`이 되어, "정적 팩토리로만 생성한다"는 규칙이 코드로 강제되지 않는다. 반드시 compact constructor를 `private`으로 선언할 것.

> compact constructor(`public MaxWeight { ... }`)에서 `throw`하는 방식은 **사용하지 않는다.**
> 도메인/애플리케이션 레이어에서는 실패를 항상 `Result`로 표현한다.

---

## 5. 함수형 프로그래밍 규칙

1. 필드 재할당(`setter`) 금지. 상태 변경이 필요하면 새 객체를 반환한다.
2. 컬렉션은 불변으로 다룬다. `List.of()`, `Map.of()`, 또는 반환 직전 `List.copyOf()`.
3. 도메인 계산 로직은 `static` 순수 함수 또는 불변 객체의 인스턴스 메서드로 작성한다.
4. 부수효과(DB 저장, 알림 발송, 외부 API 호출)는 반드시 `application` 또는 `infrastructure` 레이어에만 존재한다. `domain` 레이어 함수는 순수해야 한다.
5. Lombok 사용 시 `@Value`(불변) 계열만 허용, `@Data`(가변, setter 자동생성)는 도메인 객체에 사용하지 않는다.

**예시 (1번 — 새 객체 반환)**

```java
// Bad
vehicle.setStatus(SUSPENDED);

// Good
Vehicle suspended = vehicle.suspend();
```

**예시 (3번 — 순수 함수)**

```java
public static Fee calculateFee(double distanceKm, double weightKg) {
    double base = 3000;
    double extraDistance = Math.max(0, distanceKm - 3) * 500;
    double extraWeight = Math.max(0, weightKg - 5) * 300;
    return new Fee(base + extraDistance + extraWeight);
}
```

---

## 6. Railway 지향 프로그래밍 (Result 타입)

### 6.1 공통 타입

```java
public sealed interface Result<S, F> {

    record Success<S, F>(S value) implements Result<S, F> {}
    record Failure<S, F>(F error) implements Result<S, F> {}

    static <S, F> Result<S, F> success(S value) {
        return new Success<>(value);
    }

    static <S, F> Result<S, F> failure(F error) {
        return new Failure<>(error);
    }

    default <T> Result<T, F> map(Function<S, T> f) {
        if (this instanceof Success<S, F> success) {
            return Result.success(f.apply(success.value()));
        }
        // sealed interface has only Success/Failure, so this cast is safe
        return Result.failure(((Failure<S, F>) this).error());
    }

    default <T> Result<T, F> flatMap(Function<S, Result<T, F>> f) {
        if (this instanceof Success<S, F> success) {
            return f.apply(success.value());
        }
        // sealed interface has only Success/Failure, so this cast is safe
        return Result.failure(((Failure<S, F>) this).error());
    }

    default boolean isSuccess() {
        return this instanceof Success<?, ?>;
    }
}
```

> `instanceof Success<S, F>`(바인딩 변수 없이, 타입 파라미터를 그대로 씀)는 컴파일 에러다. 타입 소거 때문에 런타임에 `S`, `F`를 검사할 수 없어서, 바인딩 변수가 없는 일반 `instanceof`는 파라미터화된 타입을 허용하지 않는다. 반드시 `Success<?, ?>`(unbounded wildcard)로 쓸 것.

> **switch 패턴 매칭(`switch (this) { case Success<S, F> s -> ... }`)은 이 프로젝트에서 쓰지 않는다.** 이 문법은 Java 21에서야 정식 기능이 되었고, 이 프로젝트가 기준으로 하는 Java 17에서는 preview 기능이라 `--enable-preview` 없이는 컴파일되지 않는다. `Result`처럼 sealed 타입을 분기할 때는 항상 `instanceof` 패턴 매칭(Java 16+ 정식 기능)을 쓴다. 마지막 분기는 컴파일러가 exhaustiveness를 검증해주지 못하므로 sealed 타입의 나머지 하위 타입으로 캐스팅한다(위 예시 참고).

### 6.2 도메인 에러 타입

도메인별로 `DomainError`를 계층화한다.

```java
public sealed interface DomainError {
    // member
    record DuplicateMember(String email) implements DomainError {}
    record MemberNotFound(MemberId id) implements DomainError {}

    // vehicle
    record InvalidWeight(double kg) implements DomainError {}
    record OverMaxDistance(double km) implements DomainError {}

    // delivery
    record NoAvailableCourier(Location from) implements DomainError {}
    record AlreadyMatched(DeliveryRequestId id) implements DomainError {}

    // payment
    record InsufficientPoint(long required, long balance) implements DomainError {}
}
```

### 6.3 체이닝 예시 — 배송 요청 생성

```java
public Result<DeliveryRequest, DomainError> requestDelivery(DeliveryInput input) {
    return Location.of(input.from())
        .flatMap(from -> Location.of(input.to())
            .map(to -> new Route(from, to)))
        .flatMap(route -> MaxWeight.of(input.weightKg())
            .map(weight -> new PackageInfo(route, weight)))
        .flatMap(pkg -> DeliveryRequest.create(input.customerId(), pkg));
}
```

### 6.4 레이어별 규칙

| 레이어 | 실패 처리 방식 |
|---|---|
| `domain`, `application` | 예상 가능한 실패는 항상 `Result` 반환. `throw` 금지 |
| `infrastructure` | DB 연결 실패, 외부 API 타임아웃 등 진짜 예외 상황만 unchecked exception으로 `throw` |
| `presentation` (Controller) | `Result`를 받아 `Success`/`Failure`를 HTTP 응답(200 / 4xx)으로 변환하는 지점. 여기서만 `Result` → 예외/응답 변환 허용 |

---

## 7. 적용 예시 요약 (도메인별)

| 도메인 | Aggregate Root | Repository | 주요 Value Object | 대표 DomainError |
|---|---|---|---|---|
| 회원·권한 | `Member` (`Customer`/`Courier`/`Admin`) | `MemberRepository` | `MemberId`, `Email` | `DuplicateMember` |
| 운송수단·자원 | `Vehicle` | `VehicleRepository` | `MaxWeight`, `MaxDistance` | `InvalidWeight`, `OverMaxDistance` |
| 예약·매칭 | `DeliveryRequest` | `DeliveryRequestRepository` | `Route`, `PackageInfo` | `NoAvailableCourier` |
| 예약·매칭 | `Matching` | `MatchingRepository` | - | `AlreadyMatched` (선착순 경쟁 실패) |
| 결제·정산 | `PointWallet` | `PaymentRepository` | `Fee` | `InsufficientPoint` |

> `delivery` 패키지처럼 한 Bounded Context 안에 Aggregate Root가 여러 개면, Repository도 행마다 별도로 대응시킨다. Aggregate Root 하나 = Repository 하나가 원칙.

---

## 8. 포맷팅 규칙

- 들여쓰기: 스페이스 2칸 (탭 금지)
- 한 줄 최대 길이: 100자
- import 순서: `java.*` → `javax.*` → 외부 라이브러리 → `com.company.*` (그룹 사이 빈 줄 1개), wildcard import(`import java.util.*`) 금지
- 도구로 강제: **Spotless + google-java-format**
  - 이 값들은 google-java-format이 강제하는 고정값이며, 별도로 설정 가능한 옵션이 아니다. (4칸/120자 등으로 바꾸려면 도구 자체를 palantir-java-format 등으로 교체해야 함)
  - `./gradlew spotlessCheck` 를 CI에 포함, 로컬에는 pre-commit hook으로 `spotlessApply` 실행
  - IntelliJ 사용 시 **google-java-format 플러그인**을 설치하면 IDE의 자동 들여쓰기 자체가 이 규칙을 따르게 되어, 평소 타이핑만으로도 규칙에 맞는 코드가 작성된다. IDE 로컬 설정(Code Style)에만 의존하면 팀원마다 스타일이 어긋날 수 있으므로, 최종적으로는 항상 `spotlessCheck`(CI)가 기준이 된다.
- 어노테이션은 필드/메서드 선언 바로 위 줄에, 한 줄에 하나씩

---

## 9. 테스트 컨벤션

- **순수 함수(도메인 로직)는 반드시 단위 테스트를 작성한다.** 부수효과가 없으므로 mocking 없이 입력→출력만 검증.
- 테스트 메서드명: `given_when_then` 스타일 (한글 허용)

```java
@Test
void 무게가_0보다_작으면_InvalidWeight_에러를_반환한다() {
    Result<MaxWeight, DomainError> result = MaxWeight.of(-1);
    assertThat(result).isInstanceOf(Result.Failure.class);
}
```

- `Result` 타입 검증은 `Success`/`Failure` 여부 + 내부 값/에러 타입까지 함께 확인한다 (단순 null 체크 금지).
- 레이어별 테스트 전략:

| 레이어 | 테스트 종류 | 도구 |
|---|---|---|
| `domain` | 단위 테스트 (mock 없음) | JUnit5 + AssertJ |
| `application` | 단위 테스트 (Repository는 mock/fake) | JUnit5 + Mockito |
| `infrastructure` | 통합 테스트 (실제 DB) | `@DataJpaTest` + Testcontainers |
| `presentation` | API 테스트 (HTTP 요청/응답 검증) | `@SpringBootTest` + MockMvc |

- 테스트 커버리지 강제 라인은 팀 논의 후 확정 (우선 `domain` 레이어 80% 이상을 제안).

---

## 10. DTO ↔ 도메인 변환 규칙

- 도메인 객체(`record`, Aggregate)는 `presentation` 레이어 밖으로 노출하지 않는다.
- 변환은 **Application(UseCase) 레이어**에서 수행한다. Controller는 DTO만 다룬다.
- 변환 방식: 수동 매핑 메서드 (`toDto()`, `from()`) 우선, MapStruct는 도메인이 복잡해지면 도입 검토.
- **DTO 네이밍**: `{도메인명}{동작}{Request|Response}` 형식으로 짓는다 (예: `DeliveryCreateRequest`, `DeliveryCreateResponse`). Aggregate Root 이름(`DeliveryRequest`)을 DTO 이름에 그대로 붙이지 않는다 — "Request"라는 단어가 도메인 개념(고객의 배송 요청)과 HTTP 요청이라는 기술적 의미로 중복돼서 헷갈린다.

```java
// application 레이어
public record DeliveryCreateResponse(String id, String status, long feePoint) {
    public static DeliveryCreateResponse from(DeliveryRequest domain) {
        return new DeliveryCreateResponse(
            domain.getId().value(),
            domain.getStatus().name(),
            domain.getFee().point()
        );
    }
}
```

- 요청 DTO(Request) → 도메인 Input 객체 변환도 동일하게 Application 레이어 진입 시점에서 처리하고, `domain` 레이어는 프레임워크의 `@RequestBody` 등을 알지 못하게 한다.

---

## 11. HTTP 응답 매핑 규칙

`Result.Failure`의 `DomainError`를 Controller(또는 공통 `@ExceptionHandler`/`ResultResponseAdvice`)에서 아래 기준으로 HTTP 상태코드로 변환한다.

| DomainError 유형 | HTTP 상태 | 예시 |
|---|---|---|
| 입력 값 자체가 유효하지 않음 (Validation) | `400 Bad Request` | `InvalidWeight`, `OverMaxDistance` |
| 리소스 없음 | `404 Not Found` | `MemberNotFound` |
| 중복/충돌 | `409 Conflict` | `DuplicateMember` |
| 비즈니스 규칙상 처리 불가 | `422 Unprocessable Entity` | `NoAvailableCourier`, `InsufficientPoint` |
| 인증/인가 실패 | `401 Unauthorized` / `403 Forbidden` | 역할 접근 제한 위반 (부가 기능 도입 시) |

- 이 매핑은 도메인마다 흩어져 있으면 안 되고, `common` 패키지의 **공통 매핑 컴포넌트 한 곳**에서 관리한다 (예: `DomainErrorHttpMapper`).
- Controller는 `Result`를 받아 아래 패턴으로만 처리한다.

```java
@PostMapping("/deliveries")
public ResponseEntity<?> requestDelivery(@RequestBody DeliveryCreateRequest request) {
    Result<DeliveryRequest, DomainError> result = requestDeliveryUseCase.execute(request.toInput());
    if (result instanceof Result.Success<DeliveryRequest, DomainError> success) {
        return ResponseEntity.ok(DeliveryCreateResponse.from(success.value()));
    }
    // sealed interface has only Success/Failure, so this cast is safe
    return DomainErrorHttpMapper.toResponse(((Result.Failure<DeliveryRequest, DomainError>) result).error());
}
```

---

## 12. 트랜잭션 경계

- `@Transactional`은 **Application(UseCase) 레이어에만** 붙인다. `domain`, `infrastructure`(Repository 구현체 자체)에는 붙이지 않는다.
- 하나의 UseCase 메서드 = 하나의 트랜잭션 단위가 원칙. 여러 Repository 저장이 필요한 UseCase는 그 메서드 전체를 하나의 트랜잭션으로 묶는다.
- 외부 API 호출(알림 발송 등)은 트랜잭션 안에 포함하지 않는다 — 트랜잭션 커밋 이후 이벤트(`@TransactionalEventListener` 등)로 분리한다.

---

## 13. 로깅 규칙

- **도메인 레이어에서는 로그를 남기지 않는다** (순수 함수 원칙 유지, 로깅도 부수효과).
- `Result.Failure`가 발생하면 **Application 레이어의 UseCase 호출부** 또는 **Controller의 공통 매핑 지점(11번 항목)**에서 로그를 남긴다.
- 로그 레벨 기준:

| 상황 | 레벨 |
|---|---|
| 사용자 입력 오류성 실패 (`InvalidWeight` 등) | `INFO` 또는 로그 생략 (너무 빈번함) |
| 비즈니스 규칙 실패 (`NoAvailableCourier` 등) | `WARN` |
| 인프라 예외 (`throw` 되는 unchecked exception) | `ERROR` + 스택트레이스 포함 |

- 민감정보(비밀번호, 결제수단 등)는 로그에 절대 포함하지 않는다.

---

## 14. 레이어 의존성 강제

- 사람 리뷰만으로는 "`domain`이 프레임워크에 의존하면 안 된다"가 지켜지기 어려우므로 **ArchUnit**으로 빌드 시 강제한다.

```java
@Test
void domain_레이어는_spring에_의존하지_않는다() {
    noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("org.springframework..")
        .check(importedClasses);
}
```

- 최소 강제 규칙 3가지:
  1. `domain` → `application`, `infrastructure`, `presentation` 의존 금지
  2. `domain` → Spring 등 프레임워크 패키지 의존 금지
  3. Aggregate 간 서로 다른 Aggregate 클래스 직접 참조 금지 (`~Id`만 허용)

---

## 15. Git 커밋 / 브랜치 컨벤션

- 커밋 메시지: `type: 설명` 형식 (Conventional Commits 기반)
  - `feat`, `fix`, `refactor`, `test`, `docs`, `chore`
  - 예: `feat: 배송 요금 산정 로직 추가`
- 브랜치명: `type/도메인-내용` (예: `feat/delivery-fee-calculation`)
- PR 단위: 도메인 하나 + UseCase 하나 정도로 작게 유지 (Result 체이닝 특성상 리뷰가 선형적으로 읽히기 쉬움)

---

## 16. 체크리스트 (PR 리뷰 시 확인)

- [ ] `domain` 패키지에 프레임워크 어노테이션이 없는가?
- [ ] Value Object가 `record` + 정적 팩토리(`of`) + `Result` 반환 패턴을 따르는가?
- [ ] 도메인 로직 함수에 `setter`나 가변 필드가 없는가?
- [ ] 예상 가능한 실패가 `throw`가 아니라 `Result`로 표현되는가?
- [ ] Aggregate 간 참조가 객체 참조가 아니라 `~Id`로 되어 있는가?
- [ ] Aggregate Root 하나당 Repository가 하나씩 대응되는가? (패키지 하나에 여러 Aggregate Root가 있다면 Repository도 그 수만큼 나뉘어 있는가)
- [ ] Entity의 getter가 `get` 접두사를 쓰고 있는가? (Value Object의 record 스타일 접근자와 혼동하지 않았는가)
- [ ] 새로 추가한 도메인 로직에 단위 테스트가 있는가? (given_when_then 네이밍)
- [ ] Controller가 도메인 객체가 아니라 DTO만 다루는가?
- [ ] 새 `DomainError`를 추가했다면 `DomainErrorHttpMapper`에도 매핑을 추가했는가?
- [ ] `@Transactional`이 Application 레이어(UseCase)에만 붙어 있는가?
- [ ] `domain` 레이어에 로그 코드가 없는가?
- [ ] ArchUnit 테스트(레이어 의존성 규칙)가 깨지지 않는가?
- [ ] Spotless 포맷팅 검사를 통과하는가?
