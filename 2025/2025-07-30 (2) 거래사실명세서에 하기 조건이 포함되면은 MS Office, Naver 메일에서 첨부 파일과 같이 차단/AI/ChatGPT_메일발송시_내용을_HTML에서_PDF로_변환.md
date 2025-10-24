# 아래에 메일을 발송하는 소스에서 contentStr이 html로 내용이 구성되어 있는데 메일 발송하기 전에 PDF 파일로 바꾸는 방법을 알려줘
단, 무료 라이선스로 구현해야 하고, 체험판은 아니어야 되. 오직 무료 라이선스를 이용하는 방법을 알려줘.

```java
		try {
			file.createNewFile();
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath()), StandardCharsets.UTF_8));
			output.write(contentStr);
			output.close();
		} catch(UnsupportedEncodingException uee) {
		} catch(IOException ioe) {
		} 
		
		MailUtil mail = new MailUtil();
		for(String email : emailArr) {
			if(!StringUtils.equals("", email)) {
				// 메일전송
				mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, contentStr, file, filename);
				
				// 전송이력 저장
				this.insertSendMailHistory("factReport", Converter.toStr(params.get("insdate")), Converter.toStr(params.get("inedate"))
						, Converter.toStr(params.get("m_custcd")), Converter.toStr(params.get("m_shiptocd")), Converter.toStr(params.get("r_smhtype")), email, loginDto.getUserId());
			}
		}
```

---------------------------------------------------------------------


contentStr(HTML 문자열)을 PDF로 변환한 후에 메일로 전송하려면, 아래와 같은 단계(라이브러리 활용)를 통해 구현할 수 있어. 간단하게 세 단계로 정리해서 설명할게:

---

## ✅ 주요 방법 비교

| 라이브러리                       | 라이선스                | HTML 지원              | 추천 순서 |
| --------------------------- | ------------------- | -------------------- | ----- |
| **iText (pdfHTML)**         | AGPL / 상용 라이선스      | 높은 정확도               | 1     |
| **OpenPDF + Flying Saucer** | LGPL/MPL (무료 사용 가능) | 중급 (XHTML 기반)        | 2     |
| **IronPDF**                 | 상용 (체험 가능)          | 매우 높은 품질 (CSS/JS 반영) | 3     |

iText는 강력하지만 AGPL 라이선스라 코드 공개가 필요하거나 상용 라이선스를 구매해야 하고, OpenPDF(Flying Saucer)는 상대적으로 단순한 구성에 적합하며 무료 사용 가능해. IronPDF는 상용이지만 HTML 렌더링 퀄리티가 매우 좋아서 상용 프로젝트에 적합해 ([ironpdf.com][1], [위키피디아][2], [docraptor.com][3], [GitHub][4], [위키피디아][5]).

---

## ① OpenPDF + Flying Saucer (LGPL 무료 솔루션)

```java
// XHTML 변환 (Jsoup 필요)
String xhtml = Jsoup.parse(contentStr)
                    .outputSettings()
                    .syntax(Document.OutputSettings.Syntax.xml)
                    .html();

// PDF 변환
OutputStream os = new FileOutputStream(file);
ITextRenderer renderer = new ITextRenderer();
renderer.setDocumentFromString(xhtml);
renderer.layout();
renderer.createPDF(os);
os.close();
```

* **장점**: 무료(open source), 비교적 간단하게 구현 가능
* **주의**: HTML을 XHTML로 정제하고 CSS는 CSS 2.1 범위로 제한하는 것이 좋다 ([GitHub][4])

---

## ② iText + pdfHTML (AGPL 또는 상용)

```java
ByteArrayOutputStream baos = new ByteArrayOutputStream();
HtmlConverter.convertToPdf(contentStr, baos);
byte[] pdfBytes = baos.toByteArray();
```

* **장점**: 최신 HTML/CSS 지원, 이메일에 바로 바이트 배열 전송 가능
* **주의**: AGPL 라이선스로 인해 회사 코드 공개 의무 or 상용 라이선스 필요해 ([docraptor.com][3])

---

## ③ IronPDF (상용, 픽셀-정확 렌더링)

```java
PdfDocument pdf = PdfDocument.renderHtmlAsPdf(contentStr);
pdf.saveAs(Paths.get(file.getPath()));
```

* **장점**: HTML, CSS, 자바스크립트까지 렌더링 과정에서 충실하게 반영됨
* **주의**: 체험판은 가능하지만 상용 라이선스 필요하며 비용 발생 가능해 ([ironpdf.com][1], [Stack Overflow][6])

---

## 🧾 적용 예시 (OpenPDF 버전)

```java
// 1. HTML 저장
// 2. PDF 변환 (위 코드 참고)
// 3. MailUtil.sendMail() 로 파일 첨부 전송
mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, "", file, filename);
```

* `contentStr` 대신에 빈 본문(`""`)을 넣고, 파일 첨부로 PDF 전송하는 방식이 안정적이야.
* 필요 시 `byte[]`로 변환하여 `InputStream` 기반 전송도 가능해.

---

## ❓질문할 내용 정리

1. **상용 라이선스 구매 가능성**이 있는지
2. **contentStr 내부의 HTML 형태** (XHTML에 가까운가?)
3. **CSS 및 이미지 포함 여부** (복잡한 스타일/스크립트 포함 여부)

위 3가지를 알려주면 적합한 솔루션을 더 구체적으로 추천해 줄 수 있어!

[1]: https://ironpdf.com/java/tutorials/html-to-pdf/?utm_source=chatgpt.com "Convert HTML to PDF in Java (Without Losing Formatting) - IronPDF"
[2]: https://en.wikipedia.org/wiki/IText?utm_source=chatgpt.com "IText"
[3]: https://docraptor.com/java-html-to-pdf?utm_source=chatgpt.com "Top Java HTML-to-PDF Conversion Libraries - DocRaptor"
[4]: https://github.com/danfickle/openhtmltopdf?utm_source=chatgpt.com "danfickle/openhtmltopdf: An HTML to PDF library for the ... - GitHub"
[5]: https://en.wikipedia.org/wiki/Prince_%28software%29?utm_source=chatgpt.com "Prince (software)"
[6]: https://stackoverflow.com/questions/72874593/convert-html-to-pdf-using-java?utm_source=chatgpt.com "Convert HTML to PDF using Java [closed] - Stack Overflow"



---------------------------------------------------------------------



# 위의 질문에서무료 라이선스로 구현해야 하고, 체험판은 아니어야 되. 오직 무료 라이선스를 이용하는 방법을 알려줘.
아래는 contentStr를 구성하는 html 내용의 일부야. 참고해줘.
```html
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ko" xml:lang="ko">
<head>
<!-- <meta http-equiv="content-type" content="application/xhtml+xml; charset=euc-kr" /> -->
<meta http-equiv="content-type" content="application/xhtml+xml; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>거래사실확인서</title>
<meta name="keywords" content="" />
<meta name="description" content="" />

<style type="text/css">
@media print {
    html, body { -webkit-print-color-adjust:exact; width: 210mm; height: 297mm; }
    table { page-break-inside:auto; width:100%; height:100%;}
    tr    { page-break-inside:avoid; page-break-after:auto; }
    /*thead { display:table-header-group; }
    tfoot { display:table-footer-group; }*/
}
</style>
</head>

<body style="width: 100%; height: 100%; padding: 0; margin: 0;">
			
	<!-- BEGIN container -->
	<div style="width: 100%;">
		<div style="margin: 30px 10px 10px;padding: 0 30px; border: 0px solid #454545;">

			<!-- <div style="display: inline-block; margin-top: 0px; margin-bottom: 5px;"> -->
			<div style="margin-top: 0px; margin-bottom: 5px;">
				<h1 style="width: 100% !important; margin: 15px auto 25px; font-weight: 600; letter-spacing: 10px; font-size: 30px; text-align: center; display: inline-block; width: 100%; line-height: 30px; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; ">거래사실확인서</h1>			
				<table width="100%" cellpadding="0" cellspacing="0" style="font-size: 12px;  border: 2px solid #757575; ">
					<colgroup>
						<col width="4%" />
						<col width="11%" />
						<col width="16%" />
						<col width="9%" />
						<col width="12%" />
						<col width="4%" />
						<col width="11%" />
						<col width="16%" />
						<col width="9%" />
						<col width="12%" />
					</colgroup>
					<tbody>
						<tr>
							<th rowspan="3" style="padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">공급자</th>
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; border-bottom: 1px solid #ddd;">등록번호</th>
							<td colspan="3" style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; border-bottom: 1px solid #ddd; font-weight: 600;">417-81-17256</td>
							<th rowspan="3" style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">공급받는자</th>
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; border-bottom: 1px solid #ddd;">등록번호</th>
							<td colspan="3" style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; border-bottom: 1px solid #ddd; font-weight: 600;">840-81-01648</td>
						</tr>
```

---------------------------------------------------------------------


무료 라이선스(상업용 포함)만을 사용할 수 있는 HTML → PDF 변환 방법으로는 `OpenHTMLtoPDF` 또는 `Flying Saucer + OpenPDF` 조합이 가장 적합합니다. 둘 다 LGPL/MPL 라이선스 기반으로 자유롭게 사용 가능합니다.

---

## 🛠 추천 라이브러리 요약

### ✅ OpenHTMLtoPDF (LGPL-2.1 또는 최신 LGPL)

* Java 순수 기반 오픈소스 HTML/XHTML → PDF 변환 라이브러리
* Apache PDFBox를 백엔드로 사용해 PDF 생성
* CSS 2.1 및 일부 CSS3 지원, PDF/A 표준 대응 ([GitHub][1], [APITemplate.io][2])
* 상업용 프로젝트에도 무료 사용 가능 ([GitHub][1], [Software Recommendations Stack Exchange][3])

### ✅ Flying Saucer + OpenPDF (LGPL/MPL)

* XHTML + CSS2.1 스타일 문서를 PDF로 렌더링
* OpenPDF는 iText 이전 버전을 LG­­PL/MPL로 포크한 것
* Flying Saucer 자체도 LGPL 라이선스 기반 ([Stack Overflow][4])
* Java 8 이상 환경에서 사용 가능, 비교적 간단한 HTML 레이아웃에 적합 ([GitHub][1])

---

## 📄 contentStr 예시 기반 구현 제안

**당신의 HTML은 `<html xmlns="…/xhtml" …>` 구조로 XHTML 형태**로 이미 구성되어 있으므로 Flying Saucer 사용에 적합해요.

### ☑ 라이브러리 주요 의존성 (Maven 기준)

```xml
<dependency>
  <groupId>org.jsoup</groupId>
  <artifactId>jsoup</artifactId>
  <version>1.17.2</version>
</dependency>
<dependency>
  <groupId>org.xhtmlrenderer</groupId>
  <artifactId>flying-saucer-pdf-openpdf</artifactId>
  <version>9.1.20</version>
</dependency>
```

또는 최신 Flying Saucer 버전에서는 `org.xhtmlrenderer:flying-saucer-pdf`가 OpenPDF 기반으로 사용됩니다 ([Stack Overflow][4]).

---

## 🔧 Java 코드 예제 (Flying Saucer + OpenPDF)

```java
String xhtml = Jsoup.parse(contentStr)
                   .outputSettings()
                   .syntax(Document.OutputSettings.Syntax.xml)
                   .html();

try (OutputStream os = new FileOutputStream(file)) {
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(xhtml);
    renderer.layout();
    renderer.createPDF(os);
}
```

➡ 이 코드를 `BufferedWriter` 부분 전에 실행해 PDF 파일을 생성한 다음, 파일을 `MailUtil.sendMail(...)`의 첨부 파일로 넘기면 됩니다.

* 메일 본문(`contentStr`)은 빈 문자열 `""`으로 보내고
* PDF 파일을 첨부하는 방식이 안정적입니다.

---

## 📌 요점 정리

* **무료 요건 충족**: LGPL/MPL 라이선스를 따르며, 상업적 이용도 자유로움
* **contentStr 구조 적합**: XHTML 기반으로 변환 없이 그대로 사용 가능
* **CSS**: 기본 CSS2.1 스타일은 잘 지원함. JS, Flexbox, OpenType 폰트는 제한적 ([GitHub][5], [DocRaptor][6])

---

## ❓더 확인할 사항

1. **contentStr에 외부 이미지/폰트가 포함돼 있는지?**
   → 이미지와 외부 폰트 사용 시 상대 경로 또는 Base64 포함방식 필요

2. **NanumGothic 등 한글 폰트 사용 여부?**
   → PDF에 반영하려면 `FontResolver`로 직접 등록해 줘야 안정적인 출력 가능

3. **모바일 출력, 페이지 구분, 머리말·바닥글, 테이블 페이지 브레이크 등 필요 기능?**
   → Flying Saucer는 기본 PagedMedia 지원하나, 반복 헤더/푸터 같은 고급 기능은 제한적일 수 있음

---

필요하시면 위 코드를 클래스 형태로 정리해 드릴 수도 있고, 폰트 등록 예시, 이메일 전송 코드 통합 예시 등도 제공 가능합니다. 어떤 부분이 더 궁금한가요?

[1]: https://github.com/danfickle/openhtmltopdf?utm_source=chatgpt.com "danfickle/openhtmltopdf: An HTML to PDF library for the ... - GitHub"
[2]: https://apitemplate.io/blog/a-guide-to-generating-pdfs-in-java/?utm_source=chatgpt.com "Generating PDFs in Java with 3 Popular Libraries - APITemplate.io"
[3]: https://softwarerecs.stackexchange.com/questions/36861/free-for-commercial-use-java-library-to-convert-html-to-pdf?utm_source=chatgpt.com "Free for commercial use Java library to convert HTML to PDF"
[4]: https://stackoverflow.com/questions/51614867/flying-saucer-html-to-pdf-and-licensing-itext?utm_source=chatgpt.com "Flying Saucer - HTML to PDF and Licensing (iText) - Stack Overflow"
[5]: https://github.com/danfickle/openhtmltopdf/blob/open-dev-v1/LICENSE?utm_source=chatgpt.com "openhtmltopdf/LICENSE at open-dev-v1 - GitHub"
[6]: https://docraptor.com/java-html-to-pdf?utm_source=chatgpt.com "Top Java HTML-to-PDF Conversion Libraries - DocRaptor"



---------------------------------------------------------------------


1. 이미지가 포함 됨.
2. PDF에 포함되어 있으면 상관없는데, 다운로드 받는 사람이 폰트 설치를 해야는건 안되.
3. 모든 기능이 있어야 함. 


---------------------------------------------------------------------


무료 라이선스만 사용하면서, ① 이미지 포함 가능, ② 한글 폰트 내장 (다운로더가 별도 폰트 설치 불필요), ③ 다양한 PDF 기능(페이지 브레이크, 헤더/푸터 등)을 모두 지원하는 구성으로 **OpenHTMLtoPDF** 사용을 추천할게요. 이건 Flying Saucer 기반이며, **LGPL 라이선스**로 상업용에서도 무료로 사용할 수 있습니다.([GitHub][1])

---

## ✅ OpenHTMLtoPDF 구성 요건 및 구현 방법

### 1. 이미지 포함 → 데이터 URL 또는 URL 경로

* `<img src="...">` 태그로 외부 이미지 URL 혹은 Base64 인라인 데이터 이미지(`data:image/...;base64,...`) 사용 시 OpenHTMLtoPDF에서 자동 렌더링합니다([openhtmltopdf.com][2], [sandbox.openhtmltopdf.com][3]).

### 2. 한글 폰트 완전 내장 (폰트 설치 없이 보기 가능)

* OpenHTMLtoPDF는 기본 내장 폰트로 한글을 지원하지 않아서, 반드시 **TrueType(.ttf)** 한글 폰트를 별도로 포함해야 합니다([openhtmltopdf.com][2]).
* 예를 들어 “Noto Sans KR” 또는 “NanumGothic.ttf” 같은 폰트를 프로젝트에 포함하고 CSS `@font-face`로 선언 후 사용하면, PDF에 완전히 내장되어 배포하는 사용자도 폰트 설치 없이 올바르게 표시됩니다.

### 3. 모든 기능 지원 (CSS 기반 페이지 브레이크, 헤더/푸터 등)

* CSS `@page`, `page-break`, `thead/tfoot` 반복 렌더링, margin-box 등을 사용해 보고서 형식 PDF를 만들 수 있습니다. OpenHTMLtoPDF는 PDF/A, 접근성 등도 지원합니다([Stack Overflow][4]).

---

## 📌 실전 Java 코드 & CSS 예시

### CSS: 폰트 내장 선언 (HTML `<head>` 내부)

```html
<style>
@font-face {
  font-family: 'NanumGothic';
  src: url('fonts/NanumGothic.ttf');
  font-weight: normal;
  font-style: normal;
  -fs-pdf-font-embed: embed;
  -fs-pdf-font-encoding: Identity-H;
}
body { font-family: 'NanumGothic', sans-serif; }
</style>
```

* `file://` 또는 상대경로 URL로 `.ttf`를 가리켜야 하며, `-fs-pdf-font-embed: embed`와 `Identity-H` 인코딩을 반드시 지정해야 내장됩니다([Stack Overflow][4]).

### Java: PDF 생성 코드

```java
String xhtml = Jsoup.parse(contentStr)
  .outputSettings().syntax(Document.OutputSettings.Syntax.xml).html();

try (OutputStream os = new FileOutputStream(file)) {
  ITextRenderer renderer = new ITextRenderer();
  renderer.setDocumentFromString(xhtml, baseUrl); // baseUrl -> CSS/fonts 기준 URL
  // 필요하면 렌더러에 폰트 직접 등록도 가능
  renderer.getFontResolver().addFont("/path/fonts/NanumGothic.ttf",
    BaseFont.IDENTITY_H, true);
  renderer.layout();
  renderer.createPDF(os);
}
```

* 위 방법으로 **폰트 자동 포함 + 이미지 로딩** 모두 가능합니다.
* `baseUrl`은 HTML 내 `url()`이나 이미지 경로의 기준이 됩니다.

---

## 📋 정리 테이블

| 항목       | 구현 방식                                                       |
| -------- | ----------------------------------------------------------- |
| 이미지 포함   | `<img>` 태그로 외부 URL 또는 데이터 URL 사용 가능                         |
| 한글 폰트 내장 | `.ttf` 폰트를 `@font-face` 선언 + `-fs-pdf-font-embed: embed` 사용 |
| PDF 기능   | CSS 기반 페이지 브레이크, 헤더/푸터, PDF/A, 접근성 지원                       |
| 라이선스     | LGPL/MPL - 완전 무료, 상업 사용 가능                                  |

---

## ✅ 다음 단계 및 질문

1. **한글 TrueType 폰트(.ttf)** 파일 (NanumGothic, Noto Sans KR 등)을 프로젝트에 포함했는지요?
2. 이미지 경로는 외부 URL인지 아니면 Base64 인라인 방식인지 확인 부탁드려요.
3. **헤더/푸터, 반복 테이블 헤더, 페이지 넘버 등** 필요한 구체 기능이 있으면 알려주세요.

그럼 필요하신 부분에 맞춰 코드 클래스, MailUtil 통합 예시, CSS 템플릿 등을 단계별로 더 정리해드릴게요!

[1]: https://github.com/danfickle/openhtmltopdf?utm_source=chatgpt.com "danfickle/openhtmltopdf: An HTML to PDF library for the ... - GitHub"
[2]: https://openhtmltopdf.com/template-guide.pdf?utm_source=chatgpt.com "[PDF] OpenHTMLToPDF Template Guide - Open HTML to PDF"
[3]: https://sandbox.openhtmltopdf.com/?utm_source=chatgpt.com "OpenHTMLtoPDF.com Sandbox (HTML to PDF)"
[4]: https://stackoverflow.com/questions/7525403/how-to-embed-font-in-pdf-created-from-html-with-itext-and-flying-saucer?utm_source=chatgpt.com "How to embed font in PDF created from HTML with iText and Flying ..."



---------------------------------------------------------------------


# 아래는 html 전체 소스야. 이것을 참고해서 만들어줘.

```html
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ko" xml:lang="ko">
<head>
<!-- <meta http-equiv="content-type" content="application/xhtml+xml; charset=euc-kr" /> -->
<meta http-equiv="content-type" content="application/xhtml+xml; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title>거래사실확인서</title>
<meta name="keywords" content="" />
<meta name="description" content="" />

<style type="text/css">
@media print {
    html, body { -webkit-print-color-adjust:exact; width: 210mm; height: 297mm; }
    table { page-break-inside:auto; width:100%; height:100%;}
    tr    { page-break-inside:avoid; page-break-after:auto; }
    /*thead { display:table-header-group; }
    tfoot { display:table-footer-group; }*/
}
</style>
</head>

<body style="width: 100%; height: 100%; padding: 0; margin: 0;">

	<!-- BEGIN container -->
	<div style="width: 100%;">
		<div style="margin: 30px 10px 10px;padding: 0 30px; border: 0px solid #454545;">

			<!-- <div style="display: inline-block; margin-top: 0px; margin-bottom: 5px;"> -->
			<div style="margin-top: 0px; margin-bottom: 5px;">
				<h1 style="width: 100% !important; margin: 15px auto 25px; font-weight: 600; letter-spacing: 10px; font-size: 30px; text-align: center; display: inline-block; width: 100%; line-height: 30px; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; ">거래사실확인서</h1>
				<table width="100%" cellpadding="0" cellspacing="0" style="font-size: 12px;  border: 2px solid #757575; ">
					<colgroup>
						<col width="4%" />
						<col width="11%" />
						<col width="16%" />
						<col width="9%" />
						<col width="12%" />
						<col width="4%" />
						<col width="11%" />
						<col width="16%" />
						<col width="9%" />
						<col width="12%" />
					</colgroup>
					<tbody>
						<tr>
							<th rowspan="3" style="padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">공급자</th>
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; border-bottom: 1px solid #ddd;">등록번호</th>
							<td colspan="3" style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; border-bottom: 1px solid #ddd; font-weight: 600;">417-81-17256</td>
							<th rowspan="3" style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">공급받는자</th>
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; border-bottom: 1px solid #ddd;">등록번호</th>
							<td colspan="3" style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; border-bottom: 1px solid #ddd; font-weight: 600;">840-81-01648</td>
						</tr>
						<tr style="border-bottom: 1px solid #ddd;">
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">상호(법인명)</th>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: left; padding: 0 6px;">크나우프석고보드(주)</td>
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">대표자</th>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center;">송광섭</td>
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">상호(법인명)</th>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: left; padding: 0 6px;">(주)동우씨엠에스</td>
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">대표자</th>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center;">신동주</td>
						</tr>
						<tr>
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">주소</th>
							<td colspan="3" style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: left; padding: 0 6px;">전라남도 여수시  낙포단지길 45</td>
							<th style="border-left: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">주소</th>
							<td colspan="3" style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: left; padding: 0 6px;">경기도 하남시  조정대로 45</td>
						</tr>
					</tbody>
				</table>

				<table width="100%" border="0" cellpadding="0" cellspacing="0" style="border-top: 1px solid #757575 !important; font-size: 12px;  border: 2px solid #757575; ">
					<colgroup>
						<col width="15%" />
						<col width="15%" />
						<col width="20%" />
						<col width="20%" />
						<col width="30%" />
					</colgroup>

					<!-- 공급가액,세액,합계  -->

							 <!-- 납품처코드  -->




					<tbody>
						<tr style="border-bottom: 1px solid #ddd;">
							<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; font-weight: 600;">조회시작일</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; font-weight: 600;">조회종료일</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; font-weight: 600;">공급가액</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; font-weight: 600;">세액</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; font-weight: 600;">합계</th>
						</tr>
						<tr>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; color: #121212; text-align: center;">2025-07-24</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; color: #121212; text-align: center;">2025-07-26</td>
							<td style="text-align: right; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; color: #121212;">68,000</td>
							<td style="text-align: right; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; color: #121212;">6,800</td>
							<td style="text-align: right; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; color: #121212;">74,800</td>
						</tr>
					</tbody>
				</table>

				<div style="width:100%; height:15px;"></div>

				<table width="100%" border="0" cellpadding="0" cellspacing="0" style="font-size: 12px; border-collapse: collapse; border: 2px solid #757575; table-layout: fixed;">
					<colgroup>
						<col width="10%" />
						<col width="5%" />
						<col width="10%" />
						<col width="20%" />
						<col width="7%" />
						<col width="7%" />
						<col width="7%" />
						<col width="12%" />
						<col width="7%" />
						<col width="15%" />
					</colgroup>



							 <!-- 납품처코드  -->
							 <!-- 납품처명 -->
						<tbody>

															<tr>

															<td colspan="10" style="text-align:left; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;"><strong>납품처명 :</strong>(주)동우씨엠에스</td>
							</tr>

							<tr style="border-top: 1px solid #ddd;">
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">출고일자</th>
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">구분</th>
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">수주번호</th>
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">품목명</th>
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">수량</th>
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">단위</th>
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">단가</th>
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">금액</th>
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">출하지</th>
								<th style="height: 18px; line-height: 18px; padding: 6px; font-weight: normal; text-align: left; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">도착지</th>
							</tr>


								<tr style="border-top: 1px solid #ddd;">
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">20250725</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">OR</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">400274828 </td>
									<td style="text-align: left; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: left;">Uniflott  EXPORT 5 kg-Sack (200)</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">1</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">PC</td>
									<td style="text-align: right; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;">8,000</td>
									<td style="text-align: right; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;">8,000</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">기흥창고</td>
									<td style="text-aling: left; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;">서울특별시 중구  서울 중구 세종대로 136 (태평로1가, 파이</td>
								</tr>
									<!-- 소계 -->


								<tr style="border-top: 1px solid #ddd;">
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">20250725</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">OR</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">400274828 </td>
									<td style="text-align: left; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: left;">제품 예외 운송비</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">1</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">PC</td>
									<td style="text-align: right; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;">60,000</td>
									<td style="text-align: right; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;">60,000</td>
									<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">기흥창고</td>
									<td style="text-aling: left; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;">서울특별시 중구  서울 중구 세종대로 136 (태평로1가, 파이</td>
								</tr>
									<!-- 소계 -->


							<tr style="border-top: 1px solid #ddd;">
								<th colspan="4" style="height: 18px; line-height: 18px; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">소계</th>
								<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: right; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; font-weight: 600;"></td>
								<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;"></td>
								<td style="text-align: right; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;"></td>
								<td style="text-align: right; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; font-weight: 600;">68,000</td>
								<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;"></td>
								<td style="text-align: left; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212;"></td>
							</tr>

						</tbody>

																										</table>

				<div style="width:100%; height:15px;"></div>

								<table width="100%" border="0" cellpadding="0" cellspacing="0" style="font-size: 12px; border-collapse: collapse; border: 2px solid #757575; table-layout: fixed;">
					<colgroup>
						<col width="5%" />
						<col width="12%" />
						<col width="12%" />
						<col width="12%" />
						<col width="12%" />
						<col width="12%" />
						<col width="15%" />
						<col width="20%" />
					</colgroup>
					<tbody>
						<tr>
							<th rowspan="2" style="border-right: 1px solid #ddd; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">채권현황</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">전월채권</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">당월매출</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">현금수금</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">어음수금</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">당월채권</th>
							<th style="height: 18px; line-height: 18px; padding: 6px; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; text-align: center; font-weight: 600;">어음만기수금</th>
							<th style="height: 18px; line-height: 18px; background-color: #dddddd !important; padding: 6px; font-weight: 600; text-align: center; background-color: #f5f5f5; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">미도래어음</th>
						</tr>
						<tr style="border-top: 1px solid #ddd;">

							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">97,033,890</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">108,669,368</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">0</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">0</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">205,703,258</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">0</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">0</td>

							<!--
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">개선중</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">108,669,368</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">개선중</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">개선중</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">205,703,258</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: right;">0</td>
							<td style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; padding: 6px; height: 16px; font-size: 12px; word-wrap: break-word; color: #121212; text-align: center;">개선중</td>
							-->
						</tr>
					</tbody>
				</table>

				<div style="width: 100%;  padding: 30px 0 0;">
										<br><em style="float: left; font-size: 13px; padding: 0px 10px 0px 10px; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; color: #121212; font-style: normal;">*상단의 채권 현황은 <strong style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">2025년 07월</strong> 기준입니다.</em>
					<div style="height:10px"></div>
					<br><em style="float: left; font-size: 13px; padding: 0px 10px 0px 10px; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; color: #121212; font-style: normal;">*상단의 미도래어음은 자료 생성 시점<strong style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">(2025년 07월 30일)</strong>의 금액입니다.</em>
					<div style="height:10px"></div>
										<br><em style="float: left; font-size: 13px; padding: 0px 10px 0px 10px; font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; color: #121212; font-style: normal; line-height: 20px;">*귀사의 물품대금 입금계좌는 <strong style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif;">138-95141-921-05</strong>이며, 당사의 임직원은 어떠한 경우에도 고객에게 개인 계좌로 결제를 요구하지 않습니다.</em>
					<strong style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; float: left; text-align: center; width: 100%; font-size: 24px; font-weight: 600; line-height: 2em; margin-top: 60px;">
						크나우프석고보드(주)
					</strong>
					<span style="font-family: 'NanumGothic', Dotum, 돋움, Sans-serif; width: 100%; font-size: 18px; text-align: center; position: relative; display: inline-block; margin: -1.5em auto 0;">대표이사 송광섭<img src="https://neweorder.knaufapac.kr/eorder/data/config/ceoseal.png" alt="stamp" style="position: relative; width: 70px; bottom: -10px; right: -20px" /></span>
				</div>

			</div>
		</div>
	</div><!-- END container -->

</body>
</html>
```


---------------------------------------------------------------------


무료 라이선스 기반으로 **이미지 포함**, **한글 폰트 내장**, **페이지 브레이크·헤더/푸터 등 고급 기능**까지 모두 지원 가능한 방법으로, **OpenHTMLtoPDF**를 사용한 구현 예시를 드릴게.

---

## ⭐ OpenHTMLtoPDF 주요 특징

* Java 기반 LGPL/MPL 라이선스로 상업용 프로젝트에 **완전 무료 사용 가능**([GitHub][1])
* 이미지(URL 또는 Base64) 자동 로딩 지원
* CSS `@page`, 반복 헤더/푸터, 페이지 브레이크 등 문서형 레이아웃 지원
* PDF/A 및 접근성 표준 지원([GitHub][2], [GitHub][1])

---

## ✅ 구현 흐름 요약

### 1. HTML 내부에 한글 폰트 내장 선언

```html
<style>
@font-face {
  font-family: 'NanumGothic';
  src: url('fonts/NanumGothic-Regular.ttf');
  font-weight: normal;
  font-style: normal;
  -fs-pdf-font-embed: embed;
  -fs-font-subset: complete-font;
}
@font-face {
  font-family: 'NanumGothic';
  src: url('fonts/NanumGothic-Bold.ttf');
  font-weight: bold;
  font-style: normal;
  -fs-pdf-font-embed: embed;
}
body {
  font-family: 'NanumGothic', sans-serif;
}
</style>
```

* `.ttf` 파일은 프로젝트 리소스로 포함되어야 하며, `-fs-pdf-font-embed: embed` 옵션으로 PDF에 완전 내장됩니다
* `complete-font` 옵션 사용 시 폰트 서브셋 없이 전체 포함([github-wiki-see.page][3])

---

### 2. Java 코드: PDF 생성 부분

```java
Document w3cDoc = new W3CDom().fromJsoup(
    Jsoup.parse(contentStr, baseUrl));

PdfRendererBuilder builder = new PdfRendererBuilder();
builder.useFastMode();
builder.withW3cDocument(w3cDoc, baseUrl);

// 폰트를 코드에서 직접 등록해도 안전합니다
builder.useFont(new File("/path/fonts/NanumGothic-Regular.ttf"), "NanumGothic", 400, FontStyle.NORMAL, true);
builder.useFont(new File("/path/fonts/NanumGothic-Bold.ttf"), "NanumGothic", 700, FontStyle.NORMAL, true);

try (OutputStream os = new FileOutputStream(file)) {
    builder.toStream(os);
    builder.run();
}
```

* `baseUrl`은 CSS와 이미지, 폰트 파일 경로 기준
* 코드 내 `useFont()`로 폰트를 수동 등록도 가능([Stack Overflow][4], [github-wiki-see.page][3])

---

### 3. `MailUtil.sendMail(...)` 호출 방식

```java
// PDF 파일 생성 후:
mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, "",
              file, filename);
```

* 본문은 빈 문자열 `""` 사용
* PDF 파일은 첨부 파일로 전달

---

## 📋 기능 지원 정리

| 기능 항목           | 구현 방식 및 지원 여부                                                                                                                |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| 이미지 포함          | `<img src="URL">` 또는 Base64 방식 모두 렌더링 지원                                                                                     |
| 한글 폰트 내장        | `.ttf` 파일 + `@font-face` + embed 옵션으로 내장 폰트 완전 지원                                                                            |
| 페이지 브레이크, 헤더/푸터 | CSS `@page`, 반복 렌더링 설계, 테이블 내 페이지 구분 지원                                                                                      |
| PDF/A, 접근성      | OpenHTMLtoPDF는 PDF/A 및 접근성 표준 준수 가능([Javadoc][5], [Stack Overflow][6], [Javadoc][7], [github-wiki-see.page][3], [GitHub][1]) |

---

## 📦 HTML 전체에 대한 적용 예시

* 제공해준 HTML 코드의 `<head>` 내부에 위 폰트 선언 스타일 삽입
* HTML 내 `<img src="https://…/ceoseal.png">` 이미지는 URL 그대로 유지 가능
* CSS `@media print`는 OpenHTMLtoPDF가 지원하므로 그대로 반영 가능

---

## ❓확인을 요청 드려요

1. `fonts/NanumGothic-Regular.ttf` 및 Bold 폰트 파일을 프로젝트 내 포함 가능하신가요?
2. 이미지가 외부 URL인지, 내부 Base64 방식인지 알려주세요.
3. 페이지 헤더/푸터 반복, 페이지 번호 삽입, 테이블 반복 헤더 등 구체적으로 필요한 기능 있으면 알려주세요.

이 정보들 알려주시면 MailUtil 통합 클래스 전체 또는 CSS 템플릿 예시도 단계별로 제공해 드릴게요. 필요한 부분 말씀 주세요!

[1]: https://github.com/danfickle/openhtmltopdf?utm_source=chatgpt.com "danfickle/openhtmltopdf: An HTML to PDF library for the ... - GitHub"
[2]: https://github.com/danfickle/openhtmltopdf/issues/654?utm_source=chatgpt.com "Problem with embedded fonts and PDF/A generation #654 - GitHub"
[3]: https://github-wiki-see.page/m/danfickle/openhtmltopdf/wiki/Fonts?utm_source=chatgpt.com "Fonts - danfickle/openhtmltopdf GitHub Wiki"
[4]: https://stackoverflow.com/questions/75952389/how-to-use-custom-font-using-openhtmltopdf/75957929?utm_source=chatgpt.com "How to use custom font using openHtmlToPdf? - Stack Overflow"
[5]: https://javadoc.io/doc/com.openhtmltopdf/openhtmltopdf-core/1.0.7/index-all.html?utm_source=chatgpt.com "Index (Openhtmltopdf Core Renderer 1.0.7 API) - javadoc.io"
[6]: https://stackoverflow.com/questions/tagged/openhtmltopdf?utm_source=chatgpt.com "Newest 'openhtmltopdf' Questions - Stack Overflow"
[7]: https://javadoc.io/doc/com.openhtmltopdf/openhtmltopdf-pdfbox/1.0.2/com/openhtmltopdf/pdfboxout/PdfBoxFontResolver.html?utm_source=chatgpt.com "PdfBoxFontResolver - openhtmltopdf-pdfbox 1.0.2 javadoc"


---------------------------------------------------------------------



# 외부 이미지 경로이고. <img src="https://neweorder.knaufapac.kr/eorder/data/config/ceoseal.png" alt="stamp" style="position: relative; width: 70px; bottom: -10px; right: -20px" />


---------------------------------------------------------------------



무료 오픈소스 기반으로 외부 이미지 포함, 한글 폰트 완전 내장, 페이지 기능 지원까지 모두 만족하는 구성은 **OpenHTMLtoPDF** (LGPL/MPL 라이선스) 사용이 유일한 적합한 방법입니다. 아래는 주어진 전체 HTML 코드를 기반으로 구현하는 예시입니다.

---

## 📌 핵심 요약

* **라이선스**: 완전 무료 (LGPL/MPL), 상업용 사용 가능
* **이미지 지원**: 외부 URL 기반 `<img src="https://…stamp.png">` 자동 로딩 및 렌더링 지원 ([openhtmltopdf.com][1])
* **한글 폰트 내장**: `.ttf` 폰트 파일을 `@font-face` + `useFont()`로 등록 → 다운로드 사용자 폰트 설치 불필요 ([openhtmltopdf.com][1], [Stack Overflow][2])
* **PDF 기능 지원**: CSS 기반 페이지 브레이크, 반복 테이블 헤더, 헤더/푸터 설계 가능 ([openhtmltopdf.com][1])

---

## ✅ HTML `<head>` 수정 (폰트 및 css 포함 예시)

```html
<head>
  … (기존 메타, 스타일 유지)
  <style>
    @font-face {
      font-family: 'NanumGothic';
      src: url('fonts/NanumGothic-Regular.ttf');
      font-weight: normal;
      font-style: normal;
      -fs-pdf-font-embed: embed;
      -fs-font-subset: complete-font;
    }
    @font-face {
      font-family: 'NanumGothic';
      src: url('fonts/NanumGothic-Bold.ttf');
      font-weight: bold;
      font-style: normal;
      -fs-pdf-font-embed: embed;
    }
    body { font-family: 'NanumGothic', sans-serif; }
  </style>
</head>
```

* `complete-font` 옵션을 설정하면 PDF에 폰트 전체 포함됩니다 ([openhtmltopdf.com][1])
* 외부 이미지 태그는 그대로 두면 URL로부터 렌더링됨

---

## 🔧 Java 코드 예시 (OpenHTMLtoPDF 라이브러리 사용)

```java
// jsoup + W3CDom 경로
String baseUri = "/path/to/resources/";  // 폰트와 css, 이미지 경로 기준
org.w3c.dom.Document doc = new W3CDom().fromJsoup(
    Jsoup.parse(contentStr, baseUri)
);

// PDF 생성 설정
PdfRendererBuilder builder = new PdfRendererBuilder();
builder.useFastMode();
builder.withW3cDocument(doc, baseUri);

// 폰트 직접 등록 (안정성 확보)
builder.useFont(new File("/path/fonts/NanumGothic-Regular.ttf"),
                "NanumGothic", FontWeight.NORMAL, FontStyle.NORMAL, true);
builder.useFont(new File("/path/fonts/NanumGothic-Bold.ttf"),
                "NanumGothic", FontWeight.BOLD, FontStyle.NORMAL, true);

// 출력 스트림 지정 및 실행
try (OutputStream os = new FileOutputStream(file)) {
    builder.toStream(os);
    builder.run();
}
```

* `baseUri`는 HTML 내 핸들링되는 이미지/폰트 경로 해석 기준입니다 ([넷제이즈텍][3])

---

## 🧾 기능 지원 정리 테이블

| 항목              | 구현 방식 및 특징                               |
| --------------- | ---------------------------------------- |
| 이미지 포함          | 외부 URL 자동 로드 및 렌더링                       |
| 한글 폰트 내장        | `.ttf` + `@font-face` + `useFont()`      |
| 페이지 브레이크 및 레이아웃 | CSS `@page`, `page-break-*`, 헤더/푸터 설계 지원 |
| PDF 표준 준수       | PDF/A, 접근성 지원 가능 (OpenHTMLtoPDF 기능 포함)   |

---

## ❓다음 단계 질의

1. **폰트 파일** (`fonts/NanumGothic-Regular.ttf` 및 Bold) 프로젝트 포함 가능하신가요?
2. 이미지 URL 이외에 다른 이미지나 외부 CSS가 더 있나요?
3. 반복되는 **페이지 헤더/푸터**, **페이지 번호**, **테이블 헤더 반복 기능** 등이 필요하시면 구체적으로 알려 주세요.

필요하시면 `MailUtil.sendMail`과 통합된 전체 클래스 예제도 단계별로 제공해 드릴게요!

[1]: https://openhtmltopdf.com/template-guide.pdf?utm_source=chatgpt.com "[PDF] OpenHTMLToPDF Template Guide - Open HTML to PDF"
[2]: https://stackoverflow.com/questions/55339298/embed-a-custom-font-into-pdf-created-out-of-html?utm_source=chatgpt.com "Embed a custom font into PDF created out of HTML - Stack Overflow"
[3]: https://www.netjstech.com/2021/02/convert-html-to-pdf-java-Openhtmltopdf-PDFBox.html?utm_source=chatgpt.com "Convert HTML to PDF in Java + Openhtmltopdf and PDFBox"



---------------------------------------------------------------------


# 네가 추천해주는 방법과 그 방법으로 개발할 때 작업 프로세스를 단계별로 정리해줘. 그리고 이슈나 고려사항도 알려줘.



---------------------------------------------------------------------



좋아, 알겠어! 아래는 **무료 오픈소스(OpenHTMLtoPDF)** 기반으로 구현할 때 전체 작업 프로세스를 단계별로 정리한 거야. 이미지, 한글 폰트, 헤더/푸터, 페이지 브레이크 등 네가 원한 모든 기능을 포함해 정리했고, 각 단계마다 고려사항이나 잠재적인 이슈도 함께 정리했어.

---

## 1단계: 프로젝트 환경 구성 🛠

**필요 라이브러리 (Maven 기준)**

```xml
<dependency>
  <groupId>com.openhtmltopdf</groupId>
  <artifactId>openhtmltopdf-core</artifactId>
  <version>1.0.x</version>
</dependency>
<dependency>
  <groupId>com.openhtmltopdf</groupId>
  <artifactId>openhtmltopdf-pdfbox</artifactId>
  <version>1.0.x</version>
</dependency>
```

▶ OpenHTMLtoPDF는 PDFBox 엔진 기반이며, Java 8 이상, LGPL/MPL 라이선스로 상업적 무료 사용 가능해 ([GitHub][1]).

---

## 2단계: HTML / CSS 수정

### ① 한글 폰트 내장 선언 (`@font-face`)

```html
@font-face {
  font-family: 'NanumGothic';
  src: url('fonts/NanumGothic-Regular.ttf');
  font-weight: normal; font-style: normal;
  -fs-pdf-font-embed: embed;
  -fs-font-subset: complete-font;
}
@font-face {
  font-family: 'NanumGothic';
  src: url('fonts/NanumGothic-Bold.ttf');
  font-weight: bold;
  -fs-pdf-font-embed: embed;
}
body { font-family: 'NanumGothic', sans-serif; }
```

* `.ttf` 폰트 파일이 반드시 TrueType 형식이어야 하고 embed 옵션을 사용해야 PDF에 내장됨 ([openhtmltopdf.com][2]).
* italic, bold 조합이 있는 경우 각각 정의 필요.

### ② 반복 헤더/푸터 및 페이지 번호 설정

```css
<div id="header">…</div>
<div id="footer">Page <span class="pageno"></span> / <span class="pagecount"></span></div>
@page {
  margin: 20mm;
  @top-center { content: element(header) }
  @bottom-center { content: element(footer) }
}
```

* `position: running(header)` 등 CSS3 running element 사용 가능하며, 페이지마다 상단/하단 표시 지원 ([구글 사이트][3], [CUBA.Platform][4]).
* 테이블 헤더 반복은 `-fs-table-paginate: paginate;` 스타일 적용으로 지원 가능 ([CUBA.Platform][4]).

---

## 3단계: Java PDF 변환 코드 작성

```java
// XHTML 파싱
String baseUri = new File("resources/").toURI().toString();
org.w3c.dom.Document doc = new W3CDom().fromJsoup(Jsoup.parse(contentStr, baseUri));

// PDF 변환 설정
PdfRendererBuilder builder = new PdfRendererBuilder();
builder.useFastMode();
builder.withW3cDocument(doc, baseUri);

// 폰트 직접 등록 (optional 안전성 확보)
builder.useFont(new File("fonts/NanumGothic-Regular.ttf"), "NanumGothic",
                400, FontStyle.NORMAL, true);
builder.useFont(new File("fonts/NanumGothic-Bold.ttf"), "NanumGothic",
                700, FontStyle.NORMAL, true);

// PDF 생성
try (OutputStream os = new FileOutputStream(file)) {
    builder.toStream(os);
    builder.run();
}
```

* `useFastMode()`는 렌더링 속도를 높이지만 약간 정밀도 희생 가능 ([HowToDoInJava][5]).
* `baseUri`는 폰트/이미지 참조의 기준 경로로 필요해.

---

## 4단계: MailUtil 통합

* 앞서 생성한 PDF `file`을 `MailUtil.sendMail(smtpHost, title, "", email, shopName, smtpSender, "", file, filename);` 방식으로 본문 없이 첨부로 전송.
* `contentStr` 내용 그대로 본문 사용하지 않고 빈 문자열("") 전달 추천.

---

## ✅ 작업 흐름 요약 (단계별)

1. **환경 세팅**: OpenHTMLtoPDF 라이브러리 추가
2. **HTML 편집**: `<head>` 내부에 @font-face 선언, header/footer용 블록 추가
3. **CSS 설정**: @page, running 요소, 테이블 페이징 스타일 포함
4. **Java 변환 코드**: W3C DOM 변환 + PdfRendererBuilder 설정
5. **PDF 생성 후 메일 전송**: MailUtil 활용
6. **테스트 및 디버깅**: 이미지, 폰트, 레이아웃, 페이지 매김 정상 확인

---

## ⚠️ 주요 이슈 및 고려사항

* **폰트 파일 포함**: `.ttf` 파일이 누락되면 한글 깨짐 발생. embed 선언 필수
* **CSS 적용 한계**: Flexbox, OpenType, JS 렌더링 같은 최신 HTML5 기능은 지원 안 됨 ([GitHub][1], [CUBA.Platform][4], [flyingsaucerproject.github.io][6], [APITemplate.io][7])
* **헤더/푸터 표시 문제**: header/footer 요소는 content보다 먼저 배치돼야 하고, CSS `position: running()`이 정확히 선언돼야 반복됨 ([CUBA.Platform][4])
* **큰 테이블 페이지 오버랩**: 테이블이 페이지 넘치는 경우 footer와 충돌할 수 있음. `page-break-inside: avoid; padding` 조정 필요 ([Stack Overflow][8], [CUBA.Platform][4])
* **사용자 이미지 접근**: 외부 URL 이미지가 네트워크 문제로 로드 못 하면 PDF에서 누락될 수 있음. 예비 처리 또는 Base64 변환 고려
* **PDF 표준 준수**: PDF/A, 접근성 기능을 사용할 경우 설정이 따로 필요함. OpenHTMLtoPDF는 지원 가능 ([GitHub][1])

---

📌 요약하면, **OpenHTMLtoPDF**는 네가 원하는 모든 기능 — 이미지 내장, 한글 폰트 내장, 헤더/푸터 반복, 페이지 레이아웃 제어 — 를 **모두 무료 라이선스**로 지원할 수 있어.
다음 정보 알려주면 클래스나 CSS 템플릿까지 바로 만들어줄게!

1. NanumGothic 폰트 파일 위치
2. header / footer 디자인 내용 (내용, 위치, 페이지 번호 포함 여부 등)
3. 테이블 등의 긴 콘텐츠에 대한 세부 스타일 요구사항

필요하면 `MailUtil` 통합 코드 예시까지 단계별 통합도 가능하니 편하게 말해줘!

[1]: https://github.com/danfickle/openhtmltopdf?utm_source=chatgpt.com "danfickle/openhtmltopdf: An HTML to PDF library for the ... - GitHub"
[2]: https://openhtmltopdf.com/template-guide.pdf?utm_source=chatgpt.com "[PDF] OpenHTMLToPDF Template Guide - Open HTML to PDF"
[3]: https://sites.google.com/site/bingsite/web-development/java-goodies/document-generation/pdf-template-engine-xsl-fo/openhtmltopdf?utm_source=chatgpt.com "Bing's Tech Notes - OpenHtmlToPDF - Google Sites"
[4]: https://forum.cuba-platform.com/t/how-to-insert-repeatable-footer-header-blocks-on-pages-of-html-to-pdf-report/1967?utm_source=chatgpt.com "How to insert repeatable footer / header blocks on pages of HTML to ..."
[5]: https://howtodoinjava.com/java/library/convert-html-template-to-pdf/?utm_source=chatgpt.com "Java Generate PDF from HTML Template using OpenHtmlToPdf"
[6]: https://flyingsaucerproject.github.io/flyingsaucer/r8/guide/users-guide-R8.html?utm_source=chatgpt.com "The Flying Saucer User's Guide"
[7]: https://apitemplate.io/blog/a-guide-to-generating-pdfs-in-java/?utm_source=chatgpt.com "Generating PDFs in Java with 3 Popular Libraries - APITemplate.io"
[8]: https://stackoverflow.com/questions/74239886/add-header-image-and-footer-image-to-a-pdf-in-pdfbox?utm_source=chatgpt.com "Add Header image and Footer image to a pdf in PDFBox"


