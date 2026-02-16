package com.githubsearch.service;

import com.githubsearch.client.LLMClient;
import com.githubsearch.client.ModelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RAG Service
 * Simple in-memory implementation for prototype
 */
@Service
public class RagService {

    @Autowired
    private LLMClient llmClient;

    @Autowired
    private ModelClient modelClient;

    // In-memory vector store for prototype
    // Key: repoId, Value: list of (chunkId, embedding, text)
    private final Map<Long, List<CodeChunk>> vectorStore = new ConcurrentHashMap<>();

    /**
     * Index code chunks for a repository
     */
    public void indexCode(Long repoId, List<String> chunks) throws Exception {
        List<CodeChunk> codeChunks = new ArrayList<>();

        // Get embeddings in batches
        int batchSize = 32;
        for (int i = 0; i < chunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, chunks.size());
            List<String> batch = chunks.subList(i, end);
            List<float[]> embeddings = modelClient.getEmbeddings(batch);

            for (int j = 0; j < batch.size(); j++) {
                CodeChunk chunk = new CodeChunk();
                chunk.chunkId = (long) (i + j);
                chunk.text = batch.get(j);
                chunk.embedding = embeddings.get(j);
                codeChunks.add(chunk);
            }
        }

        vectorStore.put(repoId, codeChunks);
    }

    /**
     * Search similar code chunks
     */
    public List<SearchResult> search(Long repoId, String query, int topK) throws Exception {
        List<CodeChunk> chunks = vectorStore.get(repoId);
        if (chunks == null || chunks.isEmpty()) {
            return Collections.emptyList();
        }

        // Get query embedding
        float[] queryEmbedding = modelClient.getEmbedding(query);

        // Calculate cosine similarity
        List<SearchResult> results = new ArrayList<>();
        for (CodeChunk chunk : chunks) {
            double similarity = cosineSimilarity(queryEmbedding, chunk.embedding);
            results.add(new SearchResult(chunk.chunkId, chunk.text, similarity));
        }

        // Sort by similarity and return top K
        results.sort((a, b) -> Double.compare(b.score, a.score));
        return results.subList(0, Math.min(topK, results.size()));
    }

    /**
     * RAG Question Answering
     */
    public String ask(String query, Long repoId, int topK) throws Exception {
        // Retrieve relevant chunks
        List<SearchResult> results = search(repoId, query, topK);

        if (results.isEmpty()) {
            return "No relevant code found. Please index the repository first.";
        }

        // Build context
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            context.append("```\n").append(results.get(i).text).append("\n```\n\n");
        }

        // Generate answer using LLM
        return llmClient.ragAnswer(query, context.toString());
    }

    /**
     * Get similar code across all repositories
     */
    public List<SearchResult> findSimilarCode(String code, int topK) throws Exception {
        float[] queryEmbedding = modelClient.getEmbedding(code);

        List<SearchResult> allResults = new ArrayList<>();
        for (Map.Entry<Long, List<CodeChunk>> entry : vectorStore.entrySet()) {
            for (CodeChunk chunk : entry.getValue()) {
                double similarity = cosineSimilarity(queryEmbedding, chunk.embedding);
                allResults.add(new SearchResult(chunk.chunkId, chunk.text, similarity, entry.getKey()));
            }
        }

        allResults.sort((a, b) -> Double.compare(b.score, a.score));
        return allResults.subList(0, Math.min(topK, allResults.size()));
    }

    /**
     * Check if repository is indexed
     */
    public boolean isIndexed(Long repoId) {
        List<CodeChunk> chunks = vectorStore.get(repoId);
        return chunks != null && !chunks.isEmpty();
    }

    /**
     * Clear index for repository
     */
    public void clearIndex(Long repoId) {
        vectorStore.remove(repoId);
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0;

        double dotProduct = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Inner classes
    private static class CodeChunk {
        Long chunkId;
        String text;
        float[] embedding;
    }

    public static class SearchResult {
        public Long chunkId;
        public String text;
        public double score;
        public Long repoId;

        public SearchResult(Long chunkId, String text, double score) {
            this.chunkId = chunkId;
            this.text = text;
            this.score = score;
        }

        public SearchResult(Long chunkId, String text, double score, Long repoId) {
            this.chunkId = chunkId;
            this.text = text;
            this.score = score;
            this.repoId = repoId;
        }
    }
}
