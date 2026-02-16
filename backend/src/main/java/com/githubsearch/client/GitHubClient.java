package com.githubsearch.client;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * GitHub API Client
 */
@Component
public class GitHubClient {

    @Value("${github.api.base-url:https://api.github.com}")
    private String baseUrl;

    @Value("${github.api.token:}")
    private String token;

    @Value("${github.api.connect-timeout:30}")
    private int connectTimeout;

    @Value("${github.api.read-timeout:60}")
    private int readTimeout;

    private OkHttpClient httpClient;

    private OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .build();
        }
        return httpClient;
    }

    private Request.Builder createRequest(String url) {
        Request.Builder builder = new Request.Builder().url(url);
        if (StrUtil.isNotBlank(token)) {
            builder.header("Authorization", "Bearer " + token);
        }
        builder.header("Accept", "application/vnd.github.v3+json");
        return builder;
    }

    /**
     * Search Repositories
     */
    public JSONObject searchRepositories(String query, String language, int page, int perPage) throws IOException {
        String url = baseUrl + "/search/repositories?q=" + encode(query);
        if (StrUtil.isNotBlank(language)) {
            url += "+language:" + encode(language);
        }
        url += "&page=" + page + "&per_page=" + perPage + "&sort=stars&order=desc";

        Request request = createRequest(url).get().build();
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API error: " + response.code());
            }
            String body = response.body().string();
            return JSON.parseObject(body);
        }
    }

    /**
     * Get Repository Details
     */
    public JSONObject getRepository(String owner, String repo) throws IOException {
        String url = baseUrl + "/repos/" + owner + "/" + repo;
        Request request = createRequest(url).get().build();
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API error: " + response.code());
            }
            return JSON.parseObject(response.body().string());
        }
    }

    /**
     * Get Repository Contents (file tree)
     */
    public JSONArray getContents(String owner, String repo, String path) throws IOException {
        String url = baseUrl + "/repos/" + owner + "/" + repo + "/contents" + path;
        Request request = createRequest(url).get().build();
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API error: " + response.code());
            }
            return JSON.parseArray(response.body().string());
        }
    }

    /**
     * Get File Content
     */
    public String getFileContent(String owner, String repo, String path) throws IOException {
        String url = baseUrl + "/repos/" + owner + "/" + repo + "/contents/" + path;
        Request request = createRequest(url).get().build();
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            JSONObject json = JSON.parseObject(response.body().string());
            String content = json.getString("content");
            if (json.getString("encoding").equals("base64") && content != null) {
                // Remove newlines and decode base64
                content = content.replace("\n", "");
                return new String(java.util.Base64.getDecoder().decode(content));
            }
            return content;
        }
    }

    /**
     * Get README
     */
    public String getReadme(String owner, String repo) throws IOException {
        String url = baseUrl + "/repos/" + owner + "/" + repo + "/readme";
        Request request = createRequest(url).get().build();
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            JSONObject json = JSON.parseObject(response.body().string());
            String content = json.getString("content");
            if (content != null) {
                content = content.replace("\n", "");
                return new String(java.util.Base64.getDecoder().decode(content));
            }
            return null;
        }
    }

    /**
     * Search Code
     */
    public JSONObject searchCode(String query, String language, int page, int perPage) throws IOException {
        String url = baseUrl + "/search/code?q=" + encode(query);
        if (StrUtil.isNotBlank(language)) {
            url += "+language:" + encode(language);
        }
        url += "&page=" + page + "&per_page=" + perPage;

        Request request = createRequest(url).get().build();
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("GitHub API error: " + response.code());
            }
            return JSON.parseObject(response.body().string());
        }
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
