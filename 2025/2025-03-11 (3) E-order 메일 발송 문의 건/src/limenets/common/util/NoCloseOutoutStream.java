package com.limenets.common.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NoCloseOutoutStream  extends FilterOutputStream {

	protected NoCloseOutoutStream(OutputStream out){
		super(out);
	}
	
	@Override
	public void close() throws IOException{
		super.out.flush();
	}
}
