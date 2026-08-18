import fs from "node:fs/promises";
import { Presentation, PresentationFile } from "@oai/artifact-tool";

const OUT = "/Users/nam-yunjae/shinhan-delivery/REST_API_발표자료_5분.pptx";
const SCRIPT = "/Users/nam-yunjae/shinhan-delivery/REST_API_발표대본_5분.txt";
const RENDER = "/Users/nam-yunjae/shinhan-delivery/.codex-presentation-build/rest-api-deck/rendered";

const C = {
  white: "#FFFFFF", ink: "#111111", muted: "#60656F", panel: "#F1F3F5",
  line: "#BCC2CA", blue: "#3D8DFF", sky: "#DDF3FD", dark: "#132238",
  green: "#16845B", red: "#C33B3B", code: "#111827", codeText: "#E5EEF9"
};

const p = Presentation.create({ slideSize: { width: 1280, height: 720 } });

function rect(slide, left, top, width, height, fill = C.panel, line = "none", radius = false) {
  return slide.shapes.add({
    geometry: radius ? "roundRect" : "rect",
    position: { left, top, width, height },
    fill,
    line: { style: "solid", fill: line, width: line === "none" ? 0 : 1 },
    ...(radius ? { borderRadius: "rounded-xl" } : {})
  });
}

function text(slide, value, left, top, width, height, opts = {}) {
  const s = slide.shapes.add({
    geometry: "textbox",
    position: { left, top, width, height },
    fill: "none",
    line: { style: "solid", fill: "none", width: 0 }
  });
  s.text = value;
  s.text.style = {
    fontSize: opts.size ?? 22,
    fontFamily: opts.font ?? "Arial",
    bold: opts.bold ?? false,
    color: opts.color ?? C.ink,
    alignment: opts.align ?? "left",
    verticalAlignment: opts.valign ?? "top"
  };
  return s;
}

function title(slide, number, value, kicker = "SHINHAN DELIVERY · REST API") {
  text(slide, kicker, 64, 35, 520, 26, { size: 14, bold: true, color: C.blue });
  text(slide, value, 64, 75, 1120, 62, { size: 38, bold: true });
  text(slide, String(number).padStart(2, "0"), 1192, 42, 28, 22, { size: 13, bold: true, color: C.muted, align: "right" });
  rect(slide, 64, 147, 1152, 2, C.ink);
}

function code(slide, value, left, top, width, height, size = 18) {
  rect(slide, left, top, width, height, C.code, C.code, true);
  text(slide, value, left + 24, top + 20, width - 48, height - 40, {
    size, font: "Menlo", color: C.codeText
  });
}

function footer(slide, value = "실제 프로젝트 코드 기반") {
  text(slide, value, 64, 679, 500, 20, { size: 12, color: C.muted });
}

function addNotes(slide, body, sources = []) {
  const src = sources.length ? `\n\n[Sources]\n${sources.map(x => `- ${x}`).join("\n")}` : "";
  slide.speakerNotes.textFrame.setText(body + src);
  slide.speakerNotes.setVisible(true);
}

const scripts = [];
function record(n, heading, timing, body) {
  scripts.push(`[${n}장 · ${timing}] ${heading}\n${body}`);
}

// 1. Cover
{
  const s = p.slides.add(); s.background.fill = C.white;
  text(s, "SHINHAN DELIVERY", 66, 50, 400, 28, { size: 16, bold: true, color: C.blue });
  text(s, "배송 매칭 흐름으로\n이해하는 REST API", 66, 180, 730, 180, { size: 58, bold: true });
  text(s, "실제 Spring Boot 코드 · fetch 호출 · HATEOAS 확장", 70, 390, 760, 42, { size: 24, color: C.muted });
  rect(s, 900, 105, 250, 475, C.sky, "none");
  text(s, "GET", 930, 170, 190, 54, { size: 42, bold: true, color: C.dark, align: "center" });
  text(s, "↓", 930, 245, 190, 44, { size: 34, color: C.blue, align: "center" });
  text(s, "JSON", 930, 310, 190, 54, { size: 42, bold: true, color: C.dark, align: "center" });
  text(s, "↓", 930, 385, 190, 44, { size: 34, color: C.blue, align: "center" });
  text(s, "화면 갱신", 930, 456, 190, 38, { size: 26, bold: true, color: C.dark, align: "center" });
  footer(s, "5분 발표");
  const body = "안녕하세요. 저는 신한 딜리버리 프로젝트의 실제 배송 매칭 흐름을 통해 REST API가 어떻게 설계되고 동작하는지 설명하겠습니다. 발표의 중심은 REST API이며, 화면이 API를 호출할 때 사용하는 fetch와 향후 확장 개념인 HATEOAS도 짧게 연결하겠습니다.";
  addNotes(s, body); record(1, "배송 매칭 흐름으로 이해하는 REST API", "25초", body);
}

// 2. REST concept and project routes
{
  const s = p.slides.add(); s.background.fill = C.white; title(s, 2, "REST는 자원·행위·표현을 HTTP로 연결한다");
  text(s, "RESOURCE", 76, 190, 220, 28, { size: 15, bold: true, color: C.blue });
  text(s, "/api/v1/delivery-requests/123", 76, 230, 525, 48, { size: 25, bold: true });
  text(s, "배송 요청 123번이라는 자원", 76, 290, 440, 32, { size: 18, color: C.muted });
  rect(s, 628, 180, 2, 390, C.line);
  const rows = [
    ["GET", "배송 상태 조회", "200 OK"],
    ["POST", "배송 요청 생성", "201 Created"],
    ["PATCH", "픽업·완료 상태 변경", "200 OK"],
    ["DELETE", "기존 호환 취소", "204 No Content"]
  ];
  rows.forEach((r, i) => {
    const y = 184 + i * 92;
    text(s, r[0], 670, y, 125, 34, { size: 23, bold: true, color: i === 0 ? C.blue : C.ink });
    text(s, r[1], 810, y + 1, 240, 30, { size: 19 });
    text(s, r[2], 1060, y + 2, 150, 28, { size: 16, color: C.muted, align: "right" });
    rect(s, 670, y + 55, 540, 1, C.line);
  });
  text(s, "URL은 ‘무엇’, HTTP 메서드는 ‘무엇을 할지’를 표현한다.", 76, 582, 1100, 45, { size: 25, bold: true });
  footer(s);
  const body = "REST는 HTTP 위에서 자원을 URL로 표현하고, 그 자원에 할 행동을 HTTP 메서드로 구분하는 아키텍처 스타일입니다. 저희 프로젝트에서는 배송 요청을 복수형 명사인 delivery-requests로 표현합니다. 예를 들어 GET은 조회, POST는 생성, PATCH는 일부 상태 변경에 사용하며 처리 결과는 의미에 맞는 HTTP 상태 코드로 반환합니다.";
  addNotes(s, body, ["docs/architecture/REST-API-설계-규격-가이드.md", "src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java"]); record(2, "REST는 자원·행위·표현을 HTTP로 연결한다", "40초", body);
}

// 3. Controller split + architecture
{
  const s = p.slides.add(); s.background.fill = C.white; title(s, 3, "화면 제공과 데이터 제공을 분리했다");
  text(s, "화면 요청", 78, 180, 260, 34, { size: 24, bold: true });
  code(s, "@Controller\n@GetMapping(\"/matching-wait\")\npublic String matchingWait() {\n  return \"matching-wait\";\n}", 72, 225, 500, 220, 18);
  text(s, "HTML 반환", 210, 470, 220, 32, { size: 22, bold: true, color: C.green, align: "center" });
  text(s, "REST API", 704, 180, 260, 34, { size: 24, bold: true });
  code(s, "@RestController\n@RequestMapping(\"/api/v1/delivery-requests\")\n@GetMapping(\"/{deliveryRequestId}\")\npublic ResponseEntity<DeliveryDetailResponse> ...", 698, 225, 510, 220, 17);
  text(s, "JSON 반환", 846, 470, 220, 32, { size: 22, bold: true, color: C.blue, align: "center" });
  text(s, "Controller  →  Service  →  Repository  →  MariaDB", 190, 570, 900, 42, { size: 28, bold: true, align: "center" });
  footer(s);
  const body = "사용자가 매칭 대기 화면에 접속하면 일반 Controller가 matching-wait HTML을 반환합니다. 화면이 열린 뒤 최신 배송 데이터가 필요하면 RestController의 API를 호출합니다. REST Controller는 요청과 응답을 담당하고, 실제 비즈니스 로직은 Service, DB 접근은 Repository가 맡습니다. 이렇게 Controller에서 Entity를 직접 반환하지 않는 단방향 구조를 유지했습니다.";
  addNotes(s, body, ["src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryWebController.java:45", "src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java:59"]); record(3, "화면 제공과 데이터 제공을 분리했다", "45초", body);
}

// 4. Actual GET flow and DTO
{
  const s = p.slides.add(); s.background.fill = C.white; title(s, 4, "GET 한 번이 인증·조회·DTO 변환을 거친다");
  const steps = [
    ["01", "GET 요청", "/delivery-requests/123"],
    ["02", "인증·권한", "JWT · 본인/배정 배송원"],
    ["03", "Service 조회", "배송·매칭·차량 정보"],
    ["04", "DTO 응답", "DeliveryDetailResponse"]
  ];
  steps.forEach((v, i) => {
    const x = 72 + i * 298;
    text(s, v[0], x, 190, 50, 32, { size: 16, bold: true, color: C.blue });
    text(s, v[1], x, 235, 240, 36, { size: 24, bold: true });
    text(s, v[2], x, 286, 245, 50, { size: 17, color: C.muted });
    if (i < 3) text(s, "→", x + 250, 236, 42, 40, { size: 28, color: C.blue, align: "center" });
  });
  code(s, "{\n  \"id\": 123,\n  \"status\": \"MATCHED\",\n  \"feePoint\": 5000,\n  \"courierName\": \"홍길동\",\n  \"vehicleType\": \"MOTORCYCLE\"\n}", 72, 382, 490, 230, 18);
  text(s, "왜 Entity가 아니라 DTO인가?", 625, 395, 500, 38, { size: 26, bold: true });
  text(s, "• DB 구조와 API 계약 분리\n• 필요한 필드만 외부에 노출\n• 배송·매칭·차량 정보를 하나로 조합\n• 내부 필드와 민감 정보 노출 방지", 625, 455, 530, 150, { size: 21, color: C.ink });
  footer(s);
  const body = "배송 상세 GET 요청은 단순 DB 조회로 끝나지 않습니다. JWT로 인증하고, 호출자가 고객 본인이나 배정된 배송원인지 확인한 다음 배송, 매칭, 차량 정보를 조합합니다. 그리고 Entity를 그대로 노출하지 않고 DeliveryDetailResponse DTO로 변환해 JSON을 반환합니다. DTO를 사용하면 DB 구조와 외부 API 계약을 분리하고 필요한 정보만 안전하게 제공할 수 있습니다.";
  addNotes(s, body, ["src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java:102", "src/main/java/com/example/shinhandelivery/delivery/service/DeliveryService.java:155", "src/main/java/com/example/shinhandelivery/delivery/dto/response/DeliveryDetailResponse.java"]); record(4, "GET 한 번이 인증·조회·DTO 변환을 거친다", "50초", body);
}

// 5. fetch
{
  const s = p.slides.add(); s.background.fill = C.white; title(s, 5, "fetch가 REST API를 5초마다 호출한다");
  code(s, "const response = await fetch(\n  `/api/v1/delivery-requests/${deliveryId}`,\n  {\n    headers: { Authorization: header },\n    cache: 'no-store'\n  }\n);\n\nconst detail = await response.json();", 72, 185, 650, 360, 18);
  text(s, "REST API", 825, 205, 280, 40, { size: 28, bold: true, align: "center" });
  text(s, "GET + JWT", 825, 275, 280, 36, { size: 22, color: C.blue, align: "center" });
  text(s, "↓", 825, 326, 280, 35, { size: 28, color: C.blue, align: "center" });
  text(s, "JSON 응답", 825, 380, 280, 38, { size: 27, bold: true, align: "center" });
  text(s, "↓", 825, 431, 280, 35, { size: 28, color: C.blue, align: "center" });
  text(s, "MATCHED면 화면 이동", 785, 485, 360, 38, { size: 23, bold: true, color: C.green, align: "center" });
  text(s, "fetch는 REST가 아니라, 브라우저가 REST API를 호출하는 도구다.", 104, 590, 1060, 38, { size: 25, bold: true, align: "center" });
  footer(s);
  const body = "이 REST API를 실제 화면에서 호출하는 도구가 fetch입니다. 매칭 대기 화면은 5초마다 배송 상세 GET API를 호출하고 JWT를 Authorization 헤더에 전달합니다. JSON의 상태가 MATCHED로 바뀌면 폴링을 중단하고 매칭 완료 화면으로 이동합니다. 여기서 fetch 자체가 REST인 것은 아니고, 브라우저가 REST API를 사용하는 호출 수단입니다.";
  addNotes(s, body, ["src/main/resources/templates/matching-wait.html:218-342"]); record(5, "fetch가 REST API를 5초마다 호출한다", "45초", body);
}

// 6. cancellation + error
{
  const s = p.slides.add(); s.background.fill = C.white; title(s, 6, "취소는 조회와 명령을 분리하고 원자적으로 처리한다");
  text(s, "1", 78, 195, 42, 42, { size: 30, bold: true, color: C.blue });
  text(s, "GET  /{id}/cancellation-preview", 138, 190, 500, 36, { size: 24, bold: true });
  text(s, "수수료·예상 환불액만 조회", 138, 234, 450, 30, { size: 18, color: C.muted });
  rect(s, 76, 284, 540, 2, C.line);
  text(s, "2", 78, 316, 42, 42, { size: 30, bold: true, color: C.blue });
  text(s, "POST  /{id}/cancel", 138, 311, 500, 36, { size: 24, bold: true });
  text(s, "취소·환불·배송원 보상·상태 변경", 138, 355, 450, 30, { size: 18, color: C.muted });
  rect(s, 670, 178, 2, 360, C.line);
  text(s, "@Transactional", 726, 190, 380, 42, { size: 30, bold: true, color: C.green });
  text(s, "• 비관적 락으로 동시 변경 차단\n• 중간 실패 시 전체 롤백\n• 중복 요청에도 이중 환불 방지\n• 상태 충돌은 409 Conflict", 726, 260, 450, 170, { size: 21 });
  rect(s, 726, 460, 420, 72, C.sky, "none");
  text(s, "GlobalExceptionHandler → 공통 ErrorResponse", 746, 480, 380, 34, { size: 18, bold: true, align: "center" });
  text(s, "단순 DELETE가 아니라 정산을 포함한 도메인 명령", 160, 585, 960, 40, { size: 26, bold: true, align: "center" });
  footer(s);
  const body = "취소에서는 REST API의 메서드 의미가 더 분명하게 드러납니다. GET preview는 서버 상태를 바꾸지 않고 예상 금액만 조회합니다. 사용자가 동의하면 POST cancel이 실제 취소와 환불, 배송원 보상을 수행합니다. 이 작업은 하나의 트랜잭션으로 묶고 DB 행을 잠가 동시 변경을 막습니다. 이미 취소된 요청에는 기존 결과를 반환해 이중 환불도 방지합니다. 허용되지 않는 상태 전이는 409 Conflict와 공통 ErrorResponse로 변환됩니다.";
  addNotes(s, body, ["src/main/java/com/example/shinhandelivery/delivery/controller/DeliveryController.java:191-207", "src/main/java/com/example/shinhandelivery/delivery/service/DeliveryCancellationService.java", "src/main/java/com/example/shinhandelivery/common/exception/GlobalExceptionHandler.java"]); record(6, "취소는 조회와 명령을 분리하고 원자적으로 처리한다", "55초", body);
}

// 7. HATEOAS + close
{
  const s = p.slides.add(); s.background.fill = C.white; title(s, 7, "현재는 단순성을 택했고, HATEOAS는 확장 선택지다");
  text(s, "현재 구현", 76, 190, 300, 34, { size: 26, bold: true });
  code(s, "fetch(\n  `/delivery-requests/${id}/cancel`,\n  { method: 'POST' }\n);", 72, 238, 510, 150, 18);
  text(s, "프론트가 URL을 직접 구성", 110, 414, 430, 32, { size: 20, color: C.muted, align: "center" });
  text(s, "HATEOAS 적용 예시", 704, 190, 390, 34, { size: 26, bold: true });
  code(s, "\"_links\": {\n  \"self\": { \"href\": \".../123\" },\n  \"cancel\": { \"href\": \".../123/cancel\" }\n}", 698, 238, 510, 150, 17);
  text(s, "서버가 가능한 다음 행동을 안내", 738, 414, 430, 32, { size: 20, color: C.muted, align: "center" });
  rect(s, 72, 480, 1136, 2, C.ink);
  text(s, "왜 현재는 미도입인가", 76, 515, 300, 32, { size: 21, bold: true, color: C.blue });
  text(s, "같은 저장소의 Thymeleaf·API  ·  단순하고 고정된 경로  ·  도입 복잡도 대비 작은 효과", 76, 560, 1120, 38, { size: 22, bold: true });
  text(s, "REST API가 계약을 만들고, fetch가 사용하며, HATEOAS는 그 계약의 탐색성을 확장한다.", 76, 628, 1120, 36, { size: 23, bold: true, color: C.dark, align: "center" });
  footer(s, "현재 프로젝트에는 Spring HATEOAS가 구현되어 있지 않음");
  const body = "마지막으로 HATEOAS는 서버가 현재 상태에서 가능한 다음 행동을 링크로 제공하는 REST 제약 조건입니다. 현재 프로젝트는 프론트와 백엔드가 같은 저장소에 있고 API 경로도 단순하기 때문에 URL을 직접 구성하는 방식을 택했습니다. 그래서 HATEOAS는 현재 구현이 아니라 향후 모바일 앱이나 외부 클라이언트가 늘어날 때 고려할 확장 선택지입니다. 정리하면 REST API가 통신 계약을 만들고, fetch가 그 계약을 사용하며, HATEOAS는 서버가 다음 행동까지 안내하도록 계약을 확장합니다. 감사합니다.";
  addNotes(s, body, ["build.gradle:24-51", "src/main/resources/templates/matching-wait.html:259-318"]); record(7, "현재는 단순성을 택했고, HATEOAS는 확장 선택지다", "40초", body);
}

await fs.mkdir(RENDER, { recursive: true });
for (const [i, slide] of p.slides.items.entries()) {
  const png = await p.export({ slide, format: "png", scale: 1 });
  await fs.writeFile(`${RENDER}/slide-${i + 1}.png`, new Uint8Array(await png.arrayBuffer()));
  const layout = await slide.export({ format: "layout" });
  await fs.writeFile(`${RENDER}/slide-${i + 1}.layout.json`, await layout.text());
}
const montage = await p.export({ format: "webp", montage: true, scale: 1 });
await fs.writeFile(`${RENDER}/montage.webp`, new Uint8Array(await montage.arrayBuffer()));
const pptx = await PresentationFile.exportPptx(p);
await pptx.save(OUT);

const fullScript = `신한 딜리버리 REST API 발표 대본 (약 4분 40초~5분)\n${"=".repeat(62)}\n\n${scripts.join("\n\n" )}\n\n${"=".repeat(62)}\n핵심 한 문장\nREST API가 통신 계약을 만들고, fetch가 그 계약을 사용하며, HATEOAS는 다음 행동의 탐색성을 확장한다.\n`;
await fs.writeFile(SCRIPT, fullScript, "utf8");

