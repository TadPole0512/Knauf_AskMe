package com.limenets.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUtils {

	private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);
	public static Charset DEFAULT_CHARSET = getDefaultCharset();
	
	private static Charset getDefaultCharset(){
		try{
			String cs = System.getProperty("file.encoding");
			return StringUtils.isBlank(cs) ? Charset.forName(cs) : StandardCharsets.UTF_8;
		}catch(Exception e){
			return StandardCharsets.UTF_8;
		}
	}
	
	public static final int DEFAULT_CAPACITY = 1024 * 4 * 2;
	
	public static ClassLoader getClassLoader(){
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if(classLoader == null){
			classLoader = ClassLoader.getSystemClassLoader();
		}
		if(classLoader == null){
			classLoader = ResourceUtils.class.getClassLoader();
		}
		return classLoader;
	}
	
	public static void closeable(Closeable...closeables){
		if(closeables == null){
			return;
		}
		for(Closeable closeable : closeables){
			try{
				closeable.close();
			}catch(Exception e){
				//ignore
			}
		}
	}
	
	public static void doCopy(File src, File dest) throws FileNotFoundException, IOException{
		doCopy(new FileInputStream(src), new FileOutputStream(dest));
	}
	
	public static void doCopy(InputStream is, OutputStream os) throws IOException{
		ReadableByteChannel rbc = Channels.newChannel(is);
		WritableByteChannel wbc = Channels.newChannel(os);
		
		try{
			doCopy(rbc, wbc);
		}finally{
			closeable(os, is);
		}
		
	}
	
	private static void doCopy(ReadableByteChannel rbc, WritableByteChannel wbc) throws IOException{
		try{
			ByteBuffer byteBuffer = MappedByteBuffer.allocateDirect(DEFAULT_CAPACITY);
			byteBuffer.clear();
			while(rbc.read(byteBuffer) != -1){
				byteBuffer.flip();
				wbc.write(byteBuffer);
				byteBuffer.compact();
			}
			byteBuffer.clear();
		}finally{
			closeable(wbc, rbc);
		}
	}
	
	public static void doWrite(InputStream is, OutputStream os) throws IOException{
		doWrite(new InputStreamReader(is), new OutputStreamWriter(os));
	}
	
	public static void doWrite(InputStream is, OutputStream os, Charset charset) throws IOException{
		if(charset == null){
			doWrite(is, os);
		}else{
			doWrite(new InputStreamReader(is, charset), new OutputStreamWriter(os, charset));
		}
	}
	
	public static void doWrite(Reader reader, Writer writer) throws IOException{
		BufferedReader br = new BufferedReader(reader, DEFAULT_CAPACITY);
		BufferedWriter bw = new BufferedWriter(writer, DEFAULT_CAPACITY);
		
		try{
			CharBuffer targer = CharBuffer.allocate(DEFAULT_CAPACITY);
			for(int len = br.read(targer); len != -1; len = br.read(targer)){
				bw.write(targer.array(), 0, len);
			}
		}finally{
			closeable(bw, br);
		}	
	}
	
	public static String subStringFromByte(String str, int offset, int length){
		return subStringFromByte(str, offset, length, StandardCharsets.UTF_8);
	}
	
	public static String subStringFromByte(String str, int offset, int length, Charset charset){
		return new StringByteLengthDecoder(str, charset).toString(offset, length);
	}
	
	public static String sha256Base64(String str){
		return Base64.encodeBase64String(DigestUtils.sha256(str));
	}
	
	public static String sha256Hex(String str){
		return DigestUtils.sha256Hex(str);
	}
	
	public static void getFileList(List<File> files, File file){
		if(file.isDirectory()){
			for(File f : file.listFiles()){
				getFileList(files, f);
			}
		}else{
			files.add(file);
		}
	}
	
	
	public static MediaType getMediaType(File file) throws Exception{
		return getMimeType(TikaInputStream.get(file), file.getName());
	}
	
	public static MediaType getMediaType(InputStream is, String filename) throws Exception{
		TikaInputStream tis = null;
		try{
			tis = TikaInputStream.get(is);
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, filename);
			MediaType mt = new Tika().getDetector().detect(tis, metadata);
			return mt.getBaseType();
		}finally{
			if(tis != null){
				File tempFile = null;
				try{
					tempFile = tis.getFile();
				}finally{
					try{
						tis.close();
					}catch(IOException e){
						ResourceUtils.tryDeleteFile(tempFile);
					}
				}
			}
		}
	}
	
	public static void tryDeleteFile(File srcFile){
		tryDeleteFile(srcFile, 5000, TimeUnit.MICROSECONDS, MAX_TRY);
	}
	
	public static void tryDeleteFile(File srcFile, int await, TimeUnit timeUnit){
		tryDeleteFile(srcFile, await, timeUnit, MAX_TRY);
	}
	
	public static void tryDeleteFile(File srcFile, int await, TimeUnit timeUnit, int masTry){
		try{
			new ResourceUtils().deleteFile(srcFile, await, timeUnit, masTry);
		}catch(InterruptedException e){
			//ignore
		}
	}
	
	private ReentrantLock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	public static final int MAX_TRY = 5;
	
	private ResourceUtils(){
		super();
	}
	
	private void deleteFile(File srcFile, int await, TimeUnit timeUnit, int maxTry) throws InterruptedException{
		if(srcFile == null){
			return;
		}
		lock.lock();
		long nanosTimeout = timeUnit.toNanos(await);
		long awaitNanos = nanosTimeout;
		
		try{
			for(int i=0; srcFile.exists() && !srcFile.delete(); i++){
				do{
					awaitNanos = condition.awaitNanos(awaitNanos);
				}while(awaitNanos > 0);
				if(i < maxTry){
					awaitNanos = nanosTimeout;
					continue;
				}
				srcFile.deleteOnExit();
			}
		}catch(InterruptedException e){
			srcFile.deleteOnExit();
			logger.error("File delete fail : " + srcFile.getAbsolutePath() + " - deleteOnExit", e);
			throw e;
		}finally{
			lock.unlock();
		}
	}
	
	public static MediaType getMimeType(File file) throws IOException{
		return getMimeType(TikaInputStream.get(file), file.getName());
	}
	
	public static MediaType getMimeType(InputStream is, String fileName) throws IOException {
		TikaInputStream tis = null;
		try{
			tis = TikaInputStream.get(is);
			Metadata metadata = new Metadata();
			metadata.set(Metadata.RESOURCE_NAME_KEY, fileName);
			MediaType mt = new Tika().getDetector().detect(tis, metadata);
			return mt.getBaseType();
		}finally{
			if(tis != null){
				File tempFile = null;
				try{
					tempFile = tis.getFile();
				}finally{
					try{
						tis.close();
					}catch(IOException e){
						ResourceUtils.tryDeleteFile(tempFile);
					}
				}
			}
		}
	}
	
}
