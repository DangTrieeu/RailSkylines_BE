package com.fourt.railskylines.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fourt.railskylines.domain.converter.EmbeddingConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "articles")
@Getter
@Setter
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long articleId;

    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;
    private String thumbnail;

    @JsonIgnore
    @Column(columnDefinition = "longtext")
    @Convert(converter = EmbeddingConverter.class)
    private List<Double> embedding;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
