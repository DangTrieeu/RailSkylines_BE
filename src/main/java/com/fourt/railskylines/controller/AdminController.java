package com.fourt.railskylines.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourt.railskylines.domain.Article;
import com.fourt.railskylines.repository.ArticleRepository;
import com.fourt.railskylines.service.ai.EmbeddingService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ArticleRepository articleRepository;
    private final EmbeddingService embeddingService;

    public AdminController(ArticleRepository articleRepository, EmbeddingService embeddingService) {
        this.articleRepository = articleRepository;
        this.embeddingService = embeddingService;
    }

    @PostMapping("/regenerate-embeddings")
    public String regenerateAllEmbeddings() {
        List<Article> articles = articleRepository.findAll();
        int updated = 0;

        for (Article article : articles) {
            try {
                List<Double> embedding = embeddingService.embedArticle(article);
                if (!embedding.isEmpty()) {
                    article.setEmbedding(embedding);
                    articleRepository.save(article);
                    updated++;
                }
            } catch (Exception e) {
                // Continue with other articles if one fails
                System.out.println(
                        "Failed to generate embedding for article " + article.getArticleId() + ": " + e.getMessage());
            }
        }

        return "Regenerated embeddings for " + updated + " articles out of " + articles.size() + " total.";
    }

    @PostMapping("/check-embeddings")
    public String checkEmbeddings() {
        List<Article> articles = articleRepository.findAll();
        int withEmbeddings = 0;
        int withoutEmbeddings = 0;

        for (Article article : articles) {
            List<Double> embedding = article.getEmbedding();
            if (embedding != null && !embedding.isEmpty()) {
                withEmbeddings++;
            } else {
                withoutEmbeddings++;
                System.out.println(
                        "Article " + article.getArticleId() + " (" + article.getTitle() + ") has no embedding");
            }
        }

        return "Total articles: " + articles.size() + ", With embeddings: " + withEmbeddings + ", Without embeddings: "
                + withoutEmbeddings;
    }
}