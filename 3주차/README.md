# 스프링 핵심 원리 - 기본편 (김영한 강의)
Link: <https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B8%B0%EB%B3%B8%ED%8E%B8/dashboard>

## 싱글톤 컨테이너
### 웹 애플리케이션과 싱글톤
기본적으로 클라이언트들이 요청을 하면 DI 컨테이너(서버)에서 객체를 생성하고, 연결을 해야 한다.<br/>
웹 어플리케이션은 보통 여러 고객이 동시에 요청을 한다. 
<br/>그래서 수많은 클라이언트의 요청을 하나씩 만드는 것은 아래와 같이 매우 비효율적이다.<br/>
즉, 스프링 없이 순수한 DI 컨테이너인 AppConfig는 요청을 할 때마다 객체를 새로 생성하고 소멸하게 된다.<br/>
결국 고객 트래픽이 초당 100이 나오면 초당 100개가 생성되고 소멸이 된다 -> 메모리 낭비가 심하다.<br/><br/>

**그래서 객체를 하나만 만들고 그 객체 인스턴스를 공유하도록 설계하면 된다 -> 싱글톤 패턴** <br/>
싱글톤 패턴은 클래스의 인스턴스가 한개만 생성되도록 보장하는 디자인 패턴을 이야기 하는데 결국 2개 이상의 인스턴스가 만들어지지 않도록 하는 것이다.<br/>
```java
private static final SingletonService instance = new SingletonService();

public static SingletonService getInstance(){
   return instance;
}

private SingletonService(){
}
```
위 코드와 같이
1. private 생성자를 만들어서 객체가 더 이상 생성되지 않도록 막는다.
2. getter만 존재하고, final인 instance를 setting할 수 없다.
<br/><br/>

### 정리
* static 영역에 미리 객체 instance를 만들어서 올린다.
* getInstance() 메서드를 통해서만 조회할 수 있다.
* 생성자를 private으로 막아서 혹시라도 외부에서 new 키워드로 객체 인스턴스를 만드는 것을 방지한다
<br/>

+ same: == 객체 인스턴스와 비교할 때(객체의 참조값 비교)
+ equal: equals
+ instanceOf: 객체의 클래스와 비교할 때
<br/><br/>

### 싱글톤 패턴의 단점
1. 앞의 코드처럼 싱글톤 패턴을 구현하려고 코드가 길어지고 복잡해진다.
2. 클라이언트가 구체 클래스에 의존한다. getInstance()에서 -> DIP위반
3. 구체 클래스에 의존하니 나중에 수정하면서 OCP를 위반할 수 있다.
4. 즉 유연성이 떨어진다. 그래서 싱글톤 패턴을 안티패턴이라고 부르기도 한다.
<br/><br/>

### 싱글톤 컨테이너
이때 스프링 컨테이너는 싱글톤 패턴의 단점을 상쇄시켜주면서 싱글톤 패턴(싱글톤 레지스트리)을 지원한다.<br/>
*스프링 컨테이너 = 싱글톤 컨테이너*<br/><br/>

그래서 스프링 컨테이너를 보면 스프링 빈 저장소 안에 빈 이름과 매핑되는 빈 객체는 이미 생성된 하나의 객체 인스턴스이다.<br/>
ex) MemberServiceImpl@x01 <br/>

스프링 컨테이너로 test하면<br/>

memberService1 = hello.core.member.MemberServiceImpl@c94fd30<br/>
memberService2 = hello.core.member.MemberServiceImpl@c94fd30<br/>

같은 참조값을 가진다는 것을 확인할 수 있다.<br/><br/>

### 싱글톤 방식의 주의점
여러 클라이언트가 하나의 객체 인스턴스를 공유하기 때문에(동시성 문제) 싱글톤 객체는 상태를 유지(stateful)하게 설계하면 안된다.<br/>
**-> 즉 무상태(statelsess)로 설계해야 한다.** <br/>

* 클라이언트에 의존적인 필드가 있으면 안된다.
* 클라이언트가 값을 변경할 수 있는 필드가 있으면 안된다
* 가급적 읽기만 가능해야 한다
* 필드 대신에 공유되지 않는, 지역변수, 파라미터, ThreadLocal 등을 사용해야 한다.
<br/>

예를 들어,
```java
public class StatefulService {

    private int price; //상태를 유지하는 필드
    
    public void order(String name, int price){
        System.out.println("name = " + name + "price = " + price);
        this.price = price; //여기가 문제
    }

    public int getPrice(){
        return price;
    }
```
```java
@Test
void statefulServiceSingleton(){
    ApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);
    StatefulService statefulService1 = ac.getBean(StatefulService.class);
    StatefulService statefulService2 = ac.getBean(StatefulService.class);
    
    //Thread A: 사용자 A가 10000원 주문
    statefulService1.order("User A", 10000);
    //Thread B: 사용자가 B가 20000원 주문
    statefulService2.order("User B", 20000);

    //Thread A: 사용자 A가 주문 금액 조회
    int price = statefulService1.getPrice();
    System.out.println("price = " + price);
}
```
위의 예제 코드와 같이 중간에 20000원이 끼어들어서 10000원이 아닌 20000원이 출력된다.
<br/>

```java
public int order(String name, int price){
    System.out.println("name = " + name + "price = " + price);
    return price;
}
```
위의 코드와 같이 지역변수로 처리해 무상태로 만들었다.
<br/><br/>

### @Configuration과 싱글톤
* MemberService -> new MemoryMemberRepository
* OrderService -> new MemoryMemberRepository<br/>
같은 타입의 객체를 두 번 생성하기 때문에 싱글톤 패턴이 깨진다고 생각하게 된다.
그러나 @Configuration을 붙이면 순수한 자바 코드 그대로 읽지 않고 *스프링 컨테이너는 싱글톤 레지스트리를 지원한다.*
<br/><br/>

### @Configuration과 바이트 코드
```java
AppConfig bean = ac.getBean(AppConfig.class);

System.out.println("bean = " + bean.getClass()); //getClass(): 객체의 클래스를 출력하는 함수
```
class hello.core.AppConfigSpringCGLIB0가 출력된다.<br/>
내가 직접 만든 클래스가 아니라 CGLIB라는 바이트 코드 조작 라이브러리를 사용해서 AppConfig 클래스를 상속받은 임의의 다른 클래스를 만들고, 그 다른 클래스를 스프링 빈으로 등록한 것이기 때문이다. AppConfig클래스를 상속받아 메서드를 오버라이딩한다. <br/><br/>
그래서 이 다른 클래스 AppConfig@CGLIB가 싱글톤을 보장해주는 것이다.
<br/><br/><br/>

## 컴포넌트 스캔
### 컴포넌트 스캔과 의존관계 자동 주입 시작하기
스프링 빈을 등록할 때 @Bean을 직접 나열했다. 하지만 스프링 빈이 수십, 수백개가 되면 등록하기가 힘들다.<br/>
* 그래서 스프링은 자동으로 스프링 빈을 등록하는 **컴포넌트 스캔**이라는 기능을 제공한다.
* 그리고 의존 관계도 자동으로 주입하는 **@AutoWired**라는 기능도 존재한다.<br/><br/>

예를 들어,
```java
@ComponentScan(
   excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
)
//빈 저장소에 올리고 싶지 않은 것은 제외할 수 있다. 설정 정보도 자동으로 Component가 붙어있다.
```
1. @ComponentScan을 추가한다. @Component가 붙은 클래스를 스캔해서 모두 스프링 빈 저장소에 올려주는 기능을 한다.
2. @Component를 붙여준다
<br/>

그리고 컴포넌트 스캔을 했으니 의존 관계를 주입해야 한다.(DI 컨테이너가 한 일이 객체를 **생성, 연결** 두가지다.)
3. @AutoWired를 이용해서 자동으로 의존 관계를 주입한다. (클라이언트 코드의 생성자 앞에 @AutoWired를 추가한다.)
<br/>

+ 스프링 빈의 이름을 변경하고 싶으면 @Component("memberService2")를 추가하면 된다.
<br/><br/>

### 탐색 위치
디폴트는 설정 정보의 패키지로 시작 위치로 지정된다.
* basePackages = "hello.core.member",
* basePackages = {"hello.core.member", "hello.core.order"},
탐색 위치는 위와 같이 지정할 수 있다.
* basePackageClasses = AutoAppConfig.class,
지정한 클래스의 패키지를 탐색 위치로 정한다. 
<br/>

-> **설정 정보 클래스의 위치를 프로젝트 최상단에 두는 것이 권장된다.**
그래서 메인 설정 정보는 프로젝트의 시작 루트 위치에 두는 것이 좋다.<br/>
스프링 부트를 이용하면 스프링 부트의 대표 시작 정보인 @SpringBootApplication을 프로젝트 시작 루트 위치에 두는 것이 관례이다. (이 안에 ComponentScan이 존재한다. 그래서 자동으로 스프링 컨테이너가 만들어지는 것이다. CoreApplication으로 처음부터 생성되어 있다.)
<br/><br/>

### 컴포넌트 스캔 기본 대상
* @Component
* @Controller: 스프링 MVC 컨트롤러에서 사용
* @Service: 스프링 비즈니스 로직에서 사용
* @Repository: 스프링 데이터 접근 계층에서 사용
* @Configuration: 스프링 설정 정보에 사용
Component뿐만 아니라 위의 대상들도 스캔하게 되는데, 각각 들어가보면 Component가 이미 등록되어 있는 것을 확인할 수 있다.
<br/><br/>

### 컴포넌트 스캔 필터
```java
@Configuration
@ComponentScan(
        includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = MyIncludeComponent.class),
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = MyExcludeComponent.class)
)
```
어노테이션이 붙은 클래스는 include, exclude된다.
<br/><br/>

### 중복 등록과 충돌
* 자동 빈 등록 vs 자동 빈 등록: ConflitingBeanDefinitionException발생
* 수동 빈 등록 vs 자동 빈 등록: 이의 경우 수동 빈 등록이 우선된다. 그래서 수동 빈이 자동 빈을 오버라이딩한다.<br/>
요즘은 스프링 부트에서 아예 오류가 발생하도록 했다. 그래서 오버라이딩을 원하면 properties를 설정해야 한다.<br/>

-> **애초에 빈 이름을 등록할 때 중복이 없도록 해야 한다.**
<br/><br/><br/>

# 백준 문제 풀이
solved.ac class1 문제 풀이<br/>
solved.ac class2 문제 풀이<br/>

python 기초 문법<br/>
* str.zfill(n): 빈 자리에 0으로 채우기, 앞에 문자열 주의 <br/>
* len(array): array의 index개수를 반환한다.<br/>
* array.count(n): 찾을 요소 n의 개수<br/>
* array.sort() 함수: 배열을 작은 순으로 정렬한다.<br/>
* 각 자리 수의 합 더하기
1. 반복문을 통해 a % 10 이용하기
2. 문자열로 바꾸고, list의 합을 반환하는 함수인 sum을 이용하기
```python
num = sum([int(j) for j in str(i)])
```
* list를 이용한 stack
stack.append()과 del or stack.pop() 이용<br/>
stack[len(stack)-1] 대신 쉽게 stack[-1]으로 표현할 수 있다.<br/>
* split() 함수
1. split("구분자"): (디폴트는 공백이다.)
2. split("구분자", n): n은 자르는 횟수이다.
3. splitlines(): 여러 줄로 나뉘어진 문자열을 자른다.<br/>
* deque를 이용한 queue
queue.append()과 queue.popleft() 이용<br/>
* queue를 이용한 queue
queue.get()과 queue.put() 이용 <br/>
* pop, push
1. array.pop: 디폴트값은 -1로 가장 큰 인덱스의 요소를 꺼내 반환한다.
2. array.popleft()
3. array.get()
4. array.append()
5. array.appendleft()
6. array.insert(index, n): 앞에 삽입
7. array.put()<br/>
* math.sqrt(): 반환형은 float




