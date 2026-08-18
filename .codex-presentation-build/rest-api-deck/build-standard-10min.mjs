import fs from "node:fs/promises";
import { Presentation, PresentationFile } from "@oai/artifact-tool";

const OUT = "/Users/nam-yunjae/shinhan-delivery/REST_API_기술성과발표_남윤재_10분_7장.pptx";
const SCRIPT = "/Users/nam-yunjae/shinhan-delivery/REST_API_기술성과발표_남윤재_10분_7장_대본.txt";
const RENDER = "/Users/nam-yunjae/shinhan-delivery/.codex-presentation-build/rest-api-deck/standard-10min-7slides-rendered";
const C = { white:"#FFFFFF", ink:"#101114", muted:"#616771", panel:"#F0F2F4", line:"#BBC1C9", blue:"#3D8DFF", sky:"#DDF3FD", dark:"#132238", green:"#16845B", orange:"#D46B16", red:"#BE3B3B", code:"#111827", codeText:"#E8EFF8" };
const deck = Presentation.create({ slideSize: { width:1280, height:720 } });

function rect(s,l,t,w,h,fill=C.panel,line="none",round=false){return s.shapes.add({geometry:round?"roundRect":"rect",position:{left:l,top:t,width:w,height:h},fill,line:{style:"solid",fill:line,width:line==="none"?0:1},...(round?{borderRadius:"rounded-xl"}:{})});}
function txt(s,v,l,t,w,h,o={}){const x=s.shapes.add({geometry:"textbox",position:{left:l,top:t,width:w,height:h},fill:"none",line:{style:"solid",fill:"none",width:0}});x.text=v;x.text.style={fontSize:o.size??20,fontFamily:o.font??"Arial",bold:o.bold??false,color:o.color??C.ink,alignment:o.align??"left",verticalAlignment:o.valign??"top"};return x;}
function header(s,n,title){txt(s,"SHINHAN DELIVERY · REST API",64,28,520,22,{size:13,bold:true,color:C.blue});txt(s,title,64,64,1120,56,{size:36,bold:true});txt(s,String(n).padStart(2,"0"),1180,31,36,20,{size:12,bold:true,color:C.muted,align:"right"});rect(s,64,132,1152,2,C.ink);}
function code(s,v,l,t,w,h,size=16){rect(s,l,t,w,h,C.code,C.code,true);txt(s,v,l+20,t+17,w-40,h-34,{size,font:"Menlo",color:C.codeText});}
function foot(s,v="프로젝트 실제 코드 기반 · 표준 5-Slide"){txt(s,v,64,683,700,18,{size:11,color:C.muted});}
function notes(s,body,sources=[]){const src=sources.length?`\n\n[Sources]\n${sources.map(x=>`- ${x}`).join("\n")}`:"";s.speakerNotes.textFrame.setText(body+src);s.speakerNotes.setVisible(true);}
const talk=[]; function saveTalk(n,title,time,body){talk.push(`[Slide ${n} · ${time}] ${title}\n${body}`);}

// Slide 1 — standard cover + executive summary
{
 const s=deck.slides.add();s.background.fill=C.white;
 txt(s,"REST API 기술 성과 발표",68,58,720,50,{size:24,bold:true,color:C.blue});
 txt(s,"배송 상태를 연결하고,\n취소 정산을 안전하게 만든 API",68,145,760,150,{size:51,bold:true});
 txt(s,"남윤재  /  백엔드 & 신한 Delivery",72,320,640,34,{size:21,color:C.muted});
 txt(s,"배송 조회·매칭·취소 흐름을 일관된 HTTP 계약으로 제공하고\n중복·동시 요청에서도 안전한 상태 변경을 보장했습니다.",72,385,780,70,{size:22});
 rect(s,900,70,280,535,C.sky,"none");
 const metrics=[
   ["01","책임 분리","HTML Controller → REST Controller"],
   ["02","상태 자동 반영","수동 새로고침 → 5초 GET 폴링"],
   ["03","정산 일관성","중복 위험 → 트랜잭션·락·멱등"]
 ];
 metrics.forEach((m,i)=>{const y=115+i*155;txt(s,m[0],930,y,45,26,{size:15,bold:true,color:C.blue});txt(s,m[1],930,y+38,220,32,{size:24,bold:true});txt(s,m[2],930,y+82,220,52,{size:16,color:C.dark});if(i<2)rect(s,930,y+139,220,1,C.line);});
 foot(s,"발표자 남윤재 · 약 10분");
 const body="안녕하세요. 백엔드와 신한 Delivery 프로젝트를 담당한 남윤재입니다. 오늘은 배송 조회, 매칭, 취소 흐름을 중심으로 저희 프로젝트의 REST API를 설명하겠습니다. 핵심 성과는 세 가지입니다. 첫째, HTML 화면과 JSON API의 책임을 분리했습니다. 둘째, 매칭 대기 상태를 fetch가 5초마다 조회해 화면에 자동 반영했습니다. 셋째, 취소 API에는 트랜잭션과 DB 락, 멱등 처리를 적용해 중복 요청에서도 정산 일관성을 지켰습니다. fetch와 HATEOAS도 다루지만 발표의 중심은 실제 REST API 설계와 코드입니다.";
 notes(s,body,["docs/presentation/개발자-발표-PPT-표준-템플릿-가이드.md","src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java","src/main/resources/templates/matching-wait.html"]);saveTalk(1,"REST API 기술 성과 발표","1분 15초",body);
}

// Slide 2 — Before / After for greenfield
{
 const s=deck.slides.add();s.background.fill=C.white;header(s,2,"추진 배경 & 주요 목표");
 txt(s,"신규 프로젝트이므로 ‘과거 장애’가 아니라 구현 전 설계 과제와 적용 결과를 비교했습니다.",66,150,1110,32,{size:18,color:C.muted});
 const xs=[64,235,710], ws=[165,465,506];
 ["구분","구현 전 설계 과제 (Before)","REST API 적용 결과 (After)"].forEach((v,i)=>{rect(s,xs[i],198,ws[i],46,i===2?C.sky:C.panel,C.line);txt(s,v,xs[i]+12,211,ws[i]-24,24,{size:17,bold:true,color:i===2?C.dark:C.ink});});
 const rows=[
  ["구조·역할","화면 반환과 데이터·비즈니스 처리가\n한 Controller에 섞일 가능성","@Controller는 HTML, @RestController는 JSON\nController → Service → Repository로 책임 분리"],
  ["사용자 경험","매칭 여부를 확인하려면 사용자가\n전체 화면을 다시 요청해야 할 가능성","fetch가 GET API를 5초마다 호출\nMATCHED 응답 시 완료 화면으로 자동 이동"],
  ["리스크·품질","Entity 직접 노출과 Controller별\n예외 응답 불일치 위험","응답 DTO와 GlobalExceptionHandler 적용\n401·403·404·409를 의미에 맞게 반환"]
 ];
 rows.forEach((r,ri)=>{const y=244+ri*112;[r[0],r[1],r[2]].forEach((v,i)=>{rect(s,xs[i],y,ws[i],112,C.white,C.line);txt(s,v,xs[i]+14,y+18,ws[i]-28,76,{size:i===0?18:17,bold:i===0,color:i===2?C.dark:C.ink});});});
 rect(s,64,598,1152,58,C.dark,C.dark);txt(s,"핵심 가치  |  화면 새로고침 없이 최신 상태를 제공하면서 계층 책임과 API 계약을 명확하게 유지",88,616,1100,26,{size:20,bold:true,color:C.white});
 foot(s);
 const body="이 프로젝트는 기존 시스템을 REST API로 전환한 사례가 아니라 처음부터 새로 만든 프로젝트입니다. 그래서 Before를 과거의 잘못된 구현이라고 표현하지 않고, REST API를 적용하지 않았다면 생길 수 있었던 설계 과제로 정의했습니다. 첫 번째 과제는 화면과 데이터 로직이 한 Controller에 섞이는 것이었습니다. 이를 HTML을 반환하는 Controller와 JSON을 반환하는 RestController로 분리했습니다. 두 번째는 매칭 상태 확인을 위한 화면 새로고침 문제였습니다. fetch의 5초 GET 폴링으로 해결했습니다. 세 번째는 Entity 노출과 예외 응답 불일치 위험입니다. DTO와 GlobalExceptionHandler를 사용해 외부 계약과 상태 코드를 표준화했습니다. 즉, 신규 프로젝트의 장점을 살려 문제가 생긴 뒤 고친 것이 아니라 설계 단계에서 예방했습니다.";
 notes(s,body,["src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryWebController.java","src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java","src/main/java/com/example/shinhandelivery/common/exception/GlobalExceptionHandler.java","src/main/resources/templates/matching-wait.html:218-342"]);saveTalk(2,"추진 배경 & 주요 목표","1분 45초",body);
}

// Slide 3 — REST resources, methods, and status codes
{
 const s=deck.slides.add();s.background.fill=C.white;header(s,3,"배송 요청을 자원과 HTTP 메서드로 표현했다");
 txt(s,"RESOURCE",68,158,150,22,{size:14,bold:true,color:C.blue});
 txt(s,"/api/v1/delivery-requests/{id}",68,190,600,38,{size:27,bold:true});
 txt(s,"명사형 자원 · v1 버전 · Path Variable로 식별",68,237,560,26,{size:17,color:C.muted});
 const rows=[
  ["GET","/{id}","배송 상세 조회","200 OK",C.blue],
  ["POST","/","배송 요청 생성","201 Created",C.green],
  ["PATCH","/{id}/pickup","픽업 상태 변경","200 OK",C.orange],
  ["PATCH","/{id}/complete","배송 완료 처리","200 OK",C.orange],
  ["DELETE","/{id}","기존 호환 취소","204 No Content",C.red]
 ];
 ["METHOD","PATH","PROJECT ACTION","SUCCESS"].forEach((v,i)=>txt(s,v,[68,230,630,1010][i],300,[130,370,330,180][i],22,{size:13,bold:true,color:C.muted}));
 rows.forEach((r,i)=>{const y=330+i*55;rect(s,64,y,1152,47,i%2?C.white:C.panel,"none");txt(s,r[0],80,y+13,120,24,{size:17,bold:true,color:r[4]});txt(s,r[1],230,y+13,370,24,{size:16,font:"Menlo"});txt(s,r[2],630,y+13,330,24,{size:16});txt(s,r[3],1010,y+13,180,24,{size:15,bold:true,align:"right"});});
 rect(s,64,620,1152,46,C.dark,C.dark);txt(s,"실패도 계약이다  |  401 인증 · 403 권한 · 404 자원 없음 · 409 상태 충돌",91,633,1090,24,{size:19,bold:true,color:C.white,align:"center"});
 foot(s);
 const body="REST API 설계의 기본은 URL과 HTTP 메서드의 역할을 나누는 것입니다. 저희 프로젝트는 배송 요청을 delivery-requests라는 복수형 명사 자원으로 표현하고, 특정 배송은 Path Variable인 ID로 식별합니다. GET은 상세 조회, POST는 배송 요청 생성, PATCH는 픽업이나 완료처럼 일부 상태를 변경하는 데 사용했습니다. 기존 클라이언트 호환 취소는 DELETE와 204 응답을 유지합니다. 성공 응답뿐 아니라 실패도 API 계약입니다. 인증이 없으면 401, 다른 사용자의 배송이면 403, 배송이 없으면 404, 허용되지 않는 상태 전이는 409를 반환합니다. URL과 메서드, 상태 코드만 보아도 API의 의도와 결과를 예측할 수 있도록 설계했습니다.";
 notes(s,body,["src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java:59-246","src/main/java/com/example/shinhandelivery/common/exception/ErrorCode.java","docs/architecture/REST-API-설계-규격-가이드.md"]);saveTalk(3,"배송 요청을 자원과 HTTP 메서드로 표현했다","1분 20초",body);
}

// Slide 4 — architecture + actual code/visual flow
{
 const s=deck.slides.add();s.background.fill=C.white;header(s,4,"시스템 구조 & REST API 설계 아키텍처");
 const nodes=[
  [74,"Thymeleaf 화면","matching-wait.html",C.sky],
  [325,"REST Controller","HTTP·인증·DTO",C.panel],
  [576,"Service","비즈니스 규칙",C.panel],
  [827,"Repository","JPA·DB 접근",C.panel],
  [1078,"MariaDB","배송·매칭",C.sky]
 ];
 nodes.forEach((n,i)=>{rect(s,n[0],175,180,92,n[3],C.line,true);txt(s,n[1],n[0]+10,193,160,28,{size:18,bold:true,align:"center"});txt(s,n[2],n[0]+10,229,160,22,{size:14,color:C.muted,align:"center"});if(i<4)txt(s,"→",n[0]+187,204,54,36,{size:27,color:C.blue,align:"center"});});
 txt(s,"GET /api/v1/delivery-requests/{id}   ·   Authorization: Bearer JWT   ·   200 OK + JSON",142,295,996,30,{size:19,bold:true,color:C.blue,align:"center"});
 code(s,"@GetMapping(\"/{deliveryRequestId}\")\npublic ResponseEntity<DeliveryDetailResponse> getDeliveryRequest(...) {\n  return ResponseEntity.ok().cacheControl(CacheControl.noStore())\n      .body(deliveryService.getDeliveryRequestDetail(callerId, deliveryRequestId));\n}",64,350,565,218,15);
 code(s,"const response = await fetch(\n  `/api/v1/delivery-requests/${deliveryId}`,\n  { headers: { Authorization: header }, cache: 'no-store' }\n);\nconst detail = await response.json();",651,350,565,218,15);
 const principles=[
  ["① 단방향 의존성","Controller → Service → Repository"],
  ["② API 계약 분리","Entity 대신 DeliveryDetailResponse"],
  ["③ 상태·보안","JWT 권한 확인 + no-store + 상태 코드"]
 ];
 principles.forEach((v,i)=>{const x=64+i*384;txt(s,v[0],x,594,360,25,{size:17,bold:true,color:C.blue});txt(s,v[1],x,625,360,25,{size:15,color:C.ink});});
 foot(s);
 const body="전체 흐름을 실제 코드로 보겠습니다. 사용자는 먼저 Thymeleaf 매칭 대기 화면을 받습니다. 화면의 JavaScript는 배송 ID를 포함한 GET 요청과 JWT를 REST Controller로 보냅니다. Controller는 HTTP 요청과 인증 정보를 받고 Service를 호출합니다. Service는 배송, 매칭, 차량을 조회하고 고객 본인이나 배정 배송원인지 검사합니다. Repository는 JPA를 통해 MariaDB에 접근합니다. 최종 응답은 Entity가 아니라 DeliveryDetailResponse DTO이며 JSON으로 직렬화됩니다. 화면은 이 JSON을 5초마다 받아 status가 MATCHED이면 완료 화면으로 이동합니다. 클라이언트와 서버 양쪽에 no-store를 적용해 과거 상태가 캐시되지 않게 했습니다. fetch는 REST 자체가 아니라 브라우저가 REST API를 호출하는 수단이라는 점도 중요합니다.";
 notes(s,body,["src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java:102-114","src/main/java/com/example/shinhandelivery/delivery/service/DeliveryService.java:155-172","src/main/java/com/example/shinhandelivery/delivery/dto/response/DeliveryDetailResponse.java"]);saveTalk(4,"시스템 구조 & REST API 설계 아키텍처","1분 20초",body);
}

// Slide 5 — GET polling and fetch
{
 const s=deck.slides.add();s.background.fill=C.white;header(s,5,"매칭 대기 화면은 GET API를 5초마다 호출한다");
 code(s,"const response = await fetch(\n  `/api/v1/delivery-requests/${deliveryId}`,\n  {\n    headers: { Authorization: header },\n    cache: 'no-store'\n  }\n);\nconst detail = await response.json();",64,166,610,300,17);
 const flow=[["1","GET + JWT"],["2","200 OK + JSON"],["3","status 확인"],["4","MATCHED → 화면 이동"]];
 flow.forEach((v,i)=>{const y=168+i*94;txt(s,v[0],760,y,42,36,{size:25,bold:true,color:C.blue,align:"center"});rect(s,816,y-4,350,48,i===3?C.sky:C.panel,"none");txt(s,v[1],836,y+8,310,24,{size:18,bold:true,align:"center"});if(i<3)txt(s,"↓",930,y+53,100,28,{size:21,color:C.blue,align:"center"});});
 txt(s,"const POLL_INTERVAL_MS = 5000;",64,500,610,30,{size:20,bold:true,font:"Menlo",color:C.green});
 rect(s,64,552,1152,80,C.panel,"none");
 txt(s,"401 → 로그인  ·  403 → 권한 없음  ·  404 → 배송 없음  ·  response.ok 확인",88,570,1098,26,{size:18,bold:true,align:"center"});
 txt(s,"fetch는 REST가 아니라 브라우저가 REST API를 호출하는 도구",88,603,1098,22,{size:16,color:C.muted,align:"center"});
 foot(s);
 const body="이 REST API를 화면에서 실제로 사용하는 도구가 fetch입니다. 매칭 대기 화면은 배송 상세 GET API를 즉시 한 번 호출한 뒤 5초마다 반복합니다. Authorization 헤더에는 JWT를 넣고, 캐시된 과거 상태를 사용하지 않도록 no-store를 설정했습니다. 서버 역시 응답에 Cache-Control no-store를 포함합니다. JSON의 status가 REQUESTED이면 계속 기다리고, MATCHED나 PICKED_UP, COMPLETED가 되면 폴링을 중단하고 매칭 완료 화면으로 이동합니다. 오류도 상태 코드별로 처리합니다. 401이면 로그인 화면, 403이면 권한 없음, 404이면 배송 정보 없음 메시지를 보여줍니다. fetch는 404나 500을 자동으로 throw하지 않기 때문에 response.ok를 확인해야 합니다. fetch 자체는 REST가 아니라 브라우저가 REST API를 호출하는 도구입니다.";
 notes(s,body,["src/main/resources/templates/matching-wait.html:218-342","src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java:102-114"]);saveTalk(5,"매칭 대기 화면은 GET API를 5초마다 호출한다","1분 30초",body);
}

// Slide 6 — PRSI + actual code
{
 const s=deck.slides.add();s.background.fill=C.white;header(s,6,"핵심 기술 구현 — 취소 API의 정산 불일치 방지");
 txt(s,"GET /{id}/cancellation-preview",66,151,450,28,{size:19,bold:true,color:C.blue});txt(s,"예상 수수료·환불 조회",66,180,400,24,{size:15,color:C.muted});
 txt(s,"POST /{id}/cancel",530,151,320,28,{size:19,bold:true,color:C.orange});txt(s,"취소·환불·보상 실행",530,180,320,24,{size:15,color:C.muted});
 txt(s,"409 Conflict",930,151,250,28,{size:19,bold:true,color:C.red});txt(s,"허용되지 않은 상태 전이",930,180,250,24,{size:15,color:C.muted});
 const prsi=[
  ["P","Problem","중복 클릭·네트워크 재시도로\n같은 취소 POST가 반복될 수 있음",C.red],
  ["R","Root Cause","취소가 상태 변경뿐 아니라 환불·보상·\n매칭 취소·차량 복구를 함께 수행",C.orange],
  ["S","Solution","@Transactional + 비관적 락 +\n취소 완료 시 기존 결과 반환",C.blue],
  ["I","Impact","중복 요청의 이중 정산 방지\n부분 반영 없이 하나의 작업으로 처리",C.green]
 ];
 prsi.forEach((r,i)=>{const y=233+i*78;rect(s,64,y,548,66,C.white,C.line);txt(s,r[0],80,y+15,42,34,{size:26,bold:true,color:r[3],align:"center"});txt(s,r[1],136,y+13,130,26,{size:17,bold:true});txt(s,r[2],270,y+10,325,46,{size:15});});
 code(s,"@Transactional\npublic DeliveryCancellationResponse cancel(...) {\n  DeliveryRequest delivery =\n      findDeliveryRequestForUpdateOrThrow(id);\n  if (delivery.getStatus() == CANCELLED)\n    return DeliveryCancellationResponse.from(delivery);\n  settlePoints(delivery, courierId, settlement);\n}",650,233,566,284,15);
 rect(s,650,538,566,88,C.sky,"none");txt(s,"Repository의 비관적 쓰기 락",674,554,300,24,{size:17,bold:true,color:C.dark});txt(s,"@Lock(PESSIMISTIC_WRITE)  →  동일 배송의 동시 변경 차단",674,584,510,24,{size:16,color:C.dark});
 txt(s,"좋은 REST API는 URL뿐 아니라 중복·동시 요청에서도 같은 비즈니스 결과를 보장한다.",100,645,1080,28,{size:21,bold:true,align:"center"});
 foot(s);
 const body="핵심 구현은 배송 취소 API입니다. 먼저 GET cancellation-preview는 서버 상태를 바꾸지 않고 예상 수수료와 환불액만 조회합니다. 사용자가 동의하면 POST cancel이 실제 취소를 수행합니다. 문제는 버튼 중복 클릭이나 네트워크 재시도로 같은 POST가 반복될 수 있다는 점입니다. 취소는 단순 삭제가 아니라 고객 환불, 배송원 보상, 매칭 취소, 차량 상태 복구를 함께 처리하므로 일부만 성공하면 정산 불일치가 발생합니다. 그래서 Service 전체를 트랜잭션으로 묶고, Repository에서는 비관적 쓰기 락으로 배송 행을 조회합니다. 이미 고객 요청으로 취소된 배송이면 다시 정산하지 않고 기존 결과를 반환합니다. 또한 환불과 보상에는 배송 ID 기반 멱등성 키를 사용합니다. 현재 단계에서 취소할 수 없으면 InvalidDeliveryTransitionException이 발생하고 GlobalExceptionHandler가 409 Conflict로 변환합니다. 이 사례는 REST API의 품질이 URI 설계에서 끝나는 것이 아니라는 점을 보여줍니다.";
 notes(s,body,["src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java:191-207","src/main/java/com/example/shinhandelivery/delivery/service/DeliveryCancellationService.java:43-149","src/main/java/com/example/shinhandelivery/delivery/repository/DeliveryRequestRepository.java:19-22","src/main/java/com/example/shinhandelivery/common/exception/ErrorCode.java:33-40"]);saveTalk(6,"핵심 기술 구현 — 취소 API의 정산 불일치 방지","1분 55초",body);
}

// Slide 7 — KPT + HATEOAS future
{
 const s=deck.slides.add();s.background.fill=C.white;header(s,7,"회고 & 향후 발전 방향");
 const cols=[
  [64,"KEEP","계속 유지할 것",C.green,"• Controller·Service·Repository 책임 분리\n• DTO와 공통 ErrorResponse 계약\n• 트랜잭션·락·멱등성 기반 안전성"],
  [448,"PROBLEM","현재의 한계",C.orange,"• 프론트가 API URL을 직접 조합\n• 5초 폴링은 불필요한 요청 발생 가능\n• 외부 클라이언트가 늘면 결합도 증가"],
  [832,"TRY","다음 확장",C.blue,"• HATEOAS로 상태별 행동 링크 제공\n• WebSocket 이벤트와 조회 API 병행\n• 링크 관계·계약 테스트 추가"]
 ];
 cols.forEach(c=>{txt(s,c[1],c[0],164,330,34,{size:24,bold:true,color:c[3]});txt(s,c[2],c[0],205,330,26,{size:17,bold:true});rect(s,c[0],247,330,205,C.panel,"none");txt(s,c[4],c[0]+20,272,290,150,{size:18});});
 txt(s,"현재 JSON",68,487,300,24,{size:17,bold:true});code(s,'{ "id": 123, "status": "REQUESTED" }',64,522,480,70,15);
 txt(s,"HATEOAS 확장 예시",660,487,360,24,{size:17,bold:true,color:C.blue});code(s,'"_links": {\n  "self": { "href": ".../123" },\n  "cancel": { "href": ".../123/cancel" }\n}',656,522,560,92,14);
 rect(s,64,636,1152,2,C.ink);txt(s,"Key Lesson  |  REST API는 데이터를 반환하는 코드가 아니라 화면과 비즈니스를 연결하는 계약이다.",88,651,1100,28,{size:20,bold:true,align:"center"});
 foot(s,"현재 프로젝트에는 Spring HATEOAS가 구현되어 있지 않음");
 const body="회고입니다. Keep은 계층별 책임 분리, DTO와 공통 에러 계약, 그리고 트랜잭션과 멱등성으로 API 안전성을 확보한 점입니다. Problem은 현재 프론트엔드가 API 경로를 직접 조합하고, 매칭 확인을 위해 5초마다 요청한다는 점입니다. 지금처럼 Thymeleaf와 API가 같은 저장소에 있고 경로가 단순한 규모에서는 구현 복잡도를 낮추는 합리적인 선택입니다. 그래서 HATEOAS는 현재 구현이라고 말하면 안 됩니다. 향후 모바일 앱이나 외부 클라이언트가 늘어난다면 Try로 검토할 수 있습니다. 서버가 응답의 links에 현재 상태에서 가능한 cancel이나 proof-photo 같은 행동을 제공하면 클라이언트의 URL 하드코딩과 상태 판단을 줄일 수 있습니다. 또한 실시간 알림은 WebSocket으로 받고 정확한 현재 상태는 GET API로 확인하는 조합도 발전 방향입니다. 결론적으로 REST API는 JSON 반환 기능이 아니라 화면과 비즈니스 로직 사이의 명확하고 안전한 계약입니다.";
 notes(s,body,["src/main/resources/templates/matching-wait.html:259-341","build.gradle:24-51","docs/architecture/REST-API-설계-규격-가이드.md"]);saveTalk(7,"회고 & 향후 발전 방향","1분 10초",body);
}

await fs.mkdir(RENDER,{recursive:true});
for(const [i,s] of deck.slides.items.entries()){
 const png=await deck.export({slide:s,format:"png",scale:1});await fs.writeFile(`${RENDER}/slide-${i+1}.png`,new Uint8Array(await png.arrayBuffer()));
 const layout=await s.export({format:"layout"});await fs.writeFile(`${RENDER}/slide-${i+1}.layout.json`,await layout.text());
}
const montage=await deck.export({format:"webp",montage:true,scale:1});await fs.writeFile(`${RENDER}/montage.webp`,new Uint8Array(await montage.arrayBuffer()));
const pptx=await PresentationFile.exportPptx(deck);await pptx.save(OUT);
await fs.writeFile(SCRIPT,`신한 딜리버리 REST API 기술 성과 발표 대본 — 남윤재 (약 9분 30초)\n${"=".repeat(70)}\n\n${talk.join("\n\n")}\n\n${"=".repeat(70)}\n예상 질문 핵심 답변\n1. fetch는 REST인가? → 아니며, 브라우저가 REST API를 호출하는 수단입니다.\n2. 왜 POST /cancel인가? → 물리 삭제가 아니라 환불·보상까지 포함한 도메인 명령이기 때문입니다.\n3. HATEOAS를 사용했나? → 현재 미구현이며, 다중 클라이언트 확장 시 검토할 선택지입니다.\n`,"utf8");
