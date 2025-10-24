package com.limenets.eorder.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.limenets.common.util.Converter;

public class StatusUtil {
	public static final StatusUtil ORDER = new StatusUtil("od"); // 주문.
	public static final StatusUtil SALESORDER = new StatusUtil("so"); // 출고.
	public static final StatusUtil QMS = new StatusUtil("qms"); // qms.
	
	public final String div;
	
	public StatusUtil(String div) {
		this.div = div;
	}
	
	/**
	 * 키를 가지고 값을 가져온다.
	 * @param key
	 * @return
	 */
	public String getValue(String key) {
		return getMap().get(key);
	}

	/**
	 * 값을 가지고 키를 가져온다.
	 * @param value
	 * @return
	 */
	public String getKey(String value) {
		Map<String, String> reverseMap = getReverseMap();
		return reverseMap.get(value);
	}
	
	/**
	 * 루프문을 돌리기 위한 List 구함.
	 * @return
	 */
	public List<Map<String, String>> getList() {
		List<Map<String, String>> resList = new ArrayList<>();
		Map<String, String> statMap = getMap();
		
		for (Entry<String, String> elem : statMap.entrySet()) {
			Map<String, String> map = new HashMap<>();
			map.put("key", elem.getKey());
			map.put("value", elem.getValue());
			resList.add(map);
		}
		
		Collections.sort(resList, new Comparator<Map<String,String>>(){
			public int compare(Map<String,String> map1, Map<String,String> map2){
				return map1.get("key").compareTo(map2.get("key")); // String 오름차순
				//return map2.get("key").compareTo(map1.get("key")); // String 내림차순
			}
		});
		
		return resList;
	}
	public List<String> getKeyList() {
		List<String> resList = new ArrayList<>();
		Map<String, String> statMap = getMap();
		
		for (Entry<String, String> elem : statMap.entrySet()) {
			resList.add(elem.getKey());
		}
		
		return resList;
	}
	
	/**
	 * 루프문을 돌리기 위한 List 구함.
	 * @작성일 : 2020. 4. 8.
	 * @작성자 : kkyu
	 * @param String[] remove_elem : 선언한 배열 요소 제외하고 리스트 불러오기.
	 */
	public List<Map<String, String>> getList(String[] remove_elem) {
		List<Map<String, String>> resList = new ArrayList<>();
		Map<String, String> statMap = getMap();
		
		if(ArrayUtils.isEmpty(remove_elem)) this.getList();
		
		for (Entry<String, String> elem : statMap.entrySet()) {
			Map<String, String> map = new HashMap<>();
			for(String removeEl : remove_elem) {
				if(!StringUtils.equals(elem.getKey(), removeEl)) {
					map.put("key", elem.getKey());
					map.put("value", elem.getValue());
					resList.add(map);
				}
			}
		}
		
		Collections.sort(resList, new Comparator<Map<String,String>>(){
			public int compare(Map<String,String> map1, Map<String,String> map2){
				return map1.get("key").compareTo(map2.get("key")); // String 오름차순
				//return map2.get("key").compareTo(map1.get("key")); // String 내림차순
			}
		});
		
		return resList;
	}
	public List<String> getKeyList(String[] remove_elem) {
		List<String> resList = new ArrayList<>();
		Map<String, String> statMap = getMap();
		
		if(ArrayUtils.isEmpty(remove_elem)) this.getList();
		
		for (Entry<String, String> elem : statMap.entrySet()) {
			for(String removeEl : remove_elem) {
				if(!StringUtils.equals(elem.getKey(), removeEl)) {
					resList.add(elem.getKey());
				}
			}
		}
		
		return resList;
	}
	
	public List<Map<String, String>> getReverseList() {
		List<Map<String, String>> resList = new ArrayList<>();
		Map<String, String> statMap = getReverseMap();
		
		for (Entry<String, String> elem : statMap.entrySet()) {
			Map<String, String> map = new HashMap<>();
			map.put("key", elem.getKey());
			map.put("value", elem.getValue());
			resList.add(map);
		}
		
		return resList;
	}	
	
	/**
	 * 키와 값을 변경.
	 * @return
	 */
	public Map<String, String> getReverseMap() {
		Map<String, String> resMap = new HashMap<>();
		Map<String, String> statMap = getMap();
		
		for (Entry<String, String> elem : statMap.entrySet()) {
			resMap.put(elem.getValue(), elem.getKey());
		}
		
		return resMap;
	}
	
	/**
	 * 이전상태값과 변경할려는 상태값을 넣어서 가능한 상태값인지 체크한다.
	 * @param prevStat 이전상태값
	 * @param nextStat 변경할려는 상태값
	 * @return 가능여부
	 */
	public boolean statusCheck(String prevStat, String nextStat) {
		boolean isExist = false;
		boolean isSame = false;
		
		List<String[]> list = getEnableList();
		for (String[] strArr : list) {
			if (StringUtils.equals(nextStat, strArr[0])) {
				isExist = true;
				for (int i=1; i<strArr.length; i++) {
					if (StringUtils.equals(prevStat, strArr[i])) {
						isSame = true;
						break;
					}
				}
				break;
			}
		}
		
		if (!isExist) {
			return true;
		} else {
			return isSame;
		}
	}
	
	public List<String[]> getEnableList() {
		List<String[]> list = new ArrayList<>();
		if ("od".equals(div)) {
			String[] s1 = {"90", "99"}; // 90=임시저장 삭제. 
			String[] s2 = {"99", "99"}; // 99=임시저장 
			String[] s3 = {"00", "99","00"}; // 맨앞은 변경할려는 상태값. 그 뒤는 변경할 수 있는 이전 상태값. 주문접수(00)는 임시저장(99),주문접수(00)에서 변경 가능.
			
			String[] s4 = {"01", "00"};
			String[] s5 = {"02", "00"};
			String[] s6 = {"03", "00"};
			String[] s7 = {"05", "00"};
			String[] s8 = {"07", "00"};
			
			list.add(s1);
			list.add(s2);
			list.add(s3);
			list.add(s4);
			list.add(s5);
			list.add(s6);
			list.add(s7);
			list.add(s8);
		}
		if ("so".equals(div)) {

		}
		
		return list;
	}
	
	public Map<String, String> getMap() {
		Map<String, String> map = new HashMap<>();
		// E-Ordering 주문 상태값.
		if ("od".equals(div)) {
			map.put("99", "임시저장");
			map.put("00", "주문접수");
			map.put("01", "고객취소");
			map.put("02", "CS취소");
			map.put("03", "CS반려");
			map.put("05", "주문확정");
			//map.put("06", "오더분리");
			map.put("07", "주문확정(분리)");
		}
		// JDE 주문 상태값. 출고 상태.
		if ("so".equals(div)) {
			map.put("522", "오더접수");
			map.put("525", "신용체크");
			map.put("527", "출하접수");
			map.put("530", "배차완료");
			map.put("560", "출하완료");
			map.put("800", "배송완료"); // 실제 없는 상태값으로 데이터 가공 필요
		}
		// JDE 주문 상태값. 출고 상태.
		if ("qms".equals(div)) {
			map.put("001", "QMS 생성 미완료");
			map.put("002", "QMS 생성 완료");
			map.put("003", "메일 발송 완료");
			map.put("004", "QMS 회신 완료");
		}
		return map;
	}
	
	public String getMapToJson() {
		return Converter.toJson(this.getMap());
	}
}
