package com.githubsearch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.githubsearch.entity.Repository;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepositoryMapper extends BaseMapper<Repository> {
}
