package com.limenets.eorder.excel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.limenets.common.util.Converter;
import com.limenets.common.util.HttpUtils;

public class ItemManageExcel extends AbstractXlsxView {
	
	@Override
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("QMS품목현황.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
		//String where = Converter.toStr(modelMap.get("where")); // excel=관리자 엑셀, frontexcel=프론트 엑셀.
		//logger.debug("PlantConfigExcelDown > list : {}", list);
		
		Sheet sheet = workbook.createSheet("Sheet1");
		//ExcelUtil eu = new ExcelUtil(workbook);
		
		int r = 0, c = 0;
		Row row = sheet.createRow(r++);
		
		// ####### Set Header.
		List<String> headColNameList = new ArrayList<>();
		headColNameList.add("품목코드"); // O_ITEM_NEW Table Start.
		headColNameList.add("분류1");
		headColNameList.add("분류2");
		headColNameList.add("분류3");
		headColNameList.add("분류4");
		headColNameList.add("품목명1");
		headColNameList.add("품목명2");
		headColNameList.add("구매단위");
		headColNameList.add("재고유형");
		headColNameList.add("두께명");
		headColNameList.add("폭명");
		headColNameList.add("길이명");
		headColNameList.add("Line TY");
		
		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("ITEM_CD")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALES_CD1_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALES_CD2_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALES_CD3_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALES_CD4_NM")));
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("DESC1")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("DESC2")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("UNIT4")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("STOCK_TY")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("THICK_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("WIDTH_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("LENGTH_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("LINE_TY")));
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
