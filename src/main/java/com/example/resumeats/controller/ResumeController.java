package com.example.resumeats.controller;

import com.example.resumeats.dto.AtsResponse;
import com.example.resumeats.service.HuggingFaceService;
import com.example.resumeats.service.KeywordExtractorService;
import com.example.resumeats.service.ResumeParserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ats")
@CrossOrigin(origins = "*")
public class ResumeController {

    private final ResumeParserService resumeParserService;
    private final HuggingFaceService huggingFaceService;
    private final KeywordExtractorService keywordExtractorService;

    public ResumeController(ResumeParserService resumeParserService,
                             HuggingFaceService huggingFaceService,
                             KeywordExtractorService keywordExtractorService) {
        this.resumeParserService = resumeParserService;
        this.huggingFaceService = huggingFaceService;
        this.keywordExtractorService = keywordExtractorService;
    }

    /**
     * Resume file (pdf/docx/txt) + job description text lekar
     * ATS-style match score, matched/missing keywords aur suggestions return karta hai.
     */
    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public ResponseEntity<AtsResponse> analyze(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam("jobDescription") String jobDescription) {

        try {
            String resumeText = resumeParserService.extractText(resume);

            if (resumeText.isBlank() || jobDescription.isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // 1) Hugging Face model se semantic similarity score
            double similarity = huggingFaceService.computeSimilarity(jobDescription, resumeText);
            double matchPercent = Math.round(similarity * 1000.0) / 10.0; // e.g. 76.4

            // 2) Rule-based keyword gap analysis
            List<String> keywords = keywordExtractorService.extractKeywords(jobDescription, 20);
            List<String> matched = keywordExtractorService.findPresent(keywords, resumeText);
            List<String> missing = keywordExtractorService.findMissing(keywords, resumeText);

            String verdict = buildVerdict(matchPercent);
            List<String> suggestions = buildSuggestions(keywords, missing, matchPercent);

            AtsResponse response = new AtsResponse(matchPercent, matched, missing, verdict, suggestions);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(errorResponse(iae.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(errorResponse(e.getMessage() != null ? e.getMessage() : e.toString()));
        }
    }

    private AtsResponse errorResponse(String message) {
        AtsResponse r = new AtsResponse();
        r.setVerdict("Error: " + message);
        r.setMatchedKeywords(List.of());
        r.setMissingKeywords(List.of());
        r.setSuggestions(List.of());
        return r;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Resume ATS service is up");
    }

    private String buildVerdict(double matchPercent) {
        if (matchPercent >= 80) return "Excellent match — your resume aligns strongly with this job description.";
        if (matchPercent >= 60) return "Good match — a few tweaks could make this even stronger.";
        if (matchPercent >= 40) return "Moderate match — adding some important keywords would help.";
        return "Weak match — consider tailoring your resume more closely to this job description.";
    }

    private List<String> buildSuggestions(List<String> allKeywords, List<String> missing, double matchPercent) {
        if (allKeywords.isEmpty()) {
            return List.of(
                    "The job description was too short for us to extract meaningful keywords. "
                            + "Paste the full job description (responsibilities and requirements) for a more accurate analysis."
            );
        }
        if (missing.isEmpty()) {
            return List.of("Your resume already contains all the major keywords from this job description. Great job!");
        }
        List<String> top = missing.stream().limit(8).collect(Collectors.toList());
        return List.of(
                "Naturally work in these keywords where genuinely applicable: " + String.join(", ", top) + ".",
                "Mirror the language of the job description in your bullet points — both recruiters and ATS systems respond well to this.",
                "Add quantifiable achievements (numbers, percentages, metrics) to make your resume stand out further."
        );
    }
}
