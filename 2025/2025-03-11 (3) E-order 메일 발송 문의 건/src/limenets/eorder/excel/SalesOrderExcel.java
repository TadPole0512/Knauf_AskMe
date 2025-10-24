package com.limenets.eorder.excel;

import java.io.OutputStream;
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
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.limenets.common.util.Converter;
import com.limenets.common.util.ExcelUtil;
import com.limenets.common.util.HttpUtils;

public class SalesOrderExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(SalesOrderExcel.class);

	@Override
	@SuppressWarnings("unchecked")
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("전체주문현황.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
//		long start = System.currentTimeMillis();
//		long end = 0;

		List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
		String where = Converter.toStr(modelMap.get("where")); // excel=관리자 엑셀, frontexcel=프론트 엑셀.
		//logger.debug("PlantConfigExcelDown > list : {}", list);
		String colSortStr = Converter.toStr(request.getParameter("r_colsortstr")); // 0,1,2,3,.......,19
		String[] colSortArr = colSortStr.split(",", -1);
		logger.debug("colSortStr : {}", colSortStr);

		/*
		String dataSort = "";
		Cookie[] cookies = request.getCookies();
		logger.debug("cookies : {}", cookies);
		if(null != cookies) {
			for(int i=0,j=cookies.length; i<j; i++) {
				Cookie ck = cookies[i];
				String ckName = ck.getName();
				String ckValue = ck.getValue();
				logger.debug("ckName : {}", ckName);
				if(StringUtils.equals("admin/order/salesOrderList/jqGridCookie/gridList", ckName)) {
					dataSort = ckValue;
				}
			}
		}
		logger.debug("dataSort : {}", dataSort);
		*/
		
		/*
		param rowAccessWindowSize : 입력한 수 단위로 autoflush
		-1의 경우 autoflush 끔
		*/
		SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(10000);

		Sheet sheet = sxssfWorkbook.createSheet("Sheet1");

		ExcelUtil eu = new ExcelUtil(workbook);

		int r = 0, c = 0;
		Row row = sheet.createRow(r++);

		// ####### Set Header.
		List<String> headColNameList = new ArrayList<>();
		
		for(int i=0,j=colSortArr.length; i<j; i++) {
			if(StringUtils.equals("0", colSortArr[i])) headColNameList.add("오더번호"); 
			if(StringUtils.equals("1", colSortArr[i])) headColNameList.add("보류코드"); 
			if(StringUtils.equals("2", colSortArr[i])) headColNameList.add("거래처명"); 
			if(StringUtils.equals("3", colSortArr[i])) headColNameList.add("납품주소"); 
			if(StringUtils.equals("4", colSortArr[i])) headColNameList.add("출하일자"); 
			if(StringUtils.equals("5", colSortArr[i])) headColNameList.add("납품요청일"); 
			if(StringUtils.equals("6", colSortArr[i])) headColNameList.add("품목명"); 
			if(StringUtils.equals("7", colSortArr[i])) headColNameList.add("수량"); 
			if(StringUtils.equals("8", colSortArr[i])) headColNameList.add("단위"); 
			if(StringUtils.equals("9", colSortArr[i])) headColNameList.add("납품처명"); 
			if(StringUtils.equals("10", colSortArr[i])) headColNameList.add("출하지"); 
			if(StringUtils.equals("11", colSortArr[i])) headColNameList.add("헤베수량"); 
			if(StringUtils.equals("12", colSortArr[i])) headColNameList.add("주단위"); 
			if(StringUtils.equals("13", colSortArr[i])) headColNameList.add("품목코드"); 
			if(StringUtils.equals("14", colSortArr[i])) headColNameList.add("오더상태"); 
			if(StringUtils.equals("15", colSortArr[i])) headColNameList.add("기사TEL"); 
			if(StringUtils.equals("16", colSortArr[i])) headColNameList.add("오더일자"); 
			if(StringUtils.equals("17", colSortArr[i])) headColNameList.add("고객PO"); 
			if(StringUtils.equals("18", colSortArr[i])) headColNameList.add("특기사항");
			if(StringUtils.equals("19", colSortArr[i])) headColNameList.add("전화번호");
			if(StringUtils.equals("20", colSortArr[i])) headColNameList.add("영업사원");
		}
		
		//headColNameList.add("오더번호"); // 0
		//headColNameList.add("고객PO"); // 1
		//headColNameList.add("출하지"); // 2
		//headColNameList.add("오더상태"); // 3
		//headColNameList.add("수량"); // 4
		//headColNameList.add("단위"); // 5
		//headColNameList.add("출하일자"); // 6
		//headColNameList.add("납품요청일"); // 7
		//headColNameList.add("거래처명"); // 8
		//headColNameList.add("납품주소"); // 9
		//headColNameList.add("품목코드"); // 10
		//headColNameList.add("품목명"); // 11
		//headColNameList.add("납품처명"); // 12
		//headColNameList.add("특기사항"); // 13
		//headColNameList.add("헤베수량"); // 14
		//headColNameList.add("기사TEL"); // 15
		//headColNameList.add("주단위"); // 16
		//headColNameList.add("전화번호"); // 17
		//headColNameList.add("보류코드"); // 18
		//headColNameList.add("영업사원"); // 19

		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}

		// ####### Set Data.
		for (Map<String, Object> map : list) {

			//flush가 되면 메모리에 row가 없기 때문에 for문에 밖에서 선언하면 getRow메서드 사용 시 에러 출력
			if(r == 2) {
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

			c = 0;
			row = sheet.createRow(r++);
			
			// Start. 데이터 가공.
			String orderDate = Converter.toStr(map.get("ORDER_DT"));
			orderDate = (StringUtils.equals("", orderDate)) ? "" : orderDate.substring(0,4)+"-"+orderDate.substring(4,6)+"-"+orderDate.substring(6,8);
			String inDate = Converter.toStr(map.get("ACTUAL_SHIP_DT"));
			inDate = (StringUtils.equals("", inDate)) ? "" : inDate.substring(0,4)+"-"+inDate.substring(4,6)+"-"+inDate.substring(6,8);			
			String requestDt = Converter.toStr(map.get("REQUEST_DT"));
			requestDt = (StringUtils.equals("", requestDt)) ? "" : requestDt.substring(0,4)+"-"+requestDt.substring(4,6)+"-"+requestDt.substring(6,8);
			// 2024-12-13 hsg Figure-four leglock ‘전체주문현황’ 에서 ‘엑셀다운로드’시 ‘납품요청일시’가 동일하게 출력되지 않습니다. 엑셀 자료에도 화면과 나올 수 있도록 시간 값 추가 요청
			String requestTime = Converter.toStr(map.get("REQUEST_TIME"));
			requestTime = (StringUtils.equals("", requestTime)) ? "" : " "+requestTime.substring(0,2)+":"+requestTime.substring(2,4);
			String add1 = Converter.toStr(map.get("ADD1")).trim();
			String add2 = Converter.toStr(map.get("ADD2")).trim();
			if(!StringUtils.equals("", add2)) add1 += " "+add2;
			// End. 데이터 가공.
			
			for(int i=0,j=colSortArr.length; i<j; i++) {
				if(StringUtils.equals("0", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("ORDERNO")));
                if(StringUtils.equals("1", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("HOLD_CODE"))));
                if(StringUtils.equals("2", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CUST_NM"))));
                if(StringUtils.equals("3", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(add1));
                if(StringUtils.equals("4", colSortArr[i])) row.createCell(c++).setCellValue(inDate);
             // 2024-12-13 hsg Figure-four leglock ‘전체주문현황’ 에서 ‘엑셀다운로드’시 ‘납품요청일시’가 동일하게 출력되지 않습니다. 엑셀 자료에도 화면과 나올 수 있도록 시간 값 추가 요청
                if(StringUtils.equals("5", colSortArr[i])) row.createCell(c++).setCellValue(requestDt+requestTime);
                if(StringUtils.equals("6", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ITEM_DESC"))));
                if(StringUtils.equals("7", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("ORDER_QTY")));
                if(StringUtils.equals("8", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("UNIT"))));
                if(StringUtils.equals("9", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SHIPTO_NM"))));
                if(StringUtils.equals("10", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("PLANT_DESC"))));
                if(StringUtils.equals("11", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toDouble(map.get("PRIMARY_QTY")));
                if(StringUtils.equals("12", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("UNIT1"))));
                if(StringUtils.equals("13", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ITEM_CD"))));
                if(StringUtils.equals("14", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("STATUS_DESC"))));
                if(StringUtils.equals("15", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("DRIVER_PHONE"))));
                if(StringUtils.equals("16", colSortArr[i])) row.createCell(c++).setCellValue(orderDate);
                if(StringUtils.equals("17", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CUST_PO"))));
                if(StringUtils.equals("18", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ADD4"))));
                if(StringUtils.equals("19", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ADD3"))));
                if(StringUtils.equals("20", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SALESREP_NM"))));
         

                //if(StringUtils.equals("1", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("HOLD_CODE")));
//                if(StringUtils.equals("17", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("ADD1")));
//				if(StringUtils.equals("1", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("CUST_PO")));
//				if(StringUtils.equals("2", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("PLANT_DESC"))));
//				if(StringUtils.equals("3", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("STATUS_DESC")));
//				if(StringUtils.equals("4", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toDouble(map.get("ORDER_QTY")));
//				if(StringUtils.equals("5", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("UNIT")));
//				if(StringUtils.equals("6", colSortArr[i])) row.createCell(c++).setCellValue(inDate);
//				if(StringUtils.equals("7", colSortArr[i])) row.createCell(c++).setCellValue(requestDt);
//				if(StringUtils.equals("9", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(add1));
//				if(StringUtils.equals("10", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("ITEM_CD")));
//				if(StringUtils.equals("12", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SHIPTO_NM"))));
//				if(StringUtils.equals("13", colSortArr[i])) row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ADD4"))));
//				if(StringUtils.equals("15", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("DRIVER_PHONE")));
//				if(StringUtils.equals("16", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("UNIT1")));
//				if(StringUtils.equals("17", colSortArr[i])) row.createCell(c++).setCellValue(Converter.toStr(map.get("ADD3")));
			}
			
			//row.createCell(c++).setCellValue(Converter.toStr(map.get("ORDERNO")));
			//row.createCell(c++).setCellValue(Converter.toStr(map.get("CUST_PO")));
			//row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("PLANT_DESC"))));
			//row.createCell(c++).setCellValue(Converter.toStr(map.get("STATUS_DESC")));
			//row.createCell(c++).setCellValue(Converter.toDouble(map.get("ORDER_QTY")));
			//row.createCell(c++).setCellValue(Converter.toStr(map.get("UNIT")));
			//row.createCell(c++).setCellValue(inDate);
			//row.createCell(c++).setCellValue(requestDt);
			//row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CUST_NM"))));
			//row.createCell(c++).setCellValue(HttpUtils.restoreXss(add1));
			//row.createCell(c++).setCellValue(Converter.toStr(map.get("ITEM_CD")));
			//row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ITEM_DESC"))));
			//row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SHIPTO_NM"))));
			//row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ADD4"))));
			//row.createCell(c++).setCellValue(Converter.toDouble(map.get("PRIMARY_QTY")));
			//row.createCell(c++).setCellValue(Converter.toStr(map.get("DRIVER_PHONE")));
			//row.createCell(c++).setCellValue(Converter.toStr(map.get("UNIT1")));
			//row.createCell(c++).setCellValue(Converter.toStr(map.get("ADD3")));
			//row.createCell(c++).setCellValue(Converter.toStr(map.get("HOLD_CODE")));
			//row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SALESREP_NM"))));
		}

		//Flush all rows to disk.
		((SXSSFSheet)sheet).flushRows(list.size());

		// ####### 엑셀다운로드 종료후 뷰단에서 종료여부 체크중인 쿠키 생성.
		//logger.debug("### 품목현황 엑셀다운로드 종료 ###");
		String fileToken = Converter.toStr(modelMap.get("fileToken"));
		Cookie fileCookie = new Cookie(fileToken, "true");
		fileCookie.setMaxAge(60*1); // 1분만 저장.
		response.addCookie(fileCookie);
		//logger.debug("fileToken : {}", fileToken);

		OutputStream fout = response.getOutputStream();
		sxssfWorkbook.write(fout);
		fout.close();

		sxssfWorkbook.dispose(); //임시파일 제거

//		end = System.currentTimeMillis();
//		logger.debug("################## 반복문 수행 시간 = "+ (end - start)/1000.0);
	}

}
