package com.limenets.common.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

public class FileUtil  {
	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	public String target;
	private String[] allow;
	private String[] extAllow;
	
	public void setAllow(String[] allow) {
		this.allow = allow;
	}
	
	public void setExtAllow(String[] allow) {
		this.extAllow = allow;
	}
	
	// 확장자 체크
	public Boolean extCheck(String fieldName, HttpServletRequest req) {
		MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
		MultipartFile file = mtreq.getFile(fieldName);
		
		String fileName = file.getOriginalFilename();
		String ext = getExt(fileName);
		for (int i=0 ; i<extAllow.length ; i++) {
			if (extAllow[i].equals(ext)) return true;
		}
		return false;
	}
	
	// 확장자 가져오기
	public String getExt(String source) {
		return source.substring(source.lastIndexOf(".")+1, source.length()).toLowerCase();
	}	

	// MimeType 구하기
	public String getMimeType(String fileUrl) {
		File f = new File(fileUrl);
		TikaConfig tika;
		try {
			tika = new TikaConfig();
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, f.toString());
			MediaType mt = tika.getDetector().detect(TikaInputStream.get(f), metadata);
			return mt.toString();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * get mimetype
	 * @param is
	 * @param fileName
	 * @return
	 */
	public String getMimeType(InputStream is, String fileName) {
		TikaConfig tika;
		try {
			tika = new TikaConfig();
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
			MediaType mt = tika.getDetector().detect(TikaInputStream.get(is), metadata);
			return mt.toString();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean mimeCheck(String fieldName, HttpServletRequest req) throws Exception {
		MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
		MultipartFile file = mtreq.getFile(fieldName);
		if (file == null) return true;
		if (file.isEmpty()) return true;
		
		InputStream is = file.getInputStream();
		String fileName = file.getOriginalFilename();
		String mime = this.getMimeType(is, fileName);
		logger.debug("mime : {}", mime);
		
		for (int i=0 ; i<this.allow.length ; i++) {
			if (allow[i].equals(mime)) return true;
		}
		
		return false;
	}
	
	public boolean isEmptyFileCheck(String fieldName, HttpServletRequest req)throws Exception{
		MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
		MultipartFile file = mtreq.getFile(fieldName);
		return file.isEmpty();
	}

	/**
	 * 파일업로드
	 * @param folderName 업로드 폴더명
	 * @param fieldName 인풋 타입 파일의 네임명
	 * @param req
	 * @return 디비에 저장할 파일명
	 * @throws Exception
	 */
	public String upload(String folderName, String fieldName, HttpServletRequest req) throws Exception {
		MultipartHttpServletRequest mtreq = (MultipartHttpServletRequest)req;
		MultipartFile file = mtreq.getFile(fieldName);
		
		if(null == file) return "";
		
		if(!file.isEmpty()) {
			String sepa = System.getProperty("file.separator");
			String filePath = req.getSession().getServletContext().getRealPath("/") + "data" + sepa + "upload" + sepa + folderName;
			logger.debug("filePath : {}", filePath);
			
			File dir = new File(filePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			String saveFileName = file.getOriginalFilename();
			String tempFileName = saveFileName;
			
			File upfile = new File(filePath, saveFileName);
			int idx = 0;

			while (upfile.exists()) {
				 tempFileName = saveFileName.substring(0,saveFileName.lastIndexOf(".")) +idx+saveFileName.substring(saveFileName.lastIndexOf("."));
				 upfile = new File(filePath, tempFileName);
				 idx++;
			}
			
			saveFileName = tempFileName;
			file.transferTo(upfile);
			
			return saveFileName;
		}
		else {
			return "";
		}
	}
	
	public String fileUrlDownload(String fileAddress, String downloadDir) {
		File downDir = new File(downloadDir);
		if(!downDir.exists()) {
			downDir.setReadable(true);
			downDir.setWritable(true);
			downDir.setExecutable(true);
			downDir.mkdirs();
		}
		
		int slashIndex = fileAddress.lastIndexOf('/');
		int periodIndex = fileAddress.lastIndexOf('.');
		
		// 파일 어드레스에서 마지막에 있는 파일이름을 취득
		String fileName = fileAddress.substring(slashIndex + 1);
		String dbFileName = getRealFileName(getUniqueFileName(downloadDir + System.getProperty("file.separator") +  fileName));
		System.out.println("FileUpload > fileUrlDownload > dbFileName : "+ dbFileName);
		
		if (periodIndex >= 1 && slashIndex >= 0 && slashIndex < fileAddress.length() - 1) {
			fileUrlReadAndDownload(fileAddress, dbFileName, downloadDir);
			return dbFileName;
		} 
		else {
			System.err.println("path or file name NG : " + fileAddress);
			return fileName;
		}
	}
	
	// 복사하고자 하는 위치에 파일이 있는지 보고 있다면 .앞에 (1) 이런것을 붙힌다.
	public String getUniqueFileName(String dest) {
		File f = new File(dest);
		Integer i = 1;
		String newFileName = dest;
		while (f.exists()) {
			newFileName = dest.substring(0, dest.lastIndexOf(".")) + "(" + i.toString() + ")." + dest.substring(dest.lastIndexOf(".")+1, dest.length());
			f = new File(newFileName);
			i++;
		}
		this.target = newFileName;
		return newFileName;
	}
	
	// 파일 이름만 가져온다.
	public String getRealFileName(String fullFileName) {
		return fullFileName.substring(fullFileName.lastIndexOf(System.getProperty("file.separator"))+1);
	}
	
	public void fileUrlReadAndDownload(String fileAddress, String localFileName, String downloadDir) {
		OutputStream outStream = null;
		HttpURLConnection uCon = null;
		InputStream is = null;
		
		try {
			URL url;
			byte[] buf;
			int byteRead;
			int byteWritten = 0;
			url = new URL(fileAddress);
			outStream = new BufferedOutputStream(new FileOutputStream(downloadDir + System.getProperty("file.separator") + localFileName));
			uCon = (HttpURLConnection)url.openConnection();
			
			int resCode = uCon.getResponseCode();
			if (resCode != 404) {
				is = uCon.getInputStream();
				
				buf = new byte[1024];
				while ((byteRead = is.read(buf)) != -1) {
					outStream.write(buf, 0, byteRead);
					byteWritten += byteRead;
				}
				
				System.out.println("Download Successfully.");
				System.out.println("File name : " + localFileName);
				System.out.println("of bytes  : " + byteWritten);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			try {
				if (is != null) is.close();
				if (outStream != null) outStream.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
}
