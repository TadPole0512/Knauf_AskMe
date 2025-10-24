<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" class="logo" width="120"/>

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

네, 질문하신 내용을 정리하면 아래와 같습니다:

- `contentStr`에 HTML 형태의 메일 본문이 있음
- 메일을 보내기 전에 해당 내용을 PDF로 변환해서 PDF 파일을 첨부하고 싶음

아래는 자바(Java)에서 HTML을 PDF로 변환하는 대표적인 방법입니다.
보통 [OpenHTMLtoPDF](https://github.com/danfickle/openhtmltopdf)나 iText 라이브러리(상용/무료 제한 있음), Flying Saucer 등 오픈소스 PDF 라이브러리를 많이 사용합니다.

### **추천 방법: OpenHTMLtoPDF 사용**

**1. 라이브러리 추가 (Maven 기준)**

```xml
<dependency>
    <groupId>com.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-pdfbox</artifactId>
    <version>1.0.10</version>
</dependency>
```

(Gradle, 직접 JAR 다운로드 등 환경에 맞게 추가)

**2. Java 코드 – HTML을 PDF로 변환**

아래는 `contentStr`(HTML String) → PDF 파일로 만드는 예시입니다.

```java
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.*;

public void htmlToPdf(String htmlContent, File pdfFile) throws IOException {
    try (OutputStream os = new FileOutputStream(pdfFile)) {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(os);
        builder.run();
    } catch(Exception e){
        e.printStackTrace();
    }
}
```


---

**3. 기존 코드 반영 예시**

```java
// 1. PDF 변환
File pdfFile = new File("output.pdf");
htmlToPdf(contentStr, pdfFile);

// 2. 기존 메일 발송 코드에서 첨부파일을 pdfFile로 변경
for (String email : emailArr) {
    if(!StringUtils.equals("", email)) {
        mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, contentStr, pdfFile, "메일내용.pdf");
        // ...
    }
}
```


---

### **추가팁**

- contentStr에 한글/이미지 등 리소스가 포함되어 있다면 CSS, 폰트 경로나 외부 리소스 접근에 주의 필요
- PDF 저장경로, 임시파일 정리 신경쓰기


### **요약**

1. **OpenHTMLtoPDF 라이브러리를 추가**
2. **contentStr을 파일로 저장하는 대신 PDF로 변환**
3. **메일 첨부파일로 PDF 첨부**

이렇게 하시면 원하시는 대로 HTML 본문을 PDF로 변환하여 메일에 첨부할 수 있습니다.
더 궁금한 사항이 있으면 추가로 질문해주세요!

