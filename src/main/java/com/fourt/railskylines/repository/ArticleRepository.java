package com.fourt.railskylines.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fourt.railskylines.domain.Article;

public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    List<Article> findByEmbeddingIsNotNull();
}
