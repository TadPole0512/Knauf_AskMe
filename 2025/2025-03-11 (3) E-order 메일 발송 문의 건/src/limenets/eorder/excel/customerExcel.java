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

public class customerExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(customerExcel.class);
	
	@Override
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("거래처현황.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
		//logger.debug("PlantConfigExcelDown > list : {}", list);
		
		Sheet sheet = workbook.createSheet("Sheet1");
		ExcelUtil eu = new ExcelUtil(workbook);
		
		int r = 0, c = 0;
		Row row = sheet.createRow(r++);
		
		// ####### Set Header.
		List<String> headColNameList = new ArrayList<>();
		headColNameList.add("번호");
		headColNameList.add("코드");
		headColNameList.add("거래처명");
		headColNameList.add("거래처계정");
		headColNameList.add("영업담당");
		headColNameList.add("납품처");
		headColNameList.add("납품처계정");
		headColNameList.add("거래처주소");
		headColNameList.add("등록일");
		
		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		int num = 1;
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			row.createCell(c++).setCellValue(num++);
			row.createCell(c++).setCellValue(Converter.toStr(map.get("CUST_CD")));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CUST_NM"))));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("CUSTOMER_USER_CNT")));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SALESREP_NM2"))));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SHIPTO_CNT")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SHIPTO_USER_CNT")));
			
			//거래처주소
			String zip = Converter.toStr(map.get("ZIP_CD"));
			String addr = HttpUtils.restoreXss(Converter.toStr(map.get("ADD1"))) + " " + HttpUtils.restoreXss(Converter.toStr(map.get("ADD2")));
			if(!StringUtils.equals("", zip)) { addr = "[" + zip + "] " + addr; }
			
			row.createCell(c++).setCellValue(addr);
			
			String indate = Converter.toSubString(map.get("INSERT_DT"), 0, 4)+"-"+Converter.toSubString(map.get("INSERT_DT"), 4, 6)+"-"+Converter.toSubString(map.get("INSERT_DT"), 6, 8); 
			row.createCell(c++).setCellValue(indate);
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
		//logger.debug("### 거래처현황 엑셀다운로드 종료 ###");
		String fileToken = Converter.toStr(modelMap.get("fileToken"));
		Cookie fileCookie = new Cookie(fileToken, "true");
		fileCookie.setMaxAge(60*1); // 1분만 저장.
		response.addCookie(fileCookie);
		//logger.debug("fileToken : {}", fileToken);
	}
}
