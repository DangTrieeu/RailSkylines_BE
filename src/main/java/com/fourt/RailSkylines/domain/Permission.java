package com.fourt.RailSkylines.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long permissionId;

    private String apiPath;
    private String methotd;
    private String permissionName;
    private String module;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "permissions")
    @JsonIgnore
    private List<Role> roles;
}
