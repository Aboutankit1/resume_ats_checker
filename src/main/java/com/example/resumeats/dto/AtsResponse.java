package com.example.resumeats.dto;

import java.util.List;

public class AtsResponse {

    private double matchScorePercent;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
    private String verdict;
    private List<String> suggestions;

    public AtsResponse() {
    }

    public AtsResponse(double matchScorePercent, List<String> matchedKeywords,
                        List<String> missingKeywords, String verdict, List<String> suggestions) {
        this.matchScorePercent = matchScorePercent;
        this.matchedKeywords = matchedKeywords;
        this.missingKeywords = missingKeywords;
        this.verdict = verdict;
        this.suggestions = suggestions;
    }

    public double getMatchScorePercent() {
        return matchScorePercent;
    }

    public void setMatchScorePercent(double matchScorePercent) {
        this.matchScorePercent = matchScorePercent;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(List<String> matchedKeywords) {
        this.matchedKeywords = matchedKeywords;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }
}
