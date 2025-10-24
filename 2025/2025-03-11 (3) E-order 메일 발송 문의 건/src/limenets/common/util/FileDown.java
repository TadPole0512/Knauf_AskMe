package com.limenets.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.view.AbstractView;

import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;

public class FileDown extends AbstractView {
	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	public static final String FILE_SEPARATOR = System.getProperty("file.separator", "/");
	
	@Override
	protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("Pragma", "private");
		response.setHeader("Cache-Control", "private, must-revalidate");
		response.setHeader("Content-Transfer-Encoding", "binary");
		response.setHeader("Accept-Ranges", "bytes");
	}
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest req, HttpServletResponse res) throws Exception {
		Object afObj = model.get("afMap");
		if(!Map.class.isInstance(afObj)) {
			throw new LimeBizException(MsgCode.SYS_ERROR);
		}
		
		Map<?, ?> afMap = (Map<?, ?>) afObj;
		
		String fileType = Converter.toStr(afMap.get("FILE_TYPE"));
		
		try {
			res.setContentType(MediaType.parseMediaType(fileType).toString());
		} catch(Exception e) {
			res.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		}
		
		try {
			File filePath = new File(Converter.toStr(afMap.get("FOLDER_NAME")));
			File targetFile = new File(filePath, Converter.toStr(afMap.get("FILE_NAME")));
			String fileName = HttpUtils.uriEncoding(Converter.toStr(afMap.get("FILE_NAME")), DEFAULT_CHARSET);
			String aliasFileName = "";
			if(afMap.get("REAL_FILE_NAME") != null) {
			    aliasFileName = HttpUtils.uriEncoding(Converter.toStr(afMap.get("REAL_FILE_NAME")), DEFAULT_CHARSET);
			    fileName = aliasFileName;
			}
			//System.out.println("FileDown > fileName : " + fileName);
			
			long fileLength = targetFile.length();
			if (fileLength <= Integer.MAX_VALUE) {
				res.setContentLength((int) fileLength);
			}
			res.setHeader("Content-Disposition", new StringBuilder().append("attachment; filename=\"").append(fileName).append("\";").toString());
			
			ResourceUtils.doCopy(new FileInputStream(targetFile), new NoCloseOutoutStream(res.getOutputStream()));
		} catch (FileNotFoundException e) {
			throw new LimeBizException(MsgCode.FILE_NOT_FOUND_ERROR);
		}
		
	}
}
