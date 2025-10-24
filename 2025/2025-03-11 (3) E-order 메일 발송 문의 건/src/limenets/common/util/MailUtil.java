package com.limenets.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.limenets.common.dto.LoginDto;

public class MailUtil {
	public MailUtil() { super(); }

	private transient String mailId;
	private transient String mailPw;
	
	private static final Logger logger = LoggerFactory.getLogger(MailUtil.class);
	
	public boolean sendMail(final String smtpHost, final String title, final String toName, final String toEmail, final String fromName, final String fromEmail
		, final String contentStr, final File file, final String filename) throws AddressException, MessagingException, IOException, com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException {
		
		final Properties properties = System.getProperties();
		properties.put("mail.smtp.host", smtpHost);
	    logger.debug("*****************************************메일 전송 시작.******************************* ");

		final Session sessions = Session.getInstance(properties, null);
		final MimeMessage message = new MimeMessage(sessions); 
		
		final Address[] toAddresses = InternetAddress.parse(toEmail);
		message.setRecipients(Message.RecipientType.TO, toAddresses);
		
		final InternetAddress from = new InternetAddress(fromEmail, fromName, "UTF-8");
		message.setFrom(from);
		message.setSubject(title,"UTF-8");
		message.setText(contentStr,"UTF-8");
		message.setSentDate(new java.util.Date());
		message.setHeader("Content-type", "text/html; charset=UTF-8");
		
		//파일 첨부
		if(file != null) {
			MimeBodyPart attachPart = new MimeBodyPart();
			attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
			attachPart.setFileName(MimeUtility.encodeText(filename, "EUC-KR", "B"));
//			System.out.println(MimeUtility.encodeText(filename, "EUC-KR", "B"));
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(attachPart);
			message.setContent(multipart);
		}
		try {
			Transport.send(message);
			logger.debug("SMTP : SUCESS");
		}catch(Exception e) {
			logger.debug("SMTP : FAIL");
		}
		return true;
	}
	
	public boolean sendMail(final String smtpHost, final String title, final String toName, final String toEmail, final String fromName, final String fromEmail
			, final String contentPage, final String[] repStr1, final String[] repStr2, final String userId, final String userPw) throws Exception, AddressException, MessagingException {
			
		final Properties properties = System.getProperties();
		properties.put("mail.smtp.host", smtpHost);
		
		mailId = userId;
		mailPw = userPw;
        logger.debug("*****************************************메일 전송중******************************* ");

		final Authenticator auth = new SMTPAuthenticator();
		final Session sessions = Session.getDefaultInstance(properties, auth);
		final MimeMessage message = new MimeMessage(sessions); 
		
		String content = fileGetContents(contentPage);
		
		if (repStr1 != null) {
			for (int i=0; i<repStr1.length; i++) {
				content = content.replaceAll(repStr1[i], repStr2[i]);
			}
		}
		
		final Address[] toAddresses = InternetAddress.parse(toEmail);
		message.setRecipients(Message.RecipientType.TO, toAddresses);
		
		String sender = "";
		if (!"".equals(fromName)) {
			sender = fromName + "<" + fromEmail + ">";
		}
		else {
			sender = fromEmail;
		}
		
		final InternetAddress from = new InternetAddress(sender);
		message.setFrom(from);
		message.setSubject(title,"UTF-8");
		message.setContent(content,"UTF-8");
		message.setSentDate(new java.util.Date());
		message.setHeader("Content-type", "text/html; charset=UTF-8");
		//Transport.send(message);
		try {
			Transport.send(message);
			logger.debug("SMTP : SUCESS");
		}catch(Exception e) {
			logger.debug("SMTP : FAIL");
		}
		return true;
	}
	
	private class SMTPAuthenticator extends Authenticator {
		public SMTPAuthenticator() { super(); }

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(mailId, mailPw);
		}
	}

	public String fileGetContents(final String contentPage) throws IOException {
		String content = "";
		
		try (BufferedReader br = Files.newBufferedReader(Paths.get(contentPage), StandardCharsets.UTF_8)) {
			String line = null;
			while ((line = br.readLine()) != null) { //라인단위 읽기
				content += line + System.getProperty("line.separator"); 
			}
		}
		
		return content;
	}
	
	
	public boolean sendGMail(final String title, final String toName, final String toEmail, final String fromName, final String fromEmail
        , final String contentStr, final File file, final String filename) throws AddressException, MessagingException, IOException, com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException {
        
        final Properties properties = System.getProperties();
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "587");
        logger.debug("&&&&&&&&&&&&&&&&&&&&&&&&&&  GGGG메일 BEGIN &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

        mailId = "sorinhyunseok@gmail.com";
        mailPw = "bbydwdecfpqewfly";
        
        final Authenticator auth = new SMTPAuthenticator();
        final Session sessions = Session.getInstance(properties, auth);
        final MimeMessage message = new MimeMessage(sessions); 
        
        final Address[] toAddresses = InternetAddress.parse(toEmail);
        message.setRecipients(Message.RecipientType.TO, toAddresses);
        
        final InternetAddress from = new InternetAddress(fromEmail, fromName, "UTF-8");
        message.setFrom(from);
        message.setSubject(title,"UTF-8");
        message.setText(contentStr,"UTF-8");
        message.setSentDate(new java.util.Date());
        message.setHeader("Content-type", "text/html; charset=UTF-8");
        
        //파일 첨부
        if(file != null) {
            MimeBodyPart attachPart = new MimeBodyPart();
            attachPart.setDataHandler(new DataHandler(new FileDataSource(file)));
            attachPart.setFileName(MimeUtility.encodeText(filename));
            
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(attachPart);
            message.setContent(multipart);
        }
        
        //Transport.send(message);
        try {
			Transport.send(message);
			logger.debug("SMTP : SUCESS");
		}catch(Exception e) {
			logger.debug("SMTP : FAIL");
		}
        logger.debug("&&&&&&&&&&&&&&&&&&&&&&&&&&  GGGG메일END &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        return true;
    }
}
