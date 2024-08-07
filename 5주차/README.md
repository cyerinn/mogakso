# 스프링 핵심 원리 - 기본편 (김영한 강의)
Link: <https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B8%B0%EB%B3%B8%ED%8E%B8/dashboard>

## 빈 스코프
스프링 빈은 기본적으로 싱글톤 스코프로 생성된다. 이때 스코프는 빈이 존재할 수 있는 범위를 말한다.<br/><br/>

1. **싱글톤**: 스프링 컨테이너의 시작과 종료까지 유지되는 가장 넓은 범위의 스코프이다.<br/>
2. **프로토타입**: 빈의 생성과 의존 관계 주입까지만 관여하고 더 이상 관리 하지 않는 매우 짧은 범위의 스코프이다.<br/>
3. **웹 관련 스코프**: 
    * request: 웹 요청(고객 요청)이 들어오고 나갈때까지만 유지되는 스코프이다.
    * session: 웹 세션이 생성되고 종료될 때까지 유지되는 스코프이다.
    * application: 웹의 서블릿 컨텍스와 같은 범위로 유지되는 스코프이다.
    * websocket: 웹 소켓과 동일한 생명주기를 가진다.<br/><br/>

빈 스코프는 컴포넌트 스캔에 *자동 등록*을 위해 @Scope("prototype") 또는 *수동 등록*으로 원하는 @Bean 앞에 @Scope("prototype")을 붙인다.
<br/><br/>

### 프로토타입 스코프
프로토타입 스코프는 싱글톤과 다르게 스프링 컨테이너에 요청하면 항상 새로운 인스턴스를 만들어서 반환한다.<br/>

스프링 컨테이너에 요청을 하면 그 시점에 프로토타입 빈을 생성하고, 필요한 의존 관계를 주입해 초기화까지만 처리한다. 생성된 빈을 클라이언트에게 반환하고 더 이상 관리하지 않는다. 그래서 *종료 메서드가 호출되지 않는 특징*이 있다.<br/>

```java
@Test
void prototypeBeanFind(){
    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(prototypeBean.class);
    System.out.println("find bean1");
    prototypeBean bean1 = ac.getBean(prototypeBean.class);
    System.out.println("find bean2");
    prototypeBean bean2 = ac.getBean(prototypeBean.class);
    System.out.println("bean1 = " + bean1);
    System.out.println("bean2 = " + bean2);

    Assertions.assertThat(bean1).isNotSameAs(bean2);

    ac.close();
}
```

find bean1<br/>

prototypeBean.init<br/>

find bean2<br/>

prototypeBean.init<br/>

bean1 = hello.core.scope.PrototypeTest$prototypeBean@31edaa7d<br/>

bean2 = hello.core.scope.PrototypeTest$prototypeBean@26adfd2d<br/>

출력 결과는 위와 같다. ac.close()로 스프링 컨테이너를 close했음에도 소멸 메서드가 호출되지 않은 것을 확인할 수 있다.<br/>
-> 그래서 프로토타입 빈은 프로토타입 빈을 조회한 클라이언트가 관리해야 한다.
<br/><br/>

### 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 문제점
싱글톤 빈과 함께 사용할 때는 의도한 대로 잘 동작하지 않아 주의해야 한다.<br/>

예를 들어 싱글톤 빈이 의존 관계 주입을 통해 프로토타입 빈을 가져와서 사용할 때:
```java
@Autowired
private PrototypeBean prototypeBean;

public int logic(){
    prototypeBean.addCount();
    return prototypeBean.getCount();
}
```
클라이언트가 싱글톤 빈을 호출 한다. 싱글톤 빈은 logic을 호출해 프로토타입 빈의 count를 증가(0->1)시키고 반환하다.<br/>
그런데 또 다른 클라이언트가 같은 싱글톤 빈을 생성해 logic을 호출하면 프로토타입 빈인데도 count값이 1->2가 되는 문제가 발생한다.<br/><br/>

스프링 컨테이너를 생성하는 시점에 ClientBean을 생성하고 의존관계를 주입한다.<br/>
그럼 의존관계를 주입하는 시점에 PrototypeBean을 반환 받아 ClientBean이 관리를 시작하고 스프링 컨테이너는 더 이상 관리하지 않는다.<br/>
그래서 ClientBean이 생성되는 시점에서 이미 주입되니까 같은 인스턴스인 ClientBean을 여러 클라이언트가 호출하면 count는 계속 증가하는 것이다.
<br/><br/>

**-> 프로토타입 빈은 스프링 컨테이너에 요청할 때만 생성하고 반환하는 스코프이다.**<br/> 
**싱글톤 빈과 함께 사용하니까 싱글톤 빈의 스코프와 같이 유지되는 것이 문제다.**<br/><br/>

### 프로토타입 스코프 - 싱글톤 빈과 함께 사용시 Provider로 문제 해결
* 직접 필요한 의존관계를 찾는 것을 *dependency lookup(DL) 의존관계 조회*라고 한다.<br/>
* 문제는 Provider로 해결할 수 있다.<br/>

```java
@Autowired
private ObjectProvider<PrototypeBean> prototypeBeanProvider;

public int logic(){
    PrototypeBean prototypeBean = prototypeBeanProvider.getObject();
    prototypeBean.addCount();
    return prototypeBean.getCount();
}
```
1. **ObjectProvider**: prototypeBeanProvider.getObject(); 를 통해 항상 새로운 프로토타입 빈이 생성된다. <br/>
내부에서 스프링 컨테이너를 통해 빈을 찾아 반환한다.(DL)<br/>

+) ObjectProvider와 ObjectFactory가 있는데 ObjectProvider가 ObjectFactory를 상속받아 추가적인 기능을 더 제공하는 차이가 있다.<br/>
Factory는 getObject();정도만 수행한다. 그러나 둘 다 스프링에 의존한다는 특징이 있다.<br/><br/>

```java
@Autowired
private Provider<PrototypeBean> prototypeBeanProvider;

public int logic(){
    PrototypeBean prototypeBean = prototypeBeanProvider.get();
    prototypeBean.addCount();
    return prototypeBean.getCount();
}
```
2. **JSR-330 Provider**: 자바 표준을 사용하기 때문에 스프링에 의존적이지 않다. 대신 라이브러리를 추가해야 한다.
* prototypeBeanProvider.get(); 을 통해 새로운 프로토타입 빈이 생성된다. (DL)
* 매우 단순하다. (DL정도만 처리한다.)
* 자바 표준이라 스프링이 아닌 다른 컨테이너에서도 사용할 수 있다.
<br/><br/>

+ 꼭 프로토타입이 아니어도 DL이 필요할 때 ObjectProvider와 Provider를 사용할 수 있다.
+ ObjectProvider를 사용하고 다른 컨테이너를 사용할 땐 JSR-330 Provider를 사용하자.
<br/><br/>

### 웹 스코프
웹 스코프는 프로토타입과 다르게 스프링이 스코프의 종료시점까지 관리한다. 그래서 *종료 메서드가 호출된다.*<br>

* request: http 요청이 하나 들어오고 나갈 때까지 유지되는 스코프로 각각의 요청마다 별도의 빈 인스턴스가 생성된다.
* websocket: 웹 소켓과 동일한 생명주기를 가진다.<br/><br/>

### request 스코프
* @Scope(value = "request")로 지정한다.
* 빈이 생성되는 시점에 초기화 메서드를 이용해 uuid를 생성해 저장한다. HTTP마다 생성되어 uuid를 저장해 **다른 http 요청과 구분할 수 있다.**<br/>

```java
String uuid = UUID.randomUUID().toString(); // http 요청을 받으면 request 스코프를 생성하며 http 요청마다 서로 다른 uuid를 가진다.

public void log(String message){
    System.out.println("[" + uuid + "]" + "[" + requestURL + "] " + message);
}



@Controller
@RequiredArgsConstructor
public class LogDemoController {

    private final LogDemoService logDemoService;
    private final MyLogger myLogger;

    @RequestMapping("log-demo")
    @ResponseBody
    public String logDemo(HttpServletRequest request){
        String requestURL = request.getRequestURI().toString(); 
        myLogger.setRequestURL(requestURL); // http 요청을 받으면 URL을 세팅하며 request 스코프를 생성한다.

        myLogger.log("controller test"); 
        logDemoService.logic("test id");
        return "OK";
    }
}



@Service
@RequiredArgsConstructor
public class LogDemoService {

    private final MyLogger myLogger;
    public void logic(String id){
        myLogger.log("service id = " + id);
    }

}
```
위 코드로 테스트할시 controller에서 오류가 발생한다. request scope는 요청이 들어왔을 때만 만들어지는데, 스프링 컨테이너가 생성되면서 LogDemoController가 만들어지고 의존관계를 주입받는다. *요청이 없음에도 MyLogger를 생성해* 의존관계를 주입하려고 하니 문제가 발생한다.<br/><br/>

**-> 해결은 Provider를 이용하면 된다.**<br/><br/>

컨트롤러에서 requestURL(요청받은 url)을 받아서 mylogger에 저장한다.<br/>
그리고 controller에서 log를 호출하고, service에서 log를 호출해보는 예제이다.<br/><br/>

비지니스 로직이 있는 서비스 계층에서도 로그를 출력해보았다.<br/><br/>

request scope를 사용하지 않고 이 모든 정보를 서비스 계층에 넘기면 파라미터가 지저분해지고, requestURL과 같은 웹과 실질적인 관련이 없는 서비스 계층까지 넘어가기 때문에 컨트롤러까지만 사용하는 것이 좋다. 순수하게 유지하는 것이 유지보수에 좋다.<br/>

-> request scope의 MyLogger로 파라미터를 넘기지 않고, 멤버 변수에 저장해서 코드와 계층을 깔끔하게 유지할 수 있다.<br/>

+) requestURL을 mylogger에 저장하는 과정은 컨트롤러보단 공통 처리가 가능한 스프링 인터셉터나 서블릿 필터 같은 곳에 활용하는 것이 좋다. (인터셉터는 http 요청을 공통적으로 처리하는 것을 말한다.)<br/><br/>

### request 스코프 - Provider
```java
private final ObjectProvider<MyLogger> myLoggerProvider;
```
MyLogger를 lookup해서 제공해 주입해주는 Provider를 만든다.<br/>

```java
@RequestMapping("log-demo")
@ResponseBody
public String logDemo(HttpServletRequest request){
    String requestURL = request.getRequestURI().toString();
    MyLogger myLogger = myLoggerProvider.getObject();
    myLogger.setRequestURL(requestURL);

    myLogger.log("controller test");
    logDemoService.logic("test id");
    return "OK";
}
```

[7fc330f6-f647-4be7-80a2-29685219732e] request scope bean create: hello.core.common.MyLogger@286d8730<br/>

[7fc330f6-f647-4be7-80a2-29685219732e][/log-demo] controller test<br/>

[7fc330f6-f647-4be7-80a2-29685219732e][/log-demo] service id = test id<br/>

[7fc330f6-f647-4be7-80a2-29685219732e] request scope bean close: hello.core.common.MyLogger@286d8730<br/>

localhost:8080/log-demo로 들어가면 위와 같은 로그가 남는다.<br/>

그리고 여러 번 새로고침하면 서로 다른 uuid를 가진다는 것을 확인할 수 있다.<br/>

ObjectProvider를 통해 스프링 컨테이너에 요청을 지연할 수 있었다.<br/><br/>

### request 스코프 - 프록시
Provider를 이용하는 방식도 좋지만 코드를 조금 더 간결하게 표현할 수 있다.<br/>
```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```
인터페이스면 INTERFACES를 선택하고, 위와 같이 클래스면 TARGET_CLASS를 선택한다.<br/>

위와 같이 proxyMode 옵션을 추가해서 Provider 사용없이 request scope를 이용할 수 있다. 옵션만 추가하면 LogDemoController 빈이 생성되면서 의존관계 주입에 문제가 발생하지 않는다.<br/>

Provider를 주입하듯이 가짜 프록시 클래스를 만들어두고 미리 빈에 주입해둘 수 있는 것이다.<br/>

```java
System.out.println("myLogger = " + myLogger.getClass());
```

myLogger = class hello.core.common.MyLoggerSpringCGLIB0<br/>

위 코드를 실행시키면 순수한 MyLogger 클래스가 아닌 스프링 컨테이너에 가짜 프록시 객체를 주입해둔 것을 확인할 수 있다. 이 프록시 객체에 요청이 들어오면 그때 진짜 빈을 요청하는 위임 로직이 들어가있다. (바이트코드 라이브러리 이용)<br/>

이런 프로토 타입, 프록시를 이용하는 request 스코프 등은 유지보수에 어려움이 있어서 꼭 필요할 때 사용하는 것이 좋다.
<br/><br/><br/>

# 모든 개발자를 위한 HTTP 웹 기본 지식
## 인터넷 네트워크
### IP(인터넷 프로토콜)
컴퓨터는 고유한 IP주소를 가지고 있다. 패킷이라는 통신 단위로 데이터를 전달한다.<br/>
IP 패킷은 전송할 데이터에 출발지 IP, 목적지 IP 등의 정보를 함께 담고 있다. 그래서 그 패킷을 인터넷 망에 보내 전달이 된다.<br/>
이때 요청할 때와 응답할 때 서로 다른 경로로 패킷이 전달될 수 있다.<br/><br/>

IP 프로토콜의 한계점<br/>
* **비연결성**: 패킷을 받을 대상이 없거나 서비스 불능 상태여도 패킷이 전송된다.
* **비신뢰성**: 중간에 패킷이 사라질 수 있고, 패킷이 순서대로 오지 않을 수 있다.
* **프로그램 구분**: 같은 IP 서버에서 통신하는 어플리케이션이 둘 이상인 경우 구분이 어렵다.<br/>

인터넷 망 노드가 다운되면, 패킷이 사라져버릴 수 있다.<br/>
용량이 큰 패킷을 나눠서 보내는데 서로 다른 경로를 거쳐서 가다가 원래 의도했던 순서대로 목적지에 도착하지 않을 수 있다.<br/><br/>

### TCP, UDP
패킷 소실, 서비스 불능, 순서 문제가 발생하면 TCP, UDP가 해결한다.<br/>

**인터넷 프로토콜 스택의 4계층**
1. 애플리케이션(socket 라이브러리)
2. 전송 계층(TCP, UDP)
3. 인터넷 계층(IP)
4. 네트워크 인터페이스(LAN 드라이버, 장비)
<br/>

4계층을 거쳐서 겉에 정보를 덧씌여 가면서 패킷을 보내기 때문에 IP패킷의 문제점을 해결할 수 있다.<br/><br/>

**전송 제어 프로토콜(TCP)**
* 연결지향 - 3-way handshake
* 데이터 전달 보증
* 순서 보장<br/><br/>

**3-way handshake:**
1. SYN(접속 요청)
2. SYN + ACK(요청 수락)
3. ACK<br/>
서버와 클라이언트가 이제 신뢰할 수 있다.<br/>

* 3-way handshake는 가상 연결이다.
* 세번째 ACK와 함께 데이터를 전송하기도 한다.<br/><br/>

**데이터 전달 보증**: 데이터를 전송했을 때 서버가 데이터를 잘 받았다고 응답을 보냄으로써 데이터가 잘 전달이 됐는지 확인할 수 있다.<br/>

**순서 보장**: 패킷이 잘못된 순서로 도착하면 클라이언트에게 순서가 잘못된 패킷부터 다시 보내라고 요청을 한다.<br/>

-> TCP/IP패킷은 IP 패킷과 TCP segment를 붙이는데 이 TCP 정보가 위의 세가지 문제를 해결하는 것이다.<br/><br/>

**사용자 데이터그램 프로토콜(UDP)**: 기능이 거의 없다. (위의 TCP의 기능 같은) 그래서 데이터 전송과 순서가 보장되진 않지만 단순하고 빠르다.<br/>
-> IP와 거의 같은데 PORT와 체크섬이 추가된다.<br/>

* Port를 통해 패킷들의 애플리케이션들을 구분해준다. (ex 게임인지, 음악인지)
* TCP는 손을 댈 수 없지만, UDP는 애플리케이션에서 추가적으로 작업이 가능하다.
<br/><br/>

### PORT
클라이언트 여러 개의 서버와 통신, 한 서버에 여러 개의 애플리케이션 통신을 하고 있는데 이를 구분하는 것이 PORT가 한다.<br/>
TCP 세그먼트에 출발지 PORT, 목적지 PORT 정보를 포함하고 있다. + 전송 순서, 검증 정보 등도 있다.<br/>
같은 IP 내에서 프로세스를 구분하는 것이 PORT다.<br/><br/>

* 0 ~ 65635: 할당 가능
* 0 ~ 1023: 잘 알려진 포트, 사용하지 않는 것이 좋다.
ex ) http - 80 port<br/><br/>

### DNS
IP는 한눈에 보기 힘들고, IP가 쉽게 바뀔 수 있다. 그래서 *DNS(도메인 네임 시스템)*가 존재한다.<br/>

클라이언트가 도메인 명을 입력해 요청하면 DNS 서버로 간다. -> DNS 서버에는 hash table같이 도메인 명과 매칭된 IP주소가 저장되어 있다.<br/>
-> 그 IP주소를 클라이언트에 보내 응답한다. 그래서 클라이언트가 그 IP로 접속한다.<br/>

* DNS 서버는 전화번호부 같은 역할이다.
* 나중에 서버의 IP주소가 변경되면 DNS 서버의 주소만 변경하면 된다.
<br/><br/><br/>

# 백준 문제 풀이
* lamda 함수는 익명 함수라고도 불린다.<br/>
형태는 lamda 람다 인자 : 표현식 이다.<br/>
```python
def add(a, b)
    return a + b
```
위 함수를 아래와 같이 정의 할 수 있다.<br/>
```python
add = lamda x, y : x + y
```

[age, name] 중에 age만 비교하고 싶다면 아래 코드를 사용하면 된다.<br/>
```python
sort(key = lamda x : x[0])
```
또한 아래와 같이 filter 함수와 함께 사용할 수 있다.<br/>
```python
my_list2 = list(filter(lamda x: x % 2 == 1, my_list))
```
* min(list, key=len) or max(list, key=len): list 안의 문자열을 길이 순으로 반환<br/>
* str.sort(): list 안의 문자열을 사전 순으로 정렬
* str.sort(key=len): list 안의 문자열을 길이 순으로 정렬<br/>

* 브루트 포스(Brute Force) 알고리즘
brute: 무식한, force:힘 즉 무차별 대입으로 문제를 해결한다.<br/>
완전 탐색 알고리즘으로 모든 경우의 수를 탐색하는 방식으로, 단순하고 확실하지만 시간 복잡도가 높다.<br/>

* n의 index 찾는 함수
1. find(): 
    * 문자열
    * 찾는 문자가 없는 경우 -1 반환
2. index(): 
    * 문자열, 튜플, 리스트
    * 찾는 문자가 없는 경우 ValueError 발생
+ 인덱스를 뒤에서부터 찾고 싶으면 rfind(), rindex() 함수를 이용하면 된다.<br/>

* 
```python
if n % 5 == 0 and n % 3 == 0:
    print("FizzBuzz")
elif n % 5 == 0:
    print("Buzz")
elif n % 3 == 0:
    print("Fizz")
else:
    print(n)
```
위 코드를 아래와 같이 바꿀 수 있다.
```python
print('Fizz' * (n % 3 == 0) + 'Buzz' * (n % 5 == 0) or n)
```

* for i in range(start, end, n): start부터 end까지 n씩 증가하면서 반복한다.
* for [x, y] in [[1, 2], [3, 4], [5, 6]]:
* for x in [list]:
* for x in {set}:
* for x in "String":<br/>

* sys.stdin.readline()은 개행 문자를 포함하는 문자열을 반환한다.
 .strip()을 붙이면 개행 문제를 포함하지 않고 반환한다.












    



