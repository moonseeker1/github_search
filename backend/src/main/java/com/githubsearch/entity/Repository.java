package com.githubsearch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * GitHub Repository Entity
 */
@Data
@TableName("gh_repository")
public class Repository {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** GitHub Repository ID */
    private String repoId;

    /** Owner */
    private String owner;

    /** Repository Name */
    private String name;

    /** Full Name (owner/name) */
    private String fullName;

    /** Description */
    private String description;

    /** Primary Language */
    private String language;

    /** Stars Count */
    private Integer stars;

    /** Forks Count */
    private Integer forks;

    /** Clone URL */
    private String cloneUrl;

    /** Homepage */
    private String homepage;

    /** Topics */
    private String topics;

    /** Created at GitHub */
    private LocalDateTime githubCreatedAt;

    /** Updated at GitHub */
    private LocalDateTime githubUpdatedAt;

    /** Status: 0-pending, 1-parsed, 2-indexed */
    private Integer status;

    /** Create Time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** Update Time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
