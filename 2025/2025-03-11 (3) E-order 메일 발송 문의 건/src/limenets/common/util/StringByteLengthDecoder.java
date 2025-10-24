package com.limenets.common.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringByteLengthDecoder {

	private static final Logger logger = LoggerFactory.getLogger(StringByteLengthDecoder.class);
	
	private Charset charset;
	private CharsetDecoder decoder;
	private String str;
	private byte[] strBytes;
	
	public StringByteLengthDecoder(String str){
		this(str, StandardCharsets.UTF_8);
	}

	public StringByteLengthDecoder(String str, Charset charset) {
		super();
		this.charset = charset;
		CharsetDecoder decoder = this.charset.newDecoder();
		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		this.decoder = this.charset.newDecoder();
		this.str = str;
		this.strBytes = str.getBytes(charset);
	}
	
	public String toString(int offset){
		return toString(offset, strBytes.length);
	}
	
	public String toString(int offset, int length){
		ByteBuffer buff = ByteBuffer.wrap(this.strBytes, offset, length);
		try{
			CharBuffer charBuffer = decoder.decode(buff);
			return charBuffer.toString();
		}catch(CharacterCodingException e){
			String msg = MessageFormat.format("Origin String : {0}, Charset : {1}, Offset : {2}, Length : {3}", str, charset.displayName(), offset, length);
			if(logger.isErrorEnabled()){
				logger.error(msg, e);
			}
			throw new IllegalStateException(msg, e);
		}
	}

	@Override
	public String toString(){
		return str;
	}
	
}
