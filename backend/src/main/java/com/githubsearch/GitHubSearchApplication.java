package com.githubsearch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GitHub Search Application
 */
@SpringBootApplication
@MapperScan("com.githubsearch.mapper")
public class GitHubSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitHubSearchApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  GitHub Search System Started  ﾍ(◠‿◠✿)");
    }
}
