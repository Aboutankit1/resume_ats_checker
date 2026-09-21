package com.example.resumeats.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


@Service
public class ResumeParserService {

    public String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        if (filename.endsWith(".pdf")) {
            return extractFromPdf(file);
        } else if (filename.endsWith(".docx")) {
            return extractFromDocx(file);
        } else if (filename.endsWith(".txt")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + filename + ". Only .pdf, .docx and .txt are allowed.");
        }
    }

    private String extractFromPdf(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream(); PDDocument document = Loader.loadPDF(is.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractFromDocx(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}
