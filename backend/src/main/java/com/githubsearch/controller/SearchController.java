package com.githubsearch.controller;

import com.githubsearch.service.SearchService;
import com.githubsearch.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Search Controller
 */
@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * Search repositories
     */
    @GetMapping("/repositories")
    public Result<Map<String, Object>> searchRepositories(
            @RequestParam String query,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        try {
            Map<String, Object> result = searchService.searchRepositories(query, language, page, perPage);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("Search failed: " + e.getMessage());
        }
    }

    /**
     * Get repository details
     */
    @GetMapping("/repository/{owner}/{repo}")
    public Result<Map<String, Object>> getRepository(
            @PathVariable String owner,
            @PathVariable String repo) {
        try {
            Map<String, Object> result = searchService.getRepositoryDetails(owner, repo);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("Failed to get repository: " + e.getMessage());
        }
    }

    /**
     * Get repository file structure
     */
    @GetMapping("/repository/{owner}/{repo}/files")
    public Result<List<Map<String, Object>>> getFileStructure(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "") String path) {
        try {
            List<Map<String, Object>> result = searchService.getFileStructure(owner, repo, path);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("Failed to get file structure: " + e.getMessage());
        }
    }

    /**
     * Get file content
     */
    @GetMapping("/repository/{owner}/{repo}/file")
    public Result<Map<String, Object>> getFileContent(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam String path) {
        try {
            Map<String, Object> result = searchService.getFileContent(owner, repo, path);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("Failed to get file content: " + e.getMessage());
        }
    }

    /**
     * Generate repository summary
     */
    @GetMapping("/repository/{owner}/{repo}/summary")
    public Result<String> generateSummary(
            @PathVariable String owner,
            @PathVariable String repo) {
        try {
            String summary = searchService.generateSummary(owner, repo);
            return Result.success(summary);
        } catch (Exception e) {
            return Result.error("Failed to generate summary: " + e.getMessage());
        }
    }

    /**
     * Search code
     */
    @GetMapping("/code")
    public Result<Map<String, Object>> searchCode(
            @RequestParam String query,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage) {
        try {
            Map<String, Object> result = searchService.searchCode(query, language, page, perPage);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("Code search failed: " + e.getMessage());
        }
    }
}
