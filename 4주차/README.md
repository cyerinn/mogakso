# 스프링 핵심 원리 - 기본편 (김영한 강의)
Link: <https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B8%B0%EB%B3%B8%ED%8E%B8/dashboard>

## 의존관계 자동 주입
### 다양한 의존관계 주입 방법
remind> 빈 생성 -> 의존 관계 주입 두 과정을 거쳐서 스프링 컨테이너를 완성한다.

1. **생성자 주입** (생성자 앞에 @Autowired 붙이기): 생성자가 처음 호출된 시점에서만 호출된다. 그래서 필드에 대한 불변이라는 것이다. <br/>

* 그리고 필수로 생성자에 모든 객체를 넣어주는 것이 권장된다.
* 생성자가 하나만 있다면 자동으로 @Autowired가 자동으로 주입된다. 그러나 생성자가 두 개 이상이라면 반드시 @Autowired를 붙여줘야만 한다.<br/><br/>

2. **수정자 주입(setter 주입)**: setter 앞에 @Autowired를 붙인다. 생성자 주입은 객체를 생성함과 동시에 의존관계를 주입하게 되는 반면에 수정자 주입은 두 단계를 거치게 된다.<br/>

* 자바빈 프로퍼티 규약 (setter, getter 만들 때)<br/>

```java
@Autowired
public void setDiscountPolicy(DiscountPolicy discountPolicy) {
    this.discountPolicy = discountPolicy;
}

@Autowired
public void setMemberRepository(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
}
```
<br/>

3. **필드 주입**: 필드 앞에 바로 @Autowired를 붙인다. 이전엔 많이 사용했지만 외부에서 변경하기가 어려워서 안티패턴이라고도 불린다.<br/>
순수한 자바 코드가 아니기 때문에 setter 없이 순수한 자바 코드로 직접 값을 넣어 테스트하기 어렵다.<br/>

**-> 그러니 사용을 지양하는 것이 좋다.** <br/><br/>

4. **일반 메서드 주입**: 아무 메서드에 @Autowired를 붙여서 의존관계를 주입한다. 일반적으로 잘 사용하진 않는다.<br/>

```java
@Autowired
public void init(DiscountPolicy discountPolicy, MemberRepository memberRepository) {
    this.discountPolicy = discountPolicy;
    this.memberRepository = memberRepository;
}
```
<br/>

### 옵션 처리
주입할 스프링 빈이 없어도 동작해야 할 때가 있다.<br/>
@Autowired만 붙이면 디폴트가 required 옵션이 true이기 때문에 오류가 발생한다. 그래서 이때 옵션 처리가 필요하다.

```java
@Autowired(required = false)
public void setNoBean1(Member noBean1){ //Member는 스프링 빈이 아님
    System.out.println("noBean1 = " + noBean1);
}

@Autowired
public void setNoBean2(@Nullable Member noBean2){
    System.out.println("noBean1 = " + noBean2);
}

@Autowired
public void setNoBean3(Optional<Member> noBean3){
    System.out.println("noBean3 = " + noBean3);
}
```
<br/>
출력 결과<br/>
noBean2 = null<br/>
noBean3 = Optional.empty<br/><br/>

1. noBean1은 자동 주입할 대상이 없다면 수정자 메소드 자체가 호출되지 않는다.
2. noBean2는 호출은 되지만 null로 들어온다.
3. noBean3는 스프링 빈이 없다면 Optional.empty가 출력된다.
<br/><br/>

### 생성자 주입을 사용하자
**불변**
* 의존 관계 주입은 어플리케이션이 끝날 때까지 변경할 일이 거의 없다.
* 수정자 주입을 사용하면 set메서드를 public으로 열어둬야 한다.
* 생성자 주입은 객체를 생성할 때 한 번만 호출이 되므로 불변하게 설계가 가능하다.
* 생성자 주입은 필드에 final을 붙일 수 있다. 테스트 전에 생성자에 값이 설정되지 않은 오류를 확인해볼 수도 있다.<br/>

**누락** <br/>
프레임워크 없이 순수한 자바 코드를 단위 테스트하는 경우 생성자 주입을 사용하지 않으면 오류가 발생하기 쉽다.<br/>

**-> 생성자 주입 사용을 권장한다.** <br/><br/>

만약 수정자 주입을 사용한다면,<br/>
```java
OrderServiceImpl orderService = new OrderServiceImpl();
orderService.createOrder(1L, "itemA", 10000);
```
수정자 주입을 통해서 test를 실행하면 NullPointerException이 발생한다.<br/>
순수한 자바코드로 테스트를 했기 때문에 OrderServiceImpl 객체를 생성하기 위해선 memberRepository와 discountPolicy의 객체도 참조해줘야만 한다. 그러나 수정자 주입은 스프링 프레임워크에서만 작동하기 때문에 오류가 발생한 것이다.<br/><br/>

```java
@Test
void createOrder(){

    MemoryMemberRepository memberRepository = new MemoryMemberRepository();
    memberRepository.save(new Member(1L, "name", Grade.VIP));

    OrderServiceImpl orderService = new OrderServiceImpl(new FixDiscountPolicy(), memberRepository);
    Order order = orderService.createOrder(1L, "itemA", 10000);
    Assertions.assertThat(order.getDiscountPolicy()).isEqualTo(1000);
}
```
생성자 주입으로 변경 후 테스트가 잘 돌아가는 것을 확인할 수 있다.<br/><br/>

* 생성자 주입을 사용하면 final 키워드를 사용할 수 있다.<br/>
-> 생성자에 값이 설정되지 않은 경우 컴파일 시점에서 오류를 발생하도록 해준다.<br/>
* 생성자 주입을 사용하되 필요하다면 수정자 주입도 함께 사용할 수 있다. 수정자 주입을 이용해서 빈이 없는 경우를 처리할 수 있다. <br/><br/>

### 정리
* 생성자 주입을 사용하되 빈이 없는 경우 수정자 주입도 함께 사용해도 된다.
* 불변, 누락 이론에서 유용하다.
* final 키워드를 사용할 수 있다.
<br/><br/>

### 롬복
개발하다보면 대부분 불변이고 final 키워드를 사용하기 위해 생성자 주입을 선택하게 된다.<br/>
그런데 생성자를 매번 만들기는 귀찮고 필드 주입처럼 쉬운 것을 선호한다. -> **롬복** <br/>

* @RequiredArgsConstructor: 필수값인 final이 붙은 필드를 가지고 생성자를 자동으로 만들어준다.<br/>
(ctrl + F12로 생성자가 만들어진 것을 확인할 수 있다)<br/>
예를 들어 나중에 의존관계가 추가되어도 생성자를 추가할 필요 없어서 편리하다.<br/>

이 외에도,<br/>
* @Getter @Setter를 붙이면 자동으로 getter와 setter를 만들어준다. 바로 setName이나 getName 메서드를 사용하면 된다.
* @ToString<br/><br/>

*요즘은 생성자를 하나를 두고 @Autowired를 생략한다. 여기에 lombok을 이용하면 더 간결해진다.*<br/><br/>

### 조회 빈이 두개 이상인 경우
ac.getBean(class) 타입으로 조회할 때 2개 이상의 빈이 등록된 경우 문제가 발생했다.<br/>
이와 비슷하게 @Autowired는 타입으로 조회하기 때문에 2개 이상의 빈이 등록되면 에러가 발생한다.<br/>
그렇다고 빈을 조회할 때처럼 하위 타입으로 지정하는 것은 DIP를 위반한다.<br/><br/>

1. **@Autowired 필드 명**: Autowired는 처음엔 타입 매칭을 시도하고, 여러 개의 빈이 있다면 필드 이름, (생성자) 파라미터 이름으로 추가적으로 매칭한다. 그래서 생성자의 파라미터 명에 빈 이름을 적어준다.

```java
@Autowired
public OrderServiceImpl(DiscountPolicy RateDiscountPolicy, MemberRepository memberRepository) {
    this.discountPolicy = discountPolicy;
    this.memberRepository = memberRepository;
}
```
<br/>

2. **@Qualifier**: 추가적인 구분자를 붙여주는 것이다. 

```java
@Qualifier("mainDiscountPolicy") //RateDiscountPolicy 클래스 앞에

@Autowired
public OrderServiceImpl(DiscountPolicy discountPolicy, @Qualifier("mainDiscountPolicy") MemberRepository memberRepository) {
    this.discountPolicy = discountPolicy;
    this.memberRepository = memberRepository;
}
```
+ @Qualifier로 주입하면 중복 빈을 찾을 때 사용할 뿐만 아니라 못찾으면 파라미터로 넣은 이름의 스프링 빈을 추가로 찾는다. 그런데 Qualifier를 찾는 용도로 쓰는 것이 명확하다.<br/><br/>

3. **@Primary**: 우선순위를 지정하는 방법이다. 우선순위를 가진 빈에 @Primary를 붙이면 된다. <br/><br/>


*-> 메인 데이터 베이스에 커넥팅된 스프링 빈은 Primary로 관리하고, 특별한 경우에 Qualifier를 붙여주는 것이 편리하다. 물론 메인 데이터 베이스에 커넥팅된 경우에도 Qualifier를 붙여도 좋다. Qualifier가 더 명확하기 때문이다.*<br/><br/>

### 조회한 빈이 모두 필요할 때
조회한 빈이 모두 필요한 경우에 List, Map을 이용한다.<br/>
예를 들어 클라이언트가 Fix와 Rate 중에 고를 수 있는 경우, 스프링을 이용하면 전략 패턴을 구현할 수 있다.<br/><br/><br/>

## 빈 생명주기 콜백
클라이언트가 어플리케이션을 사용하기 위해, 미리 서버와 네트워크를 연결하고 종료하는 객체가 필요하다. <br/>

```java
public class NetworkClient {

    private String url;

    public NetworkClient(){
        System.out.println("생성자 호출, url = " + url);
        connect();
        call("초기화 연결 메시지");
    }

    public void setUrl(String url) {
        this.url = url;
    }

    //서비스 시작시 호출
    public void connect(){
        System.out.println("connect = " + url);
    }

    public void call(String message){
        System.out.println("call = " + url + " message = " + message);
    }

    //서비스 종료시 호출
    public void disconnect(){
        System.out.println("close");
    }
}
```
이때 객체 초기화 작업은 객체 생성 -> 의존관계 주입 두 단계가 완료된 후 호출해야 한다.<br/>
**-> 그래서 안전하게 작동하는지 작동하는 지 알아야 한다.(빈 생명주기 콜백)** <br/>

스프링은 의존관계까지 주입이 된 후 초기화 시점을 알려주는 기능과 스프링이 종료되는 시점에 소멸 콜백을 준다.<br/>

스프링 빈의 이벤트 라이프 사이클 (싱글톤)<br/>
스프링 컨테이너 생성 -> 스프링 빈 생성 -> 의존관계 주입 -> 초기화 콜백 -> 사용 -> 소멸 전 콜백 -> 스프링 종료

+ 예시 코드를 보면 생성자에 초기화를 한번에 하지 않고 setURL 메서드를 통해 초기화를 했다. 단일 책임의 원칙과 같이 (유지, 보수를 위해) 객체의 생성과 초기화를 분리해야 한다. 초기화는 외부 커넥션을 연결하는 무거운 동작을 소화하기 때문이다.<br/><br/>

### 1. 인터페이스 InitializingBean, DisposableBean
initializing이 초기화, disposable이 종료해주는 역할이다.<br/>
초기화, 소멸 메서드를 오버라이딩해서 사용한다.<br/>

테스트 결과<br/>
NetworkClient.afterPropertiesSet #의존관계 연결 후 초기화 메시지<br/>
connect = http://naver.com<br/>
call = http://naver.com message = 초기화 연결 메시지<br/>
NetworkClient.destroy # ac.close() 이후 종료 메시지<br/>
close<br/>

위와 같이 안전하게 스프링 초기화, 종료가 됨을 확인할 수 있다.<br/><br/>

단점으로 스프링 전용 인터페이스로 프레임워크에 의존한다. 코드를 고칠 수 없는 외부 라이브러리에 적용할 수 없다.<br/>
또한 초기화, 소멸 메서드 이름을 고칠 수 없다.<br/>

**그래서 현재는 거의 사용하지 않는다.** <br/><br/>

### 2. 빈 등록 초기화, 소멸 메서드
```java
@Bean(initMethod = "init", destroyMethod = "close")
public NetworkClient networkClient(){
    NetworkClient networkClient = new NetworkClient();
    networkClient.setUrl("http://naver.com");
    return networkClient;
}
```

* 메서드 이름을 지정할 수 있다.
* 스프링 빈이 스프링 코드에 의존하지 않는다. (아래 코드의 클래스를 보면)
* 코드가 아닌 설정 정보를 사용해서 외부 라이브러리에도 초기화, 종료 메서드 적용이 가능하다<br/>

```java
public void init() {
    System.out.println("NetworkClient.init");
    connect();
    call("초기화 연결 메시지");
}

public void close() {
    System.out.println("NetworkClient.close");
    disconnect();
}
```
<br/>

destroyMethod는 디폴트가 inferred다. 즉 종료 메서드를 입력하지 않아도 알아서 추론을 한다.<br/>
그래서 외부 라이브러리는 close, shutdown이라는 이름의 메서드를 많이 사용하는데 설정 정보를 입력하지 않아도 되는 것이다.<br/>
추론 기능을 사용하고 싶지 않다면 destroyMethod=""로 빈 공백을 지정하면 된다.<br/><br/>

### 3. 애노테이션 @PostConstruct, @PreDestroy
초기화, 소멸 메서드 이전에 애노테이션만 붙이면 된다. 해당 방법이 권장된다.<br/>

* 매우 편리하다.
* javax 패키지로 스프링에 종속된 기술이 아니라 자바 표준이다.
* 메서드 이름을 지정할 수 있다.
* 단점 -> 외부 라이브러리에 적용하지 못한다. 그래서 이러한 경우에는 빈 설정 정보를 이용하는 것이 좋다.
<br/><br/><br/>

# 백준 문제 풀이
```python
import sys

N = int(sys.stdin.readline())

countList = [0] * (10000 + 1) # 최대값이 마지막 인덱스, 1 ~ 10000 -> [1] ~ [10000] , [0]은 비워둠
for i in range(N):
   countList[int(sys.stdin.readline())] += 1

for i in range(len(countList)):
   for _ in range(countList[i]):
      print(i)
```
* 메모리 초과 문제를 해결하기 위해 계수 정렬 사용(메모리 사용이 적고 빠르다는 장점이 있다.)
* 입력받은 리스트를 만들지 않고, 입력 받자마자 counting으로 변경해 메모리 사용을 줄였다.
* 원래 코드는 sort 사용, (input()또한 메모리 소모가 크다)<br/><br/>

* 이항계수 nCk = N! / (N - K)! / K!
1. math.factorial 함수 이용
2. 팩토리얼 함수 직접 정의 <br/>

* sys.stdin.readline()은 개행 문자를 포함한 문자열을 반환한다. <br/>

* sort 함수<br/>
array.sort(): 디폴트는 reverse = False라서 올림차순으로 정렬<br/>

* 최대공약수(greatest common division): 유클리드 호제법을 사용<br/>
n = m * a + b 에서 n이 나누어 떨어질 때까지 반복
* 최소공배수(least common multiple): 최소공배수는 n * m을 최대공약수로 나눈 값

* 순열(Permutations), 조합(Combinations)<br/>
from itertools import combinations<br/>
combinations(list, n): list에서 n개의 요소를 가진 조합을 반환한다. <br/>

* 올림 함수(math.ceil()), 내림 함수(math.floor()) 
