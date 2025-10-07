package com.fourt.railskylines.service.ai;

import java.util.ArrayList;
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
        List<Article> articles = articleRepository.findAll();
        List<Article> toUpdate = new ArrayList<>();
        for (Article article : articles) {
            List<Double> embedding = article.getEmbedding();
            if (embedding == null || embedding.isEmpty()) {
                List<Double> computed = embeddingService.embedArticle(article);
                if (!computed.isEmpty()) {
                    article.setEmbedding(computed);
                    toUpdate.add(article);
                }
            }
        }
        if (!toUpdate.isEmpty()) {
            articleRepository.saveAll(toUpdate);
            LOGGER.info("Rebuilt embeddings for {} article(s).", toUpdate.size());
        } else {
            LOGGER.info("All articles already have embeddings.");
        }
    }
}
