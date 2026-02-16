package com.githubsearch.controller;

import com.githubsearch.client.GitHubClient;
import com.githubsearch.client.LLMClient;
import com.githubsearch.service.RagService;
import com.githubsearch.service.SearchService;
import com.githubsearch.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * RAG Controller
 */
@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = "*")
public class RagController {

    @Autowired
    private RagService ragService;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GitHubClient gitHubClient;

    @Autowired
    private LLMClient llmClient;

    /**
     * Index repository for RAG
     */
    @PostMapping("/index")
    public Result<String> indexRepository(@RequestBody Map<String, Object> request) {
        try {
            String owner = (String) request.get("owner");
            String repo = (String) request.get("repo");
            Long repoId = ((Number) request.get("repoId")).longValue();

            // Get repository files
            List<Map<String, Object>> files = searchService.getFileStructure(owner, repo, "");

            // Collect code files
            List<String> codeChunks = new ArrayList<>();
            collectCodeFiles(owner, repo, "", codeChunks, 50); // Limit to 50 files for prototype

            if (codeChunks.isEmpty()) {
                return Result.error("No code files found");
            }

            // Index code chunks
            ragService.indexCode(repoId, codeChunks);

            return Result.success("Indexed " + codeChunks.size() + " code files");
        } catch (Exception e) {
            return Result.error("Indexing failed: " + e.getMessage());
        }
    }

    /**
     * Ask question about repository
     */
    @PostMapping("/ask")
    public Result<Map<String, Object>> ask(@RequestBody Map<String, Object> request) {
        try {
            String query = (String) request.get("query");
            Long repoId = ((Number) request.get("repoId")).longValue();
            int topK = request.containsKey("topK") ? ((Number) request.get("topK")).intValue() : 5;

            // Check if repository is indexed
            if (!ragService.isIndexed(repoId)) {
                return Result.error("Repository not indexed. Please index it first.");
            }

            // Get relevant chunks
            List<RagService.SearchResult> results = ragService.search(repoId, query, topK);

            // Generate answer
            String answer = ragService.ask(query, repoId, topK);

            Map<String, Object> response = new HashMap<>();
            response.put("answer", answer);
            response.put("sources", results);

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("Question answering failed: " + e.getMessage());
        }
    }

    /**
     * Find similar code
     */
    @PostMapping("/similar")
    public Result<List<RagService.SearchResult>> findSimilar(@RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            int topK = request.containsKey("topK") ? ((Number) request.get("topK")).intValue() : 10;

            List<RagService.SearchResult> results = ragService.findSimilarCode(code, topK);
            return Result.success(results);
        } catch (Exception e) {
            return Result.error("Similar code search failed: " + e.getMessage());
        }
    }

    /**
     * Summarize code
     */
    @PostMapping("/summarize")
    public Result<String> summarizeCode(@RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            String language = (String) request.getOrDefault("language", "text");

            String summary = llmClient.summarizeCode(code, language);
            return Result.success(summary);
        } catch (Exception e) {
            return Result.error("Summarization failed: " + e.getMessage());
        }
    }

    /**
     * Check if repository is indexed
     */
    @GetMapping("/indexed/{repoId}")
    public Result<Boolean> isIndexed(@PathVariable Long repoId) {
        return Result.success(ragService.isIndexed(repoId));
    }

    /**
     * Clear repository index
     */
    @DeleteMapping("/index/{repoId}")
    public Result<String> clearIndex(@PathVariable Long repoId) {
        ragService.clearIndex(repoId);
        return Result.success("Index cleared");
    }

    // Helper method to collect code files recursively
    private void collectCodeFiles(String owner, String repo, String path, List<String> chunks, int limit) throws Exception {
        if (chunks.size() >= limit) return;

        List<Map<String, Object>> files = searchService.getFileStructure(owner, repo, path);

        for (Map<String, Object> file : files) {
            if (chunks.size() >= limit) break;

            String type = (String) file.get("type");
            String name = (String) file.get("name");
            String filePath = (String) file.get("path");

            if ("dir".equals(type)) {
                // Skip common non-code directories
                if (!name.equals("node_modules") && !name.equals(".git") &&
                    !name.equals("dist") && !name.equals("build") &&
                    !name.equals("target") && !name.equals("vendor")) {
                    collectCodeFiles(owner, repo, filePath, chunks, limit);
                }
            } else if ("file".equals(type) && isCodeFile(name)) {
                try {
                    Map<String, Object> content = searchService.getFileContent(owner, repo, filePath);
                    String code = (String) content.get("content");
                    if (code != null && !code.isEmpty()) {
                        // Split into chunks of ~1000 characters
                        int chunkSize = 1000;
                        for (int i = 0; i < code.length(); i += chunkSize) {
                            int end = Math.min(i + chunkSize, code.length());
                            String chunk = "// File: " + filePath + "\n" + code.substring(i, end);
                            chunks.add(chunk);
                        }
                    }
                } catch (Exception e) {
                    // Skip files that can't be read
                }
            }
        }
    }

    private boolean isCodeFile(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        return lower.endsWith(".java") || lower.endsWith(".py") || lower.endsWith(".js") ||
               lower.endsWith(".ts") || lower.endsWith(".go") || lower.endsWith(".rs") ||
               lower.endsWith(".c") || lower.endsWith(".cpp") || lower.endsWith(".h") ||
               lower.endsWith(".cs") || lower.endsWith(".rb") || lower.endsWith(".php") ||
               lower.endsWith(".swift") || lower.endsWith(".kt") || lower.endsWith(".scala") ||
               lower.endsWith(".vue") || lower.endsWith(".jsx") || lower.endsWith(".tsx");
    }
}
