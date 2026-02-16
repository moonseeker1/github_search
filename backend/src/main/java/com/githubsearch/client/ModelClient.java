package com.githubsearch.client;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Model Service Client (BGE-M3 & Reranker)
 */
@Component
public class ModelClient {

    @Value("${model.embedding.url:http://127.0.0.1:8000}")
    private String embeddingUrl;

    @Value("${model.reranker.url:http://127.0.0.1:8001}")
    private String rerankerUrl;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    /**
     * Get embeddings from BGE-M3
     */
    public List<float[]> getEmbeddings(List<String> texts) throws IOException {
        JSONObject body = new JSONObject();
        body.put("texts", texts);

        Request request = new Request.Builder()
                .url(embeddingUrl + "/embedding")
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body.toJSONString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Embedding service error: " + response.code());
            }
            JSONObject result = JSONObject.parseObject(response.body().string());
            JSONArray embeddings = result.getJSONArray("embeddings");
            return embeddings.toList(float[].class);
        }
    }

    /**
     * Get single embedding
     */
    public float[] getEmbedding(String text) throws IOException {
        List<float[]> embeddings = getEmbeddings(List.of(text));
        return embeddings.isEmpty() ? null : embeddings.get(0);
    }

    /**
     * Rerank documents
     */
    public List<Double> rerank(String query, List<String> documents) throws IOException {
        JSONObject body = new JSONObject();
        body.put("query", query);
        body.put("documents", documents);

        Request request = new Request.Builder()
                .url(rerankerUrl + "/rerank")
                .header("Content-Type", "application/json")
                .post(RequestBody.create(body.toJSONString(), MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Reranker service error: " + response.code());
            }
            JSONObject result = JSONObject.parseObject(response.body().string());
            JSONArray scores = result.getJSONArray("scores");
            return scores.toList(Double.class);
        }
    }
}
