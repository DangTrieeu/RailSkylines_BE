package com.fourt.railskylines.service;

import org.springframework.data.domain.Pageable;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fourt.railskylines.domain.Article;
import com.fourt.railskylines.domain.response.ResultPaginationDTO;
import com.fourt.railskylines.repository.ArticleRepository;
import com.fourt.railskylines.service.ai.EmbeddingService;

@Service
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final EmbeddingService embeddingService;

    public ArticleService(ArticleRepository articleRepository, EmbeddingService embeddingService) {
        this.articleRepository = articleRepository;
        this.embeddingService = embeddingService;
    }

    public ResultPaginationDTO handleFretchAllArticle(Specification<Article> spec, Pageable pageable) {
        Page<Article> pageArticle = this.articleRepository.findAll(spec, pageable);
        ResultPaginationDTO res = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPage(pageArticle.getTotalPages());
        meta.setTotal(pageArticle.getTotalElements());

        res.setMeta(meta);
        res.setResult(pageArticle.getContent());

        return res;
    }

    public Article handleCreateArticle(Article article) {
        article.setEmbedding(embeddingService.embedArticle(article));
        return articleRepository.save(article);
    }

    public Article fetchArticleById(long id) {
        Optional<Article> articleOptional = this.articleRepository.findById(id);
        if (articleOptional.isPresent()) {
            return articleOptional.get();
        }
        return null;
    }

    public Article handleUpdateArticle(Long id, Article article) {
        Article tmpArticle = this.fetchArticleById(id);
        if (tmpArticle != null) {
            tmpArticle.setTitle(article.getTitle());
            tmpArticle.setContent(article.getContent());
            tmpArticle.setThumbnail(article.getThumbnail());
            tmpArticle.setEmbedding(embeddingService.embedArticle(tmpArticle));

            this.articleRepository.save(tmpArticle);
        }
        return tmpArticle;
    }

    public void handleDeleteArticle(Long id) {
        this.articleRepository.deleteById(id);
    }
}
