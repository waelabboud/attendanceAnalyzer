package com.attendance.analyzer.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelExtractor {
	
	static final int EXPECTED_HOURS_WORKED = 9;

	public String writeToExcel(Map<String, Double> hoursPerDay, String userId) {

		String sourceFile = "data/time_template.xlsx";
		String outputFile = null;
		try {
			FileInputStream inputStream = new FileInputStream(new File(sourceFile));
			Workbook workbook = WorkbookFactory.create(inputStream);

			Sheet sheet = workbook.getSheetAt(0);

			int rowIdx = 1;

			for (Entry<String, Double> entry : hoursPerDay.entrySet()) {
				Row row = sheet.getRow(rowIdx);
				if (row == null) {
					row = sheet.createRow(rowIdx);
				}
				rowIdx++;

				Cell cellA = row.createCell(0);
				cellA.setCellValue(entry.getKey().replaceAll("\"", "")); 
				
				Cell cellB = row.createCell(1);
				cellB.setCellValue(entry.getValue());

				Cell cellC = row.createCell(2);
				cellC.setCellValue(EXPECTED_HOURS_WORKED);
			}

			// set user id
			Row row1 = sheet.getRow(0);
			Cell userIdCell = row1.createCell(6);
			userIdCell.setCellValue(userId);

			// set report date
			Row row2 = sheet.getRow(1);
			Cell reportDateCell = row2.createCell(6);
			reportDateCell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

			// re-evaluate the functions
			HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

			inputStream.close();

			outputFile = createNewFileName(userId);
			FileOutputStream outputStream = new FileOutputStream(outputFile);
			workbook.write(outputStream);
			workbook.close();
			outputStream.close();

		} catch (IOException | EncryptedDocumentException | IllegalFormatException ex) {
			ex.printStackTrace();
		}

		return outputFile;
	}

	private String createNewFileName(String userId) {
		String reportDate = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
		return "output/worked_hours_" + userId + "_" + reportDate + ".xlsx";
	}

}
