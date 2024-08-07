# 스프링 핵심 원리 - 기본편 (김영한 강의)
Link: <https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B8%B0%EB%B3%B8%ED%8E%B8/dashboard>

## 스프링 핵심 원리2 - 객체 지향 원리 적용
### 이전 코드의 문제점
1. 이전 프로그램은 **다형성**을 활용하고 **역할과 구현을 분리**해냈다.<br/>
2. 하지만 **OCP, DIP** 같은 객체지향 설계 원칙을 준수하지 못했다.<br/>
* DIP: OrderServiceImpl에서 discountPolicy를 결정하는데 인터페이스(추상)에 의존할뿐만 아니라 구현(구체)에도 의존해 **DIP**를 위반했다.
* OCP: OrderServiceImpl에서 discountPolicy를 수정해야 하는 문제가 발생한다. 확장이 아닌 수정까지 해야하는 문제로 **OCP**를 위반했다.

```java
private final DiscountPolicy discountPolicy = new FixDiscountPolicy();
```

그래서 위와 같은 코드를 아래와 같이 바꿔보았다.

```java
private DiscountPolicy discountPolicy;
```

DIP, OCP 문제는 해결했지만 구현 객체가 없어서 null pointer exception이 발생한다.
<br/><br/>

### 관심사 분리
이전 코드의 문제점을 비유하면 로미오의 역할을 하는 배우(구현)가 줄리엣의 역할을 하는 배우(구현)을 정하는 것과 다름 없다.<br/>
그래서 역할을 정하는 사람은 "공연 기획자"가 해야 한다.<br/>
*즉, 구현 객체를 생성하고, 연결하는 책임을 가지는 별도의 설정 클래스를 만들어야 한다. -> AppConfig*
<br/>

1. 생성자 주입
```java
public OrderServiceImpl(DiscountPolicy discountPolicy, MemberRepository memberRepository) {
        this.discountPolicy = discountPolicy;
        this.memberRepository = memberRepository;
    }
```
<br/>

2. AppConfig에서 객체를 **생성**하고, **연결**해서 클라이언트의 구현과 인터페이스의 구현을 분리했다.
```java
public class AppConfig {
    
    public MemberService memberService() {
        return new MemberServiceImpl(new MemoryMemberRepository());
    }

    public OrderService orderService() {
        return new OrderServiceImpl(
            new MemoryMemberRepository(),
            new FixDiscountPolicy()
        );
    }
}
```
<br/>

**이제 memberServiceImpl이나 orderServiceImpl은 의존관계에 대한 고민은 외부(AppConfig)에 맡기고 실행에만 집중하면 된다.**<br/><br/>


+ *DI(Dependency Injection): 의존관계 주입 혹은 의존성 주입이라고 한다.*<br/>

+ *test에서 @BeforeEach를 이용하면 각 test 실행 전에 메소드를 실행하고, test를 한다.*
```java
@BeforeEach
    public void beforeEach(){
        AppConfig appConfig = new AppConfig();
        memberService = appConfig.memberService();
    }
```
<br/>

### AppConfig 리팩터링
현재 AppConfig는 **중복**이 있고, **역할**에 따른 구현이 안 보인다. 역할들을 드러나게 하는 것이 중요하다. 그 방법이 리팩터링이다. <br/>

```java
public MemberService memberService() {
    return new MemberServiceImpl(memberRepository());
}

public OrderService orderService() {
    return new OrderServiceImpl(memberRepository(),discountPolicy());
}

public MemberRepository memberRepository() {
    return new MemoryMemberRepository();
}

public DiscountPolicy discountPolicy() {
    return new FixDiscountPolicy();
}
```

이렇게 memberService, memberRepository, orderService, discountPolicy 네가지 모든 역할을 쉽게 볼 수 있도록 해 어플리케이션 전체 구성이 어떻게 되어있는지 한눈에 확인하기 좋다.<br/><br/>

### 새로운 구조와 할인 정책 적용
이제 할인 정책을 변경하려고 하면 AppConfig만 변경하면 된다. 사용 영역의 어떤 코드도 변경할 필요가 없다. 구성 영역만 변경된다.<br/>

* (OCP) 클라이언트 코드를 변경할 필요가 없어졌다.<br/> 구현 객체를 생성하고 연결하는 책임을 가진 AppConfig가 담당 = 소프트웨어 요소를 새롭게 확장해도 사용 영역의 변경은 닫혀있다
* (DIP) 클라이언트 코드는 역할에만 의존하고 있다.
```java
private final MemberRepository memberRepository;
private final DiscountPolicy discountPolicy;
```
<br/>

### IoC, DI, 그리고 컨테이너
**IoC(제어의 역전, Inversion of Control)**: 개발자가 직접 객체를 생성하고 호출하고 컨트롤, 제어하는 것이 아니라 프레임워크가 대신 코드를 호출해주는 것이다.<br/><br/>

이전 프로그램은 클라이언트 구현 객체가 서버 구현 객체를 생성하고, 연결하고, 실행했다. 클라이언트 구현 객체가 프로그램의 제어 흐름을 스스로 조종한다.<br/>

AppConfig가 등장하고 난 후, 더 이상 클라이언트 구현 객체가 필요한 인터페이스를 호출은 하지만 어떤 구현 객체가 실행될지는 전혀 알지 못한다. 즉 AppConfig가 프로그램의 제어 흐름을 담당하게 되는 것이다. 이렇게 외부에서 관리하는 것이 제어의 역전이다.<br/><br/>

**프레임워크 vs 라이브러리**:
* 프레임워크는 내가 작성한 코드를 제어하고, 대신 실해하는 것이다. ex) JUnit
* 내가 직접 작성한 코드가 직접 제어의 흐름을 담당하면 라이브러리다.
<br/>

**정적인 의존 관계 vs 동적인 의존 관계**
* 정적인 의존 관계: 클라이언트 구현 객체를 보면 실행하지 않고도 정적으로 어떤 의존 관계를 가지고 있는 지 알 수 있다. 하지만 실행에서 실제 어떤 객체가 들어가는 지 알 수 없다.
* 동적인 의존 관계: 어플리케이션 실행 시점에서 실제로 생성된 객체 인스턴스의 참조가 연결된 의존 관계이다.<br/>

-> *그래서 의존관계 주입을 사용하면 클라이언트 코드를 건드리지 않고(정적인 의존 관계) 동적인 의존 관계를 쉽게 변경할 수 있다.*
<br/>

이러한 역할을 하는 것을 IoC 컨테이너 혹은 DI 컨테이너라고 부른다. (IoC는 범용적인 개념이기 때문에 보통 **DI 컨테이너**라고 부른다.)
<br/><br/>

### 정리
* 이전 프로그램은 다형성을 지키고 역할과 구현을 분리했다.
* OCP, DIP 문제를 해결하고자 *클라이언트 코드에 생성자를 주입*하고, AppConfig에서 *객체를 생성, 연결*을 했다.
* 중복되지 않고 역할이 잘 드러나도록 AppConfig을 *리팩터링*했다.
* AppConfig가 *의존관계를 주입해* DI 컨테이너의 역할을 한 것이다. 즉 클라이언트 코드(정적인 의존 관계)를 건드리지 않고 동적인 의존 관계를 쉽게 변경할 수 있다. -> OCP문제 해결
<br/><br/>

### 스프링으로 전환하기
DI 컨테이너 앞에 @Configuration, 각 메소드엔 @Bean을 붙인다. <br/>
@Bean을 붙이면 스프링 컨테이너에 자동으로 올라간다.<br/>

예를 들어 MemberApp에서 ApplicationContext를 이용해서 AppConfig에 있는 환경 설정 정보를 가져온다. <br/>
@Bean이 붙은 메소드를 스프링 컨테이너에 가져와서 객체 생성을 관리해준다. 만든 스프링 컨테이너에서 무엇을 가져올 지 정하면 된다.
```java
ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
MemberService memberService = applicationContext.getBean("memberService",MemberService.class(반환형));
```
<br/>

* ApplicationContext = **스프링 컨테이너**
기존에는 AppConfig를 사용해서 직접 객체를 생성하고 DI 했다. 이제부턴 스프링 컨테이너를 통해서 사용한다.
+ name은 기본적으로 Bean 메소드의 이름을 사용한다.
+ 이제 getBean 메소드를 이용해서 가져온다.
<br/><br/><br/>

## 스프링 컨테이너와 스프링 빈
### 스프링 컨테이너 생성
ApplicationContext는 스프링 컨테이너로 인터페이스이다.<br/>

스프링 컨테이너를 부를 때 BeanFactory, ApplicationContext로 구분하지만 BeanFactory는 직접 사용할 일이 거의 없다.<br/><br/>

1. 스프링 컨테이너를 만들 때 구성 정보(AppConfig)를 지정해서 파라미터로 보내준다. 그리고 스프링 컨테이너 안에 스프링 빈 저장소들이 있는데 빈 이름, 빈 객체를 구성 정보로부터 불러온다.

2. 그리고 스프링 컨테이너 안에 동적인 의존 관계를 연결해준다. (DI)
<br/>

+ @Bean(name = "memberService2") 이런 방식으로 이름을 부여해줄 수도 있다. 반드시 빈 이름은 항상 다른 이름을 부여해야 한다.
<br/><br/>

### 컨테이너에 등록된 빈 조회
* ac.getBeanDefinitionNames(): 스프링에 등록된 모든 빈 이름 조회
* ac.getBean(): 빈 이름으로 빈 객체를 조회한다.
    * getBean("이름", 타입)
    * getBean(타입)<br/>
타입에 구현 객체 타입으로 넣어도 된다. 물론 역할(인터페이스)에 의존하지 않고 구현에 의존했기 때문에 좋은 것은 아니다. 유연성이 떨어진다.
<br/>

```java
public class ApplicationContextBasicFindTest {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

    @Test
    @DisplayName("빈 이름으로 조회")
    void findByName(){
        MemberService memberService = ac.getBean("memberService", MemberService.class);
        System.out.println("memberService = " + memberService);
        System.out.println("memberService.getClass() = " + memberService.getClass());

        // memberService의 인스턴스가 MemberServiceImpl의 인스턴스면 Test 통과
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("이름 없이 타입으로만 조회")
    void findByType(){
        MemberService memberService = ac.getBean(MemberService.class);
        System.out.println("memberService = " + memberService);
        System.out.println("memberService.getClass() = " + memberService.getClass());

        // memberService의 인스턴스가 MemberServiceImpl의 인스턴스면 Test 통과
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("구체 타입으로 조회")
    void findByName2(){
        MemberService memberService = ac.getBean("memberService", MemberServiceImpl.class);
        System.out.println("memberService = " + memberService);
        System.out.println("memberService.getClass() = " + memberService.getClass());

        // memberService의 인스턴스가 MemberServiceImpl의 인스턴스면 Test 통과
        assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
    }

    @Test
    @DisplayName("빈 이름으로 조회 실패")
    void findByNameX(){
        // 오른쪽을 실행했을 때 왼쪽이 나타나야 Test 성공
        assertThrows(NoSuchBeanDefinitionException.class,
                () -> ac.getBean("xxxx", MemberService.class));
    }
}
```
<br/>

```java
// memberService의 인스턴스가 MemberServiceImpl의 인스턴스면 Test 통과
assertThat(memberService).isInstanceOf(MemberServiceImpl.class);
```

getRole()
* ROLE_APPLICATION 직접 등록한 애플리케이션 빈
* ROEL_INFRASTRUCTURE 내부에서 사용하는 빈
<br/><br/>

### 스프링 빈 조회 - 동일한 타입이 둘 이상
```java
public class ApplicationContextSameBeanFindTest {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(sameBeanConfig.class);

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면, 중복 오류가 발생한다.")
    void findBeanByTypeDuplicate(){
        // MemberRepository bean = ac.getBean(MemberRepository.class);
        Assertions.assertThrows(NoUniqueBeanDefinitionException.class,
                () -> ac.getBean(MemberRepository.class));
    }

    @Test
    @DisplayName("타입으로 조회시 같은 타입이 둘 이상 있으면, 빈 이름을 지정하면 된다.")
    void findBeanByName(){
        MemberRepository bean = ac.getBean("memberRepository1", MemberRepository.class);
        assertThat(bean).isInstanceOf(MemberRepository.class);
    }

    @Test
    @DisplayName("특정 타입을 모두 조회하기")
    void findAllBeanByType(){
        Map<String, MemberRepository> beansOfType = ac.getBeansOfType(MemberRepository.class);
        for (String key : beansOfType.keySet()) {
            System.out.println("key = " + key + " value = " + beansOfType.get(key));
        }
        System.out.println("beansOfType = " + beansOfType);
        assertThat(beansOfType.size()).isEqualTo(2);
    }

    @Configuration
    static class sameBeanConfig {

        @Bean
        public MemberRepository memberRepository1(){
            return new MemoryMemberRepository();
        }

        @Bean
        public MemberRepository memberRepository2(){
            return new MemoryMemberRepository();
        }
    }
}
```
<br/>

### 스프링 빈 조회 - 상속 관계 
부모 타입으로 조회를 하면, 자식 타입도 함께 조회한다. 그래서 object 타입으로 조회하면 모든 스프링 빈을 조회한다.
<br/>

```java
public class ApplicationContextExtendsFindTest {
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);

    @Test
    @DisplayName("부모 타입으로 조회시, 자식이 둘 이상 있으면, 중복 오류가 발생한다.")
    void findBeanByParentTypeDuplicate(){
        //DiscountPolicy bean = ac.getBean(DiscountPolicy.class);
        Assertions.assertThrows(NoUniqueBeanDefinitionException.class,
                () -> ac.getBean(DiscountPolicy.class));
    }

    @Test
    @DisplayName("자식이 둘 이상이 있으면, 빈 이름을 지정하면 된다.")
    void findBeanByParentTypeBeanName(){
        DiscountPolicy bean = ac.getBean("rateDiscountPolicy", DiscountPolicy.class);
        assertThat(bean).isInstanceOf(RateDiscountPolicy.class);
    }

    @Test
    @DisplayName("특정 하위 타입으로 지정")
    void findBeanBySubType(){
        DiscountPolicy bean = ac.getBean(FixDiscountPolicy.class);
        assertThat(bean).isInstanceOf(FixDiscountPolicy.class);
    }

    @Test
    @DisplayName("부모 타입으로 모두 조회하기")
    void findAllBeanByParentType(){
        Map<String, DiscountPolicy> beansOfType = ac.getBeansOfType(DiscountPolicy.class);
        assertThat(beansOfType.size()).isEqualTo(2);
        for (String key : beansOfType.keySet()) {
            System.out.println("key = " + key + " value = " + beansOfType.get(key));
        }
    }

    @Test
    @DisplayName("부모 타입으로 모두 조회하기 - Object")
    void findAllBeanByObjectType(){
        Map<String, Object> beansOfType = ac.getBeansOfType(Object.class);
        for (String key : beansOfType.keySet()) {
            System.out.println("key = " + key + " value = " + beansOfType.get(key));
        }
    }

    @Configuration
    static class TestConfig{

        @Bean
        public DiscountPolicy rateDiscountPolicy(){
            return new RateDiscountPolicy();
        }

        @Bean
        public DiscountPolicy fixDiscountPolicy(){
            return new FixDiscountPolicy();
        }
    }
}
```
<br/>

### BeanFactory와 ApplicationContext
**BeanFactroy**: 스프링 컨테이너의 최상위 인터페이스이다. 스프링 빈을 관리하고 조회하는 역할을 담당한다.
<br/>

**ApplicationContext**: BeanFactory 기능을 모두 상속받아서 제공한다. 뿐만 아니라 여러 인터페이스도 함께 상속받고 있다.
* MessageSource: 메세지소스를 활용한 국제화 기능, 예를 들어 한국에서 들어오면 한국어로 영어권에서 들어오면 영어로 출력되는 부가기능을 제공한다.
* EnvironmentCapable: 로컬, 개발, 운영 등을 구분해서 처리한다. 예를 들어 개발DB에 연결할지, 운영DB에 연결할지를 처리한다.
* ApplicationEventPublisher: 이벤트를 발행하고 구독하는 모델을 편리하게 지원한다.
* ResourceLoader: 파일, 클래스패스, 외부 등에서 리소스를 편리하게 조회한다.
*즉 BeanFactory의 기능과 편리한 부가 기능을 제공하는 것이다.*
<br/><br/>

### 다양한 설정 형식 지원 - 자바 코드, XML
스프링 컨테이너는 다양한 형식의 설정 정보를 받아들일 수 있게 유연하게 설계되어 있다.<br/>
자바 코드, XML, Groovy등
<br/><br/>
ex) ApplicationContext를 상속받아서<br/>
* AnnotationConfig ApplicationContext -> AppConfig.class
* GenericXml ApplicationContext -> appConfig.xml
* Xxx ApplicationContext -> appConfig.xxx

New -> XML Configuration File -> Spring Config로 XML파일 생성이 가능하다.
<br/> <br/>

### 스프링 빈 설정 메타 정보 - BeanDefinition
스프링 컨테이너는 자바 코드든, XML이든 상관없이 BeanDefinition만 알면 된다.<br/>
자바 코드든, XML이든 BeanDefinition을 만들면 이를 보는 것이다. 이때 BeanDefinition은 인터페이스로 설정 메타정보라고 한다.<br/>
이 메타정보를 이용해서 스프링 컨테이너를 만드는 것이다.<br/>

* 설정 정보를 읽고, 빈 메타 정보를 생성한다 -> 이 빈 메타 정보를 가지고 -> 빈 저장소에 빈 이름, 객체를 넣고 의존 관계를 주입해서 ApplicationContext인 스프링 컨테이너를 만든다.
<br/><br/>

### 정리
* 순수한 자바만으로 작성 -> DI 컨테이너를 이용해서 의존관계를 주입해 OCP, DIP문제를 해결 -> 앞의 DI 컨테이너를 스프링 컨테이너으로 변경, 스프링 컨테이너에 넣어 의존관계를 주입함으로써 스프링 컨테이너 내에서 관리하고 여러 장점을 가져올 수 있다.<br/><br/>
* 스프링 컨테이너를 생성하면 그 안에 빈 이름과 빈 객체로 이루어진 빈 저장소가 존재한다. 그리고 이를 조회할 수 있다.
* 스프링 컨테이너는 다양한 형식의 설정 정보를 받을 수 있게 설계되어 있다.
<br/><br/><br/>

# 백준 문제 풀이
solved.ac class1 문제 풀이<br/>

python 기초 문법<br/>
* print("String", end=""): print()는 디폴트로 end="\n"이 포함되어 있다. <br/>
* 파이썬 자료형
1. numeric: 실수, 정수, 복소수
2. boolean: True, False
3. String: "spring"
4. List: ["apple", "cherry"]
5. Tuple: ("apple", "cherry") 튜플은 리스트와 다르게 요소가 변할 수 없다.
6. Dictionary: {"apple" : 1000, "cherry" : "fruit"}
7. Set: {"apple", "cherry", "apple"} 중복된 요소를 가질 수 없다.<br/>
* while문이 무한대로 작동하기 때문에 try, except를 이용해 런타임 에러가 발생했을 때 프로그램을 중지할 수 있도록 한다.<br/>
* chr: 정수를 아스키코드로 / ord: 아스키코드를 정수로<br/>
* 파이썬은 증감연산자가 없고 a += 1를 사용해야 한다.<br/>
* sum() 함수
1. sum(iterable): 반드시 인자로는 "숫자로만 이루어진 리스트 혹은 튜플" 이어야 한다. 
2. sum(iterable, start = 0): 시작값 디폴트는 0이고, 지정할 수 있다. <br/>
* 문자열 정렬
1. 문자열.rjust(n): 오른쪽 정렬
2. 문자열.ljust(n): 왼쪽 정렬
3. 문자열.center(n): 가운데 정렬 
4. 문자열.zfill(n): 숫자 남은 자리에 0이 채워짐<br/>
* list 원소 추가
1. list.append(n): 리스트 마지막에 원소 하나 추가
2. list1 + list2: + 연산자로 이어 붙이기
k = m + n or k += [11, 23]
3. list.expend([11, 23])<br/>
* list 원소 삭제
1. del list[index]
2. list.remove(n): 찾을 원소인 n이 없으면 ValueError<br/>
* max(), min() 함수
1. max(list): 리스트 중에 최댓값을 찾는다. 만약 문자라면 알파벳 순서로 최댓값을 찾는다.
2. min(list): 리스트 중에 최솟값을 찾는다.<br/>
* list.index() 함수
1. list.index(n): 찾을 원소인 n의 index를 반환한다.
2. list.index(n, start_index, end_index): start_index부터 end_index사이의 찾을 언소인 n의 index를 반환한다.
만약 중복된 원소가 있으면 가장 작은 index를 반환한다. 문자열도 가능하다.<br/>
* 문자열을 리스트 형태로 출력이 가능하다.
<br/>String[1]
<br/>String[0]
<br/>String[-1]
<br/>String[0:5]: 0번째 인덱스부터 5번 출력
<br/>String[-5:]: 뒤에서부터 5번 출력
<br/>String[::2]: 두글자 간격으로 출력<br/><br/>
* 문자열.split() 함수
1. split()
2. spilt('구분자')
3. spilt('구분자', 분할횟수)<br/>
* 문자열끼리 A + B가 가능하지만, A - B는 불가능하기 때문에 int로 형변환했다.