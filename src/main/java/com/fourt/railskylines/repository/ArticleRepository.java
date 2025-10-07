package com.fourt.railskylines.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.fourt.railskylines.domain.Article;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    @Query("SELECT a FROM Article a WHERE a.embedding IS NOT NULL AND SIZE(a.embedding) > 0")
    List<Article> findByEmbeddingIsNotNull();
}
