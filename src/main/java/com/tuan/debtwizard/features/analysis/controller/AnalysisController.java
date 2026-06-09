package com.tuan.debtwizard.features.analysis.controller;

import com.tuan.debtwizard.features.analysis.dto.DtiResponse;
import com.tuan.debtwizard.features.analysis.service.AnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }


    @GetMapping("/dti/{userId}")
    public DtiResponse getCurrentDti(@PathVariable Long userId) {
        return analysisService.calculateCurrentDti(
                userId);}
}
