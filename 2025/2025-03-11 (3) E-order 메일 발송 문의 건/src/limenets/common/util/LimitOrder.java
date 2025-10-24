package com.limenets.common.util;

import java.time.LocalDate;
import java.time.LocalTime;

public class LimitOrder {

	public static boolean checkLimitOrderDate(String operationType) {		
		if(!operationType.toUpperCase().equals("REAL"))
			return true;
		
		boolean chkD = checkCurrentDate();
		boolean chkT = checkCurrentTime();
		if( !(chkD && chkT) )
			return false;
		
		return true;
	}
	
	private static boolean checkCurrentDate() {
		LocalDate curDate = LocalDate.now();
		LocalDate stDate = LocalDate.of(2024, 01, 01);
		LocalDate fnDate = LocalDate.of(2024, 06, 30);
		
		if(curDate.isAfter(stDate) && curDate.isBefore(fnDate))
			return true;
		
		return false;
	}
	
	private static boolean checkCurrentTime() {
		LocalTime curTime = LocalTime.now();
		LocalTime stTime = LocalTime.of(07, 00);
		LocalTime fnTime = LocalTime.of(19, 00);
		
		if(curTime.isAfter(stTime) && curTime.isBefore(fnTime))
			return true;
		
		return false;
	}
}
