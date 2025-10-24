package com.limenets.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.view.AbstractView;

import com.limenets.common.exception.LimeBizException;
import com.limenets.common.exception.MsgCode;

public class FileCompressDown extends AbstractView {
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
		
	    ZipOutputStream zout = null;
	    
	    Object afObj = model.get("afMap");
		if(!Map.class.isInstance(afObj)) {
			throw new LimeBizException(MsgCode.SYS_ERROR);
		}
		
		Map<?, ?> afMap = (Map<?, ?>) afObj;
        File zipFile = null;
        File filePath = new File(Converter.toStr(afMap.get("FOLDER_NAME")));
        List fileList = convertObjectToList(afMap.get("FILE_LIST"));
        
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
        Date time = new Date();
        String time1 = format1.format(time);
		String zipName = "QMS_" + time1 + ".zip";

		if(fileList.size() > 0) {
		    try {
    	        //tempPath = "/temp/";       //ZIP 압축 파일 저장경로
    
    	        //ZIP파일 압축 START
		        zipFile = new File(filePath, zipName);
    	        zout = new ZipOutputStream(new FileOutputStream(zipFile));
    	        byte[] buffer = new byte[1024];
    	        FileInputStream in = null;
    	        File targetFile = null;
    	        
    	        String fileInfo = "";
    	        String fileType = "";
    	        String realFileName = "";
    	        for ( int k=0; k<fileList.size(); k++) {
    	            fileInfo = fileList.get(k).toString();
    	            fileType = fileInfo.split(";")[1];
    	            realFileName = fileInfo.split(";")[0] + "." + fileType.split("\\.")[1];
    	            targetFile = new File(filePath, fileInfo.split(";")[1]);
        	        in = new FileInputStream(targetFile);      //압축 대상 파일
        	        zout.putNextEntry(new ZipEntry(realFileName));  //압축파일에 저장될 파일명
        
        	        int len;
        	        while((len = in.read(buffer)) > 0){
        	            zout.write(buffer, 0, len);          //읽은 파일을 ZipOutputStream에 Write
        	        }
        
        	        zout.closeEntry();
        	        in.close();
    	        }
    
    	        zout.close();
    	        //ZIP파일 압축 END
    
    	        //파일다운로드 START
    	        res.setContentType("application/zip");
    	        res.addHeader("Content-Disposition", "attachment;filename=" + zipName);
    	        ResourceUtils.doCopy(new FileInputStream(zipFile), new NoCloseOutoutStream(res.getOutputStream()));
    	        FileInputStream fis = new FileInputStream(zipFile);
    	        BufferedInputStream bis = new BufferedInputStream(fis);
    	        ServletOutputStream so = res.getOutputStream();
    	        BufferedOutputStream bos = new BufferedOutputStream(so);
    
    	        int n = 0;
    	        while((n = bis.read(buffer)) > 0){
        	        bos.write(buffer, 0, n);
        	        bos.flush();
    	        }
    
    	        if(bos != null) bos.close();
    	        if(bis != null) bis.close();
    	        if(so != null) so.close();
    	        if(fis != null) fis.close();
    	        //파일다운로드 END
	        }catch(IOException e){
	            //Exception
	            System.out.println("에러발생");
	            System.out.println(e.getMessage());
	        }finally{
    	        if (zout != null){
    	            zout = null;
    	        }
	        }
		}
	}
	
	public static List<?> convertObjectToList(Object obj) {
	    List<?> list = new ArrayList<>();
	    if (obj.getClass().isArray()) {
	        list = Arrays.asList((Object[])obj);
	    } else if (obj instanceof Collection) {
	        list = new ArrayList<>((Collection<?>)obj);
	    }
	    return list;
	}
	
}
