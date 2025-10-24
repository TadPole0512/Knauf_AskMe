package com.limenets.eorder.excel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.limenets.common.util.Converter;
import com.limenets.common.util.ExcelUtil;
import com.limenets.common.util.HttpUtils;

public class factReportExcel extends AbstractXlsxView {
	private static final Logger logger = LoggerFactory.getLogger(factReportExcel.class);
	
	@Override
	protected void buildExcelDocument(Map<String, Object> modelMap, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filename = HttpUtils.uriEncoding("거래사실확인서.xlsx", StandardCharsets.UTF_8);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
		
		@SuppressWarnings("unchecked")
		Map<String, Object> supplier = (Map<String, Object>) modelMap.get("supplier"); //공급자,공급받는자
		List<Map<String, Object>> salesOrderList = (List<Map<String, Object>>)modelMap.get("salesOrderList"); //거래내역 리스트
		Map<String, Object> sumPrice = (Map<String, Object>) modelMap.get("sumPrice"); //합계금액
		long failPrice = Converter.toLong(modelMap.get("failPrice")); //미도래금액
		
		Sheet sheet = workbook.createSheet("거래사실확인서");
		ExcelUtil eu = new ExcelUtil(workbook);
		
		for(int i=0; i<39; i++) {
			sheet.setColumnWidth(i, 600);
		}
		sheet.setDefaultRowHeight((short) 330);
		
		// 셀 합치기(행,행,열,열)
		sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 37));	//거래사실확인서
		
		sheet.addMergedRegion(new CellRangeAddress(2, 4, 0, 0));	//공급자
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 4));	//등록번호
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 5, 18));	//등록번호 DATA
		sheet.addMergedRegion(new CellRangeAddress(2, 4, 19, 19));	//공급받는자
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 20, 23));	//등록번호
		sheet.addMergedRegion(new CellRangeAddress(2, 2, 24, 37));	//등록번호 DATA
		
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 1, 4));	//상호(법인명)
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 5, 10));	//상호 DATA
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 11, 14));	//대표자
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 15, 18));	//대표자 DATA
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 20, 23));	//상호(법인명)
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 24, 29));	//상호 DATA
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 30, 33));	//대표자
		sheet.addMergedRegion(new CellRangeAddress(3, 3, 34, 37));	//대표자 DATA
		
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 4));	//주소
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 5, 18));	//주소 DATA
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 20, 23));	//주소
		sheet.addMergedRegion(new CellRangeAddress(4, 4, 24, 37));	//주소 DATA
		
		sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 4));	//조회시작일
		sheet.addMergedRegion(new CellRangeAddress(5, 5, 5, 10));	//조회종료일
		sheet.addMergedRegion(new CellRangeAddress(5, 5, 11, 18));	//공급가액
		sheet.addMergedRegion(new CellRangeAddress(5, 5, 19, 27));	//세액
		sheet.addMergedRegion(new CellRangeAddress(5, 5, 28, 37));	//합계
		
		sheet.addMergedRegion(new CellRangeAddress(6, 6, 0, 4));	//조회시작일 DATA
		sheet.addMergedRegion(new CellRangeAddress(6, 6, 5, 10));	//조회종료일 DATA
		sheet.addMergedRegion(new CellRangeAddress(6, 6, 11, 18));	//공급가액 DATA
		sheet.addMergedRegion(new CellRangeAddress(6, 6, 19, 27));	//세액 DATA
		sheet.addMergedRegion(new CellRangeAddress(6, 6, 28, 37));	//합계 DATA
		
		// 7포인트
		Font font7 = workbook.createFont();
		font7.setFontHeightInPoints((short)7);
		font7.setFontName("맑은 고딕");
		
		// 8포인트
		Font font8 = workbook.createFont();
		font8.setFontHeightInPoints((short)8);
		font8.setFontName("맑은 고딕");
		
		// 9포인트
		Font font9 = workbook.createFont();
		font9.setFontHeightInPoints((short)9);
		font9.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font9.setFontName("맑은 고딕");
		
		// 19포인트
		Font font19 = workbook.createFont();
		font19.setFontHeightInPoints((short)19);
		font19.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font19.setFontName("맑은 고딕");
		
		// 타이틀
		CellStyle title = workbook.createCellStyle();
		title.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		title.setAlignment(CellStyle.ALIGN_CENTER);
		title.setWrapText(true);
		title.setBorderTop((short)1);
		title.setBorderBottom((short)1);
		title.setBorderLeft((short)1);
		title.setBorderRight((short)1);
		title.setFont(font19);
		
		// 컬럼명 개행문자
		CellStyle verticalTxt = workbook.createCellStyle();
		verticalTxt.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		verticalTxt.setAlignment(CellStyle.ALIGN_CENTER);
		verticalTxt.setWrapText(true);
		verticalTxt.setBorderTop((short)1);
		verticalTxt.setBorderBottom((short)1);
		verticalTxt.setBorderLeft((short)1);
		verticalTxt.setBorderRight((short)1);
		verticalTxt.setFont(font9);
		
		// 컬럼명
		CellStyle subTitle = workbook.createCellStyle();
		subTitle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		subTitle.setAlignment(CellStyle.ALIGN_CENTER);
		subTitle.setWrapText(true);
		subTitle.setBorderTop((short)1);
		subTitle.setBorderBottom((short)1);
		subTitle.setBorderLeft((short)1);
		subTitle.setBorderRight((short)1);
		subTitle.setFont(font9);
		
		// data
		CellStyle normal = workbook.createCellStyle();
		normal.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		normal.setAlignment(CellStyle.ALIGN_CENTER);
		normal.setWrapText(true);
		normal.setBorderTop((short)1);
		normal.setBorderBottom((short)1);
		normal.setBorderLeft((short)1);
		normal.setBorderRight((short)1);
		normal.setFont(font8);
		
		// 품목리스트
		CellStyle listStyle = workbook.createCellStyle();
		listStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		listStyle.setAlignment(CellStyle.ALIGN_CENTER);
		listStyle.setWrapText(true);
		listStyle.setBorderTop((short)1);
		listStyle.setBorderBottom((short)1);
		listStyle.setBorderLeft((short)1);
		listStyle.setBorderRight((short)1);
		listStyle.setFont(font7);
		
		// 오른쪽정렬
		CellStyle alignRight = workbook.createCellStyle();
		alignRight.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		alignRight.setAlignment(CellStyle.ALIGN_RIGHT);
		alignRight.setWrapText(true);
		alignRight.setBorderTop((short)1);
		alignRight.setBorderBottom((short)1);
		alignRight.setBorderLeft((short)1);
		alignRight.setBorderRight((short)1);
		alignRight.setFont(font7);
		
		// 왼쪽정렬
		CellStyle alignLeft = workbook.createCellStyle();
		alignLeft.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		alignLeft.setAlignment(CellStyle.ALIGN_LEFT);
		alignLeft.setWrapText(true);
		alignLeft.setBorderTop((short)1);
		alignLeft.setBorderBottom((short)1);
		alignLeft.setBorderLeft((short)1);
		alignLeft.setBorderRight((short)1);
		alignLeft.setFont(font7);
		
		// 테두리
		CellStyle border = workbook.createCellStyle();
	    border.setWrapText(true);
	    border.setBorderTop((short)1);
	    border.setBorderBottom((short)1);
	    border.setBorderLeft((short)1);
	    border.setBorderRight((short)1);
	    border.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		
		Row row1 = sheet.createRow(1);
		row1.setHeight((short)800);
		for (int j=0; j<38; j++) {
			row1.createCell(j);
			row1.getCell(j).setCellStyle(border);
		}
		
		row1.getCell(0).setCellStyle(title);
		row1.getCell(0).setCellValue("거 래 사 실 확 인 서");
		
		Row row2 = sheet.createRow(2);
		row2.setHeight((short)330);
		for (int j=0; j<38; j++) {
			row2.createCell(j);
			row2.getCell(j).setCellStyle(border);
		}
		
		row2.getCell(0).setCellStyle(verticalTxt);
		row2.getCell(0).setCellValue("공급자");
		
		row2.getCell(1).setCellStyle(subTitle);
		row2.getCell(1).setCellValue("등록번호");
		
		row2.getCell(5).setCellStyle(normal);
		row2.getCell(5).setCellValue(Converter.toStr(supplier.get("ABTAX")));
		
		row2.getCell(19).setCellStyle(verticalTxt);
		row2.getCell(19).setCellValue("공급받는자");
		
		row2.getCell(20).setCellStyle(subTitle);
		row2.getCell(20).setCellValue("등록번호");
		
		row2.getCell(24).setCellStyle(normal);
		row2.getCell(24).setCellValue(Converter.toStr(supplier.get("CUST_REG")));
		
		
		Row row3 = sheet.createRow(3);
		row3.setHeight((short)640);
		for (int j=0; j<38; j++) {
			row3.createCell(j);
			row3.getCell(j).setCellStyle(border);
		}
		
		row3.getCell(1).setCellStyle(verticalTxt);
		row3.getCell(1).setCellValue("상호\n(법인명)");
		
		row3.getCell(5).setCellStyle(normal);
		row3.getCell(5).setCellValue(Converter.toStr(supplier.get("ABALPH")));
		
		row3.getCell(11).setCellStyle(subTitle);
		row3.getCell(11).setCellValue("대표자");
		
		row3.getCell(15).setCellStyle(normal);
		row3.getCell(15).setCellValue(Converter.toStr(supplier.get("WWMLNM")));
		
		row3.getCell(20).setCellStyle(subTitle);
		row3.getCell(20).setCellValue("상호\n(법인명)");
		
		row3.getCell(24).setCellStyle(normal);
		row3.getCell(24).setCellValue(Converter.toStr(supplier.get("CUST_NM")));
		
		row3.getCell(30).setCellStyle(subTitle);
		row3.getCell(30).setCellValue("대표자");
		
		row3.getCell(34).setCellStyle(normal);
		row3.getCell(34).setCellValue(Converter.toStr(supplier.get("CUST_CEO")));
		
		Row row4 = sheet.createRow(4);
		row4.setHeight((short)330);
		for (int j=0; j<38; j++) {
			row4.createCell(j);
			row4.getCell(j).setCellStyle(border);
		}
		
		row4.getCell(1).setCellStyle(subTitle);
		row4.getCell(1).setCellValue("주소");	
		
		row4.getCell(5).setCellStyle(normal);
		row4.getCell(5).setCellValue(Converter.toStr(supplier.get("ALADD1")));
		
		row4.getCell(20).setCellStyle(subTitle);
		row4.getCell(20).setCellValue("주소");	
		
		row4.getCell(24).setCellStyle(normal);
		row4.getCell(24).setCellValue(Converter.toStr(supplier.get("CUST_ADDR")));
		
		
		Row row5 = sheet.createRow(5);
		row5.setHeight((short)330);
		for (int j=0; j<38; j++) {
			row5.createCell(j);
			row5.getCell(j).setCellStyle(subTitle);
		}
		row5.getCell(0).setCellValue("조회시작일");	
		row5.getCell(5).setCellValue("조회종료일");	
		row5.getCell(11).setCellValue("공급가액");	
		row5.getCell(19).setCellValue("세액");	
		row5.getCell(28).setCellValue("합계");	
		
		Row row6 = sheet.createRow(6);
		row6.setHeight((short)330);
		for (int j=0; j<38; j++) {
			row6.createCell(j);
			row6.getCell(j).setCellStyle(subTitle);
		}
		row6.getCell(0).setCellValue("");	
		row6.getCell(5).setCellValue("");	
			
		
		// 납품처갯수
		String preShiptoCd = "";
		String shiptoCd = "";
		for(int a=0; a<salesOrderList.size(); a++) {
			if("".equals(preShiptoCd)) {
				preShiptoCd = Converter.toStr(salesOrderList.get(a).get("SDSHAN")); 
				shiptoCd += preShiptoCd+","+Converter.toStr(salesOrderList.get(a).get("ABALPH"))+"|";
			}
			
			if(!preShiptoCd.equals(Converter.toStr(salesOrderList.get(a).get("SDSHAN")))) {
				preShiptoCd = Converter.toStr(salesOrderList.get(a).get("SDSHAN"));
				shiptoCd += preShiptoCd+","+ Converter.toStr(salesOrderList.get(a).get("ABALPH"))+"|";
			}
		}
		
		String[] shiptoArr = shiptoCd.split("\\|", -1);
		
		Row row;
		int r = 7;
		
		long price = 0L;
		long vatPrice = 0L;
		long totalPrice = 0L;
		
		// ####### Set Data.
		for (String shipto : shiptoArr) {
			if(!"".equals(shipto)) {
				
				String[] shiptoInfo = shipto.split(",");
				String shiptoCode = shiptoInfo[0]; //납품처코드
				String shiptoName = shiptoInfo[1]; //납품처명
				
				row = sheet.createRow(r);
				row.setHeight((short)330);
				
				sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 3));	//납품처명
				sheet.addMergedRegion(new CellRangeAddress(r, r, 4, 37));	//납품처명 DATA
				r++;
				
				for (int j=0; j<38; j++) {
					row.createCell(j);
					row.getCell(j).setCellStyle(subTitle);
				}
				row.getCell(0).setCellValue("납품처명");
				
				row.getCell(4).setCellStyle(alignLeft);
				row.getCell(4).setCellValue(shiptoName);
				
				row = sheet.createRow(r);
				sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 2));	//출고일자
				sheet.addMergedRegion(new CellRangeAddress(r, r, 3, 4));	//구분
				sheet.addMergedRegion(new CellRangeAddress(r, r, 5, 8));	//수주번호
				sheet.addMergedRegion(new CellRangeAddress(r, r, 9, 16));	//품목명
				sheet.addMergedRegion(new CellRangeAddress(r, r, 17, 19));	//수량
				sheet.addMergedRegion(new CellRangeAddress(r, r, 20, 21));	//단위
				sheet.addMergedRegion(new CellRangeAddress(r, r, 22, 24));	//단가
				sheet.addMergedRegion(new CellRangeAddress(r, r, 25, 28));	//금액
				sheet.addMergedRegion(new CellRangeAddress(r, r, 29, 31));	//출하지
				sheet.addMergedRegion(new CellRangeAddress(r, r, 32, 37));	//도착지
				r++;
				
				for (int j=0; j<38; j++) {
					row.createCell(j);
					row.getCell(j).setCellStyle(subTitle);
				}
				
				row.getCell(0).setCellValue("출고일자");
				row.getCell(3).setCellValue("구분");
				row.getCell(5).setCellValue("수주번호");
				row.getCell(9).setCellValue("품목명");
				row.getCell(17).setCellValue("수량");
				row.getCell(20).setCellValue("단위");
				row.getCell(22).setCellValue("단가");
				row.getCell(25).setCellValue("금액");
				row.getCell(29).setCellValue("출하지");
				row.getCell(32).setCellValue("도착지");
				
				
				long sumSduorg = 0L;  //수량합계
				long sumSdaexp = 0L;  //금액합계
				
				for (Map<String, Object> order : salesOrderList) {
					if(shiptoCode.equals(Converter.toStr(order.get("SDSHAN")))) {
						String sddcto = Converter.toStr(order.get("SDDCTO")); //구분
						
						row = sheet.createRow(r);
						row.setHeight((short)400);
						
						sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 2));	//출고일자
						sheet.addMergedRegion(new CellRangeAddress(r, r, 3, 4));	//구분
						sheet.addMergedRegion(new CellRangeAddress(r, r, 5, 8));	//수주번호
						sheet.addMergedRegion(new CellRangeAddress(r, r, 9, 16));	//품목명
						sheet.addMergedRegion(new CellRangeAddress(r, r, 17, 19));	//수량
						sheet.addMergedRegion(new CellRangeAddress(r, r, 20, 21));	//단위
						sheet.addMergedRegion(new CellRangeAddress(r, r, 22, 24));	//단가
						sheet.addMergedRegion(new CellRangeAddress(r, r, 25, 28));	//금액
						sheet.addMergedRegion(new CellRangeAddress(r, r, 29, 31));	//출하지
						sheet.addMergedRegion(new CellRangeAddress(r, r, 32, 37));	//도착지
						r++;
						
						for (int j=0; j<38; j++) {
							row.createCell(j);
							row.getCell(j).setCellStyle(listStyle);
						}
						
						//출고일자
						String SDADDJ = Converter.toStr(order.get("SDADDJ"));
						row.getCell(0).setCellValue(SDADDJ.substring(4, 6)+"/"+SDADDJ.substring(6, 8)); 
						
						//구분
						row.getCell(3).setCellValue(sddcto); 							   
						
						//수주번호
						row.getCell(5).setCellValue(Converter.toStr(order.get("SDDOCO"))); 
						
						//품목명
						row.getCell(9).setCellStyle(alignLeft);
						row.getCell(9).setCellValue(Converter.toStr(order.get("SDDSC1"))); 
						
						//수량
						long sduorg = 0L;
						if(!"CA".equals(sddcto) && !"CO".equals(sddcto) && !"CR".equals(sddcto)) {
							sduorg = Converter.toLong(order.get("SDUORG"));
						}
						row.getCell(17).setCellStyle(alignRight);
						row.getCell(17).setCellValue(sduorg); 
						
						//단위
						row.getCell(20).setCellStyle(listStyle);
						row.getCell(20).setCellValue(Converter.toStr(order.get("SDUOM")));  
						
						//단가
						row.getCell(22).setCellStyle(alignRight);
						row.getCell(22).setCellValue(Converter.toStr(order.get("SDUPRC"))); 
						
						 //금액
						long sdaexp = Converter.toLong(order.get("SDAEXP"));
						row.getCell(25).setCellStyle(alignRight);
						row.getCell(25).setCellValue(sdaexp);
						
						//출하지
						row.getCell(29).setCellStyle(listStyle);
						row.getCell(29).setCellValue(Converter.toStr(order.get("MCU_NAME"))); 
						
						//도착지
						row.getCell(32).setCellStyle(alignLeft);
						row.getCell(32).setCellValue(Converter.toStr(order.get("OAADD1"))); 
						
						sumSduorg += sduorg; //수량합계
						sumSdaexp += sdaexp;
					}
				}
						
				row = sheet.createRow(r);
				sheet.addMergedRegion(new CellRangeAddress(r, r, 0, 16));	//소계
				sheet.addMergedRegion(new CellRangeAddress(r, r, 17, 19));  //수량합계
				sheet.addMergedRegion(new CellRangeAddress(r, r, 20, 21));
				sheet.addMergedRegion(new CellRangeAddress(r, r, 22, 24));
				sheet.addMergedRegion(new CellRangeAddress(r, r, 25, 28));  //금액합계
				sheet.addMergedRegion(new CellRangeAddress(r, r, 29, 31));
				sheet.addMergedRegion(new CellRangeAddress(r, r, 32, 37));
				r++;
				
				for (int j=0; j<38; j++) {
					row.createCell(j);
					row.getCell(j).setCellStyle(subTitle);
				}
				
				row.getCell(0).setCellValue("소계");
				
				//수량합계
				row.getCell(17).setCellStyle(alignRight);
				row.getCell(17).setCellValue(sumSduorg);
				
				//금액합계
				row.getCell(25).setCellStyle(alignRight);
				row.getCell(25).setCellValue(sumSdaexp);
				
				price += sumSdaexp;
			}
		}
		
		//공급가액
		row6.getCell(11).setCellStyle(alignRight);
		row6.getCell(11).setCellValue(price);	
		
		//세액
		vatPrice = (long) (price * 0.1);
		row6.getCell(19).setCellStyle(alignRight);
		row6.getCell(19).setCellValue(vatPrice);
		
		//총액
		totalPrice = price + vatPrice;
		row6.getCell(28).setCellValue(totalPrice);
		
		
		row = sheet.createRow(r);
		sheet.addMergedRegion(new CellRangeAddress(r, r+1, 0, 1)); //채권현황
		sheet.addMergedRegion(new CellRangeAddress(r, r, 2, 7));   //전월채권
		sheet.addMergedRegion(new CellRangeAddress(r, r, 8, 13));  //당월매출
		sheet.addMergedRegion(new CellRangeAddress(r, r, 14, 19)); //현금수금
		sheet.addMergedRegion(new CellRangeAddress(r, r, 20, 25)); //어음수금
		sheet.addMergedRegion(new CellRangeAddress(r, r, 26, 31)); //당원채권
		sheet.addMergedRegion(new CellRangeAddress(r, r, 32, 37)); //미도래어음
		r++;
		
		for (int j=0; j<38; j++) {
			row.createCell(j);
			row.getCell(j).setCellStyle(listStyle);
		}
		
		row.getCell(0).setCellValue("채권\n현황");
		row.getCell(2).setCellValue("전월채권");
		row.getCell(8).setCellValue("당월매출");
		row.getCell(14).setCellValue("현금수금");
		row.getCell(20).setCellValue("어음수금");
		row.getCell(26).setCellValue("당원채권");
		row.getCell(32).setCellValue("미도래어음");
		
		
		row = sheet.createRow(r);
		sheet.addMergedRegion(new CellRangeAddress(r, r, 2, 7));  
		sheet.addMergedRegion(new CellRangeAddress(r, r, 8, 13));
		sheet.addMergedRegion(new CellRangeAddress(r, r, 14, 19));
		sheet.addMergedRegion(new CellRangeAddress(r, r, 20, 25));
		sheet.addMergedRegion(new CellRangeAddress(r, r, 26, 31));
		sheet.addMergedRegion(new CellRangeAddress(r, r, 32, 37));
		r++;
		
		for (int j=0; j<38; j++) {
			row.createCell(j);
			row.getCell(j).setCellStyle(listStyle);
		}
		
		long OPEN_AMOUNT = 0L;
		long GROSS_AMOUNT = 0L;
		long RECEIVED_CASH = 0L;
		long RECEIVED_NOTE = 0L;
		long CURRENT_AMOUNT = 0L;
		
		if(!CollectionUtils.isEmpty(sumPrice)) {
			OPEN_AMOUNT = Converter.toLong(sumPrice.get("OPEN_AMOUNT"));
			GROSS_AMOUNT = Converter.toLong(sumPrice.get("OPEN_AMOUNT"));
			RECEIVED_CASH = Converter.toLong(sumPrice.get("RECEIVED_CASH"));
			RECEIVED_NOTE = Converter.toLong(sumPrice.get("RECEIVED_NOTE"));
			CURRENT_AMOUNT = Converter.toLong(sumPrice.get("CURRENT_AMOUNT"));
		}
		
		row.getCell(2).setCellStyle(normal);
		row.getCell(2).setCellValue(OPEN_AMOUNT); //전월채권
		
		row.getCell(8).setCellStyle(normal);
		row.getCell(8).setCellValue(GROSS_AMOUNT); //당월매출
		
		row.getCell(14).setCellStyle(normal);
		row.getCell(14).setCellValue(RECEIVED_CASH); //현금수금
		
		row.getCell(20).setCellStyle(normal);
		row.getCell(20).setCellValue(RECEIVED_NOTE); //어음수금
		
		row.getCell(26).setCellStyle(normal);
		row.getCell(26).setCellValue(CURRENT_AMOUNT); //당월채권
		
		row.getCell(32).setCellStyle(normal);
		row.getCell(32).setCellValue(failPrice); //미도래어음
		
		
		/*
		// ####### 엑셀다운로드 종료후 뷰단에서 종료여부 체크중인 쿠키 생성.
		//logger.debug("### 품목현황 엑셀다운로드 종료 ###");
		String fileToken = Converter.toStr(modelMap.get("fileToken"));
		Cookie fileCookie = new Cookie(fileToken, "true");
		fileCookie.setMaxAge(60*1); // 1분만 저장.
		response.addCookie(fileCookie);
		//logger.debug("fileToken : {}", fileToken);
		*/
	}
}
