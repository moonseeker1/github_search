package com.githubsearch.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.githubsearch.client.GitHubClient;
import com.githubsearch.client.LLMClient;
import com.githubsearch.entity.Repository;
import com.githubsearch.mapper.RepositoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search Service
 */
@Service
public class SearchService {

    @Autowired
    private GitHubClient gitHubClient;

    @Autowired
    private LLMClient llmClient;

    @Autowired
    private RepositoryMapper repositoryMapper;

    /**
     * Search repositories from GitHub
     */
    public Map<String, Object> searchRepositories(String query, String language, int page, int perPage) throws IOException {
        JSONObject result = gitHubClient.searchRepositories(query, language, page, perPage);

        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getInteger("total_count"));
        response.put("incompleteResults", result.getBoolean("incomplete_results"));

        List<Map<String, Object>> items = new ArrayList<>();
        JSONArray itemsArray = result.getJSONArray("items");

        if (itemsArray != null) {
            for (int i = 0; i < itemsArray.size(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                Map<String, Object> repo = new HashMap<>();
                repo.put("id", item.getLong("id"));
                repo.put("name", item.getString("name"));
                repo.put("fullName", item.getString("full_name"));
                repo.put("owner", item.getJSONObject("owner").getString("login"));
                repo.put("description", item.getString("description"));
                repo.put("language", item.getString("language"));
                repo.put("stars", item.getInteger("stargazers_count"));
                repo.put("forks", item.getInteger("forks_count"));
                repo.put("url", item.getString("html_url"));
                repo.put("avatar", item.getJSONObject("owner").getString("avatar_url"));
                items.add(repo);
            }
        }

        response.put("items", items);
        return response;
    }

    /**
     * Get repository details
     */
    public Map<String, Object> getRepositoryDetails(String owner, String repo) throws IOException {
        JSONObject result = gitHubClient.getRepository(owner, repo);

        Map<String, Object> response = new HashMap<>();
        response.put("id", result.getLong("id"));
        response.put("name", result.getString("name"));
        response.put("fullName", result.getString("full_name"));
        response.put("owner", result.getJSONObject("owner").getString("login"));
        response.put("description", result.getString("description"));
        response.put("language", result.getString("language"));
        response.put("stars", result.getInteger("stargazers_count"));
        response.put("forks", result.getInteger("forks_count"));
        response.put("url", result.getString("html_url"));
        response.put("cloneUrl", result.getString("clone_url"));
        response.put("homepage", result.getString("homepage"));
        response.put("topics", result.getJSONArray("topics"));
        response.put("avatar", result.getJSONObject("owner").getString("avatar_url"));

        // Parse dates
        String createdAt = result.getString("created_at");
        String updatedAt = result.getString("updated_at");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        if (createdAt != null) {
            response.put("createdAt", LocalDateTime.parse(createdAt, formatter));
        }
        if (updatedAt != null) {
            response.put("updatedAt", LocalDateTime.parse(updatedAt, formatter));
        }

        // Get README
        try {
            String readme = gitHubClient.getReadme(owner, repo);
            response.put("readme", readme);
        } catch (Exception e) {
            response.put("readme", null);
        }

        return response;
    }

    /**
     * Get repository file structure
     */
    public List<Map<String, Object>> getFileStructure(String owner, String repo, String path) throws IOException {
        JSONArray contents = gitHubClient.getContents(owner, repo, path);

        List<Map<String, Object>> files = new ArrayList<>();
        if (contents != null) {
            for (int i = 0; i < contents.size(); i++) {
                JSONObject item = contents.getJSONObject(i);
                Map<String, Object> file = new HashMap<>();
                file.put("name", item.getString("name"));
                file.put("path", item.getString("path"));
                file.put("type", item.getString("type")); // "file" or "dir"
                file.put("size", item.getInteger("size"));
                file.put("sha", item.getString("sha"));
                files.add(file);
            }
        }

        return files;
    }

    /**
     * Get file content
     */
    public Map<String, Object> getFileContent(String owner, String repo, String path) throws IOException {
        String content = gitHubClient.getFileContent(owner, repo, path);

        Map<String, Object> response = new HashMap<>();
        response.put("path", path);
        response.put("content", content);
        response.put("language", getLanguageFromPath(path));

        return response;
    }

    /**
     * Generate repository summary using LLM
     */
    public String generateSummary(String owner, String repo) throws IOException {
        Map<String, Object> details = getRepositoryDetails(owner, repo);
        String readme = (String) details.get("readme");
        String description = (String) details.get("description");

        // Get basic structure
        List<Map<String, Object>> rootFiles = getFileStructure(owner, repo, "");
        StringBuilder structure = new StringBuilder();
        for (Map<String, Object> file : rootFiles) {
            structure.append(file.get("type")).append(": ").append(file.get("name")).append("\n");
        }

        return llmClient.summarizeRepository(
                readme != null ? readme : "No README available",
                structure.toString(),
                description != null ? description : "No description"
        );
    }

    /**
     * Search code in GitHub
     */
    public Map<String, Object> searchCode(String query, String language, int page, int perPage) throws IOException {
        JSONObject result = gitHubClient.searchCode(query, language, page, perPage);

        Map<String, Object> response = new HashMap<>();
        response.put("total", result.getInteger("total_count"));

        List<Map<String, Object>> items = new ArrayList<>();
        JSONArray itemsArray = result.getJSONArray("items");

        if (itemsArray != null) {
            for (int i = 0; i < itemsArray.size(); i++) {
                JSONObject item = itemsArray.getJSONObject(i);
                Map<String, Object> code = new HashMap<>();
                code.put("name", item.getString("name"));
                code.put("path", item.getString("path"));
                code.put("repository", item.getJSONObject("repository").getString("full_name"));
                code.put("url", item.getString("html_url"));
                items.add(code);
            }
        }

        response.put("items", items);
        return response;
    }

    private String getLanguageFromPath(String path) {
        if (path == null) return "text";
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex > 0) {
            String ext = path.substring(dotIndex + 1).toLowerCase();
            return switch (ext) {
                case "java" -> "java";
                case "py" -> "python";
                case "js" -> "javascript";
                case "ts" -> "typescript";
                case "go" -> "go";
                case "rs" -> "rust";
                case "c" -> "c";
                case "cpp", "cc", "cxx" -> "cpp";
                case "h" -> "c";
                case "hpp" -> "cpp";
                case "cs" -> "csharp";
                case "rb" -> "ruby";
                case "php" -> "php";
                case "swift" -> "swift";
                case "kt" -> "kotlin";
                case "scala" -> "scala";
                case "vue" -> "vue";
                case "jsx" -> "jsx";
                case "tsx" -> "tsx";
                case "md" -> "markdown";
                case "json" -> "json";
                case "yaml", "yml" -> "yaml";
                case "xml" -> "xml";
                case "html" -> "html";
                case "css" -> "css";
                case "scss" -> "scss";
                case "sql" -> "sql";
                case "sh" -> "bash";
                default -> "text";
            };
        }
        return "text";
    }
}
