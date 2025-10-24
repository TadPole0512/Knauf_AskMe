package com.limenets.eorder.excel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.limenets.common.util.Converter;
import com.limenets.common.util.ExcelUtil;
import com.limenets.common.util.HttpUtils;

public class OrderExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(OrderExcel.class);
	
	@Override
	@SuppressWarnings("unchecked")
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("웹주문현황.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
		Map<String, Object> orderStatus = (Map<String, Object>)modelMap.get("orderStatus");
		String where = Converter.toStr(modelMap.get("where")); // excel=관리자 엑셀, frontexcel=프론트 엑셀.
		//logger.debug("PlantConfigExcelDown > list : {}", list);
		
		Sheet sheet = workbook.createSheet("Sheet1");
		ExcelUtil eu = new ExcelUtil(workbook);
		
		int r = 0, c = 0;
		Row row = sheet.createRow(r++);
		
		// ####### Set Header.
		List<String> headColNameList = new ArrayList<>();
		headColNameList.add("주문상태");
		headColNameList.add("주문번호");
		headColNameList.add("주문일시");
		headColNameList.add("납품요청일시");
		if(StringUtils.equals("excel", where)) { // 관리자 에서만 노출.
			headColNameList.add("거래처명");
			headColNameList.add("거래처코드");
		}
		headColNameList.add("납품처명");
		headColNameList.add("납품처코드");
		headColNameList.add("납품주소(우편번호)");
		headColNameList.add("납품주소(주소)");
		headColNameList.add("납품주소(상세주소)");
		headColNameList.add("주문자명");
		headColNameList.add("주문자아이디");
		headColNameList.add("영업담당명");
		headColNameList.add("영업담당아이디");
		headColNameList.add("CS(고정)명");
		headColNameList.add("CS(고정)아이디");
		headColNameList.add("품목개수");
		headColNameList.add("접속유형");
		
		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			row.createCell(c++).setCellValue(Converter.toStr(orderStatus.get(Converter.toStr(map.get("STATUS_CD")))));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("REQ_NO")));
			
			String inDate = Converter.toStr(map.get("INDATE"));
			//logger.debug("inDate : {}", inDate);
			inDate = (StringUtils.equals("", inDate)) ? inDate : inDate.substring(0, 16); 
			row.createCell(c++).setCellValue(inDate);
			
			String requestDt = Converter.toStr(map.get("REQUEST_DT"));
			requestDt = (StringUtils.equals("", requestDt)) ? "" : requestDt.substring(0,4)+"-"+requestDt.substring(4,6)+"-"+requestDt.substring(6,8);
			String requestTime = Converter.toStr(map.get("REQUEST_TIME"));
			// 2024-12-13 hsg Figure-four leglock ‘웹주문현황’ ‘엑셀다운로드’시 ‘납품요청일시’ 값 오출력 현상 => 마지막 분 설정 부분에 requestDt를 requestTime로 변경
//			requestTime = (StringUtils.equals("", requestTime)) ? "" : " "+requestTime.substring(0,2)+":"+requestDt.substring(2,4);
			requestTime = (StringUtils.equals("", requestTime)) ? "" : " "+requestTime.substring(0,2)+":"+requestTime.substring(2,4);
			row.createCell(c++).setCellValue(requestDt+requestTime);
			
			if(StringUtils.equals("excel", where)) { // 관리자 에서만 노출.
				row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CUST_NM"))));
				row.createCell(c++).setCellValue(Converter.toStr(map.get("CUST_CD")));
			}
			
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SHIPTO_NM"))));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SHIPTO_CD")));
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("ZIP_CD")));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ADD1"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ADD2"))));
			
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("USER_NM"))));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("USERID")));
			
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SALESUSER_NM"))));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALESUSERID")));
			
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CSUSER_NM"))));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("CSUSERID")));
			
			row.createCell(c++).setCellValue(Converter.toInt(map.get("ITEM_CNT")));
			
			String accessDevice = Converter.toStr(map.get("ACCESS_TYPE"));
			row.createCell(c++).setCellValue((StringUtils.equals("2", accessDevice) ? "APP" : "WEB"));
		}
		
		// ####### Set width 자동조절.
		if(1 < r) {
			for (int i=0,j=sheet.getRow(1).getPhysicalNumberOfCells(); i<j; i++) {
				sheet.autoSizeColumn(i);
				int width = sheet.getColumnWidth(i);
				int minWidth = headColNameList.get(i).getBytes().length * 450;
				int maxWidth = 18000;
				if (minWidth > width) {
					sheet.setColumnWidth(i, minWidth);
				} else if (width > maxWidth) {
					sheet.setColumnWidth(i, maxWidth);
				} else {
					sheet.setColumnWidth(i, width + 2000);
				}
			}
		}
		
		// ####### 엑셀다운로드 종료후 뷰단에서 종료여부 체크중인 쿠키 생성.
		//logger.debug("### 품목현황 엑셀다운로드 종료 ###");
		String fileToken = Converter.toStr(modelMap.get("fileToken"));
		Cookie fileCookie = new Cookie(fileToken, "true");
		fileCookie.setMaxAge(60*1); // 1분만 저장.
		response.addCookie(fileCookie);
		//logger.debug("fileToken : {}", fileToken);
		
	}
}
