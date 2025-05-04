package com.fourt.railskylines.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourt.railskylines.domain.Article;
import com.fourt.railskylines.domain.Train;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.service.ArticleService;
import com.fourt.railskylines.util.annotation.APIMessage;
import com.fourt.railskylines.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

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

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("/articles")
    @APIMessage("Create New Article")
    public ResponseEntity<Article> hadleCreateNewArticle(@Valid @RequestBody Article newArticle)
            throws IdInvalidException {
        Article article = this.articleService.handleCreateArticle(newArticle);
        return ResponseEntity.status(HttpStatus.OK).body(article);
    }

    @PutMapping("/articles/{id}")
    @APIMessage("Update article by ID")
    public ResponseEntity<Article> handleUpdateArticle(@PathVariable("id") Long id, @Valid @RequestBody Article article)
            throws IdInvalidException {
        if (this.articleService.fetchArticleById(id) == null) {
            throw new IdInvalidException("Article with id = not exits " + id + " , pls check again");
        }
        Article updateArticle = this.articleService.handleUpdateTrain(id, article);
        return ResponseEntity.ok(updateArticle);
    }

    @DeleteMapping("/articles/{id}")
    @APIMessage("Delete article by ID")
    public ResponseEntity<String> handleDeletaArticle(@PathVariable("id") Long id)
            throws IdInvalidException {
        if (this.articleService.fetchArticleById(id) == null) {
            throw new IdInvalidException("Article with id = not exits " + id + " , pls check again");
        }
        this.articleService.handleDeleteTrain(id);
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
