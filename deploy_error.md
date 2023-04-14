# React 흰 화면 문제

> [이전글](https://github.com/happyjamy/TIL/blob/main/deploy.md)에서 설명 한 것과 같이 React build 파일을 배포하다가 흰화면과 여러 에러들을 마주하였다... 나는 React 개발을 해본적도 없거니와 네트워크 공부도 약해서 클라이언트 개발자분과 같이 상황을 공유해 고치려고 했다.

<br/>

## Request URL

- http://HOST/client/user  
  현재 클라이언트에서 사용하는 baseUrl은 `/client` 이고 메인페이지가 위 URL을 사용한다.

**APP.tsx**

```
const App = () => {
  return (
    <BrowserRouter basename="client">
      <Routes>
        <Route element={<Layout />}>
          <Route path="/user" element={<Home />} />
          <Route path="/user/login" element={<Signin />} />
          <Route path="/user/registration" element={<Register />} />
          <Route path="/user/my" element={<MyPage />} />
          <Route path="/user/noticeb" element={<NoticeBoard />} />
          <Route path="/user/referb" element={<ReferBoard />} />
          <Route path="/qnaRegi" element={<QnARegi />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
export default App;
```

홈페이지 path 설정이 이런식으로 되어있다.

<br/>

## 브라우저 console에 뜬 에러메세지

1. Refused to execute script from 'http://HOST/static/js/main.ff4f5da6.js' because its MIME type ('text/html') is not executable, and strict MIME type checking is enabled.
2. GET http://HOST/static/js/main.ff4f5da6.js net::ERR_ABORTED 404 (Not Found)
3. client:1 Refused to apply style from 'http://HOST/static/css/main.e6c13ad2.css' because its MIME type ('text/html') is not a supported stylesheet MIME type, and strict MIME checking is enabled.

저 3개의 에러가 console 에 동시에 떴고 1,3은 MIME type이 안맞는다는 말같고  
2는 아예 저 URL 자체의 GET 요청이 불가능하다는 말이었다. (유효하지 않은 URL)

분명 프록시 서버에서 `/client/*` 로 잘 매핑 해뒀는데 왜 404가 뜨는지 처음에는 전혀 이해하지 못했다...

## base Url 설정이 안되고 있다

그러다가 요청이 `http://HOST/client/static/js/main.ff4f5da6.js` 으로 가야 프록시서버가 배포한 클라이언트 파일에 연결 시켜주는데, 클라이언트 build 폴더에 index.html 파일은 `main.ff4f5da6.js` 라는 파일을 `http://HOST/static/js/main.ff4f5da6.js` 이런식으로 그냥 `/client` 를 넣지 않고 찾는다는 사실을 알았다.

**그림으로 표현하면 이런 상황이다.**  
![에러상황](https://user-images.githubusercontent.com/78072370/232095551-75327bee-df76-4edf-85ef-dbe6e19b2e41.png)

실제로 첫요청 이후에는 `index.html` 파일이 다른 요청들을 보낼때 `/client` path를 안붙여주기 때문에 프록시 서버가 제대로 자원을 못찾아 404를 뜨는 사실을 인식했다.

<br/>

## SPA 작동방식

이후 상황을 클라이언트 개발자님과 공유했고, React를 build 하거나 실행시킬때 별다른 설정을 하지 않으면 모든 구동은 `homepage`로 설정한 URL을 기반으로 작동한다는 사실을 알게되었다.  
React는 SPA 방식으로 작동하는데 이런 구조를 간과한 결과인 것같았다🥺  
매번 root에서 path 설정을 해서 이런 문제가 있는지 몰랐고 base Url 설정하면 자동으로 React도 그곳을 root로 인식하는줄 알았다...

### SPA 란?

과거에는 페이지 로딩 요청을 서버에 보내면, 서버가 URL에 따라 새로운 html 페이지(정적 리소스)를 보내주고 이것을 렌더링 하는 방식으로 웹뷰를 관리했다.(MPA 방식) 하지만 이 방식은 매번 요청마다 새로고침이 발생되고 불필요한 부분도 새로 로드해 매우 비효율적이어서 속도,사용성,반응성 모두 좋지 못하다. 반면 **SPA(Single Page Application)은 처음에 이 웹에서 필요한 모든 정적 리소스를 한번에 다 다운 받는다.**
다른 페이지(URL)로 이동을 원하면, 페이지 갱신에 필요한 데이터만을 JSON으로 전달받아 페이지를 갱신하므로 전체적인 트래픽을 감소할 수 있고, 전체 페이지를 다시 렌더링하지 않고 변경되는 부분만을 갱신하므로 새로고침이 발생하지 않아 속도,사용성,반응성이 모두 MPA에 비해 우수한 것을 알 수 있다. 단점으로는 초기 구동속도가 느리고 SEO (검색엔진 최적화) 문제가 있다.

<br/>

### 그럼 SPA는 어떻게 동작할까? (근데 React를 곁들인)

**1단계**

1. 맨 처음 요청에는 index.html 파일을 보내준다.
2. 처음에는 브라우저에 어떤 JS 코드도 로드 되어있지 않다. 그렇기에 다음 요청은 항상 (index.html 상에 존재하는) **리액트 서버에 대한 요청**이 된다.
3. 그러면 React 및 React Router 등을 로드하는 데 필요한 스크립트 태그가 포함된 페이지가 반환된다. (ex. main.js | main.css)

**2단계 : 유저 클릭 이벤트가 발생할때 (새로운 페이지를 요청할때)**

4. URL 은 locally changed 되지만 (브라우저 상에서 바뀌지만) **서버에 대한 요청은 없다.** 대신 React Router가 클라이언트 측에서 작업을 수행하고 렌더링할 React 뷰를 결정하고 렌더링한다.

따라서 기본적으로 새로운 페이지 로드 요청이 오면, 페이지를 새로 고침 하지 않고 주소 표시줄의 URL을 조작하는 일부 JavaScript가 실행되어 React Router가 클라이언트 측에서 페이지 전환을 수행하게 한다는 것이다.  
그래서 우리는 배포한 서버에 static html을 잘 배치하고 URL을 제대로 알려줘야 알잘딱깔센으로 파일을 가져올 수 있는 것이다.

<br/>

## 나의 해결법

- 우선 proxy 서버에 요청이 잘 들어가게 하기 위해서 모든 요청에 `/client`를 붙이는 설정을 해줘야 했다. (root 를 변경하는) 방법은 2가지 이다.
  - .env 에 `PUBLIC_URL` 환경변수로 설정해 주기  
    ex. `PUBLIC_URL=/client`
  - package.json 파일에서 hompage 추가해주기  
    ex. `hompage: "/client"`
- 만약 배포를 실행파일을 시작시키는 방식으로 했다면 여기까지만 해도 작동이 잘 된다.  
  하지만 build 파일을 serve 하는 방식으로 배포했다면 `index.html` 파일 이후 request URL이 `http://HOST/client/static/js/main.ff4f5da6.js` 로 되기 때문에 build 폴더 안에 생성되는 static 폴더를 client 라는 폴더를 하나 만든 뒤 거기에 넣어줘야 한다.

<br/>

**빌드 파일 구조가 이런식으로 되어야 한다는 것이다**  
![빌드구조](https://user-images.githubusercontent.com/78072370/232095345-f6356d53-0cfe-49ef-8a54-47ace1c08a5f.png)  
나는 .env 에서 `BUILD_PATH=build/client` 환경변수 설정을 하고,  
package.json 파일 scripts 설정에서 `npm run build` 실행이 될때 index.html 파일 위치를 옮기도록 `"build": "react-scripts build && mv ./build/client/index.html ./build/index.html"` build를 이렇게 수정해서 썼다.

## 결론

이 사실을 알기까지 수 많은 네트워크 패킷 확인과 리액트 실행방식 spa까지 여러 공부들이 병행 됐다.  
모든 기술은 그냥 실행 결과만 알고 썼다가는 찝찝함과 더불어 엄청난 에러를 남긴다는 사실을 알게 되었다. 모든걸 아는채로 코드를 쓰지는 못하겠지만 ~~(그럼 아무것도 못써)~~ 적어도 명령어만 알고 대충 코딩하는 습관을 버려야겠다는 생각을 했다! 또한 서버 개발자로서 시간관계상 클라이언트 버그를 같이 디버깅하면서 새로운 사실들을 알게되어 너무 재미있었다 ㅎㅎ 시간이 나면 전체적인 네트워크 공부와 더불어 클라이언트 공부도 하고싶다...

---

<br/>

## Reference

[리액트 구동방식관련 stackoverflow](https://stackoverflow.com/questions/27928372/react-router-urls-dont-work-when-refreshing-or-writing-manually?rq=1)  
[SPA](https://velog.io/@gwanuuoo/SPA%EB%8A%94-%EA%B8%B0%EC%A1%B4-%EC%9B%B9%EC%82%AC%EC%9D%B4%ED%8A%B8%EC%99%80-%EC%B0%A8%EC%9D%B4)  
[React 환경변수](https://create-react-app.dev/docs/advanced-configuration/#:~:text=for%20more%20details.-,WDS_SOCKET_PORT,-%E2%9C%85%20Used)
