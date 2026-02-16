package com.githubsearch.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Code File Entity
 */
@Data
@TableName("gh_code_file")
public class CodeFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Repository ID */
    private Long repoId;

    /** File Path */
    private String filePath;

    /** File Name */
    private String fileName;

    /** Programming Language */
    private String language;

    /** File Size */
    private Integer size;

    /** Content Hash (SHA) */
    private String contentHash;

    /** Content */
    private String content;

    /** Is Indexed */
    private Integer isIndexed;

    /** Create Time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** Update Time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
