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

public class SendMailHistoryExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(SendMailHistoryExcel.class);
	
	@Override
	@SuppressWarnings("unchecked")
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("이메일전송기록.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
		
		Sheet sheet = workbook.createSheet("Sheet1");
		ExcelUtil eu = new ExcelUtil(workbook);
		
		int r = 0, c = 0;
		Row row = sheet.createRow(r++);
		
		// ####### Set Header.
		List<String> headColNameList = new ArrayList<>();
		headColNameList.add("전송기준");
		headColNameList.add("시작일");
		headColNameList.add("종료일");
		headColNameList.add("거래처");
		headColNameList.add("납품처");
		headColNameList.add("받는사람");
		headColNameList.add("보낸사람");
		headColNameList.add("보낸시간");
		
		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			//전송기준
			String smhType = "";
			if(StringUtils.equals("1", Converter.toStr(map.get("SMH_TYPE")))) smhType = "거래처 거래사실확인서";
			else smhType = "납품처 거래사실확인서";
			
			row.createCell(c++).setCellValue(smhType);
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SMH_SDATE")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SMH_EDATE")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SMH_CUSTNM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SMH_SHIPTONM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SMH_EMAIL")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SMH_ID")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SMH_INDATE")).substring(0, 16));
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
		String fileToken = Converter.toStr(modelMap.get("fileToken"));
		Cookie fileCookie = new Cookie(fileToken, "true");
		fileCookie.setMaxAge(60*1); // 1분만 저장.
		response.addCookie(fileCookie);
		//logger.debug("fileToken : {}", fileToken);
		
	}
}
