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

public class SalesOrderMainExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(SalesOrderMainExcel.class);
	
	@Override
	@SuppressWarnings("unchecked")
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("거래내역(주문).xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
		String where = Converter.toStr(modelMap.get("where")); // excel=관리자 엑셀, frontexcel=프론트 엑셀.
		//logger.debug("PlantConfigExcelDown > list : {}", list);
		
		Sheet sheet = workbook.createSheet("Sheet1");
		ExcelUtil eu = new ExcelUtil(workbook);
		
		int r = 0, c = 0;
		Row row = sheet.createRow(r++);
		
		// ####### Set Header.
		List<String> headColNameList = new ArrayList<>();
		headColNameList.add("출고일자");
		headColNameList.add("납품처코드");
		headColNameList.add("납품처명");
		headColNameList.add("수주번호");
		headColNameList.add("SO번호");
		headColNameList.add("품목명");
		headColNameList.add("수량");
		headColNameList.add("SM");
		headColNameList.add("KG");
		headColNameList.add("출하지");
		headColNameList.add("도착지");
		
		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			String inDate = Converter.toStr(map.get("ACTUAL_SHIP_DT"));
			inDate = (StringUtils.equals("", inDate)) ? "" : inDate.substring(0,4)+"-"+inDate.substring(4,6)+"-"+inDate.substring(6,8);
			row.createCell(c++).setCellValue(inDate);
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SHIPTO_CD")));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SHIPTO_NM"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ORDERNO"))));
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("")));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ITEM_DESC"))));
			row.createCell(c++).setCellValue(Converter.toDouble(map.get("ORDER_QTY")));
			
			String unit1 = Converter.toStr(map.get("UNIT1"));
			double primary_qty = Converter.toDouble(map.get("PRIMARY_QTY"));
			double second_qty = Converter.toDouble(map.get("SECOND_QTY"));
			
			double sm_content = 0;
			double sh_content = 0;
			double kg_content = 0;
			
			// SM
			if(StringUtils.equals("SM", unit1)) {
				sm_content = primary_qty;
			
			}else if(StringUtils.equals("SH", unit1)){
				sh_content = second_qty;
			
			}else if(StringUtils.equals("KG", unit1)){
				kg_content = primary_qty;
			}
			
			if(StringUtils.equals("SM", unit1)) {
				row.createCell(c++).setCellValue(sm_content);
			}else {
				row.createCell(c++).setCellValue(sh_content);
			}
			
			// KG
			if(StringUtils.equals("KG", unit1) && kg_content != 0) {
				row.createCell(c++).setCellValue(kg_content);
			}else {
				row.createCell(c++).setCellValue("");
			}
			
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("PLANT_DESC"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ADD1"))));
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
