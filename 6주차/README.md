# 모든 개발자를 위한 HTTP 웹 기본 지식 (김영한 강의)
Link: https://www.inflearn.com/course/http-%EC%9B%B9-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC
## URI와 웹 브라우저 요청 흐름
### URI
**URI(uniform resource identifier)**
* URI는 locater, name 또는 둘 다 추가로 분류될 수 있다.
* URL(Resource Locator), URN(Resource Name)
<br/>
resource: 자원, URI로 식별할 수 있는 모든 것
<br/><br/>

### **URL 문법**: scheme://[userinfo@]host[:port][/path][?query][#fragment]
<br/>
ex) https://www.google.com/search?q=dog&oq=do
<br/><br/>
 
**scheme**: 주로 프로토콜을 사용한다.
* 프로토콜: 어떤 방식으로 자원에 접근할 지에 대한 약속 규칙
ex) http, https, ftp
* http는 80포트, https는 443을 주로 사용하고 포트는 생략이 가능하다.
* https는 http에 보안을 추가한 것이다.
<br/>

**userinfo**: 사용자 정보를 인증할 때 사용, 거의 사용하지 않는다.<br/>

**host**: 호스트 명으로 도메인 명이나 아이피 주소를 직접 입력할 수 있다.<br/>

**port**: 접속 포트, 일반적으로 생략한다.<br/>

**path**: 리소스 경로, 계층적 구조이다.<br/>

**query**: ket=value 형태, ?로 시작하며 &으로 추가 가능하다<br/>

**fragment**: html 내부 북마크 등에 사용하고 서버에 전송하는 것은 아니다.<br/>
<br/>

### 웹 브라우저 요청 흐름
1. DNS를 조회한다.
2. https port는 생략한다.
3. http 요청 메시지를 생성한다.<br/>
GET /search?q=dog&hl=do HTTP/1.1<br/>
Host: www.google.com
4. 웹 브라우저가 생성한 http message를 socket 라이브러리를 통해 전달한다.
5. TCP/IP 패킷을 생성하고, http 메시지를 포함한다.
6. 인터넷 망으로 던지면 서버로 전달이 된다.
7. 패킷을 까서 http 메시지를 해석하고 http 응답 메시지를 보낸다.
8. 웹 브라우저가 html 렌더링 후 사람이 확인할 수 있다.
<br/><br/><br/>

## HTTP 기본
### 모든 것이 HTTP
**http(HyperText Transfer Protocol)**: 모든 것이 http로 전송된다. html, text, image, 영상, JSON, XML (API), 거의 모든 형태의 데이터를 전송할 수 있다.<br/>

* TCP: HTTP/1.1, HTTP/2
* UDP: HTTP/3
주로 1.1을 사용하지만 2와 3의 사용이 증가하고 있다. (http/3는 UDP를 사용해서 3-way handshaking을 하지 않아 빠르다.)
<br/><br/>

### 클라이언트 서버 구조
1. HTTP 특징: **클라이언트 서버 구조**<br/>
Request, Response의 구조이다.<br/><br/>

클라이언트는 서버에 요청을 보내고, 응답을 대기한다. 서버가 요청에 대한 결과를 만들어서 응답한다.<br/>

(여기서 중요한 것은 클라이언트와 서버를 개념적으로 분리하는 것이다. 비지니스 로직이나 데이터는 서버에 모두 밀어 넣고, 클라이언트는 UI에 집중한다. 그래서 둘 다 독립적으로 발전될 수 있다.)<br/><br/>

### stateful, stateless
2. HTTP 특징: **stateless (무상태 프로토콜)**<br/>
서버가 클라이언트의 상태를 보존하지 않는다.<br/><br/>

클라이언트 요청이 증가해도 서버를 대거 투입할 수 있다.<br/><br/>

무상태는 응답 서버를 쉽게 바꿀 수 있다. -> **필요하면 무한하게 서버를 증설할 수 있다. (스케일 아웃)**<br/>
또한 stateful인 경우, 클라이언트A가 서버1과 통신하고 있으면 서버1가 다운될 시 클라이언트A가 요청을 처음부터 다시 보내야한다. (서버1만 클라이언트의 상태를 저장하고 있기 때문이다)
<br/><br/>

*한계점*: 모든 것을 무상태로 설계할 수 없는 경우도 있다.<br/>
ex) 로그인 (로그인 했다는 상태를 서버에서 유지해야만 한다. 보통 쿠키와 서버 세션등을 사용해서 상태를 유지한다.)<br/><br/>

### 비연결성(connectionless)
3. HTTP 특징: **비연결성**<br/>
서버와 클라이언트가 연결을 계속 유지하면 요청을 보내지 않더라도 서버 자원을 소모한다. 그런데 http 프로토콜은 요청을 하고 응답을 보내면 바로 연결을 끊는다.<br/>

클라이언트가 항상 요청을 보내는 것이 아니기 때문에 연결을 유지해서 서버 자원을 쓸 필요가 없다.<br/><br/>


*한계점*:
* TCP/IP 연결을 새로 맺어야 한다. - 3-way handshaking 시간이 추가된다.
* 또한 웹 브라우저에서 사이트 요청을 한다면 html 뿐만 아니라 자바스크립트, css, 추가 이미지 등 많은 자원이 함께 다운로드 된다. 그래서 여러 리소스를 따로따로 연결하고 종료해서 다운받는 것은 비효율적이다.<br/>
-> *현재는 HTTP 지속 연결(Persistent Connections)을 사용한다.*<br/><br/>

*지속 연결*:
1. 3-way handshaking
2. 요청 / html 응답, 요청 / 자바스크립트 응답 등
3. 종료
<br/>

**즉, HTTP는 비연결성의 특징이 있지만, 지속 연결을 사용한다.**
<br/><br/>

그러나 선착순 이벤트나 티켓팅과 같은 한 시점에 요청이 몰리는 경우 비연결성이 의미가 없다.<br/>
-> stateless로 설계하자. 그래야 서버를 늘리기에 유리하다.
<br/><br/>

### HTTP 메시지 구조
1. start-line
2. header
3. empty line (CRLF)
4. message body 
<br/><br/>

요청 메시지 예시:
```
GET /search?... HTTP/1.1 <- start-line
Host:www.google.com <- header
empty line
```
요청도 메시지 바디를 가질 수 있다.<br/><br/>

응답 메시지 예시:
```
HTTP/1.1 200 OK <- start-line
Content-Type: text/html;charset=UTF-8
Content-Length: 3121 <- header
empty line
<html> 
<body>..</body>
</html> <- message body 
```
<br/>

### 요청 메시지
* **시작 라인(start-line)** = request-line
request-line = method SP(공백) request-target SP HTTP-version CRLF(엔터)<br/><br/>

1. HTTP 메서드 -> GET, POST(요청 내역 처리), PUT, DELETE 등
2. 요청 대상 -> absolute-path[?query], 절대 경로로 시작한다.
3. HTTP 버전

### 응답 메시지
* **start-line** = status-line<br/>
status-line = HTTP-version SP status-code SP reason-phrase CRLF<br/><br/>

상태 코드(요청이 성공했는지, 실패 했는지)를 확인 할 수 있다. 200은 성공, 400은 클라이언트 요청 오류, 500은 서버 내부 오류이다. 그리고 이유 문구로 끝이 난다.<br/>

* **Header**:<br/>
header-field = field-name":" OWS field-value OWS (OWS: 띄어쓰기 허용)<br/>

HTTP 전송에 필요한 모든 부가정보를 담고 있다.<br/><br/>

* **메시지 바디**:<br/>
(실제 전송할) byte로 표현 가능한 모든 데이터<br/>
<br/><br/>

## HTTP 메서드
### HTTP API 설계
HTTP API를 설계할 때 **리소스 식별**이 중요하다.<br/>
이때 회원을 수정하고 조회하는 행동이 리소스가 아니라 회원이라는 개념 자체가 리소스다.<br/><br/>

그래서 설계를 한다면,<br/>
* 회원 목록 조회 /members
* 회원 조회 /members/{id}
* 회원 등록 /members/{id}<br/>

**리소스를 식별**할 수 있고, URI의 **계층 구조(path)**로 만들었다.<br/><br/>

그럼 회원을 등록하고 조회하고 삭제하는 API가 모두 같은데 어떻게 구분을 할 것인가? -> **메서드**를 이용한다.><br/>
URI는 오직 리소스만 식별을 하는 것이고, 리소스와 행위를 분리하는 것이다. 행위는 조회, 등록, 삭제, 변경과 같은 메서드를 사용해야 한다.<br/>

+) 정리
* 리소스 = URL(URI) // 리소스 식별, URI 계층구조
* 행위 = 메서드
<br/><br/>

### HTTP 메서드
* GET: 리소스 조회
* POST: 요청 데이터 처리, 등록
* PUT: 리소스를 대체, 해당 리소스가 없다면 생성
* PATCH: 리소스 부분 변경
* DELETE: 리소스 삭제<br/>
이외의 기타 메서드들이 존재한다.<br/><br>

1. **GET** <br/>
리소스 조회, 서버에 전달하고 싶은 데이터는 query를 통해서 전달한다.<br/><br/>

예를 들어,
```
GET /members/100 HTTP/1.1
Host:...
```
요청 메시지를 던진다.<br/><br/>

```
(
    "username": "kim",
    "age": 20
)
```
서버에서 위의 메시지 바디와 함께 응답 메시지를 던진다.<br/>
<br/>

### POST
요청 데이터 처리, 메시지 바디를 통해 서버로 요청 데이터를 전달한다.<br/><br/>

예를 들어,
```
POST /members HTTP/1.1
Content-Type: application/json
{
    "username": "kim",
    "age": 20
}
```
요청 메시지를 보낸다.<br/><br/>

그럼 서버는 members/100 라는 신규 식별자를 생성하고,<br/>
```
HTTP/1.1 201 Created
...
Location: /members/100 <- 생성된 리소스의 경로
{
    "username": "kim",
    "age": 20
}
```
응답 메시지를 보낸다.<br/><br/>

POST 메서드는 단순히 게시하는 것만이 아니라 다양한 기능을 한다.<br/>
* html 양식으로 입력된 필드와 같은 데이터 블록을 데이터 처리 프로세스에 제공한다.
* 블로그에 메시지를 게시한다.
* 서버가 아직 식별하지 않은 새 리소스를 생성한다.
* 기존 자원에 데이터를 추가한다.
* 다른 메서드를 사용하기 애매한 경우에 POST를 사용해도 좋다.<br/><br/>

URL에 POST 요청이 들어오면 **요청 데이터를 어떻게 처리할지는 리소스마다 알아서 정의해야 한다.**<br/><br/>

+ 리소스 단위로 설계가 어려운 경우, /orders/{orderId}/start-delivery <br/>
URI에는 계층 구조로 리소스만을 사용하려고 하되, 설계가 어려운 경우 위와 같은 컨트롤 URI을 사용할 수도 있다.<br/><br/>


3. **PUT**<br/>
리소스를 대체, 리소스가 없다면 생성한다.<br/>
POST와 다르게 클라이언트가 리소스를 식별한다.<br/>
ex) PUT /members/**100** <br/><br/>

PUT /members/100 HTTP/1.1<br/>
...<br/>
덮어 씌운다고 생각하면 된다. (원래 리소스를 완전히 대체한다! age만 PUT하면 원래 있던 username field는 사라진다. 리소스를 지우고 생성한다고 생각하면 된다.)<br/><br/>

4. **PATCH**<br/>
리소스를 부분 변경한다. 이 또한 post와 다르게 클라이언트가 리소스를 식별한다.<br/>
patch가 지원되지 않는 서버도 있다. 이의 경우에는 post를 사용하면 된다.<br/><br/>

PATCH /members/100 HTTP/1.1<br/>
Content-Type: application/json<br/>
{<br/>
    "age": 80<br/>
}<br/>
요청 메시지를 보내면 username은 그대로 두고 age만 변경한다.<br/><br/>

5. **DELETE**<br/>
리소스를 제거한다.<br/><br/>

DELETE /members/100 HTTP/1.1<br/>
Host:...<br/>
요청 메시지를 보내면 members/100 리소스를 제거한다.<br/><br/>

### HTTP 메서드의 속성
1. Safe Methods
2. Idempotent Methods (멱등)
3. Cacheable Methods (캐시 가능)
<br/><br/>

1. **Safe** <br/>
* 호출해도 리소스를 변경하지 않는다.
* GET, HEAD와 같은 메서드 (변경이 되는 POST, PUT, PATCH, DELETE는 안전하지 않다.)
<br/>

2. **Idempotent** <br/>
* 몇 번을 호출하든 결과가 똑같다.
* GET, PUT, DELETE와 같은 메서드가 멱등 메서드이다.
    * PUT은 결과를 대체하기 때문에 똑같은 PUT 요청을 여러번 날려도 최종 결과는 항상 같다.
    * DELETE를 몇 번 호출하든 삭제된 결과는 항상 같다.
    * POST의 경우 멱등이 아니다. 여러 번 결제를 중복하는 것과 같다.

멱등 메서드의 활용의 예로, *자동 복구 메커니즘*이 있다.<br/>
예를 들어 DELETE 요청을 보냈는데 정상 응답을 주지 못했다. 그러나 또 DELETE 요청을 보내도 원하는 결과에 대해선 문제가 없다.<br/>
이때 재요청을 하는 중간에 다른 곳에서 리소스를 변경해버리면 문제가 생기지만 이런 케이스는 고려하지 않는다.<br/><br/>

3. **Cacheable** <br/>
* 응답 결과 리소스를 캐시해서 사용해도 되는 메서드를 말한다.
* GET, HEAD, POST, PATCH는 캐시 가능 메서드이다.<br/>
(그러나 실제론 GET, HEAD 정도만 캐시로 사용한다. POST, PATCH는 메시지 바디까지 캐시 키를 고려해야 하기 때문에 잘 사용하지 않는다.)
<br/><br/><br/>

# 백준 문제 풀이
* 1018 체스판 다시 칠하기
```python
for x in range(N, N+8):
    for y in range(M, M+8):
        if (x + y) % 2 == 0: # 체스판 모양으로 서로 다름을 확인
            if board[x][y] != 'B':
                index1 += 1
            if board[x][y] != 'W':
                index2 += 1
            else:
                if board[x][y] != 'W':
                    index1 += 1
                if board[x][y] != 'B':
                    index2 += 1
```
<br/>

* 이진 탐색(Binary search)<br/>
정렬된 데이터에서 특정한 값을 찾는다. 탐색 범위를 반으로 줄여가면서 찾으며 시간 복잡도는 O(log n)이다.<br/>
그래서 대용량 데이터에서 특정한 위치를 찾는 데 유용하다. 단점으론 정렬된 데이터에서만 가능하다는 것이며, 생성과 수정에 취약하다.<br/><br/>

1. 중간 인덱스(mid)를 찾는다.
2. target과 mid_value를 비교한다.
3. target이 mid_value보다 작으면 mid를 기준으로 왼쪽 배열을 탐색한다.
    그렇지 않으면 mid를 기준으로 오른쪽 배열을 탐색한다.
4. target과 mid_value가 같을 때까지 위의 과정을 반복한다.
<br/>

* list에서는 in, not in의 시간복잡도가 O(N)이고, set에서는 in, not in의 시간복잡도가 O(1)이다.<br/>

* list에서 insert는 시간복잡도가 O(N)이고,<br/>
deque에서 appendleft는 시간복잡도가 O(1)이다.<br/><br/>

덱은 double-ended queue로 양 끝의 elements를 삭제/삽입한다. <br/>
double-linked list로 구현돼 삭제/삽입의 시간 복잡도는 O(1)이다.<br/><br/>

list는 고정된 사이즈를 갖는 array 형태다. <br/>
그래서 맨 앞에 있는 요소를 삭제/삽입하면 다른 요소들도 함께 이동을 해야하므로 삽입/삭제에 O(n)의 시간복잡도를 가진다. <br/>

* 그리디 알고리즘: 현재 상황에서 가장 좋은 것을 고르는 알고리즘이다. 하지만 최종적인 결과에 대해 최적해를 보장해주진 않는다.<br/>
그래서 그리디 알고리즘을 사용하려면 항상 안전하다는 것이 보장되어야 한다. 즉 최적해를 도출해내야 한다.





