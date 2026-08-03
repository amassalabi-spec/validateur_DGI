package com.audit.dgi.validateur_dgi.service.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

@Service
public class PoiExtractionService {

    /**
     * Extracts a cleaned plain-text from given MultipartFile. Supports .docx, .xlsx and pdf.
     */
    public String extractText(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) filename = "unknown";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".docx")) {
            return extractFromDocx(file.getInputStream());
        } else if (lower.endsWith(".xlsx")) {
            return extractFromXlsx(file.getInputStream());
        } else if (lower.endsWith(".pdf")) {
            return extractFromPdf(file.getInputStream());
        } else {
            // fallback: read as text
            try (InputStream in = file.getInputStream()) {
                return new String(in.readAllBytes()).replaceAll("\r\n", " ").replaceAll("\n", " ").trim();
            }
        }
    }

    private String extractFromDocx(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(in)) {
            for (XWPFParagraph p : doc.getParagraphs()) {
                String txt = p.getText();
                if (txt != null && !txt.isBlank()) {
                    sb.append(txt.trim()).append("\n");
                }
            }
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String cellText = cell.getText();
                        if (cellText != null && !cellText.isBlank()) {
                            sb.append(cellText.trim()).append(" | ");
                        }
                    }
                    sb.append("\n");
                }
            }
        }
        return cleanText(sb.toString());
    }

    private String extractFromXlsx(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (XSSFWorkbook wb = new XSSFWorkbook(in)) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            int numberOfSheets = wb.getNumberOfSheets();
            for (int s = 0; s < numberOfSheets; s++) {
                XSSFSheet sheet = wb.getSheetAt(s);
                sb.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                for (int r = sheet.getFirstRowNum(); r <= sheet.getLastRowNum(); r++) {
                    XSSFRow row = sheet.getRow(r);
                    if (row == null) continue;
                    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                        XSSFCell cell = row.getCell(c);
                        if (cell == null) continue;
                        switch (cell.getCellType()) {
                            case NUMERIC -> sb.append(BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString());
                            case STRING -> sb.append(cell.getStringCellValue());
                            case BOOLEAN -> sb.append(cell.getBooleanCellValue());
                            case FORMULA -> sb.append(cell.getCellFormula());
                            case BLANK -> sb.append("");
                            default -> sb.append(cell.toString());
                        }
                        sb.append("\t");
                    }
                    sb.append("\n");
                }
                sb.append("\n");
            }
        }
        return cleanText(sb.toString());
    }

    private String extractFromPdf(InputStream in) throws Exception {
        try (PDDocument doc = Loader.loadPDF(in.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return cleanText(stripper.getText(doc));
        }
    }

    private String cleanText(String raw) {
        if (raw == null) return "";
        // normalize whitespace and remove control characters
        return raw.replaceAll("\\u00A0", " ").replaceAll("\\t", " ")
                .replaceAll("\r\n", "\n").replaceAll("\\p{Cntrl}", "").trim();
    }
}

