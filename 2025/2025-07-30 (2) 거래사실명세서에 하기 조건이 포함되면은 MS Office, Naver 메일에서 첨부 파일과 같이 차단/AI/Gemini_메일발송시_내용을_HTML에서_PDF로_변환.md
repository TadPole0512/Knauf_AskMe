# 아래에 메일을 발송하는 소스에서 contentStr이 html로 내용이 구성되어 있는데 메일 발송하기 전에 PDF 파일로 바꾸는 방법을 알려줘

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


네, `contentStr` HTML 문자열을 메일 발송 전에 PDF 파일로 변환하는 방법을 알려드리겠습니다. Java에서 HTML을 PDF로 변환할 때는 **iText** 라이브러리를 사용하는 것이 가장 일반적이고 강력한 방법입니다.

아래 단계에 따라 기존 코드를 수정할 수 있습니다.

-----

### 1\. iText 라이브러리 추가 (Maven 의존성)

먼저, `pom.xml` 파일에 iText7 관련 의존성을 추가해야 합니다. HTML을 PDF로 변환하기 위해서는 `html2pdf` 모듈이 필요합니다.

```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version> <type>pom</type>
</dependency>

<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>html2pdf</artifactId>
    <version>4.0.5</version> </dependency>
```

💡 **Tip:** Maven이나 Gradle 같은 빌드 도구를 사용하지 않는 경우, iText 공식 웹사이트에서 해당 `.jar` 파일들을 직접 다운로드하여 프로젝트의 라이브러리 경로에 추가해야 합니다.

-----

### 2\. 소스 코드 수정

기존 코드에서 HTML 파일을 생성하는 부분을 iText를 사용하여 PDF를 생성하는 코드로 변경합니다.

**핵심 로직**
기존의 `BufferedWriter`를 사용한 파일 작성 로직을 `HtmlConverter.convertToPdf()` 메소드로 대체합니다.

```java
import com.itextpdf.html2pdf.HtmlConverter;
import java.io.FileOutputStream;
// ... 기타 필요한 import

// ...

// 1. 파일 이름과 경로를 PDF용으로 변경합니다.
String filename = "report_" + System.currentTimeMillis() + ".pdf";
File file = new File("/path/to/save/" + filename); // 실제 저장 경로로 변경

try {
    // 2. FileOutputStream을 사용하여 HTML 문자열을 바로 PDF 파일로 변환합니다.
    HtmlConverter.convertToPdf(contentStr, new FileOutputStream(file));

} catch (IOException ioe) {
    // 예외 처리
    ioe.printStackTrace();
}

// MailUtil을 사용하여 변환된 PDF 파일을 첨부하여 메일을 발송합니다.
MailUtil mail = new MailUtil();
for (String email : emailArr) {
    if (!StringUtils.equals("", email)) {
        // 메일 전송 (file 객체가 이제 PDF 파일을 가리킵니다)
        mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, contentStr, file, filename);
        
        // ... (이하 동일)
    }
}
```

-----

### 3\. 수정된 전체 소스 코드 예시

사용자의 기존 코드에 위 로직을 적용한 전체 예시는 다음과 같습니다.

```java
// 필요한 iText 클래스를 import 합니다.
import com.itextpdf.html2pdf.HtmlConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// ... (기존 코드)

// PDF 파일명 및 객체를 미리 정의합니다.
// 파일명에 타임스탬프를 추가하여 중복을 방지하는 것이 좋습니다.
String filename = "report_" + System.currentTimeMillis() + ".pdf"; 
File file = new File("임시_저장_경로/" + filename); // ⚠️ 실제 서버의 파일 저장 경로로 수정해야 합니다.

try {
    // iText를 사용하여 HTML 문자열(contentStr)을 PDF 파일로 변환
    HtmlConverter.convertToPdf(contentStr, new FileOutputStream(file));
    
    // PDF 변환 성공 로그 (선택 사항)
    System.out.println("PDF 파일 생성 성공: " + file.getAbsolutePath());

} catch(IOException ioe) {
    // PDF 변환 중 에러 처리
    ioe.printStackTrace();
    // PDF 생성 실패 시 메일 발송을 중단하거나 다른 처리를 할 수 있습니다.
    return; // 예시: 에러 발생 시 메서드 종료
}

MailUtil mail = new MailUtil();
for(String email : emailArr) {
    if(!StringUtils.equals("", email)) {
        // 메일전송: 이제 file과 filename은 PDF 파일 정보입니다.
        // contentStr은 이메일 본문으로 계속 사용할 수 있습니다.
        mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, contentStr, file, filename);
        
        // 전송이력 저장
        this.insertSendMailHistory("factReport", Converter.toStr(params.get("insdate")), Converter.toStr(params.get("inedate"))
                , Converter.toStr(params.get("m_custcd")), Converter.toStr(params.get("m_shiptocd")), Converter.toStr(params.get("r_smhtype")), email, loginDto.getUserId());
    }
}

// (선택 사항) 메일 발송 후 임시로 생성한 PDF 파일 삭제 로직 추가 가능
// if (file.exists()) {
//     file.delete();
// }
```

### ⚠️ 추가 고려사항

  * **한글 폰트:** HTML 내용에 한글이 포함된 경우, PDF 변환 시 글자가 깨질 수 있습니다. 이 경우, 나눔고딕과 같은 한글을 지원하는 폰트를 서버에 설치하고 iText에 폰트 정보를 설정해주어야 합니다.
  * **CSS 스타일:** HTML에 포함된 CSS 스타일(특히 외부 CSS 파일)이 제대로 적용되지 않을 수 있습니다. 가급적 인라인 스타일을 사용하거나, `ConverterProperties`를 설정하여 CSS 경로를 지정해주는 것이 좋습니다.
  * **파일 경로:** `new File()`에 사용되는 경로는 서버에 실제 파일이 저장될 수 있는 유효한 경로여야 하며, 쓰기 권한이 필요합니다.



---------------------------------------------------------------------


# 위의 내용을 OpenHTMLToPDF를 이용하는 방법으로 알려줘.


---------------------------------------------------------------------



네, 물론입니다. **OpenHTMLToPDF**를 사용해 HTML 문자열을 PDF로 변환하는 방법은 다음과 같습니다. OpenHTMLToPDF는 Flying Saucer 프로젝트에 기반한 오픈소스 라이브러리로, Apache PDFBox를 렌더링 백엔드로 사용합니다.

-----

### 1\. OpenHTMLToPDF 라이브러리 추가 (Maven 의존성)

`pom.xml` 파일에 OpenHTMLToPDF 의존성을 추가합니다. PDF 생성을 위해 `openhtmltopdf-pdfbox` 아티팩트가 필요합니다. 이 아티팩트는 핵심 라이브러리(`openhtmltopdf-core`)를 자동으로 포함합니다.

```xml
<dependency>
    <groupId>io.github.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-pdfbox</artifactId>
    <version>1.1.28</version> </dependency>
```

💡 **참고:** 이전 버전은 `com.openhtmltopdf` 그룹 ID를 사용했지만, 최신 버전은 `io.github.openhtmltopdf`를 사용합니다.

-----

### 2\. 소스 코드 수정

기존 파일 생성 로직을 OpenHTMLToPDF의 `PdfRendererBuilder`를 사용하는 코드로 교체합니다.

**핵심 로직**
`PdfRendererBuilder`를 사용하여 HTML 콘텐츠와 출력 스트림을 설정하고, `run()` 메소드를 호출하여 PDF를 생성합니다.

```java
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.OutputStream;
import java.io.FileOutputStream;
// ... 기타 필요한 import

// ...

// 1. 파일 이름과 경로를 PDF용으로 변경합니다.
String filename = "report_" + System.currentTimeMillis() + ".pdf";
File file = new File("/path/to/save/" + filename); // 실제 저장 경로로 변경

// 2. try-with-resources 구문을 사용하여 OutputStream을 안전하게 처리합니다.
try (OutputStream os = new FileOutputStream(file)) {
    PdfRendererBuilder builder = new PdfRendererBuilder();
    builder.useFastMode(); // 빠른 렌더링 모드 사용
    
    // HTML 문자열과 BaseURI를 설정합니다. (BaseURI는 이미지, CSS 등 상대 경로 리소스를 찾기 위해 필요)
    // 여기서는 BaseURI가 필요 없으므로 null로 설정합니다.
    builder.withHtmlContent(contentStr, null);
    
    // 출력 스트림으로 PDF를 생성합니다.
    builder.toStream(os);
    builder.run();
} catch (IOException ioe) {
    // 예외 처리
    ioe.printStackTrace();
}

// MailUtil을 사용하여 변환된 PDF 파일을 첨부하여 메일을 발송합니다.
// ... (이하 로직은 iText 예시와 동일)
```

-----

### 3\. 수정된 전체 소스 코드 예시

사용자의 기존 코드에 OpenHTMLToPDF 로직을 적용한 전체 예시입니다.

```java
// 필요한 OpenHTMLToPDF 클래스를 import 합니다.
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;

// ... (기존 코드)

// PDF 파일명 및 객체를 미리 정의합니다.
String filename = "report_" + System.currentTimeMillis() + ".pdf";
File file = new File("임시_저장_경로/" + filename); // ⚠️ 실제 서버의 파일 저장 경로로 수정해야 합니다.

// try-with-resources를 사용하여 스트림을 자동으로 닫도록 합니다.
try (OutputStream os = new FileOutputStream(file)) {
    PdfRendererBuilder builder = new PdfRendererBuilder();

    // 폰트 설정 (한글 깨짐 방지) - 아래 '추가 고려사항' 참고
    // builder.useFont(new File("C:/Windows/Fonts/malgun.ttf"), "Malgun Gothic");

    builder.useFastMode();
    builder.withHtmlContent(contentStr, null); // HTML 문자열을 소스로 지정
    builder.toStream(os); // 출력 대상을 파일 스트림으로 지정
    builder.run(); // PDF 생성 실행

    System.out.println("PDF 파일 생성 성공: " + file.getAbsolutePath());

} catch (Exception e) { // IOException 외 다른 예외도 처리
    e.printStackTrace();
    return; // 에러 발생 시 메서드 종료
}

MailUtil mail = new MailUtil();
for(String email : emailArr) {
    if(!StringUtils.equals("", email)) {
        // 메일전송: 이제 file과 filename은 PDF 파일 정보입니다.
        mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, contentStr, file, filename);
        
        // 전송이력 저장
        this.insertSendMailHistory("factReport", Converter.toStr(params.get("insdate")), Converter.toStr(params.get("inedate"))
                , Converter.toStr(params.get("m_custcd")), Converter.toStr(params.get("m_shiptocd")), Converter.toStr(params.get("r_smhtype")), email, loginDto.getUserId());
    }
}
```

### ⚠️ 추가 고려사항: 한글 처리 (중요)

HTML 콘텐츠에 한글이 포함된 경우, PDF에서 글자가 보이지 않거나 깨질 수 있습니다. **서버에 한글 폰트 파일(.ttf)을 위치시키고** `PdfRendererBuilder`에 해당 폰트를 등록해야 합니다.

1.  서버의 특정 경로에 `NanumGothic.ttf` 와 같은 한글 폰트 파일을 업로드합니다.
2.  `builder`에 `useFont` 메소드를 사용하여 폰트를 추가합니다.

<!-- end list -->

```java
// ...
try (OutputStream os = new FileOutputStream(file)) {
    PdfRendererBuilder builder = new PdfRendererBuilder();

    // 폰트 파일 경로와 CSS에서 사용할 폰트 패밀리 이름을 지정합니다.
    File fontFile = new File("/path/to/fonts/NanumGothic.ttf");
    builder.useFont(fontFile, "NanumGothic");

    // 이제 HTML의 CSS에서 "NanumGothic"을 font-family로 사용할 수 있습니다.
    // 예: <body style="font-family: 'NanumGothic';"> ... </body>
    
    builder.useFastMode();
    builder.withHtmlContent(contentStr, null);
    builder.toStream(os);
    builder.run();
}
// ...
```

만약 폰트를 코드에서 직접 지정하지 않고 CSS의 `@font-face`를 사용하고 싶다면, `withHtmlContent`의 두 번째 인자인 `baseUri`를 설정하여 폰트 파일의 상대 경로를 올바르게 해석할 수 있도록 해야 합니다.