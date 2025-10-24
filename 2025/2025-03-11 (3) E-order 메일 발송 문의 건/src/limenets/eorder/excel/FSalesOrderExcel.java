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

public class FSalesOrderExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(FSalesOrderExcel.class);
	
	@Override
	@SuppressWarnings("unchecked")
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("전체주문현황.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
		String where = Converter.toStr(modelMap.get("where")); // excel=관리자 엑셀, frontexcel=프론트 엑셀.
		
		String colSortStr = Converter.toStr(request.getParameter("r_colsortstr")); // 0,1,2,3,.......,19
        String[] colSortArr = colSortStr.split(",", -1);
        
		Sheet sheet = workbook.createSheet("Sheet1");
		ExcelUtil eu = new ExcelUtil(workbook);
		
		int r = 0, c = 0;
		Row row = sheet.createRow(r++);
		
		// ####### Set Header.
		List<String> headColNameList = new ArrayList<>();
		for(int i=0,j=colSortArr.length; i<j; i++) {
    		if(StringUtils.equals("0", colSortArr[i])) headColNameList.add("오더번호");
    		if(StringUtils.equals("1", colSortArr[i])) headColNameList.add("주문번호");
    		if(StringUtils.equals("2", colSortArr[i])) headColNameList.add("오더코드");
    		if(StringUtils.equals("3", colSortArr[i])) headColNameList.add("품목코드");
    		if(StringUtils.equals("4", colSortArr[i])) headColNameList.add("품목명");
    		if(StringUtils.equals("5", colSortArr[i])) headColNameList.add("수량");
    		if(StringUtils.equals("6", colSortArr[i])) headColNameList.add("단위");
    		if(StringUtils.equals("7", colSortArr[i])) headColNameList.add("오더상태");
    		if(StringUtils.equals("8", colSortArr[i])) headColNameList.add("납품처명");
    		if(StringUtils.equals("9", colSortArr[i])) headColNameList.add("납품주소");
    		if(StringUtils.equals("10", colSortArr[i])) headColNameList.add("출하지");
    		if(StringUtils.equals("11", colSortArr[i])) headColNameList.add("헤베수량");
    		if(StringUtils.equals("12", colSortArr[i])) headColNameList.add("기사TEL");
    		if(StringUtils.equals("13", colSortArr[i])) headColNameList.add("납품요청일");
    		if(StringUtils.equals("14", colSortArr[i])) headColNameList.add("출하일자");
    		if(StringUtils.equals("15", colSortArr[i])) headColNameList.add("전화번호");
    		if(StringUtils.equals("16", colSortArr[i])) headColNameList.add("보류코드");
    		if(StringUtils.equals("17", colSortArr[i])) headColNameList.add("접수자");
		}
		
		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			for(int i=0,j=colSortArr.length; i<j; i++) {
			    if(StringUtils.equals("0", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("ORDERNO")));
			    if(StringUtils.equals("1", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("CUST_PO")));
			    if(StringUtils.equals("2", colSortArr[i]))row.createCell(c++).setCellValue(Converter.toStr(map.get("ORDERTY")));
    			if(StringUtils.equals("3", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("ITEM_CD")));
    			if(StringUtils.equals("4", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ITEM_DESC"))));
    			if(StringUtils.equals("5", colSortArr[i]))row.createCell(c++).setCellValue(Converter.toDouble(map.get("ORDER_QTY")));
    			if(StringUtils.equals("6", colSortArr[i]))row.createCell(c++).setCellValue(Converter.toStr(map.get("UNIT")));
    			if(StringUtils.equals("7", colSortArr[i]))row.createCell(c++).setCellValue(Converter.toStr(map.get("STATUS_DESC")));
    			if(StringUtils.equals("8", colSortArr[i]))row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SHIPTO_NM"))));
    			String add1 = Converter.toStr(map.get("ADD1")).trim();
    			String add2 = Converter.toStr(map.get("ADD2")).trim();
    			if(!StringUtils.equals("", add2)) add1 += " "+add2;
    			if(StringUtils.equals("9", colSortArr[i]))row.createCell(c++).setCellValue(HttpUtils.restoreXss(add1));
    			if(StringUtils.equals("10", colSortArr[i]))row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("PLANT_DESC"))));
    			if(StringUtils.equals("11", colSortArr[i]))row.createCell(c++).setCellValue(Converter.toDouble(map.get("PRIMARY_QTY"))); // 헤베수량
    			if(StringUtils.equals("12", colSortArr[i]))row.createCell(c++).setCellValue(Converter.toStr(map.get("DRIVER_PHONE")));
			
    			String requestDt = Converter.toStr(map.get("REQUEST_DT"));
    			requestDt = (StringUtils.equals("", requestDt)) ? "" : requestDt.substring(0,4)+"-"+requestDt.substring(4,6)+"-"+requestDt.substring(6,8);
    			// 2024-12-23 hsg HeadLock 납품요청일에 납품요청시간 추가
			    String requestTime = Converter.toStr(map.get("REQUEST_TIME"));
			    requestDt = (StringUtils.equals("", requestTime)) ? "" : requestDt + " " + requestTime.substring(0,2)+":"+requestTime.substring(2,4);
			    if(StringUtils.equals("13", colSortArr[i]))row.createCell(c++).setCellValue(requestDt);

			    String inDate = Converter.toStr(map.get("ACTUAL_SHIP_DT"));
    			inDate = (StringUtils.equals("", inDate)) ? "" : inDate.substring(0,4)+"-"+inDate.substring(4,6)+"-"+inDate.substring(6,8);
    			if(StringUtils.equals("14", colSortArr[i]))row.createCell(c++).setCellValue(inDate);
    			if(StringUtils.equals("15", colSortArr[i]))row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ADD3"))));
    			if(StringUtils.equals("16", colSortArr[i]))row.createCell(c++).setCellValue(Converter.toStr(map.get("HOLD_CODE")));
    			if(StringUtils.equals("17", colSortArr[i]))row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SALESREP_NM"))));
			}
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
