package com.fourt.railskylines.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fourt.railskylines.domain.Article;

public interface ArticleRepository extends JpaRepository<Article, Long>,JpaSpecificationExecutor<Article> {

    
    
}
