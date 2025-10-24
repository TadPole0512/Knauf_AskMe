package com.limenets.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;

public class FileUpload {
	private static final Logger logger = LoggerFactory.getLogger(FileUpload.class);
	
	private Map<String, List<String>> docFiles;
	private Map<String, List<String>> imageFiles;
	private String fileCheckDiv;
	private String uploadDir;

	public FileUpload() {

	}
	
	public FileUpload(Map<String, List<String>> docFiles, Map<String, List<String>> imageFiles) {
		this.docFiles = docFiles;
		this.imageFiles = imageFiles;
	}
	
	/**
	 * 여러개의 필드명이 존재하고, 필드명에는 파일이 1개만 있는 경우
	 * @param mtreq
	 * @param folderName
	 * @param fieldName
	 * @return
	 * @throws LimeBizException 
	 */
	public List<Map<String, Object>> execManyField(MultipartHttpServletRequest mtreq, String fileCheckDiv, String uploadDir, String... fieldName) throws LimeBizException {
		String acceptHeader = mtreq.getHeader("Accept");
		
		this.fileCheckDiv = fileCheckDiv;
		this.uploadDir = uploadDir;
		List<String> fNms = Arrays.asList(fieldName);
		System.out.println(this.fileCheckDiv);
		System.out.println(this.uploadDir);
		System.out.println(fNms);
		
		for (String fNm : fNms) {
			if (!checkMimeExt(mtreq.getFiles(fNm))) {
				throw new LimeBizException(MsgCode.FILE_DENY_ERROR);
			}
		}
		
		List<Map<String, Object>> result = new ArrayList<>();
		try {
			if (!acceptHeader.contains("application/json")){
				for (String fNm : fNms) {
					for (MultipartFile mt : mtreq.getFiles(fNm)) {
						Map<String, Object> upMap = upload(mt, fNm);
						result.add(upMap);
					}
				}
			}
			else { // Json.
				for (String fNm : fNms) {
					if(mtreq.getFiles(fNm).isEmpty()) {
						result.add(Collections.emptyMap());
					}
					for (MultipartFile mt : mtreq.getFiles(fNm)) {
						Map<String, Object> upMap = upload(mt, fNm);
						result.add(upMap);
					}
				}
			}
			
		} catch(Exception e) {
			if(logger.isErrorEnabled()){
				logger.error("파일업로드시 에러가 발생하였습니다.", e);
			}
			throw new LimeBizException(MsgCode.FILE_UPLOAD_ERROR);
		}
		
		return result;
	}
	
	/**
	 * 하나의 필드명으로 여러개의 파일을 업로드
	 * @param mtreq
	 * @param folderName
	 * @param fieldName
	 * @return
	 * @throws LimeBizException 
	 */
	public List<Map<String, Object>> execOneField(MultipartHttpServletRequest mtreq, String fileCheckDiv, String uploadDir, String fieldName) throws LimeBizException {
		//String acceptHeader = mtreq.getHeader("Accept");
		
		this.fileCheckDiv = fileCheckDiv;
		this.uploadDir = uploadDir;
		
		List<MultipartFile> mpfList = mtreq.getFiles(fieldName);
		for(MultipartFile mf : mpfList) {
			String fileName = mf.getOriginalFilename();
			String ext = parseExtension(fileName).toUpperCase();
			if(!ext.equals(String.valueOf("zip").toUpperCase())) {
				if(!checkMimeExt(mf)){
					throw new LimeBizException(MsgCode.FILE_DENY_ERROR);
				}	
			}
		}
		
		List<Map<String, Object>> result = new ArrayList<>();
		try {
			for (MultipartFile mf : mpfList) {
				result.add(upload(mf, fieldName));
			}
		} catch(Exception e) {
			if(logger.isErrorEnabled()){
				logger.error("파일업로드시 에러가 발생하였습니다.", e);
			}
			throw new LimeBizException(MsgCode.FILE_UPLOAD_ERROR);
		}

		return result;
	}
	
	private Map<String, Object> upload(MultipartFile file, String fieldName) {
		Map<String, Object> resMap = new HashMap<>();
		if (file == null || file.isEmpty()) {
			return Collections.emptyMap();
		}
		
		String saveFileName = file.getOriginalFilename();
		//String saveFileName = HttpUtils.replaceXss(file.getOriginalFilename());
		String mime = getMimeType(file);
		String tempFileName = saveFileName;
		
		// 디렉토리 없다면 생성
		File upDir = new File(this.uploadDir);
		if(!upDir.exists()) {
			upDir.setReadable(true);
			upDir.setWritable(true);
			upDir.setExecutable(true);
			upDir.mkdirs();
		}
		
		File upfile = new File(this.uploadDir, saveFileName);
		int idx = 0;

		while (upfile.exists()) {
			 tempFileName = new StringBuilder(saveFileName.substring(0, saveFileName.lastIndexOf("."))).append(idx).append(saveFileName.substring(saveFileName.lastIndexOf("."))).toString();
			 upfile = new File(this.uploadDir, tempFileName);
			 idx++;
		}
		
		saveFileName = tempFileName;
		
		try {
			file.transferTo(upfile);
		} catch(IllegalStateException | IOException e) {
			if(logger.isErrorEnabled()){
				logger.error(MessageFormat.format("Origin file name : {0}, File extension : {1}, MIME type : {2}, Save file name : {3}.", file.getOriginalFilename(), parseExtension(saveFileName), mime, saveFileName), e);
			}
		}
		
		resMap.put("fieldName", fieldName);
		resMap.put("userFileName", file.getOriginalFilename());
		resMap.put("saveFileName", saveFileName);
		resMap.put("mimeType", mime);
		return resMap;
	}	
	
	private boolean checkMimeExt(List<MultipartFile> mfs) {
		for(MultipartFile mf : mfs){
			if(!checkMimeExt(mf)) {
				return false;
			}
		}
		return true;
	}

	private boolean checkMimeExt(MultipartFile mf) {
		if(mf == null){
			return true;
		}
		
		if(mf.isEmpty()){
			return true;
		}
		
		String fileName = mf.getOriginalFilename();
		String mime = getMimeType(mf);
		String ext = parseExtension(fileName);
		if(logger.isDebugEnabled()){
			logger.debug("mime : {}, ext : {}", mime, ext);
		}
		
		List<String> typs = getAllowMimeTypes(ext);
		
		if (CollectionUtils.isEmpty(typs)) {
			return false;
		}
		return typs.contains(mime);
	}
	
	public String getMimeType(File file){
		try {
			return ResourceUtils.getMimeType(file).toString();
		} catch(Exception e) {
			//ignore
			if(logger.isWarnEnabled()){
				logger.warn("TikaInputStream init error.", e);
			}
			return "";
		}
	}
	
	public String getMimeType(MultipartFile multipartFile) {
		try {
			return getMimeType(multipartFile.getInputStream(), multipartFile.getOriginalFilename());
		} catch (IOException e) {
			//ignore
			if(logger.isWarnEnabled()){
				logger.warn("TikaInputStream init error.", e);
			}
			return "";
		}
	}
	
	public String getMimeType(InputStream is, String fileName) {
		try {
			return ResourceUtils.getMimeType(is, fileName).toString();
		} catch(IOException e) {
			//ignore
			if(logger.isWarnEnabled()){
				logger.warn("TikaInputStream init error.", e);
			}
			return "";
		}
	}
	
	public String parseExtension(String source){
		if(StringUtils.isBlank(source)) {
			return "";
		}
		
		int dotIdx = source.lastIndexOf(".");
		if (dotIdx == -1) {
			return source;
		}
		
		return source.substring(dotIdx + 1, source.length()).toLowerCase();
	}	
	
	public List<String> getAllowMimeTypes(String extension) {
		switch (this.fileCheckDiv) {
			case "docFiles":
				if (docFiles.get(extension) == null) {
					return Collections.emptyList();
				} else {
					return new CopyOnWriteArrayList<>(docFiles.get(extension));
				}
			case "imageFiles":
				if (imageFiles.get(extension) == null) {
					return Collections.emptyList();
				} else {
					return new CopyOnWriteArrayList<>(imageFiles.get(extension));
				}
			default:
				if (docFiles.get(extension) == null) {
					return Collections.emptyList();
				} else {
					return new CopyOnWriteArrayList<>(docFiles.get(extension));
				}
		}
	}

	public void deleteList(List<Map<String, Object>> fileList, String uploadDir) {
		if (fileList != null) {
			for(Map<String, Object> map : fileList) {
				deleteFile(new File(uploadDir, Converter.toStr(map.get("saveFileName"))));
			}
		}
	}
	
	public Boolean deleteFile(String uploadDir, String srcFile) {
		return deleteFile(new File(uploadDir, srcFile));
	}
	
	public Boolean deleteFile(File srcFile) {
		if(!srcFile.exists()) {
			return false;
		}
		
		if(srcFile.delete()) {
			return true;
		} else {
			ResourceUtils.tryDeleteFile(srcFile);
			return false;
		}
	}
	
	public Map<String, List<String>> getDocFiles() {
		return docFiles;
	}

	public void setDocFiles(Map<String, List<String>> docFiles) {
		this.docFiles = docFiles;
	}

	public Map<String, List<String>> getImageFiles() {
		return imageFiles;
	}

	public void setImageFiles(Map<String, List<String>> imageFiles) {
		this.imageFiles = imageFiles;
	}
	
	/**
	 * 여러개의 필드명이 존재하고, 필드명에는 파일이 1개만 있는 경우 - 환경설정 이미지 업로드
	 * @param mtreq
	 * @param folderName
	 * @param fieldName
	 * @return
	 * @throws LimeBizException 
	 */
	public List<Map<String, Object>> execManyFieldConfig(MultipartHttpServletRequest mtreq, String fileCheckDiv, String uploadDir, String... fieldName) throws LimeBizException {
		this.fileCheckDiv = fileCheckDiv;
		this.uploadDir = uploadDir;
		List<String> fNms = Arrays.asList(fieldName);
		
		for (String fNm : fNms) {
			if (!checkMimeExt(mtreq.getFiles(fNm))) {
				throw new LimeBizException(MsgCode.FILE_DENY_ERROR);
			}
		}
		
		List<Map<String, Object>> result = new ArrayList<>();
		try {
			for (String fNm : fNms) {
				for (MultipartFile mt : mtreq.getFiles(fNm)) {
					result.add(uploadConfig(mt, fNm));
				}
			}
		} catch(Exception e) {
			if(logger.isErrorEnabled()){
				logger.error("파일업로드시 에러가 발생하였습니다.", e);
			}
			throw new LimeBizException(MsgCode.FILE_UPLOAD_ERROR);
		}
		
		return result;
	}
	
	/**
	 * 환경설정 업로드
	 */
	private Map<String, Object> uploadConfig(MultipartFile file, String fieldName) {
		if (file == null || file.isEmpty()) {
			return Collections.emptyMap();
		}
		
		String saveFileName = file.getOriginalFilename();
		String mime = getMimeType(file);
		
		// 디렉토리 없다면 생성
		File upDir = new File(this.uploadDir);
		if(!upDir.exists()) {
			upDir.setReadable(true);
			upDir.setWritable(true);
			upDir.setExecutable(true);
			upDir.mkdirs();
		}
		
		//환경설정 이미지명 변경
		if(StringUtils.equals("m_systemlogo", fieldName)) {
			saveFileName = "logo.png"; // 로고
			
		}else if(StringUtils.equals("m_ceoseal", fieldName)) {
			saveFileName = "ceoseal.png"; // 대표자 직인 이미지
			
		}else if(StringUtils.equals("m_faviconimg", fieldName)) {
			saveFileName = "favicon.ico"; // 파비콘
			
		}else if(StringUtils.equals("m_bannerimg", fieldName)) {
			saveFileName = "left-img.jpg"; // 화면메뉴상단배너이미지
		}
		
		File upfile = new File(this.uploadDir, saveFileName);

		//기존파일 삭제
		while (upfile.exists()) {
			deleteFile(this.uploadDir, saveFileName);
		}
		
		try {
			file.transferTo(upfile);
		} catch(IllegalStateException | IOException e) {
			if(logger.isErrorEnabled()){
				logger.error(MessageFormat.format("Origin file name : {0}, File extension : {1}, MIME type : {2}, Save file name : {3}.", file.getOriginalFilename(), parseExtension(saveFileName), mime, saveFileName), e);
			}
		}
		
		Map<String, Object> resMap = new HashMap<>();
		resMap.put("fieldName", fieldName);
		resMap.put("userFileName", file.getOriginalFilename());
		resMap.put("saveFileName", saveFileName);
		resMap.put("mimeType", mime);
		return resMap;
	}
	
}
