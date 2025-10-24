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


Java에서 HTML 내용을 PDF로 변환하여 메일로 발송하는 방법을 알려드리겠습니다. 가장 널리 사용되는 라이브러리들을 이용한 방법들을 제시해드릴게요.

```java
// 방법 1: iText 7 + pdfHTML 사용 (추천)
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HtmlToPdfMailSender {

    /**
     * 방법 1: iText 7 + pdfHTML을 사용한 HTML -> PDF 변환
     */
    public void sendMailWithPdfUsingIText(String contentStr, String[] emailArr, 
                                        String smtpHost, String title, String shopName, 
                                        String smtpSender, String filename, 
                                        Map<String, Object> params, LoginDto loginDto) {
        
        File pdfFile = null;
        try {
            // PDF 파일 생성
            String pdfFilename = filename.replace(".html", ".pdf");
            pdfFile = new File(pdfFilename);
            
            // HTML을 PDF로 변환
            convertHtmlToPdf(contentStr, pdfFile);
            
            // 메일 발송
            MailUtil mail = new MailUtil();
            for(String email : emailArr) {
                if(!StringUtils.equals("", email)) {
                    // PDF 파일과 함께 메일전송
                    mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, 
                                "PDF 파일을 첨부하여 발송드립니다.", pdfFile, pdfFilename);
                    
                    // 전송이력 저장
                    this.insertSendMailHistory("factReport", 
                        Converter.toStr(params.get("insdate")), 
                        Converter.toStr(params.get("inedate")),
                        Converter.toStr(params.get("m_custcd")), 
                        Converter.toStr(params.get("m_shiptocd")), 
                        Converter.toStr(params.get("r_smhtype")), 
                        email, loginDto.getUserId());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            // 임시 파일 삭제
            if(pdfFile != null && pdfFile.exists()) {
                pdfFile.delete();
            }
        }
    }
    
    /**
     * iText 7을 사용한 HTML -> PDF 변환
     */
    private void convertHtmlToPdf(String htmlContent, File pdfFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            PdfWriter writer = new PdfWriter(fos);
            PdfDocument pdf = new PdfDocument(writer);
            
            // HTML을 PDF로 변환
            HtmlConverter.convertToPdf(htmlContent, pdf);
        }
    }

    /**
     * 방법 2: Flying Saucer (xhtmlrenderer) 사용
     */
    public void sendMailWithPdfUsingFlyingSaucer(String contentStr, String[] emailArr, 
                                               String smtpHost, String title, String shopName, 
                                               String smtpSender, String filename, 
                                               Map<String, Object> params, LoginDto loginDto) {
        
        File pdfFile = null;
        try {
            // PDF 파일 생성
            String pdfFilename = filename.replace(".html", ".pdf");
            pdfFile = new File(pdfFilename);
            
            // HTML을 PDF로 변환 (Flying Saucer 사용)
            convertHtmlToPdfWithFlyingSaucer(contentStr, pdfFile);
            
            // 메일 발송 로직은 동일
            MailUtil mail = new MailUtil();
            for(String email : emailArr) {
                if(!StringUtils.equals("", email)) {
                    mail.sendMail(smtpHost, title, "", email, shopName, smtpSender, 
                                "PDF 파일을 첨부하여 발송드립니다.", pdfFile, pdfFilename);
                    
                    this.insertSendMailHistory("factReport", 
                        Converter.toStr(params.get("insdate")), 
                        Converter.toStr(params.get("inedate")),
                        Converter.toStr(params.get("m_custcd")), 
                        Converter.toStr(params.get("m_shiptocd")), 
                        Converter.toStr(params.get("r_smhtype")), 
                        email, loginDto.getUserId());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(pdfFile != null && pdfFile.exists()) {
                pdfFile.delete();
            }
        }
    }
    
    /**
     * Flying Saucer를 사용한 HTML -> PDF 변환
     */
    private void convertHtmlToPdfWithFlyingSaucer(String htmlContent, File pdfFile) 
            throws DocumentException, IOException {
        
        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            
            // XHTML로 변환 (Flying Saucer는 well-formed XHTML이 필요)
            String xhtml = convertToXhtml(htmlContent);
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(fos);
        }
    }
    
    /**
     * HTML을 XHTML로 변환 (간단한 방법)
     */
    private String convertToXhtml(String html) {
        // 기본적인 XHTML 구조로 감싸기
        if (!html.toLowerCase().contains("<html")) {
            html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" " +
                   "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
                   "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
                   "<head><meta charset=\"UTF-8\"/></head>" +
                   "<body>" + html + "</body></html>";
        }
        
        // 자주 발생하는 문제들 수정
        html = html.replaceAll("<br>", "<br/>");
        html = html.replaceAll("<hr>", "<hr/>");
        html = html.replaceAll("<img([^>]*?)>", "<img$1/>");
        html = html.replaceAll("<input([^>]*?)>", "<input$1/>");
        
        return html;
    }
    
    /**
     * 방법 3: 메모리 내에서 PDF 생성 (파일 생성 없이)
     */
    public void sendMailWithPdfInMemory(String contentStr, String[] emailArr, 
                                      String smtpHost, String title, String shopName, 
                                      String smtpSender, String filename, 
                                      Map<String, Object> params, LoginDto loginDto) {
        
        try {
            // 메모리에서 PDF 생성
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            HtmlConverter.convertToPdf(contentStr, pdf);
            
            byte[] pdfBytes = baos.toByteArray();
            String pdfFilename = filename.replace(".html", ".pdf");
            
            // 메일 발송 (바이트 배열 사용)
            MailUtil mail = new MailUtil();
            for(String email : emailArr) {
                if(!StringUtils.equals("", email)) {
                    // MailUtil이 바이트 배열을 지원한다면
                    mail.sendMailWithAttachment(smtpHost, title, "", email, shopName, 
                                              smtpSender, "PDF 파일을 첨부하여 발송드립니다.", 
                                              pdfBytes, pdfFilename);
                    
                    this.insertSendMailHistory("factReport", 
                        Converter.toStr(params.get("insdate")), 
                        Converter.toStr(params.get("inedate")),
                        Converter.toStr(params.get("m_custcd")), 
                        Converter.toStr(params.get("m_shiptocd")), 
                        Converter.toStr(params.get("r_smhtype")), 
                        email, loginDto.getUserId());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * Maven 의존성 설정 (pom.xml)
 */
/*
<!-- 방법 1: iText 7 + pdfHTML (상용 라이센스 필요) -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>html2pdf</artifactId>
    <version>4.0.5</version>
</dependency>

<!-- 방법 2: Flying Saucer + iText 2.x (오픈소스) -->
<dependency>
    <groupId>org.xhtmlrenderer</groupId>
    <artifactId>flying-saucer-pdf</artifactId>
    <version>9.1.22</version>
</dependency>

<!-- 방법 3: OpenHTMLToPDF (오픈소스, 추천) -->
<dependency>
    <groupId>com.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-pdfbox</artifactId>
    <version>1.0.10</version>
</dependency>
<dependency>
    <groupId>com.openhtmltopdf</groupId>
    <artifactId>openhtmltopdf-slf4j</artifactId>
    <version>1.0.10</version>
</dependency>
*/

/**
 * OpenHTMLToPDF 사용 예제 (오픈소스, 추천)
 */
/*
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

private void convertHtmlToPdfWithOpenHTML(String htmlContent, File pdfFile) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, null);
        builder.toStream(fos);
        builder.run();
    }
}
*/
```

HTML을 PDF로 변환하여 메일로 발송하는 방법을 제시해드렸습니다. 주요 옵션들은 다음과 같습니다:

## 추천 방법들

### 1. **OpenHTMLToPDF** (가장 추천)
- 완전 오픈소스
- 현대적인 CSS 지원
- 한글 폰트 지원 우수
- 메모리 효율적

### 2. **iText 7 + pdfHTML**
- 상용 라이센스 필요 (상업적 사용시)
- 가장 강력하고 안정적
- 우수한 문서화

### 3. **Flying Saucer**
- 오픈소스
- 오래된 라이브러리지만 안정적
- XHTML 형식 필요

## 구현시 고려사항

1. **임시 파일 관리**: PDF 파일 생성 후 반드시 삭제
2. **메모리 사용**: 큰 HTML의 경우 메모리 내 처리 고려
3. **한글 폰트**: 한글이 포함된 경우 폰트 설정 필요
4. **CSS 지원**: 복잡한 CSS는 일부 제한될 수 있음

기존 코드를 최소한으로 수정하면서 PDF 변환 기능을 추가할 수 있도록 구성했습니다. 어떤 방법을 선택하실지, 또는 추가 설명이 필요한 부분이 있으시면 알려주세요!