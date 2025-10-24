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

public class QmsOrderExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(OrderExcel.class);
	
	@Override
	@SuppressWarnings("unchecked")
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("QMS조회및등록.xlsx", StandardCharsets.UTF_8);
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
		headColNameList.add("오더번호");
		headColNameList.add("입력구분");
		headColNameList.add("출고일자");
		headColNameList.add("품목명");
		headColNameList.add("Lot No");
		headColNameList.add("수량");
		headColNameList.add("QMS 수량");
		headColNameList.add("QMS오더번호");
		headColNameList.add("거래처");
		headColNameList.add("납품처");
		headColNameList.add("메일Y/N");
		headColNameList.add("첨부Y/N");
		headColNameList.add("마감Y/N");
		headColNameList.add("납품주소");
		headColNameList.add("현장명");
		headColNameList.add("분기");
		headColNameList.add("라인번호");
		headColNameList.add("확정오더번호");
		headColNameList.add("거래처 코드");
		headColNameList.add("주문일자");
		headColNameList.add("납품처 코드");
		headColNameList.add("영업사원");
		
		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ORDERNO"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("QMS_STEP"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ACTUAL_SHIP_DT"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ITEM_DESC"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("LOTN"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ORDER_QTY"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("QMS_ARR_QTY"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("QMS_ARR"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CUST_NM"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SHIPTO_NM"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MAIL_YN"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("FILE_YN"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ACTIVEYN"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ADDR"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("QMS_ARR_SHIPTO"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ACTUAL_SHIP_QUARTER"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("LINE_NO"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CUST_PO"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CUST_CD"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ORDER_DT"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SHIPTO_CD"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SALESREP_NM"))));
			
			
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
