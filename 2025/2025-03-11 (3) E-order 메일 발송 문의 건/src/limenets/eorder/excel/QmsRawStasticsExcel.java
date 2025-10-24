package com.limenets.eorder.excel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.limenets.common.util.Converter;
import com.limenets.common.util.HttpUtils;

public class QmsRawStasticsExcel extends AbstractXlsxView {
//	private static final Logger logger = LoggerFactory.getLogger(QmsRawStasticsExcel.class);
	
	@Override
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("QMS 발급대장.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		@SuppressWarnings("unchecked")
        List<Map<String, Object>> list = (List<Map<String, Object>>)modelMap.get("list");
        //logger.debug("PlantConfigExcelDown > list : {}", list);
        
        Sheet sheet = workbook.createSheet("Sheet1");
//      ExcelUtil eu = new ExcelUtil(workbook);
        
        CellStyle styleTop01 = workbook.createCellStyle();
        styleTop01.setAlignment(CellStyle.ALIGN_CENTER);
        styleTop01.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        styleTop01.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        styleTop01.setFillPattern(CellStyle.SOLID_FOREGROUND);
        styleTop01.setBorderTop(HSSFCellStyle.BORDER_THIN);
        styleTop01.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleTop01.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        styleTop01.setBorderRight(HSSFCellStyle.BORDER_THIN);
        
        CellStyle styleTop02 = workbook.createCellStyle();
        styleTop02.setAlignment(CellStyle.ALIGN_LEFT);
        styleTop02.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        styleTop02.setBorderTop(HSSFCellStyle.BORDER_THIN);
        styleTop02.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleTop02.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        styleTop02.setBorderRight(HSSFCellStyle.BORDER_THIN);
        
        CellStyle styleHeader = workbook.createCellStyle();
        styleHeader.setAlignment(CellStyle.ALIGN_CENTER);
        styleHeader.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        styleHeader.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
        styleHeader.setBorderTop(HSSFCellStyle.BORDER_THIN);
        styleHeader.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleHeader.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        styleHeader.setBorderRight(HSSFCellStyle.BORDER_THIN);
        
        CellStyle styleNormal = workbook.createCellStyle();
        styleNormal.setAlignment(CellStyle.ALIGN_CENTER);
        styleNormal.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        styleNormal.setBorderTop(HSSFCellStyle.BORDER_THIN);
        styleNormal.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        styleNormal.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        styleNormal.setBorderRight(HSSFCellStyle.BORDER_THIN);
        
        
        int r = 0, c = 0;
        Cell cell = null;
        Row row = sheet.createRow(r++);

        cell = row.createCell(c++);
        cell.setCellValue("상호");
        cell.setCellStyle(styleTop01);
        
        cell = row.createCell(c++);
        cell.setCellValue("크나우프석고보드 주식회사");
        cell.setCellStyle(styleTop02);
        
        cell = row.createCell(c++);
        cell.setCellValue("법인등록번호");
        cell.setCellStyle(styleTop01);
        
        cell = row.createCell(c++);
        cell.setCellValue("206211-00114098");
        cell.setCellStyle(styleTop02);
        
        cell = row.createCell(c++);
        cell.setCellValue("사업자등록번호");
        cell.setCellStyle(styleTop01);
        
        cell = row.createCell(c++);
        cell.setCellValue("417-81-17256");
        cell.setCellStyle(styleTop02);
        
        c=0;
        row = sheet.createRow(r++);
        row.setHeight((short) 1500);
        cell = row.createCell(c++);
        cell.setCellValue("제조현장 소재지");
        cell.setCellStyle(styleTop01);
        
        cell = row.createCell(c++);
        cell.setCellValue("충남 당진시 송악읍 부곡공단4길81\n울산시 남구 남도로 158\n전남 여수시 낙포단지길 45");
        cell.setCellStyle(styleTop02);
        
        cell = row.createCell(c++);
        cell.setCellValue("");
        cell.setCellStyle(styleTop02);
        
        cell = row.createCell(c++);
        cell.setCellValue("");
        cell.setCellStyle(styleTop02);
        
        cell = row.createCell(c++);
        cell.setCellValue("전화번호");
        cell.setCellStyle(styleTop01);
        
        cell = row.createCell(c++);
        cell.setCellValue("02-6902-3181");
        cell.setCellStyle(styleTop02);
        
        /*c=0;
        row = sheet.createRow(r++);
        for(int i = 0; i < 32; i++) { 
            if(i==1) row.createCell(c++).setCellValue("내화구조 정보");
            else if(i==10) row.createCell(c++).setCellValue("시공현장 정보");
            else if(i==15) row.createCell(c++).setCellValue("공급업자 정보");
            else if(i==20) row.createCell(c++).setCellValue("시공업자 정보");
            else if(i==25) row.createCell(c++).setCellValue("제조업자 정보");
            else row.createCell(c++).setCellValue("");
        }
        
        c = 0;
        row = sheet.createRow(r++);
        
        // ####### Set Header.
        List<String> headColNameList = new ArrayList<>();
        //내화구조 정보
        headColNameList.add("순번");
        headColNameList.add("회사명");
        headColNameList.add("품목");
        headColNameList.add("인정번호");
        headColNameList.add("내화성능(시간)");
        headColNameList.add("상품명");
        headColNameList.add("내화구조명");
        headColNameList.add("규격");
        headColNameList.add("수량");
        headColNameList.add("비고");
        //시공현장 정보
        headColNameList.add("현장명");
        headColNameList.add("현장주소(광역시, 도)");
        headColNameList.add("현장주소(상세)");
        headColNameList.add("시공회사");
        headColNameList.add("감리회사");
        //공급업자 정보
        headColNameList.add("회사명");
        headColNameList.add("확인자");
        headColNameList.add("사업자등록증번호");
        headColNameList.add("소재지");
        headColNameList.add("전화번호");
        //시공업자 정보
        headColNameList.add("회사명");
        headColNameList.add("확인자");
        headColNameList.add("사업자등록증번호");
        headColNameList.add("소재지");
        headColNameList.add("전화번호");
        //제조업자 정보
        headColNameList.add("품질관리확인서 번호");
        headColNameList.add("작성일자");
        headColNameList.add("로트번호");
        headColNameList.add("확인자");
        headColNameList.add("회사명");
        headColNameList.add("사업자등록증번호");
        headColNameList.add("소재지");
        headColNameList.add("전화번호");
        
        for(String headColName : headColNameList) {
            row.createCell(c++).setCellValue(headColName);
        }
        
        // ####### Set Data.
        for (Map<String, Object> map : list) {
            c = 0;
            row = sheet.createRow(r++);
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("RR"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_COMPANY"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_ITEM"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_AUTH_NUMBER"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_TIME"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_PRODUCT"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_NAME"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_STANDARD"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_CNT"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_REMARK"))));
            
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CONSTRUCTION_SITE"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CONSTRUCTION_SIDO"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CONSTRUCTION_ADDR"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CONSTRUCTION_COMPANY"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPERVISION_COMPANY"))));

            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_COMPANY"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_USER"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_BS_NUMBER"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_ADDR"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_TELL_NUMBER"))));
            
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CNSTR_NM"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CNSTR_USER"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CNSTR_BIZ_NO"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CNSTR_ADDR"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CNSTR_TEL"))));

            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_QUAL_NUMBER"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_CDATE"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_LOTNUMBER"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_USER"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_COMPANY"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_BS_NUMBER"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_ADDR"))));
            row.createCell(c++).setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_TELL_NUMBER"))));
        }*/
        
        c=0;
        row = sheet.createRow(r++);
        for(int i = 0; i < 26; i++) { 
            cell = row.createCell(c++);
            cell.setCellStyle(styleHeader);
            
            if(i==0) cell.setCellValue("일련번호");
            else if(i==1) cell.setCellValue("건축자재내역");
            else if(i==12) cell.setCellValue("건축공사장");
            else if(i==14) cell.setCellValue("공사감리자");
            else if(i==18) cell.setCellValue("공사시공자");
            else if(i==22) cell.setCellValue("자재유통업자");
            else cell.setCellValue("");
        }
        
        c = 0;
        row = sheet.createRow(r++);
        
        // ####### Set Header.
        List<String> headColNameList = new ArrayList<>();
        //내화구조 정보
        headColNameList.add("");
        
        // #1
        headColNameList.add("품질관리확인서 번호");
        headColNameList.add("작성일자");
        headColNameList.add("인정품목");
        headColNameList.add("품질인정번호");
        headColNameList.add("내화성능시간");
        headColNameList.add("상품명");
        headColNameList.add("구조 또는 제품명");
        headColNameList.add("사용부위");
        headColNameList.add("로트번호");
        headColNameList.add("규격");
        headColNameList.add("수량");  
        
        // #12
        headColNameList.add("현장명");
        headColNameList.add("대지위치 및 지번");
        
        // #14
        headColNameList.add("성명");
        headColNameList.add("사무소명");
        headColNameList.add("주소");
        headColNameList.add("전화번호");

        // #18
        headColNameList.add("성명");
        headColNameList.add("시공회사");
        headColNameList.add("주소");
        headColNameList.add("전화번호");
        
        // #22
        headColNameList.add("확인자");
        headColNameList.add("회사명");
        headColNameList.add("주소");
        headColNameList.add("전화번호");
        
        for(String headColName : headColNameList) {
            cell = row.createCell(c++);
            cell.setCellValue(headColName);
            cell.setCellStyle(styleHeader);
        }
        
        // ####### Set Data.
        for (Map<String, Object> map : list) {
            c = 0;
            row = sheet.createRow(r++);
            
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("RR"))));
            
            String fType = Converter.toStr(map.get("REFRACT_NAME"));
            String usePart = "";
            int idx_l = fType.lastIndexOf("(");
            
            String s1 = "";          
            if(!fType.isEmpty() && (idx_l>0))
            	s1 = idx_l<fType.length() ? fType.substring(0, idx_l) : "";
            
            if(s1.isEmpty())
            	usePart="";
            else if(s1.toUpperCase().contains("BEAM")) 
                usePart="보";
            else if(s1.toUpperCase().contains("COLUMN"))
                usePart="기둥";
            else if(s1.contains("기둥-3"))
                usePart="기둥";
            else 
                usePart="비내력벽"; 
            
            //건축자재내역
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_QUAL_NUMBER"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_CDATE"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_ITEM"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_AUTH_NUMBER"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_TIME"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_PRODUCT"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_NAME"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(usePart);
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_LOTNUMBER"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_STANDARD"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("REFRACT_CNT"))));
            
            //건축공사장
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);            
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CONSTRUCTION_SITE"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CONSTRUCTION_SIDO")) + Converter.toStr(map.get("CONSTRUCTION_ADDR")) ));
            
            //공사감리자
            /*cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_USER"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_COMPANY"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_ADDR"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_TELL_NUMBER"))));*/
            
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(/*Converter.toStr(map.get("SUPPLIER_USER"))*/""));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPERVIS_COMPANY"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPERVIS_ADDR"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPERVIS_TEL"))));
            
            //공사시공자
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CNSTR_USER"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CNSTR_NM"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CNSTR_ADDR"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("CNSTR_TEL"))));
            
            //자재유통업자
            /*cell = row.createCell(c++);
            cell.setCellStyle(styleNormal); 
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_USER"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_COMPANY"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_ADDR"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("MANUFACTURER_TELL_NUMBER"))));*/
            
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal); 
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_USER"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_COMPANY"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_ADDR"))));
            cell = row.createCell(c++);
            cell.setCellStyle(styleNormal);
            cell.setCellValue(HttpUtils.restoreXss(Converter.toStr(map.get("SUPPLIER_TELL_NUMBER"))));
        }
        
        
        // ####### Set width 자동조절.
        /*if(1 < r) {
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
        }*/
        
        sheet.setColumnWidth(0, 4500); // 일련번호
        sheet.setColumnWidth(1, 7000); // 품질관리확인서 번호
        sheet.setColumnWidth(2, 5000); // 작성일자
        sheet.setColumnWidth(3, 5000); // 인정품목
        sheet.setColumnWidth(4, 5000); // 품질인정번호
        sheet.setColumnWidth(5, 4200); // 내화성능시간
        sheet.setColumnWidth(6, 5000); // 상품명
        sheet.setColumnWidth(7, 9000); // 구조 또는 제품명
        sheet.setColumnWidth(8, 4000); // 사용부위
        sheet.setColumnWidth(9, 4000); // 로트번호
        sheet.setColumnWidth(10, 7000); // 규격 
        sheet.setColumnWidth(11, 2000); // 수량
        sheet.setColumnWidth(12, 12000); // 현장명
        sheet.setColumnWidth(13, 12000); // 대지위치및 지번
        sheet.setColumnWidth(14, 3000); // 성명
        sheet.setColumnWidth(15, 7000); // 사무소명
        sheet.setColumnWidth(16, 12000); // 주소
        sheet.setColumnWidth(17, 4000); // 전화번호
        sheet.setColumnWidth(18, 3000); // 성명
        sheet.setColumnWidth(19, 4000); // 시공회사
        sheet.setColumnWidth(20, 12000); // 주소
        sheet.setColumnWidth(21, 4000); // 전화번호
        sheet.setColumnWidth(22, 3000); //확인자
        sheet.setColumnWidth(23, 8000); // 회사명
        sheet.setColumnWidth(24, 12000); // 주소
        sheet.setColumnWidth(25, 4000); // 전화번호
      
        
        sheet.addMergedRegion(new CellRangeAddress(1,1, 1,3));
        
        sheet.addMergedRegion(new CellRangeAddress(2,3, 0,0));
        sheet.addMergedRegion(new CellRangeAddress(2,2, 1,11));
        sheet.addMergedRegion(new CellRangeAddress(2,2, 12,13));
        sheet.addMergedRegion(new CellRangeAddress(2,2, 14,17));
        sheet.addMergedRegion(new CellRangeAddress(2,2, 18,21));
        sheet.addMergedRegion(new CellRangeAddress(2,2, 22,25));
        
        // ####### 엑셀다운로드 종료후 뷰단에서 종료여부 체크중인 쿠키 생성.
        //logger.debug("### 품목현황 엑셀다운로드 종료 ###");
        String fileToken = Converter.toStr(modelMap.get("fileToken"));
        Cookie fileCookie = new Cookie(fileToken, "true");
        fileCookie.setMaxAge(60*1); // 1분만 저장.
        response.addCookie(fileCookie);
        //logger.debug("fileToken : {}", fileToken);
	}
}
