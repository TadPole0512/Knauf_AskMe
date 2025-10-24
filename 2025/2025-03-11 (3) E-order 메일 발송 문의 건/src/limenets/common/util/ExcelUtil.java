package com.limenets.common.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelUtil {
	private Workbook workbook;
	
	public ExcelUtil(Workbook workbook) {
		super();
		this.workbook = workbook;
	}
	
	public Cell setAlign(Cell cell, short align) {
		CellStyle cs = workbook.createCellStyle();
		cs.setAlignment(align);
		// cell.setCellStyle(cs);		//
		return setBorder(cell);
	}
	
	public Cell setAlign(Cell cell) {
		return setAlign(cell, CellStyle.ALIGN_CENTER);
	}
	
	public Cell setBorder(Cell cell) {
		CellStyle cs = workbook.createCellStyle();
		cs.setBorderRight(CellStyle.BORDER_THIN);
		cs.setBorderLeft(CellStyle.BORDER_THIN);
		cs.setBorderTop(CellStyle.BORDER_THIN);
		cs.setBorderBottom(CellStyle.BORDER_THIN);
		// cell.setCellStyle(cs);		//
		return cell;
	}
	
	public Cell setCellPattern(Cell cell, String pattern) {
		CellStyle cs = workbook.createCellStyle();
		DataFormat df = workbook.createDataFormat();
		cs.setDataFormat(df.getFormat(pattern));
		// cs.setAlignment(CellStyle.ALIGN_RIGHT);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		cell.setCellStyle(cs);
		return setBorder(cell);
	}
	
	public Cell setCellPattern(Cell cell) {
		return setCellPattern(cell, "#,##0");
	}
	
	public Object excelChangeType(Cell cell, String pattern) {
		Object res;
		if (cell == null) {
			res = "";
		} else {
			if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				res = cell.getStringCellValue();
			} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				if (DateUtil.isCellDateFormatted(cell)) {
					res = Converter.dateToStr(pattern, cell.getDateCellValue());
				} else {
					res = cell.getNumericCellValue();
				}
			} else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
				res = cell.getBooleanCellValue() ? "1" : "0";
			} else {
				res = cell.getStringCellValue();
			}
		}
		
		return res;
	}

	public Object excelChangeType(Cell cell) {
		return excelChangeType(cell, "yyyy-MM-dd HH:mm");
	}	
}
