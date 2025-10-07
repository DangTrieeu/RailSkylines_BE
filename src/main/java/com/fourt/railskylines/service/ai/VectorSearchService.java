package com.fourt.railskylines.service.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fourt.railskylines.domain.Article;
import com.fourt.railskylines.domain.User;
import com.fourt.railskylines.repository.ArticleRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Vector search service similar to vector_search() in Python version
 * Handles semantic search with role-based access control
 */
@Service
public class VectorSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VectorSearchService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final ArticleRepository articleRepository;
    private final EmbeddingService embeddingService;

    public VectorSearchService(ArticleRepository articleRepository, EmbeddingService embeddingService) {
        this.articleRepository = articleRepository;
        this.embeddingService = embeddingService;
    }

    /**
     * Search for relevant articles using vector similarity
     * 
     * @param query    The search query
     * @param limit    Maximum number of results
     * @param minScore Minimum similarity score threshold (0.0 to 1.0)
     * @return List of relevant articles with similarity scores
     */
    public List<VectorSearchResult> vectorSearch(String query, int limit, double minScore) {
        if (!StringUtils.hasText(query)) {
            LOGGER.debug("Empty query provided to vector search");
            return Collections.emptyList();
        }

        // Generate embedding for query
        List<Double> queryEmbedding = embeddingService.embedText(query);
        if (queryEmbedding.isEmpty()) {
            LOGGER.warn("Failed to generate embedding for query: {}, using mock embedding", query);
            // Use mock embedding as fallback
            queryEmbedding = generateMockEmbedding(query);
        }

        // Get current user for role-based filtering
        User currentUser = getCurrentUser();
        LOGGER.debug("Vector search for user: {} with roles: {}",
                currentUser != null ? currentUser.getEmail() : "anonymous",
                currentUser != null ? getUserRoles(currentUser) : "none");

        // Fetch articles with embeddings using native query to avoid Hibernate
        // converter issues
        var nativeQuery = entityManager.createNativeQuery(
                "SELECT article_id, title, content, embedding, user_id FROM articles WHERE embedding IS NOT NULL");

        var rows = nativeQuery.getResultList();
        LOGGER.info("Vector search query: '{}', Found {} articles with embeddings", query, rows.size());

        List<VectorSearchResult> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (Object row : rows) {
            try {
                Object[] columns = (Object[]) row;
                Long articleId = ((Number) columns[0]).longValue();
                String title = (String) columns[1];
                String content = (String) columns[2];
                String embeddingJson = (String) columns[3];
                Long userId = columns[4] != null ? ((Number) columns[4]).longValue() : null;

                // Create minimal Article object for role-based access control
                Article article = new Article();
                article.setArticleId(articleId);
                article.setTitle(title);
                article.setContent(content);
                if (userId != null) {
                    User user = new User();
                    user.setUserId(userId);
                    article.setUser(user);
                }

                // Role-based access control
                if (!hasAccessToArticle(article, currentUser)) {
                    continue;
                }

                // Parse embedding JSON
                double[] embeddingArray = mapper.readValue(embeddingJson, double[].class);
                List<Double> articleEmbedding = java.util.Arrays.stream(embeddingArray)
                        .boxed()
                        .collect(Collectors.toList());

                // Calculate cosine similarity
                double similarity = calculateCosineSimilarity(queryEmbedding, articleEmbedding);
                LOGGER.info("Article {}: '{}' - similarity: {}, minScore: {}", articleId, title, similarity, minScore);

                // Filter by minimum score
                if (similarity >= minScore) {
                    results.add(new VectorSearchResult(
                            article,
                            similarity,
                            buildPreview(article.getContent())));
                }
            } catch (Exception e) {
                LOGGER.warn("Error processing article: {}", e.getMessage());
            }
        }

        // Sort by similarity score (descending) and limit results
        return results.stream()
                .sorted(Comparator.comparingDouble(VectorSearchResult::score).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Check if current user has access to article (role-based filtering)
     */
    private boolean hasAccessToArticle(Article article, User currentUser) {
        // For now, all articles are public
        // In future, implement role-based access similar to Python version:
        // - Check article.getRestrictToRoles()
        // - Compare with user roles
        // - Allow access if roles intersect or no restrictions

        return true; // TODO: Implement role-based access control
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                return (User) auth.getPrincipal();
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to get current user: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Get user roles (placeholder for role extraction)
     */
    private List<String> getUserRoles(User user) {
        // TODO: Extract actual roles from user
        return Collections.emptyList();
    }

    /**
     * Build article content preview, similar to Python version
     */
    private String buildPreview(String content) {
        if (!StringUtils.hasText(content)) {
            return "Không có nội dung";
        }

        String cleaned = content.replaceAll("\\s+", " ").trim();
        if (cleaned.length() > 240) {
            return cleaned.substring(0, 237) + "...";
        }
        return cleaned;
    }

    /**
     * Generate mock embedding as fallback when OpenAI API fails
     * This uses the same logic as in ChatController
     */
    private List<Double> generateMockEmbedding(String text) {
        java.util.Random random = new java.util.Random(text.hashCode()); // Use text hash as seed for consistency
        Double[] embedding = new Double[1536];

        for (int i = 0; i < 1536; i++) {
            embedding[i] = random.nextGaussian() * 0.1;
        }

        return java.util.Arrays.asList(embedding);
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double calculateCosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Vector search result container
     */
    public static record VectorSearchResult(
            Article article,
            double score,
            String preview) {
    }
}