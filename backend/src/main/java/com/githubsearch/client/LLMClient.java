package com.githubsearch.client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * LLM Client (Qwen)
 */
@Component
public class LLMClient {

    @Value("${llm.api-key}")
    private String apiKey;

    @Value("${llm.base-url}")
    private String baseUrl;

    @Value("${llm.model:qwen3-max}")
    private String model;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    /**
     * Chat completion
     */
    public String chat(String systemPrompt, String userMessage) throws IOException {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("stream", false);

        JSONArray messages = new JSONArray();

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
        }

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        body.put("messages", messages);

        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(body.toJSONString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("LLM API error: " + response.code() + " - " + response.body().string());
            }
            JSONObject result = JSONObject.parseObject(response.body().string());
            return result.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }

    /**
     * Generate code summary
     */
    public String summarizeCode(String code, String language) throws IOException {
        String systemPrompt = "You are a code analysis expert. Summarize the following code concisely in Chinese.";
        String userMessage = String.format("Language: %s\n\nCode:\n```\n%s\n```\n\nPlease provide:\n1. Functionality summary (2-3 sentences)\n2. Key features\n3. Usage example if applicable",
                language, code);
        return chat(systemPrompt, userMessage);
    }

    /**
     * RAG Question Answering
     */
    public String ragAnswer(String question, String context) throws IOException {
        String systemPrompt = "You are a code expert assistant. Answer questions based on the provided code context. " +
                "If the context is not relevant, say you don't know. Respond in Chinese.";

        String userMessage = String.format("Context:\n```\n%s\n```\n\nQuestion: %s\n\nPlease provide a detailed answer with code examples if applicable.",
                context, question);
        return chat(systemPrompt, userMessage);
    }

    /**
     * Generate repository summary
     */
    public String summarizeRepository(String readme, String structure, String description) throws IOException {
        String systemPrompt = "You are a project analysis expert. Create a comprehensive summary of this GitHub repository in Chinese.";

        String userMessage = String.format(
                "Description: %s\n\nREADME:\n```\n%s\n```\n\nProject Structure:\n%s\n\n" +
                "Please provide:\n1. Project overview\n2. Main features\n3. Tech stack\n4. Getting started guide",
                description, readme, structure);

        return chat(systemPrompt, userMessage);
    }
}
