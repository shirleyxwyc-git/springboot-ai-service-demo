package com.ai.demo_ai_service.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.web.multipart.MultipartFile;

public class FileExtractUtil {
   public static String extractText(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename().toLowerCase();

        try (InputStream is = file.getInputStream()) {
            if (fileName.endsWith(".pdf")) {
                return extractPdf(is);
            } 
            // 👇 支援 Word .docx
            else if (fileName.endsWith(".docx")) {
                return extractDocx(is);
            } 
            // 👇 新！支援 .txt 檔！
            else if (fileName.endsWith(".txt")) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            } 
            else {
                return "不支持的文件格式：" + fileName;
            }
        }
    }

    private static String extractPdf(InputStream is) throws Exception {
        try (PDDocument document = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true); // 排版更整齊
            return stripper.getText(document);
        }
    }

    private static String extractDocx(InputStream is) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                sb.append(p.getText()).append("\n");
            }
            return sb.toString();
        }
    }
}
