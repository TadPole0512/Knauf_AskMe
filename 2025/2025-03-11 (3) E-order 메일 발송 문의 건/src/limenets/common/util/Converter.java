package com.limenets.common.util;

import java.io.StringWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author LG
 *
 */
public class Converter {
	/**
	 * "" 값을 null 로
	 * @param obj
	 * @return
	 */
	public static String blankToNull(Object obj) {
		String str = toStr(obj);
		return "".equals(str) ? null : str;
	}
	
	/**
	 * toStr
	 */
	public static String toStr(Object obj) {
		return toStr(obj, "");
	}
	public static String toStr(Object obj, String defaultValue) {
		if (obj == null) return defaultValue;
		if ("".equals(obj.toString())) return defaultValue;
		return obj.toString();
	}
	
	/**
	 * toInt
	 */
	public static int toInt(Object obj, int defaultValue) {
		if (obj != null) {
			if ("".equals(obj.toString())) return defaultValue;
			else return Integer.parseInt(obj.toString());
		}
		else {
			return defaultValue;
		}
	}
	public static int toInt(Object obj) {
		return toInt(obj, 0);
	}
	
	/**
	 * toLong
	 */
	public static Long toLong(Object obj) {
		return toLong(obj, 0l);
	}
	public static Long toLong(Object obj, long defaultValue) {
		if (obj == null || "".equals(obj.toString())) return defaultValue;
		return Math.round(Double.parseDouble(obj.toString()));
	}
	
	/**
	 * toDouble
	 */
	public static double toDouble(Object obj) {
		return toDouble(obj, 0d);
	}
	public static double toDouble(Object obj, double defaultValue) {
		String tmp = toStr(obj).replaceAll(",", "");
		if ("".equals(tmp)) return defaultValue;
		else return Double.parseDouble(tmp);
	}
	
	/**
	 * decimalFormat
	 */
	public static String decimalFormat(Object inVal, String pattern) {
		DecimalFormat form = new DecimalFormat(pattern);
		return form.format(toDouble(inVal));
	}
	
	/**
	 * addComma
	 */
	public static String addComma(Object inVal) {
		return decimalFormat(inVal, "#,###");
	}
	
	/**
	 * toDate
	 */
	public static Date toDate(String pattern, Object obj) throws Exception {
		String tmp = toStr(obj);
		Date res = FastDateFormat.getInstance(pattern, Locale.KOREA).parse(tmp);
		return res;
	}
	
	/**
	 * dateToStr
	 */
	public static String dateToStr(String pattern, Date date) {
		return FastDateFormat.getInstance(pattern, Locale.KOREA).format(date);
	}
	public static String dateToStr(String pattern) {
		return dateToStr(pattern, new Date());
	}
	
	/**
	 * dateAdd > 날짜더하기
	 * @param date 기준날짜
	 * @param div 구분 (YEAR:1, MONTH:2, DATE:5, DAY_OF_WEEK:7)
	 * @param days 더하는값
	 * @return
	 */
	public static Date dateAdd(Date date, int div, int days) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.add(div, days);
	    return cal.getTime();
	}
	
	/**
	 * inList
	 */
	public static Boolean inList(List<Map<String, Object>> list, String fieldName, Object val) {
		if (!CollectionUtils.isEmpty(list)) {
			for (Map<String, Object> map : list) {
				if (StringUtils.equals(toStr(map.get(fieldName)), toStr(val))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * isEmpty
	 */
	public static boolean isEmpty(Object obj) {
		if( obj instanceof String ) {
			return obj == null || "".equals(obj.toString().trim());
		} else if( obj instanceof List ) {
			return obj == null || ((List)obj).isEmpty();
		} else if( obj instanceof Map ) {
			return obj == null || ((Map)obj).isEmpty();
		} else if( obj instanceof Object[] ) {
			return obj == null || Array.getLength(obj) == 0;
		} else {
			return obj == null;
		}
	}
	
	/**
	 * isNotEmpty
	 */
	public static boolean isNotEmpty(Object obj) {
		return !isEmpty(obj);
	}
	
	/**
	 * inArray > arr에 val 이 있는지 체크.
	 * @param arr
	 * @param val
	 * @return
	 */
	public static Boolean inArray(String[] arr, Object val) {
		if (arr != null) {
			for (String str : arr) {
				if (str.equals(Converter.toStr(val))) {
					return true;
				}
			}
		}
		return false;
	}
	public static Boolean inArray(int[] arr, Object val) {
		if (arr != null) {
			for (int i : arr) {
				if (i == Converter.toInt(val)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String zeroPlus(Object obj, int len) {
		return zeroPlus(obj, len, "0");
	}
	
	/**
	 * zeroPlus > obj 앞에 len 기준으로 미달되는 길이는 앞에다가 div를 붙혀준다.
	 * @param obj
	 * @param len
	 * @param div
	 * @return
	 */
	public static String zeroPlus(Object obj, Integer len, String div) {
		String val = toStr(obj);
		String res;
		if (val.length() < len) {
			res = val;
			for (int k=len; k>val.length(); k--) {
				res = div + res;
			}
			return res;
		}
		else return val;
	}
	
	/**
	 * dateDiff > 날짜 차이 계산
	 * @param sdate	yyyyMMdd
	 * @param edate	yyyyMMdd
	 * @param div	M:월
	 * @return
	 */
	public static int dateDiff(String sdate, String edate, String div) {
		int res = 0;
		
		if ("M".equals(div)) {
			int strtYear = toInt(sdate.substring(0,4));
			int strtMonth = toInt(sdate.substring(4,6));

			int endYear = toInt(edate.substring(0,4));
			int endMonth = toInt(edate.substring(4,6));

			res = ((endYear - strtYear) * 12) + (endMonth - strtMonth);
		}
		
		return res;
	}
	
	/**
	 * toJson
	 */
	public static String toJson(Map<?, ?> value) {
		ObjectMapper objectMapper = new ObjectMapper();
		StringWriter writer = new StringWriter();
		try{
			objectMapper.writeValue(writer, value);
			return writer.toString();
		}catch(Exception e){
			return "{}";
		}finally {
			ResourceUtils.closeable(writer);
		}
	}
	
	/**
	 * listToStr
	 */
	public static String listToStr(List<Map<String, Object>> list, String fieldName, String division) {
		String res = "";
		if (!CollectionUtils.isEmpty(list)) {
			for (Map<String, Object> map : list) {
				if (StringUtils.equals(res, "")) {
					res = Converter.toStr(map.get(fieldName));
				} else {
					res += division + Converter.toStr(map.get(fieldName));
				}
			}
		}
		return res;
	}
	
	public static String toSubString(Object obj, int start, int end) {
		String res = toStr(obj);
		if(StringUtils.equals("", res)) return "";
		if(start > res.length()) return "";
		if(end > res.length()) return res.substring(start, res.length());
		return res.substring(start, end);
	}
	
	/**
	 * decimalScale
	 * @param obj : 부동소수
	 * @param loc : 자리수 제한위치. ex)loc=2이면 소수점 2자리까지.
	 * @param mode : 1=내림,2=반올림,3=올림. ex)loc=2이고 mode=2이면 소수점 3번째자리에서 반올림.
	 * @return Double
	 */
	public static double decimalScale(Object obj , int loc , int mode) {
		BigDecimal bd = new BigDecimal(toDouble(obj));
		BigDecimal result = null;
		
		if(mode == 1) {
			result = bd.setScale(loc, BigDecimal.ROUND_DOWN); //내림
		} else if(mode == 2) {
			result = bd.setScale(loc, BigDecimal.ROUND_HALF_UP); //반올림
		} else if(mode == 3) {
			result = bd.setScale(loc, BigDecimal.ROUND_UP); //올림
		} else if (mode == 0) {
			result = bd;
		}
		
		return result.doubleValue();
	}
	public static double decimalScale(double decimal , int loc , int mode) {
		BigDecimal bd = new BigDecimal(decimal);
		BigDecimal result = null;
		
		if(mode == 1) {
			result = bd.setScale(loc, BigDecimal.ROUND_DOWN); //내림
		} else if(mode == 2) {
			result = bd.setScale(loc, BigDecimal.ROUND_HALF_UP); //반올림
		} else if(mode == 3) {
			result = bd.setScale(loc, BigDecimal.ROUND_UP); //올림
		} else if (mode == 0) {
			result = bd;
		}
		
		return result.doubleValue();
	}
	public static double decimalScale(Object obj , Integer loc , Integer mode) {
		return decimalScale(obj, toInt(loc), toInt(mode));
	}
	
	public static double decimalScale(Object obj) {
		return decimalScale(obj, 0, 2);
	}	
	public static double decimalScale2(Object obj) {
		return decimalScale(obj, 2, 2);
	}

	/**
	 * decimalScaleToStr > JSP에서 정확한 수를 표현하기 위해 사용.
	 * @param val
	 * @param loc
	 * @param mode
	 * @return String
	 */
	public static String decimalScaleToStr(double val , int loc , int mode) {
		BigDecimal bd = BigDecimal.valueOf(val);
		//BigDecimal bd = new BigDecimal(val);
		BigDecimal result = null;
		
		if(mode == 1) {
			result = bd.setScale(loc, BigDecimal.ROUND_DOWN); //내림
		} else if(mode == 2) {
			result = bd.setScale(loc, BigDecimal.ROUND_HALF_UP); //반올림
		} else if(mode == 3) {
			result = bd.setScale(loc, BigDecimal.ROUND_UP); //올림
		} else if (mode == 0) {
			result = bd;
		}
	      
		String pattern = "#,###.";
		for (int i=0; i<loc; i++) {
			pattern += "#";
		}
		
		DecimalFormat df = new DecimalFormat(pattern);
		return df.format(result);
	}
	public static String decimalScaleToStr(Object obj , Integer loc , Integer mode) {
		return decimalScaleToStr(toDouble(obj), toInt(loc), toInt(mode));
	}
	
	/**
	 * getSupPrice > 총합계액으로부터 공급가액/vat 구하기.
	 * @param sumPrice : 총합계액
	 * @param priceType : 0=영세,1=면세,2=과세
	 * @return Array[0]=공급가격, Array[1]=부가세
	 */
	public static double [] getSupPrice(double sumPrice, String priceType) {
		double [] retArr = new double[2];
		double supPrice = (!StringUtils.equals("2", priceType)) ? decimalScale(sumPrice, 4, 1) : decimalScale(sumPrice * 10 / 11, 4, 1);
		double vat = decimalScale(sumPrice - supPrice, 4, 1);
		
		retArr[0] = supPrice;
		retArr[1] = vat;
		
		return retArr;
	}
	public static String [] getSupPrice(Object obj, String priceType) {
		String [] retArr = new String[2];
		double sumPrice = toDouble(obj);
		double supPrice = (!StringUtils.equals("2", priceType)) ? decimalScale(sumPrice, 4, 1) : decimalScale(sumPrice * 10 / 11, 4, 1);
		double vat = decimalScale(sumPrice - supPrice, 4, 1);
		
		retArr[0] = decimalScaleToStr(supPrice, 4, 1);
		retArr[1] = decimalScaleToStr(vat, 4, 1);
		
		return retArr;
	}

	/**
	 * html 태그 제거.
	 */
	public static String removeTag(String str) {
		return HttpUtils.restoreXss(str).replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
	}
	
	public static String getSapNumber(String type, Object obj) {
		if(Objects.isNull(obj)) {
			return null;
		}
		String val = obj.toString();
		
		/*char c = val.charAt(val.length()-1);
		
		if(c=='-') {
			val="-" + val.substring(0, val.length()-1);
		}

		if( type.equals("CR") || type.equals("RE") || type.equals("ZRE") ) {
			c = val.charAt(0);
			if(c != '-') {
				val="-" + val;
			}
		}*/
		
		return val;
	}
	
	public static String getSapQty(String type, Object obj) {
		if(Objects.isNull(obj)) {
			return null;
		}

		String val = obj.toString().trim();
		/*if( type.equals("CR") || type.equals("RE") || type.equals("ZRE") ) {
			char c = val.charAt(0);
			if(c != '-') {
				val="-" + val;
			}
		}*/
		
		return val;
	}
	
	public static Map<String, Object> getQuaterDate(Calendar cal) {
		Map<String, Object> res = new HashMap<String, Object>();
		
		Integer year = cal.get(Calendar.YEAR);
		Integer month = cal.get(Calendar.MONTH) + 1;
		Integer quater = (int)Math.ceil(month / 3.0);
		
		Calendar calStart = Calendar.getInstance();
		Calendar calFinish = Calendar.getInstance();
		SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd");
		switch(quater) {
			case 1: {
				calStart.set(Calendar.MONTH, 0);
				calStart.set(Calendar.DATE, 1);
				calFinish.set(Calendar.MONTH, 2);
				calFinish.set(Calendar.DATE, calFinish.getActualMaximum(Calendar.DAY_OF_MONTH));
				break;
			}
			
			case 2: {
				calStart.set(Calendar.MONTH, 3);
				calStart.set(Calendar.DATE, 1);
				calFinish.set(Calendar.MONTH, 5);
				calFinish.set(Calendar.DATE, calFinish.getActualMaximum(Calendar.DAY_OF_MONTH));
				break;
			}
			
			case 3: {
				calStart.set(Calendar.MONTH, 6);
				calStart.set(Calendar.DATE, 1);
				calFinish.set(Calendar.MONTH, 8);
				calFinish.set(Calendar.DATE, calFinish.getActualMaximum(Calendar.DAY_OF_MONTH));
				break;
			}
			
			case 4: {
				calStart.set(Calendar.MONTH, 9);
				calStart.set(Calendar.DATE, 1);
				calFinish.set(Calendar.MONTH, 11);
				calFinish.set(Calendar.DATE, calFinish.getActualMaximum(Calendar.DAY_OF_MONTH));
				break;
			}
		}

		res.put("ordersdt", dtf.format(calStart.getTime()));
		res.put("orderedt", dtf.format(calFinish.getTime()));
		res.put("preYear", String.valueOf(cal.get(Calendar.YEAR))+"년");
		res.put("preQuat", String.valueOf(quater)+"분기");
		
		return res;
	}
}
