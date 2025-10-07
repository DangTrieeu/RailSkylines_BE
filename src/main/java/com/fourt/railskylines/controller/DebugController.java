package com.fourt.railskylines.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourt.railskylines.domain.Article;
import com.fourt.railskylines.repository.ArticleRepository;
import com.fourt.railskylines.service.ai.EmbeddingService;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final ArticleRepository articleRepository;
    private final EmbeddingService embeddingService;

    public DebugController(ArticleRepository articleRepository, EmbeddingService embeddingService) {
        this.articleRepository = articleRepository;
        this.embeddingService = embeddingService;
    }

    @GetMapping("/articles/embedding-status")
    public ResponseEntity<Map<String, Object>> checkEmbeddingStatus() {
        List<Article> allArticles = articleRepository.findAll();

        Map<String, Object> result = new HashMap<>();
        int totalArticles = allArticles.size();
        int nullEmbeddings = 0;
        int emptyEmbeddings = 0;
        int validEmbeddings = 0;

        for (Article article : allArticles) {
            List<Double> embedding = article.getEmbedding();
            if (embedding == null) {
                nullEmbeddings++;
            } else if (embedding.isEmpty()) {
                emptyEmbeddings++;
            } else {
                validEmbeddings++;
            }
        }

        result.put("totalArticles", totalArticles);
        result.put("nullEmbeddings", nullEmbeddings);
        result.put("emptyEmbeddings", emptyEmbeddings);
        result.put("validEmbeddings", validEmbeddings);
        result.put("articlesNeedingEmbedding", nullEmbeddings + emptyEmbeddings);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/articles/generate-embeddings")
    public ResponseEntity<Map<String, Object>> generateMissingEmbeddings() {
        List<Article> allArticles = articleRepository.findAll();

        int processed = 0;
        int successful = 0;
        int failed = 0;

        for (Article article : allArticles) {
            List<Double> embedding = article.getEmbedding();

            // Only process articles with null or empty embeddings
            if (embedding == null || embedding.isEmpty()) {
                processed++;

                List<Double> newEmbedding = embeddingService.embedArticle(article);
                if (newEmbedding != null && !newEmbedding.isEmpty()) {
                    article.setEmbedding(newEmbedding);
                    articleRepository.save(article);
                    successful++;
                } else {
                    failed++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("processed", processed);
        result.put("successful", successful);
        result.put("failed", failed);
        result.put("message", "Generated embeddings for " + successful + " articles");

        return ResponseEntity.ok(result);
    }

    @PostMapping("/articles/fix-embeddings-mock")
    public ResponseEntity<Map<String, Object>> fixEmbeddingsWithMock() {
        List<Article> allArticles = articleRepository.findAll();

        int processed = 0;
        int successful = 0;

        for (Article article : allArticles) {
            List<Double> embedding = article.getEmbedding();

            // Only process articles with null or empty embeddings
            if (embedding == null || embedding.isEmpty()) {
                processed++;

                // Generate mock embedding directly
                List<Double> mockEmbedding = generateMockEmbedding(article);
                if (mockEmbedding != null && !mockEmbedding.isEmpty()) {
                    article.setEmbedding(mockEmbedding);
                    articleRepository.save(article);
                    successful++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("processed", processed);
        result.put("successful", successful);
        result.put("message", "Fixed embeddings for " + successful + " articles using mock data");

        return ResponseEntity.ok(result);
    }

    /**
     * Generate mock embedding (deterministic based on content)
     */
    private List<Double> generateMockEmbedding(Article article) {
        if (article == null) {
            return Collections.emptyList();
        }

        String content = "";
        if (article.getTitle() != null) {
            content += article.getTitle();
        }
        if (article.getContent() != null) {
            content += " " + article.getContent();
        }

        if (content.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Generate deterministic embedding based on content hash
        int seed = content.hashCode();
        java.util.Random random = new java.util.Random(seed);

        int dimension = 1536; // OpenAI dimension
        java.util.List<Double> embedding = new java.util.ArrayList<>();

        for (int i = 0; i < dimension; i++) {
            embedding.add(random.nextGaussian());
        }

        // Normalize to unit vector
        double magnitude = Math.sqrt(embedding.stream().mapToDouble(x -> x * x).sum());
        if (magnitude > 0) {
            embedding = embedding.stream().map(x -> x / magnitude).collect(java.util.stream.Collectors.toList());
        }

        return embedding;
    }

    /**
     * Generate mock embedding for string text
     */
    private List<Double> generateMockEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Generate deterministic embedding based on content hash
        int seed = text.hashCode();
        java.util.Random random = new java.util.Random(seed);

        int dimension = 1536; // OpenAI dimension
        java.util.List<Double> embedding = new java.util.ArrayList<>();

        for (int i = 0; i < dimension; i++) {
            embedding.add(random.nextGaussian());
        }

        // Normalize to unit vector
        double magnitude = Math.sqrt(embedding.stream().mapToDouble(x -> x * x).sum());
        if (magnitude > 0) {
            embedding = embedding.stream().map(x -> x / magnitude).collect(java.util.stream.Collectors.toList());
        }

        return embedding;
    }

    @GetMapping("/articles/sample")
    public ResponseEntity<List<Map<String, Object>>> getSampleArticles() {
        List<Article> articles = articleRepository.findAll();

        return ResponseEntity.ok(
                articles.stream()
                        .limit(5)
                        .map(article -> {
                            Map<String, Object> info = new HashMap<>();
                            info.put("id", article.getArticleId());
                            info.put("title", article.getTitle());
                            info.put("contentLength", article.getContent() != null ? article.getContent().length() : 0);
                            List<Double> embedding = article.getEmbedding();
                            if (embedding == null) {
                                info.put("embeddingStatus", "NULL");
                            } else if (embedding.isEmpty()) {
                                info.put("embeddingStatus", "EMPTY");
                            } else {
                                info.put("embeddingStatus", "HAS_DATA");
                                info.put("embeddingSize", embedding.size());
                            }
                            return info;
                        })
                        .toList());
    }

    @GetMapping("/test-vector-search")
    public ResponseEntity<Map<String, Object>> testVectorSearch() {
        try {
            // Test với mock embedding để tìm articles tương tự
            List<Double> testEmbedding = generateMockEmbedding("test query about trains");

            // Tìm articles có embedding similarity cao
            List<Article> allArticles = articleRepository.findAll();
            List<Map<String, Object>> results = allArticles.stream()
                    .filter(article -> article.getEmbedding() != null && !article.getEmbedding().isEmpty())
                    .map(article -> {
                        double similarity = embeddingService.cosineSimilarity(testEmbedding, article.getEmbedding());
                        Map<String, Object> result = new HashMap<>();
                        result.put("articleId", article.getArticleId());
                        result.put("title", article.getTitle());
                        result.put("similarity", similarity);
                        result.put("embeddingSize", article.getEmbedding().size());
                        return result;
                    })
                    .sorted((a, b) -> Double.compare((Double) b.get("similarity"), (Double) a.get("similarity")))
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("testEmbeddingSize", testEmbedding.size());
            response.put("totalArticlesWithEmbedding", results.size());
            response.put("searchResults", results);

            return ResponseEntity.ok(Map.of(
                    "statusCode", 200,
                    "error", null,
                    "message", "Vector search test completed",
                    "data", response));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "statusCode", 500,
                    "error", e.getMessage(),
                    "message", "Vector search test failed",
                    "data", null));
        }
    }
}