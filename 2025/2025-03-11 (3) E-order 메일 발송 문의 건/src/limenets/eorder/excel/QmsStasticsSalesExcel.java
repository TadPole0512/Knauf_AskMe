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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.limenets.common.util.Converter;
import com.limenets.common.util.ExcelUtil;
import com.limenets.common.util.HttpUtils;

public class QmsStasticsSalesExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(QmsStasticsSalesExcel.class);
	
	@Override
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("QMS 회수 실적현황.xlsx", StandardCharsets.UTF_8);
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
		headColNameList.add("Department");
		headColNameList.add("Team");
		headColNameList.add("담당자");
		headColNameList.add("오더수량");
		headColNameList.add("QMS발행");
		headColNameList.add("QMS마감");
		headColNameList.add("회수율(%)");
		headColNameList.add("팀 발행");
		headColNameList.add("팀 회수");
		headColNameList.add("팀 회수율");
		
		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("QMS_DEPT_NM")));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("QMS_TEAM_NM"))));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALESREP_NM")));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("ORDER_QTY"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("QMS_ORDER_CNT"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("QMS_END_CNT"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("RETURN_RATE"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("TEAM_ORDER_CNT"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("TEAM_RETURN_CNT"))));
			row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("TEAM_RETURN_RATE"))));
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
