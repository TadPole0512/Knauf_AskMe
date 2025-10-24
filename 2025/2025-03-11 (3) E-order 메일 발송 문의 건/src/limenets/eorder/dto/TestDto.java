package com.limenets.eorder.dto;

import java.io.Serializable;

public class TestDto implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private long TS_0;
	private String TS_1;
	private String TS_2;
	private String TS_3;
	private String TS_INDATE1;
	private String TS_INDATE2;
	
	public TestDto() {}
	
	public long getTS_0(){ return TS_0; }
	public void setTS_0(long ts_0){TS_0 = ts_0;}
	
	public String getTS_1(){ return TS_1; }
	public void setTS_1(String ts_1){TS_1 = ts_1;}
	
	public String getTS_2(){ return TS_2; }
	public void setTS_2(String ts_2){TS_2 = ts_2;}
	
	public String getTS_3(){ return TS_3; }
	public void setTS_3(String ts_3){TS_3 = ts_3;}
	
	public String getTS_INDATE1(){ return TS_INDATE1; }
	public void setTS_INDATE1(String ts_indate1){TS_INDATE1 = ts_indate1;}
	
	public String getTS_INDATE2(){ return TS_INDATE2; }
	public void setTS_INDATE2(String ts_indate2){TS_INDATE2 = ts_indate2;}
	
}
