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

public class ItemExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(ItemExcel.class);
	
	@Override
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("품목현황.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
		String where = Converter.toStr(modelMap.get("where")); // excel=관리자 엑셀, frontexcel=프론트 엑셀.
		//logger.debug("PlantConfigExcelDown > list : {}", list);
		
		Sheet sheet = workbook.createSheet("Sheet1");
		ExcelUtil eu = new ExcelUtil(workbook);
		
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
		headColNameList.add("SEARCH-TEXT");
		headColNameList.add("구매단위");
		headColNameList.add("재고유형");
		headColNameList.add("두께명");
		headColNameList.add("폭명");
		headColNameList.add("길이명");
		headColNameList.add("약칭코드");
		headColNameList.add("Line TY");
		headColNameList.add("연관검색어"); // ItemInfo Table Start.
		headColNameList.add("파레트적재단위");
		headColNameList.add("이미지1");
		headColNameList.add("이미지2");
		headColNameList.add("이미지3");
		headColNameList.add("이미지4");
		headColNameList.add("이미지5");
		headColNameList.add("추천상품1");
		headColNameList.add("추천상품2");
		headColNameList.add("추천상품3");
		headColNameList.add("추천상품4");
		headColNameList.add("추천상품5");
		headColNameList.add("추천상품6");
		headColNameList.add("추천상품7");
		headColNameList.add("추천상품8");
		headColNameList.add("추천상품9");
		headColNameList.add("추천상품10");
		if(StringUtils.equals("frontexcel", where)) { // 프론트 에서만 노출.
			headColNameList.add("즐겨찾기 순서");
			headColNameList.add("즐겨찾기 등록 여부");
		}

		headColNameList.add("노출순서");

		for(String headColName : headColNameList) {
			row.createCell(c++).setCellValue(headColName);
		}
		
		// ####### Set Data.
		for (Map<String, Object> map : list) {
			c = 0;
			row = sheet.createRow(r++);
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("ITEM_CD")));
			
			// 분류.
			//String cate1 = Converter.toStr(map.get("SALES_CD1_NM"));
			//String cate2 = Converter.toStr(map.get("SALES_CD2_NM"));
			//String cate3 = Converter.toStr(map.get("SALES_CD3_NM"));
			//String cate4 = Converter.toStr(map.get("SALES_CD4_NM"));
			//if(!StringUtils.equals("", cate2)) cate1 += " > "+cate2;
			//if(!StringUtils.equals("", cate3)) cate1 += " > "+cate3;
			//if(!StringUtils.equals("", cate4)) cate1 += " > "+cate4;
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALES_CD1_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALES_CD2_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALES_CD3_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SALES_CD4_NM")));
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("DESC1")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("DESC2")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SEARCH_TEXT")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("UNIT4")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("STOCK_TY")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("THICK_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("WIDTH_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("LENGTH_NM")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("SHORT_CD")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("LINE_TY")));
			
			row.createCell(c++).setCellValue(HttpUtils.replaceXss(Converter.toStr(map.get("ITI_SEARCHWORD"))));
			
			// 파레트적재단위.
			double pallet = Converter.decimalScale2(map.get("ITI_PALLET"));
			if(0d != pallet) eu.setCellPattern(row.createCell(c++)).setCellValue(pallet);
			else row.createCell(c++).setCellValue("");
			
			row.createCell(c++).setCellValue(Converter.toStr(map.get("ITI_FILE1")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("ITI_FILE2")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("ITI_FILE3")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("ITI_FILE4")));
			row.createCell(c++).setCellValue(Converter.toStr(map.get("ITI_FILE5")));
			
			// Start. 추천상품. 
			// 추천상품이 없어도 itemRecommendsArr.length가 0이 아니라 1이 되네... 빈값으로 들어감...
			String itemRecommends = Converter.toStr(map.get("ITEM_RECOMMENDS"));
			String[] itemRecommendsArr = itemRecommends.split("@D@,", -1);
			int recommendCnt = itemRecommendsArr.length; // 추천상품 개수
			
			for(int i=0; i<10; i++) {
				String one = "";
				
				// 추천상품 개수가 0 => 이상하게 추천상품이 없어도 개수가 1이나오네...그래서 결국 얘는 안타네...
				if(0 == recommendCnt) row.createCell(c++).setCellValue("");
				// 추천상품 개수가 1~10개.
				else {
					if(i < recommendCnt) { // 추천상품 개수가 1 ==> i=0 / 2 ==> i=0,1 / 3 ==> i=0,1,2 / 4 ==> i=0,1,2,3 ... / 10 ==> i=0,1,2,3,4,5,6,7,8,9 
						if(i==0) one = StringUtils.equals("", itemRecommendsArr[i].replaceAll(" ", "").replaceAll("@D@", "")) ? "" : itemRecommendsArr[i].replaceAll("@D@", "");
						else one = itemRecommendsArr[i].replaceAll("@D@", "");
						row.createCell(c++).setCellValue(one);
					}else {
						row.createCell(c++).setCellValue("");
					}
				}
			} 
			// End. 추천상품.
			
			
			// 프론트 에서만 노출.
			if(StringUtils.equals("frontexcel", where)) {
				row.createCell(c++).setCellValue(Converter.toStr(map.get("ITB_SORT")));
				row.createCell(c++).setCellValue(Converter.toStr(map.get("BOOKMARK_YN")));
			}
				row.createCell(c++).setCellValue(Converter.toStr(map.get("ITI_SORT")));
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
