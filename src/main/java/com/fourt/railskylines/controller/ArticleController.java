package com.fourt.railskylines.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourt.railskylines.domain.Article;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.service.ArticleService;
import com.fourt.railskylines.service.PusherService;
import com.fourt.railskylines.service.ai.EmbeddingService;
import com.fourt.railskylines.util.annotation.APIMessage;
import com.fourt.railskylines.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import java.util.Map;
import java.util.HashMap;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1")
public class ArticleController {

    private final ArticleService articleService;
    private final PusherService pusherService;
    private final EmbeddingService embeddingService;

    public ArticleController(ArticleService articleService, PusherService pusherService,
            EmbeddingService embeddingService) {
        this.articleService = articleService;
        this.pusherService = pusherService;
        this.embeddingService = embeddingService;
    }

    @PostMapping("/articles")
    @APIMessage("Create New Article")
    public ResponseEntity<Article> hadleCreateNewArticle(@Valid @RequestBody Article newArticle)
            throws IdInvalidException {
        Article article = this.articleService.handleCreateArticle(newArticle);

        // Generate embedding for the new article
        try {
            embeddingService.embedArticle(article);
        } catch (Exception e) {
            // Log error but don't fail article creation
            System.err.println(
                    "Failed to generate embedding for article " + article.getArticleId() + ": " + e.getMessage());
        }

        // Trigger Pusher event for article creation
        this.pusherService.triggerArticleCreated(article);

        return ResponseEntity.status(HttpStatus.OK).body(article);
    }

    @PutMapping("/articles/{id}")
    @APIMessage("Update article by ID")
    public ResponseEntity<Article> handleUpdateArticle(@PathVariable("id") Long id, @Valid @RequestBody Article article)
            throws IdInvalidException {
        if (this.articleService.fetchArticleById(id) == null) {
            throw new IdInvalidException("Article with id = not exits " + id + " , pls check again");
        }
        Article updateArticle = this.articleService.handleUpdateArticle(id, article);

        // Regenerate embedding for the updated article
        try {
            embeddingService.embedArticle(updateArticle);
        } catch (Exception e) {
            // Log error but don't fail article update
            System.err.println("Failed to regenerate embedding for article " + updateArticle.getArticleId() + ": "
                    + e.getMessage());
        }

        // Trigger Pusher event for article update
        this.pusherService.triggerArticleUpdated(updateArticle);

        return ResponseEntity.ok(updateArticle);
    }

    @GetMapping("/articles/{id}/debug-embedding")
    @APIMessage("Debug article embedding")
    public ResponseEntity<Map<String, Object>> debugArticleEmbedding(@PathVariable("id") Long id)
            throws IdInvalidException {
        Article article = this.articleService.fetchArticleById(id);
        if (article == null) {
            throw new IdInvalidException("Article with id = not exits " + id + " , pls check again");
        }

        Map<String, Object> debug = new HashMap<>();
        debug.put("articleId", article.getArticleId());
        debug.put("title", article.getTitle());
        debug.put("hasEmbedding", article.getEmbedding() != null);
        if (article.getEmbedding() != null) {
            debug.put("embeddingLength", article.getEmbedding().size());
            debug.put("embeddingPreview", article.getEmbedding().toString().substring(0,
                    Math.min(100, article.getEmbedding().toString().length())));
        }

        return ResponseEntity.ok(debug);
    }

    @DeleteMapping("/articles/{id}")
    @APIMessage("Delete article by ID")
    public ResponseEntity<String> handleDeletaArticle(@PathVariable("id") Long id)
            throws IdInvalidException {
        if (this.articleService.fetchArticleById(id) == null) {
            throw new IdInvalidException("Article with id = not exits " + id + " , pls check again");
        }
        this.articleService.handleDeleteArticle(id);

        // Trigger Pusher event for article deletion
        this.pusherService.triggerArticleDeleted(id);

        return ResponseEntity.ok("Delete Success");
    }

    @GetMapping("/articles/{id}")
    @APIMessage("Fetch article by ID")
    public ResponseEntity<Article> getArticleById(@PathVariable("id") Long id) throws IdInvalidException {
        if (this.articleService.fetchArticleById(id) == null) {
            throw new IdInvalidException("Train with id = " + id + " not exits , pls check again");
        }
        Article article = articleService.fetchArticleById(id);
        return ResponseEntity.status(HttpStatus.OK).body(article);
    }

    @GetMapping("/articles")
    @APIMessage("Fetch All Articles")
    public ResponseEntity<ResultPaginationDTO> handleFretchAllArticle(
            @Filter Specification<Article> spec,
            Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.articleService.handleFretchAllArticle(spec, pageable));
    }

}
