package com.example.resumeats.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Job description se important keywords (skills, tech terms) nikaalta hai
 * aur resume text ke against check karta hai ki kaunse present/missing hain.
 * Yeh rule-based hai (fast + free) — production me isse spaCy/NER ya
 * ek chhota Hugging Face NER model (e.g. "dslim/bert-base-NER") se replace kiya ja sakta hai.
 */
@Service
public class KeywordExtractorService {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "but", "for", "with", "to", "of", "in", "on", "at",
            "is", "are", "was", "were", "be", "been", "being", "this", "that", "these", "those",
            "you", "your", "we", "our", "will", "should", "must", "can", "as", "by", "from",
            "about", "into", "than", "then", "so", "such", "role", "work", "working", "years",
            "year", "experience", "team", "job", "including", "etc", "using", "strong", "good",
            "ability", "skills", "skill", "candidate", "candidates", "responsibilities",
            "requirements", "preferred", "plus", "across", "within", "have", "has"
    );

    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9+#./-]{1,}");

    public List<String> extractKeywords(String jobDescription, int topN) {
        Map<String, Integer> freq = new LinkedHashMap<>();
        var matcher = WORD_PATTERN.matcher(jobDescription);

        while (matcher.find()) {
            String word = matcher.group().toLowerCase();
            if (word.length() < 3 || STOP_WORDS.contains(word)) continue;
            freq.merge(word, 1, Integer::sum);
        }

        return freq.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<String> findPresent(List<String> keywords, String resumeText) {
        String lowerResume = resumeText.toLowerCase();
        return keywords.stream()
                .filter(k -> lowerResume.contains(k.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<String> findMissing(List<String> keywords, String resumeText) {
        String lowerResume = resumeText.toLowerCase();
        return keywords.stream()
                .filter(k -> !lowerResume.contains(k.toLowerCase()))
                .collect(Collectors.toList());
    }
}
