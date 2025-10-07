package com.fourt.railskylines.service.ai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fourt.railskylines.domain.Article;
import com.fourt.railskylines.repository.ArticleRepository;

@Component
public class EmbeddingStartupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddingStartupService.class);

    private final ArticleRepository articleRepository;
    private final EmbeddingService embeddingService;

    public EmbeddingStartupService(ArticleRepository articleRepository, EmbeddingService embeddingService) {
        this.articleRepository = articleRepository;
        this.embeddingService = embeddingService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildMissingEmbeddings() {
        LOGGER.info("Checking for articles without embeddings...");

        List<Article> articles = articleRepository.findAll();
        int totalArticles = articles.size();
        int processedCount = 0;
        int generatedCount = 0;

        for (Article article : articles) {
            List<Double> embedding = article.getEmbedding();
            if (embedding == null || embedding.isEmpty()) {
                try {
                    // embedArticle now handles saving to database automatically
                    List<Double> computed = embeddingService.embedArticle(article);
                    if (!computed.isEmpty()) {
                        generatedCount++;
                        LOGGER.debug("Generated embedding for article {} ({})",
                                article.getArticleId(), article.getTitle());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to generate embedding for article {}: {}",
                            article.getArticleId(), e.getMessage());
                }
            }
            processedCount++;

            // Log progress for large datasets
            if (processedCount % 10 == 0 || processedCount == totalArticles) {
                LOGGER.info("Progress: {}/{} articles processed, {} embeddings generated",
                        processedCount, totalArticles, generatedCount);
            }
        }

        if (generatedCount > 0) {
            LOGGER.info("Successfully generated embeddings for {} out of {} articles.",
                    generatedCount, totalArticles);
        } else {
            LOGGER.info("All {} articles already have embeddings.", totalArticles);
        }
    }
}
