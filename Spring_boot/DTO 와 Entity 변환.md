# DTO 와 Entity 변환

발표자: 김주환

# 왜 DTO 를 써야 하나요?

DTO(Data Transfer Object, 데이터 전송 객체)란 프로세스 간 데이터를 전달하는 객체이다.

원격 인터페이스로 작업할 때 호출 수를 줄이기 위해 고안된 디자인 패턴. [Martin Fowler가 자신의 블로그에서 정의한](https://martinfowler.com/eaaCatalog/dataTransferObject.html) 것처럼 Data Transfer Object를 사용하는 주된 이유는 여러 원격 호출을 단일 호출로 일괄 처리하는 것.

❗️**Entity 를 그냥 보내면 안되나요?**

- 상용하는 서비스에서는 도메인 Entity가 외부에 노출 되어선 안 됨.
  - 민감한 User 정보(실명,계좌번호 등등)가 외부에 노출 될 시 보안 문제와 직결
  - 클라이언트가 엔티티의 모든 데이터를 알게 되면 캡슐화 실패. 객체지향 프로그래밍 원칙 중 정보 은닉 원칙이 깨지게 됨
    [캡슐화?](https://www.notion.so/c81c8486cc904ba8a1ad81c04549b765?pvs=21)
- Entity 와 View 사이 강한 결합이 형성 됨. Entity는 절대 View에 의존해서는 안된다.
  - Entity 는 비즈니스 로직을 구현하는 핵심 클래스
  - 반면 DTO는 뷰, 클라이언트에 특화 된 클래스
  - 그러므로 하나의 변화가 서로에게 영향을 주어서는 안됨!
- 불필요한 데이터 전송으로 인한 네트워크 트래픽 증가와 처리시간 증가.
- 무엇을 accept 할지 제한하기 힘들다.
  - update 를 하는 `PUT` 엔드포인트에서 Entity 그 자체를 받는다면 선별적으로 update 하기 힘들 수도 있음
  - **Bean Validation** 기능을 Entity 에서 공통적으로 사용하기 힘듬.
  - 결국 Entity를 사용하면 검증 로직을 컨트롤러 이후에서 구현해야함

---

</br>
</br>
</br>

# 그럼 DTO - Entity 매핑을 어떤 식으로 해야하나요?

</br>

# 1. 자동 Mapper 사용

</br>
</br>

## 1) ModelMapper

→ 이름과 타입이 일치하는 속성을 자동으로 매핑하고, 커스텀 매핑 전략도 정의할 수 있습니다

- **만들어지는 대상은 Getter 만드는 대상은 Setter가 필요하다.**
  - Entity가 DTO로 변환된다고 한다면 Entity에는 각 필드값을 읽을 수 있는 Getter가 존재해야되고 DTO는 필드값을 넣을 수 있는 Setter들이 존재해야 한다
- **필드 작명, Standard(Default Staragy) 기준**
  - 필드 이름이 같을 경우 자동으로 매핑이 이루어지지만, 필드이름이 다를 경우 매핑이 이루어지지 않는다
  - 필드가 다를 시 하나하나 설정해 줘야함
  ```java
  ModelMapper modelMapper = new ModelMapper();
  modelMapper.addMappings(new PropertyMap<Order, OrderDTO>() {
    protected void configure() {
      map().setCustomerName(source.getCustomer().getName());
      map().setCustomerBillingAddress(source.getCustomer().getBillingAddress());
    }
  });
  OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
  ```

## 장점

- 필드명이 같을때 매핑하면 간편하다

1. `Entity -> DTO`, `modelMapper.map(Entity 객체, DTO클래스명.class)`
2. `DTO -> Entity`, `modelMapper.map(DTO 객체, Entity클래스명.class)`

## 단점

- Runtime시 Java의 리플렉션 API를 이용해서 매핑을 진행하기 때문에, 매핑 과정에서 오버헤드가 있다
  - Java의 리플렉션(Reflection) API는 런타임 중에 클래스, 인터페이스, 필드, 메서드 등에 대한 정보를 가져오거나 수정하거나 새로운 인스턴스를 생성하는 등의 작업을 할 수 있게 합니다. 즉, 프로그램이 자기 자신을 조사하고 수정할 수 있는 기능을 제공하는 것입니다.
  - ModelMapper는 리플렉션 API를 이용해서 매핑을 수행합니다. 즉, 런타임 시에 소스 객체의 필드 이름과 타겟 객체의 필드 이름이 같은지 확인하고, 같으면 해당 필드의 값을 복사하는 작업을 합니다.

</br>
</br>

## 2) MapStruct

→ MapStruct는 매핑 메서드를 정의하는 인터페이스를 제공하며, 구현체는 자동으로 생성

```java
@Mappings({
            @Mapping(source = "title", target = "title2"),
            @Mapping(source = "content", target = "content"),
    })
    Lost toEntity(LostCreateRequest lostRequest);
```

내부 구현체

```java
public Lost toEntity(LostCreateRequest lostRequest) {
	Lost lost = new Lost();
	lost.setTitle(lostRequest.getTilte());
	lost.setContent(lostRequest.getCotent());

	return lost;
}
```

MapStruct는 주로 Getter/Setter 또는 공개 필드에 의존하여 동작하지만, MapStruct 1.4.2 이후부터 생성자나 빌더패턴을 이용해서도 매핑을 수행할 수 있음!

```java
@Builder
@Getter
public class CarDto {
    private final String make;
    private final int seatCount;
}

@Mapper
public interface CarMapper {
    @Mapping(source = "numberOfSeats", target = "seatCount")
    CarDto carToCarDto(Car car);
}
```

```java
public class CarMapperImpl implements CarMapper {
    @Override
    public CarDto carToCarDto(Car car) {
        if ( car == null ) {
            return null;
        }

        CarDto.CarDtoBuilder carDto = CarDto.builder();

        carDto.make(car.getMake());
        carDto.seatCount(car.getNumberOfSeats());

        return carDto.build();
    }
}
```

## 장점

- 컴파일 시점에서 어노테이션을 읽어 구현체를 만들어내기 때문에 리플렉션이 발생하지 않음

→ 자바 코드 매핑과 대등한 성능

- 다른 이름 매핑 정도는 간편하게 가능
- 한 곳에서 도메인 관련 매핑 로직을 관리해서 유지 보수가 편하다

## 단점

- 단순 매핑이 아니고 가공을 할 수록 매핑 로직이 굉장히 복잡해지고 가독성 떨어짐

```java
@Mapping(target = "location", expression = "java(LostMapper.toPoint(lostRequest.getLat(), lostRequest.getLon()))"),
```

- 인터페이스만을 작성하기 때문에 필요한 다른 class 인젝션 불가능
- 러닝커브가 좀 있음
- Lombok Library와 충돌이 발생할 수 있다. (실제로는 Lombok annotation processor가 getter나 builder 등을 만들기 전에 mapstruct annotation processor가 동작하여 매핑할 수 있는 방법을 찾지 못해 발생하는 문제이다. )

---

</br>
</br>

# 2. 수동 Mapper 사용

</br>
</br>

## 1) java beans 패턴

```java
User user = new User();
user.setName("John Doe");
user.setEmail("john.doe@example.com");
user.setAge(25);
```

## 장점

- 필드가 적을때 간단하게 생성 가능
- 각각의 setter 메소드 호출이 명시적이기 때문에, 어떤 필드에 어떤 값을 설정하는지 코드를 읽는 사람이 쉽게 이해할 수 있음

## 단점

- 객체 일관성 보장 X : 객체가 완전히 초기화되기 전까지 일관성이 깨질 수 있음. 즉, 필요한 필드가 모두 설정되지 않고도 객체가 사용 될 수 있음
- 불변성 X : 객체 생성 이후에도 값이 변경가능 하므로 클래스를 불변으로 만들 수 없다. 멀티스레드 환경에서 객체 상태 추적하기 힘듬.
- 코드 작성 과정에서 개발자의 실수로 휴먼에러 발생 가능

</br>
</br>

## 2) 생성자

```java
// Entity -> ResoponseDto
public class ResDto {
    private final String field;

    public ResDto(Entity entity) {
        this.field = entity.getField();
    }
}

// RequestDto -> Entity
public class Entity {
    private final String field;

    public Entity(ReqDto reqDto) {
        this.field = reqDto.getField();
    }
}

// 사용할때
ResDto resDto = new ResDto(entity);
Entity entity = new Entity(reqDto);
```

## 장점

- 필드가 적을때는 DTO를 넘기는 방식 말고 필드를 파라미터로 넣는 방식으로도 간단하게 생성 가능

```java
ResDto resDto = new ResDto(entity.getField());
Entity entity = new Entity(reqDto.getField());
```

- 생성과 동시에 값을 넣어줄 수 있음 → 불변

## 단점

- 필드를 생성자의 파라미터로 넘기는 경우 순서 실수 등으로 휴먼에러 발생
- 생성자를 사용해 인스턴스화 하는 것이기때문에 이름 부여 불가능 → 명시적이지 않음
- 동일한 시그니처 생성자는 2개 이상 불가능

```java
public class A {

    private double 내월급;
    private double 남월급;

	// 둘 중 하나는 무조건 쓸 수 없다
    public A(double 내월급){
		this.내월급 = Math.round(내월급);
    }

    public A(double 남월급){
    	this.남월급 = Math.floor(남월급);
    }
}
```

- Entity 가 DTO를 알아야한다 → DTO가 추가 될때마다 Entity 내부 코드 변경

</br>
</br>

## 3) 정적 팩토리 메서드

→ 주로 객체를 생성하고 초기화하는 데 사용

→ 팩토리 메서드 패턴(factory method pattern)은 객체 생성에 관한 디자인 패턴. 이 패턴에서는 객체를 생성하는 로직을 별도의 메서드에 두고, 이 메서드를 호출하여 객체를 생성.

```java
// Entity -> ResoponseDto
public class ResDto {
    private final String field;

    public static of(Entity entity) {
		ResDto resDto = new ResDto();
        resDto.setField(entity.getField());

		return resDto;
    }
}

// RequestDto -> Entity
public class ReqDto {
    private final String field;

		// this 사용을 위해 static 제거
    public toEntity() {
		Entity entity = new Entity();
        entity.setField(this.field);

		return entity;
    }
}

// 사용할때
ResDto.of(entity);
```

## 장점

- 이름을 가질 수 있음 → 읽는 사람에게 더 명확하게 메서드를 설명 가능 (의미 부여 가능)
  - 예컨대 정적팩토리메서드는 네이밍 컨벤션이 있음`from`, `of`, `valueOf`, `instanse`, `getInstance`, `create`, `getType`, `newType`
    이들을 잘 지키면 코드를 읽는 사람이 해당 메서드 기능 유추 가능

사실 정적 팩토리 메서드의 이점은 많지만, 현재 Mapping 관점에서의 장점은 이정도.

[[이펙티브 자바] 1. 생성자 대신 정적 팩터리 메서드를 고려하라](https://velog.io/@0sunset0/이펙티브-자바-1.-생성자-대신-정적-팩터리-베서드를-고려하라)

</br>
</br>

## 4) Builder 패턴

```java
public class Entity {
    private final String field;

		@Builder
    public Entity(String field) {
        this.field = field;
    }
}

// RequestDto -> Entity
public class ReqDto {
    private final String field;

    public toEntity() {
				return Entity.builder()
								.field(this.field);
    }
}
```

## 장점

- 생성시 파라미터에 요구되는 필드 값이 많아도 명시적이고 간단하게 객체 생성 가능
  - 생성자 패턴은 시그니처 순서에 맞게 호출해야함 → 하지만 빌더는 매개변수의 이름을 사용하여 값 설정
- 선택적 매개변수가 많은 경우 압도적으로 편함
  - 생성자나 자바빈즈 패턴은 모든 경우 따로 만들어줘야함 → 하지만 빌더 패턴은 하나로 선택적 생성 가능

## 단점

- 롬복 `@Builder` 를 사용하지 않으면 코드 구현이 길어짐.
  - `@Builder` 를 class 에 붙일때
    - <모든 필드를 파라미터로 받는 생성자> 가 있어야한다.
    - 이때 생성자 생성을 아예 막으려면 `@AllArgsConstructor(access = AccessLevel.*PRIVATE*)` 설정 or 생성자 `private` 변경
  - `@Builder` 를 생성자에 붙일때
    - 선택적으로 매개변수 받기 불가능. 무조건 선언 된 매개변수를 다 받아야함.
      ⇒ 선택적 매개변수를 허용한다는 것 자체가 필수 제공 필드에 대한 명확성을 떨어트리므로 생성자 레벨에서 사용 하시옵소서

---

</br>
</br>

# 👏 결론

1. 본인은 Entity 에 Setter 을 사용하지 않음
   1. `1-1 ModelMapper` , `2-1 java beans 패턴` ⇒ Setter 필수
2. `2-2 생성자` 방식을 `2-3 정적 팩토리 메서드` `2-4 Builder 패턴` 이 잘 보완함
   1. 순서 실수 가능 + 매개변수 많아짐 ⇒ `2-4 Builder 패턴`
   2. 생성자 자체가 명시적으로 나타내는 것 알기 힘듬 ⇒ `2-3 정적 팩토리 메서드`
3. `1-2 MapStruct` 는 복잡한 매핑 패턴을 처리하기 쉽지 않음
   1. 생각보다 그냥 매핑하는 것 보다 가공이 들어가는 경우가 많은데 그때 마다 방법을 찾아보고 하는게 힘듬

## ⇒ `2-3 정적 팩토리 메서드` 를 사용하며 안에서 실제 자바 코드는 `2-4 Builder 패턴` 으로 쓰는 것으로..

```java
// Entity -> ResoponseDto
public class ResDto {
    private final String field;

    public static of(Entity entity) {
				return ResDto.builder()
								.field(entity.getField());
    }
```

🔺 이런의견도 있습니다

![스크린샷 2023-07-25 오전 5.21.28.png](https://file.notion.so/f/s/bfd9dd35-c7d9-453a-990e-5f327fcb99c0/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-07-25_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_5.21.28.png?id=f63a9e9b-27b4-4e7a-a9dd-6c3df0aaf211&table=block&spaceId=5db2dd73-ceda-4a45-af28-91717d885111&expirationTimestamp=1690920000000&signature=gud9SJIYhvRQw-JZXnDFUQQrJcW79NvPaDkNicm7LtU&downloadName=%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA+2023-07-25+%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB+5.21.28.png)

</br>
</br>

# DTO 변환은 언제 해야 하나요?

## 1. Controller 에서 변환

![Untitled](DTO%20%E1%84%8B%E1%85%AA%20Entity%20%E1%84%87%E1%85%A7%E1%86%AB%E1%84%92%E1%85%AA%E1%86%AB%20edabd566ad1642a1abed745d88e2e849/Untitled.png)

- Service 는 Entity 만 받는다.

### 장점

- Service가 비즈니스 로직과 연관이 깊은 Entity에만 의존하기때문에 어느곳과도 강한 결합이 생기지 않음 → 유연성과 재사용성이 높아짐.

### 단점

- **Service 메소드별로 원하는 포맷이 다를 경우 Dto로 꼭 받아야 될 일이 생길 것이고, 그렇다면 Entity로 받기로한 일관성이 깨질 수 있음**
- (반환할때) **Presentation 레이어에서 도메인을 알고 있는 것 자체가 문제가 될 수 있다**
  - Presentation 레이어에서 도메인 레이어의 내용을 직접 알게 되면, 서로 간의 결합도가 높아져서 변경에 유연하지 못해질 수 있음. ⇒ 하나바꾸면 다 바꿈 파국!
- **불완전한 엔티티를 Service 파라미터로 받는 것은 적절하지 않다**
  - 클라이언트로부터 받은 데이터로 엔티티 객체를 완전하게 채우지 못할 수 있음. 즉, 이러한 경우 불완전한 상태의 엔티티를 Service 단에서 바로 사용하게됨.
  - 또한 클라이언트에서 받은 Dto 로 엔티티를 만드는 것 자체가 비즈니스 로직일 경우에는 Entity 로 전달 불가.

</br>

## 2. Service 에서 변환

![Untitled](DTO%20%E1%84%8B%E1%85%AA%20Entity%20%E1%84%87%E1%85%A7%E1%86%AB%E1%84%92%E1%85%AA%E1%86%AB%20edabd566ad1642a1abed745d88e2e849/Untitled%201.png)

### 장점

- **Presentation 레이어는 도메인을 알지 못하고 오직 Dto 만 알고 있도록 만들 수 있음**

### 단점

- DTO로 전달하게 되면, 뷰 - 컨트롤러 - 서비스 간 통신에 같은 DTO가 유지. ⇒ 뷰와 컨트롤러 서비스 로직 간에 강한 결합이 생김
- Controller 가 원하는 Dto 와 Service 가 원하는 Dto가 다를 수 있다.
  - 클라이언트로 부터 넘어온 Dto 가 외부 Api 를 통해 가공 된 후 우리의 Service 로 가야 한다면?
  - Controller에서 받은 Dto 와는 다른 형태의 data가 Service 로 갈 것임

</br>

## 3. Service 를 위한 Dto 를 따로 만들자

![Untitled](DTO%20%E1%84%8B%E1%85%AA%20Entity%20%E1%84%87%E1%85%A7%E1%86%AB%E1%84%92%E1%85%AA%E1%86%AB%20edabd566ad1642a1abed745d88e2e849/Untitled%202.png)

- 중간에 Mapper 객체를 생성해서 거기서 dto-Entity convert 진행

### 장점

- RequestDTO가 변경되더라도 ServiceRequest 객체는 변하지 않기 때문에 Mapper 객체만 수정해주면 서비스 계층에는 아무런 영향을 미치지 않는다 ⇒ **Presentation 레이어와 도메인의 완벽한 분리**

### 단점

- 하나의 endpoint 에 무수히 많은 Dto가 생김
  - 관리 복잡도 증가

![Untitled](DTO%20%E1%84%8B%E1%85%AA%20Entity%20%E1%84%87%E1%85%A7%E1%86%AB%E1%84%92%E1%85%AA%E1%86%AB%20edabd566ad1642a1abed745d88e2e849/Untitled%203.png)

---

</br>

## ✔️ Dto 만들때 신경 써야 될 것

- 비즈니스 로직을 Dto 에 절대 넣으면 안됨
  - 이 패턴의 목적은 데이터 전송의 최적화와 계약 구조를 명확하게 하는 것. 따라서 모든 비즈니스 로직은 도메인 계층에 위치해야 함.
- LocalDTOs라고 불리는 경우, DTO는 도메인 간에 데이터를 전달함. → 매핑 유지 관리 비용 생각
  - 도메인 모델의 캡슐화 Good
- 모든 경우 Dto 를 만드는건 낭비 일 수 있다

</br>

# 진짜 결론

![스크린샷 2023-07-25 오전 5.27.40.png](https://file.notion.so/f/s/20f2b6f4-fcd5-42e2-b7b3-05cf4a474b8d/%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA_2023-07-25_%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB_5.27.40.png?id=de8eaa91-d0a7-4a5b-b5de-71540b2a1d23&table=block&spaceId=5db2dd73-ceda-4a45-af28-91717d885111&expirationTimestamp=1690920000000&signature=YBTsXMenf2yUt4MWN5L8vdvTsP4QksYDAqkfp7n-jXM&downloadName=%E1%84%89%E1%85%B3%E1%84%8F%E1%85%B3%E1%84%85%E1%85%B5%E1%86%AB%E1%84%89%E1%85%A3%E1%86%BA+2023-07-25+%E1%84%8B%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB+5.27.40.png)

</br>

_Reference_

[DTO, Entity and Mapper](https://velog.io/@hklog/DTO-Entity-and-Mapper)

[Controller에서 Service에 값을 어떻게 전달할까?](https://kafcamus.tistory.com/12?category=912020\)

[잊을만 하면 돌아오는 정산 신병들 | 우아한형제들 기술블로그](https://techblog.woowahan.com/2711/)

[DTO 작성법, Entity <-> DTO 매핑에 대하여](https://velog.io/@sanizzang00/DTO-작성법-Entity-DTO-매핑에-대하여)

[객체 변환하기. 자바 코드 매핑 vs MapStruct vs ModelMapper?](https://lob-dev.tistory.com/entry/객체-변환하기-자바-코드-매핑-vs-MapStruct-vs-ModelMapper)

[DTO에 관한 생각](https://velog.io/@aidenshin/DTO에-관한-고찰)

[Automatically Mapping DTO to Entity on Spring Boot APIs](https://auth0.com/blog/automatically-mapping-dto-to-entity-on-spring-boot-apis/)

[DTO는 대체 어디서 변환하는 것이 좋을까?](https://sedangdang.tistory.com/296)

[📚 Dto와 Entity를 분리하는 이유, 분리하는 방법](https://velog.io/@0sunset0/Dto와-Entity를-분리하는-이유-분리하는-방법#️-entity를-보호할-수-있다)
