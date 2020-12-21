package com.ltz.news.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ltz.news.constant.*;
import com.ltz.news.exception.GraceException;
import com.ltz.news.mapper.ArticleMapper;
import com.ltz.news.mapper.ArticleMapperCustom;
import com.ltz.news.pojo.Article;
import com.ltz.news.pojo.Category;
import com.ltz.news.pojo.NewsKafkaMessage;
import com.ltz.news.pojo.bo.NewArticleBO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.result.ResponseStatusEnum;
import com.ltz.news.service.IArticleService;
import com.ltz.news.utils.JsonUtils;
import com.ltz.news.utils.PagedGridResult;
import com.ltz.news.utils.RedisOperator;
import com.mongodb.client.gridfs.GridFSBucket;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;


@Service
public class ArticleServiceImpl implements IArticleService {

    @Autowired
    private Sid sid;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleMapperCustom articleMapperCustom;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 发布文章
     *
     * @param newArticleBO
     * @param category
     */
    @Override
    public void createArticle(NewArticleBO newArticleBO, Category category) {
        String articleId = sid.nextShort();

        Article article = new Article();
        BeanUtils.copyProperties(newArticleBO, article);

        article.setId(articleId);
        article.setCategoryId(category.getId());
        article.setArticleStatus(ArticleReviewStatus.REVIEWING.type);
        article.setCommentCounts(0);
        article.setReadCounts(0);

        article.setIsDelete(YesOrNo.NO.type);
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());

        if (article.getIsAppoint() == ArticleAppointType.TIMING.type) {
            article.setPublishTime(newArticleBO.getPublishTime());
        } else if (article.getIsAppoint() == ArticleAppointType.IMMEDIATELY.type) {
            article.setPublishTime(new Date());
        }

        int res = articleMapper.insert(article);
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_CREATE_ERROR);
        }


        // 发送延迟消息到mq，计算定时发布时间和当前时间的时间差，则为往后延迟的时间
//        if (article.getIsAppoint() == ArticleAppointType.TIMING.type) {
//
//            Date endDate = newArticleBO.getPublishTime();
//            Date startDate = new Date();
//
////            int delayTimes = (int)(endDate.getTime() - startDate.getTime());
//
//            // FIXME: 为了测试方便，写死10s
//            int delayTimes = 10 * 1000;

//            MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
//                @Override
//                public Message postProcessMessage(Message message) throws AmqpException {
//                    // 设置消息的持久
//                    message.getMessageProperties()
//                            .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//                    // 设置消息延迟的时间，单位ms毫秒
//                    message.getMessageProperties()
//                            .setDelay(delayTimes);
//                    return message;
//                }
//            };
//
//            rabbitTemplate.convertAndSend(
//                    RabbitMQDelayConfig.EXCHANGE_DELAY,
//                    "publish.delay.display",
//                    articleId,
//                    messagePostProcessor);
//
//            System.out.println("延迟消息-定时发布文章：" + new Date());
//        }


        // 通过阿里智能AI实现对文章文本的自动检测（自动审核）
//        String reviewTextResult = aliTextReviewUtils.reviewTextContent(newArticleBO.getContent());

//
//        if (reviewTextResult
//                .equalsIgnoreCase(ArticleReviewLevel.PASS.type)) {
//            // 修改当前的文章，状态标记为审核通过
//            this.updateArticleStatus(articleId, ArticleReviewStatus.SUCCESS.type);
//        } else if (reviewTextResult
//                .equalsIgnoreCase(ArticleReviewLevel.REVIEW.type)) {
//            // 修改当前的文章，状态标记为需要人工审核
//            this.updateArticleStatus(articleId, ArticleReviewStatus.WAITING_MANUAL.type);
//        } else if (reviewTextResult
//                .equalsIgnoreCase(ArticleReviewLevel.BLOCK.type)) {
//            // 修改当前的文章，状态标记为审核未通过
//            this.updateArticleStatus(articleId, ArticleReviewStatus.FAILED.type);
//        }
    }

    /**
     * 更新定时发布为即时发布
     */
    @Transactional
    @Override
    public void updateAppointToPublish() {
        articleMapperCustom.updateAppointToPublish();
    }

    /**
     * 更新单条文章为即时发布
     *
     * @param articleId
     */
    @Override
    public void updateArticleToPublish(String articleId) {

    }

    /**
     * 用户中心 - 查询我的文章列表
     *
     * @param userId
     * @param keyword
     * @param status
     * @param startDate
     * @param endDate
     * @param page
     * @param pageSize
     */
    @Override
    public PagedGridResult queryMyArticleList(String userId, String keyword, Integer status, Date startDate, Date endDate, Integer page, Integer pageSize) {
        Example example = new Example(Article.class);
        example.orderBy("createTime").desc();
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("publishUserId", userId);

        if (StringUtils.isNotBlank(keyword)) {
            criteria.andLike("title", "%" + keyword + "%");
        }

        if (ArticleReviewStatus.isArticleStatusValid(status)) {
            criteria.andEqualTo("articleStatus", status);
        }

        if (status != null && status == 12) {
            criteria.andEqualTo("articleStatus", ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus", ArticleReviewStatus.WAITING_MANUAL.type);
        }

        criteria.andEqualTo("isDelete", YesOrNo.NO.type);

        if (startDate != null) {
            criteria.andGreaterThanOrEqualTo("publishTime", startDate);
        }
        if (endDate != null) {
            criteria.andLessThanOrEqualTo("publishTime", endDate);
        }

        PageHelper.startPage(page, pageSize);
        List<Article> list = articleMapper.selectByExample(example);
        return setterPagedGrid(list, page);
    }

    /**
     * 更改文章的状态
     *
     * @param articleId
     * @param pendingStatus
     */
    @Override
    public void updateArticleStatus(String articleId, Integer pendingStatus) {
        Example example = new Example(Article.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", articleId);

        Article pendingArticle = new Article();
        pendingArticle.setArticleStatus(pendingStatus);

        int res = articleMapper.updateByExampleSelective(pendingArticle, example);
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }
    }

    /**
     * 关联文章和gridfs的html文件id
     *
     * @param articleId
     * @param articleMongoId
     */
    @Transactional
    @Override
    public void updateArticleToGridFS(String articleId, String articleMongoId) {
        Article pendingArticle = new Article();
        pendingArticle.setId(articleId);
        pendingArticle.setMongoFileId(articleMongoId);
        articleMapper.updateByPrimaryKeySelective(pendingArticle);
    }

    /**
     * 管理员查询文章列表
     *
     * @param status
     * @param page
     * @param pageSize
     */
    @Override
    public PagedGridResult queryAllArticleListAdmin(Integer status, Integer page, Integer pageSize) {
        Example articleExample = new Example(Article.class);
        articleExample.orderBy("createTime").desc();

        Example.Criteria criteria = articleExample.createCriteria();
        if (ArticleReviewStatus.isArticleStatusValid(status)) {
            criteria.andEqualTo("articleStatus", status);
        }

        // 审核中是机审和人审核的两个状态，所以需要单独判断
        if (status != null && status == 12) {
            criteria.andEqualTo("articleStatus", ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus", ArticleReviewStatus.WAITING_MANUAL.type);
        }

        //isDelete 必须是0
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);

        /**
         * page: 第几页
         * pageSize: 每页显示条数
         */
        PageHelper.startPage(page, pageSize);
        List<Article> list = articleMapper.selectByExample(articleExample);
        return setterPagedGrid(list, page);
    }

    /**
     * 删除文章
     *
     * @param userId
     * @param articleId
     */
    @Override
    public void deleteArticle(String userId, String articleId) {
        Example articleExample = makeExampleCriteria(userId, articleId);

        Article pending = new Article();
        pending.setIsDelete(YesOrNo.YES.type);

        int result = articleMapper.updateByExampleSelective(pending, articleExample);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_DELETE_ERROR);
        }

        deleteHTML(articleId);
    }

    /**
     * 撤回文章
     *
     * @param userId
     * @param articleId
     */
    @Override
    public void withdrawArticle(String userId, String articleId) {
        Example articleExample = makeExampleCriteria(userId, articleId);

        Article pending = new Article();
        pending.setArticleStatus(ArticleReviewStatus.WITHDRAW.type);

        int result = articleMapper.updateByExampleSelective(pending, articleExample);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_WITHDRAW_ERROR);
        }

        deleteHTML(articleId);
    }

    @Autowired
    private GridFSBucket gridFSBucket;
    /**
     * 文章撤回删除后，删除静态化的html
     */
    private void deleteHTML(String articleId) {
        // 1. 查询文章的mongoFileId
        Article pending = articleMapper.selectByPrimaryKey(articleId);
        String articleMongoId = pending.getMongoFileId();

        // 2. 删除GridFS上的文件
        gridFSBucket.delete(new ObjectId(articleMongoId));

        // 3. 删除消费端的HTML文件
//        doDeleteArticleHTML(articleId);
        doDeleteArticleHTMLByKafka(articleId);
    }



    private void doDeleteArticleHTMLByKafka(String articleId) {
        kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new NewsKafkaMessage(
                1,
                articleId
        )));
    }

    private PagedGridResult setterPagedGrid(List<?> list,
                                            Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setPage(page);
        gridResult.setRecords(pageList.getTotal());
        gridResult.setTotal(pageList.getPages());
        return gridResult;
    }

    private Example makeExampleCriteria(String userId, String articleId) {
        Example articleExample = new Example(Article.class);
        Example.Criteria criteria = articleExample.createCriteria();
        criteria.andEqualTo("publishUserId", userId);
        criteria.andEqualTo("id", articleId);
        return articleExample;
    }
}
