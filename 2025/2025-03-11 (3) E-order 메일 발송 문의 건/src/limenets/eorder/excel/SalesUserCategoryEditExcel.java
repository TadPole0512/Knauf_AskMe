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
import com.limenets.common.util.HttpUtils;

public class SalesUserCategoryEditExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(SalesUserCategoryEditExcel.class);
	
	@Override
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("영업조직현황.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
		
		Sheet sheet = workbook.createSheet("Sheet1");
		//ExcelUtil eu = new ExcelUtil(workbook);
		
		int r = 0, c = 0;
		Row row = sheet.createRow(r++);
		
		// ####### Set Header.
		List<String> headColNameList = new ArrayList<>();
		headColNameList.add("권한"); // O_ITEM_NEW Table Start.
		headColNameList.add("영업담당자 아이디");
		headColNameList.add("영업담당자명");
		headColNameList.add("상위 SH 아이디");
		headColNameList.add("상위 SH 이름");
		headColNameList.add("상위 SM 아이디");
		headColNameList.add("상위 SM 이름");

		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			String authority = Converter.toStr(map.get("AUTHORITY"));
			String userid = Converter.toStr(map.get("USERID"));
			String user_nm = Converter.toStr(map.get("USER_NM"));
			String user_cate2 = Converter.toStr(map.get("USER_CATE2"));
			String user_cate2_name = Converter.toStr(map.get("USER_CATE2_NAME"));
			String user_cate3 = Converter.toStr(map.get("USER_CATE3"));
			String user_cate3_name = Converter.toStr(map.get("USER_CATE3_NAME"));
			
			//if(StringUtils.equals("19000101", userid)) { // 비사용자그룹(19000101)
			//	continue; 
			//}
			
			if(StringUtils.equals("19000101", userid) || StringUtils.equals("19000101", user_cate2)) { // 비사용자그룹(19000101)
				authority = "RT";
			}
			
			row.createCell(c++).setCellValue(authority);
			row.createCell(c++).setCellValue(userid);
			row.createCell(c++).setCellValue(user_nm);
			
			if(StringUtils.equals("SH", authority) || StringUtils.equals("RT", authority)) {
				row.createCell(c++).setCellValue("");
				row.createCell(c++).setCellValue("");
			}else {
				row.createCell(c++).setCellValue(user_cate2);
				row.createCell(c++).setCellValue(user_cate2_name);
			}
			
			if(StringUtils.equals("SH", authority) || StringUtils.equals("SM", authority) || StringUtils.equals("RT", authority)) {
				row.createCell(c++).setCellValue("");
				row.createCell(c++).setCellValue("");
			}else {
				row.createCell(c++).setCellValue(user_cate3);
				row.createCell(c++).setCellValue(user_cate3_name);
			}
		}
		
		// ####### Set width 자동조절.
		if(1 < r) {
			for (int i=0,j=sheet.getRow(1).getPhysicalNumberOfCells(); i<j; i++) {
				sheet.setColumnWidth(i, 3800);
				/*
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
				*/
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
