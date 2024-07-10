# 스프링 핵심 원리 - 기본편 (김영한 강의)
Link: <https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B8%B0%EB%B3%B8%ED%8E%B8/dashboard>

## 1. 객체 지향 설계와 스프링
### 스프링 프레임워크와 스프링 부트
여러 스프링 프레임워크들이 존재하고, 그 스프링 프레임 워크를 쉽게 사용할 수 있도록 하는 중간다리 역할이 스프링 부트이다.<br/><br/>

### 스프링 핵심 개념
자바 언어 기반의 프레임 워크 = 객체 지향 언어<br/>
즉, 좋은 객체 지향 어플리케이션을 개발할 수 있도록 도와주는 프레임워크다.<br/><br/>

### 객체 지향 프로그래밍
객체의 모임과 그들은 메시지로 정보를 주고 받는다. 이때 개발에서 유연하고 변경에 용이하게 사용할 수 있다.<br/>

**다형성**: 유연하고 변경에 용이함
ex) 자동차의 역할(인터페이스)과 자동차 구현의 차이

클라이언트인 운전자 역할은 자동차의 역할만 알면 된다!

클라이언트인 운전자 역할은 자동차 구현을 알 필요 없다!<br/>
= 즉, 구현은 대체되어도 된다.
<br/>

역할 = 인터페이스, 구현 = 인터페이스를 구현한 클래스, 구현 객체
클라이언트(service) = 요청, 서버 = 응답

- 역할과 구현을 분리한다.
- 역할은 클라이언트에 영향을 주지 않는 변경이 가능하고 안정적으로 설계하는 것이 중요하다.
<br/><br/>

**SOLID**: 좋은 객체 지향 설계의 5가지 원칙
1. **SRP 단일 책임 원칙**<br/>
한 클래스는 하나의 책임만 가져야 한다.<br/>
2. **OCP 개방-폐쇄 원칙**<br/>
확장에는 열려 있으나 변경에는 닫혀 있어야 한다.<br/>
3. **LSP 리스코프 치환 원칙**<br/>
ex) 자동차 인터페이스의 엑셀은 앞으로 가는 기능이다. 뒤로 가게 구현하면 LSP 위반<br/>
4. **ISP 인터페이스 분리 원칙**<br/>
자동차 인터페이스 = 운전 인터페이스 + 정비사 인터페이스<br/>
사용자 클라이언트 = 운전자 클라이언트 + 정비사 클라이언트<br/>
인터페이스가 명확해지고, 변경이 쉽다. (대체 가능성이 높다)<br/>
5. **DIP 의존관계 역전 원칙**<br/>
추상화에 의존해야지, 구체화에 의존하면 안 된다.<br/>
= 클라이언트가 역할(인터페이스)에 의존하게 해야 한다. 구현 클래스에 의존하면 안 된다.
<br/><br/>

```java
public class MemberService{
    //private MemberRepository memberRepository = new MemoryMemberRepository(); (기존코드)
    private MemberRepository memberRepository = new jdbcMemberRepository();
}
```
다형성은 지켰지만 OCP, DIP를 위반한다. 그래서 객체를 생성하고, 연관관걔를 맺어주는 별도의 조립, 설정자가 필요하다.<br/><br/>

**스프링은 다형성 + OCP, DIP가 가능하게 지원한다. = 클라이언트 코드의 변경 없이 기능을 확장하는 것**
- DI(Dependency Injection): 의존관계, 의존성 주입
- DI 컨테이너
<br/><br/>

## 2. 스프링 핵심 원리 이해1 - 예제 만들기
* Grade, Member 클래스
* MemberRepository 인터페이스: save, findById 메소드
    * MemoryMemberRepository: store HashMap 저장소를 만들고 save, findById 메소드를 이용해서 저장 및 찾기 (HashMap은 동시성 이슈 때문에 concurrentHashMap 사용하는 것이 좋다.)

* MemberService 인터페이스: join, findMember 메소드
    * MemberServiceImpl

* order 클래스
* OrderService 인터페이스: createOrder 메소드
    * OrderServiceImpl

* DiscountPolicy 인터페이스: discount 메소드
    * FixDiscountPolicy
<br/>

직접 main에서 test하는 것은 한계가 있다.
-> 그래서 junit이라는 test framework이용 (검증은 Assertions(org.assertj.core.api이용))<br/>

```java
public class OrderServiceTest {
    MemberService memberService = new MemberServiceImpl();
    OrderService orderService = new OrderServiceImpl();

    @Test
    void createOrder(){
        Long memberId = 1L;
        Member member = new Member(memberId, "memberA", Grade.VIP);
        memberService.joinMember(member);

        Order order = orderService.createOrder(memberId, "itemA", 10000);
        Assertions.assertThat(order.getDiscountPolicy()).isEqualTo(1000);
    }
}
```
<br/><br/>

## 문제점
```java
public class OrderServiceImpl implements OrderService {

    //private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
    private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
    private final MemberRepository memberRepository = new MemoryMemberRepository();
    
    @Override
    public Order createOrder(Long memberId, String itemName, int itemPrice) {
        Member member = memberRepository.findById(memberId);
        int discountPrice = discountPolicy.discount(member, itemPrice);

        return new Order(memberId, itemName, itemPrice, discountPrice);
    }
}
```
<br/>

```java
private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
```
RateDicountPolicy는 인터페이스의 구현이다. 인터페이스뿐만 아니라 구현도 의존하고 있어서 DIP를 위반한다.
<br/>

```java
//private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
private final DiscountPolicy discountPolicy = new RateDiscountPolicy();
```
코드를 수정해야 하는 상황이 발생하므로 OCP를 위반한다.
<br/><br/><br/>

# 백준 문제 풀이
solved.ac class1 문제 풀이<br/>

python 기초 문법<br/>
* **(제곱) //(몫) %(나머지)
* 비교연산자
* and, or, not
* in, not in [리스트] (튜플) '문자열'
*  + 는 변수의 자료형이 같아야 함.
```
print(n, "*", (i+1), "=", n*(i+1)) //","는 자동으로 한 칸씩 띄어서 출력 된다.
print("{} * {} = {}".format(n, i+1, n*(i+1)))
print(f"{n} * {i+1} = {n*(i+1)}")
```
* \t, \n, \', \", \\
