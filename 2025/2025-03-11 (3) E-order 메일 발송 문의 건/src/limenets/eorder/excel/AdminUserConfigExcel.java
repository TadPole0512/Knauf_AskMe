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

public class AdminUserConfigExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(AdminUserConfigExcel.class);
	
	@Override
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("내부사용자.xlsx", StandardCharsets.UTF_8);
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
		headColNameList.add("권한");
		headColNameList.add("아이디");
		headColNameList.add("임직원명");
		headColNameList.add("직책");
		headColNameList.add("거래처수");
		headColNameList.add("담당");
		headColNameList.add("휴대폰번호");
		headColNameList.add("전화번호");
		headColNameList.add("이메일");
		headColNameList.add("사용여부");
		headColNameList.add("등록일");
		
		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("AUTHORITY")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("USERID")));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("USER_NM"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("USER_POSITION"))));
			
			// 영업사원만 > 거래처수
			double customerCnt = Converter.decimalScale(map.get("CUSTOMER_CNT"));
			if(-1 == customerCnt) row.createCell(c++).setCellValue("");
			else eu.setCellPattern(row.createCell(c++)).setCellValue(customerCnt);
			
			// 영업사원만 > CS담당
			String csSalesUser = HttpUtils.restoreXss(Converter.toStr(map.get("CS_SALESUSER")));
			if(!StringUtils.equals("", csSalesUser) && !StringUtils.equals("-", csSalesUser)) csSalesUser = "CS "+csSalesUser;
			row.createCell(c++).setCellValue(csSalesUser);
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("CELL_NO")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("TEL_NO")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("USER_EMAIL")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("USER_USE")));
			row.createCell(c++).setCellValue(Converter.toSubString(map.get("INDATE"), 0, 16));
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
