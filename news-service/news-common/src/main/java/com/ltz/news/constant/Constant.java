package com.ltz.news.constant;

public class Constant {

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";
    public static final String REDIS_ADMIN_TOKEN = "redis_admin_token";
    public static final String REDIS_ALL_CATEGORY = "redis_all_category";
    public static final String REDIS_ARTICLE_READ_COUNTS = "redis_article_read_counts";
    public static final String REDIS_WRITER_FANS_COUNTS = "redis_writer_fans_counts";
    public static final String REDIS_MY_FOLLOW_COUNTS = "redis_my_follow_counts";
    public static final String REDIS_ALREADY_READ = "redis_already_read";
    public static final String REDIS_ARTICLE_COMMENT_COUNTS = "redis_article_comment_counts";
    public static final Integer COOKIE_MONTH = 30 * 24 * 60 * 60;
    public static final Integer COOKIE_DELETE = 0;
    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_PAGE_SIZE = 10;

    //Kafka消息的Topic
    public static final String TOPIC = "news_article_op";
}
