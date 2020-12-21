package com.ltz.news.controller;

import com.alibaba.fastjson.JSON;
import com.ltz.news.constant.ArticleCoverType;
import com.ltz.news.constant.ArticleReviewStatus;
import com.ltz.news.constant.Constant;
import com.ltz.news.constant.YesOrNo;
import com.ltz.news.controller.article.ArticleControllerApi;
import com.ltz.news.pojo.Category;
import com.ltz.news.pojo.NewsKafkaMessage;
import com.ltz.news.pojo.bo.NewArticleBO;
import com.ltz.news.pojo.vo.ArticleDetailVO;
import com.ltz.news.result.GraceJSONResult;
import com.ltz.news.result.ResponseStatusEnum;
import com.ltz.news.service.IArticleService;
import com.ltz.news.utils.JsonUtils;
import com.ltz.news.utils.PagedGridResult;
import com.ltz.news.utils.RedisOperator;
import com.mongodb.client.gridfs.GridFSBucket;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.ltz.news.constant.Constant.REDIS_ALL_CATEGORY;

@RestController
public class ArticleController implements ArticleControllerApi {

    @Autowired
    private IArticleService articleService;

    @Autowired
    private RedisOperator redis;

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public GraceJSONResult createArticle(@Valid NewArticleBO newArticleBO) {
        //        // 判断BindingResult是否保存错误的验证信息，如果有，则直接return
//        if (result.hasErrors()) {
//            Map<String, String> errorMap = getErrors(result);
//            return GraceJSONResult.errorMap(errorMap);
//        }

        // 判断文章封面类型，单图必填，纯文字则设置为空
        if (newArticleBO.getArticleType() == ArticleCoverType.ONE_IMAGE.type) {
            if (StringUtils.isBlank(newArticleBO.getArticleCover())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_COVER_NOT_EXIST_ERROR);
            }
        } else if (newArticleBO.getArticleType() == ArticleCoverType.WORDS.type) {
            newArticleBO.setArticleCover("");
        }

        // 判断分类id是否存在
        String allCatJson = redis.get(REDIS_ALL_CATEGORY);
        Category temp = null;
        if (StringUtils.isBlank(allCatJson)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        } else {
            List<Category> catList =
                    JsonUtils.jsonToList(allCatJson, Category.class);
            for (Category c : catList) {
                if(c.getId() == newArticleBO.getCategoryId()) {
                    temp = c;
                    break;
                }
            }
            if (temp == null) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_CATEGORY_NOT_EXIST_ERROR);
            }
        }


        articleService.createArticle(newArticleBO, temp);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryMyList(String userId, String keyword, Integer status, Date startDate, Date endDate, Integer page, Integer pageSize) {
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_QUERY_PARAMS_ERROR);
        }

        if (page == null) {
            page = Constant.COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = Constant.COMMON_PAGE_SIZE;
        }

        // 查询我的列表，调用service
        PagedGridResult grid = articleService.queryMyArticleList(userId,
                keyword,
                status,
                startDate,
                endDate,
                page,
                pageSize);

        return GraceJSONResult.ok(grid);
    }

    @Override
    public GraceJSONResult queryAllList(Integer status, Integer page, Integer pageSize) {
        if (page == null) {
            page = Constant.COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = Constant.COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = articleService.queryAllArticleListAdmin(status, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult doReview(String articleId, Integer passOrNot) {
        Integer pendingStatus;
        if (passOrNot == YesOrNo.YES.type) {
            // 审核成功
            pendingStatus = ArticleReviewStatus.SUCCESS.type;
        } else if (passOrNot == YesOrNo.NO.type) {
            // 审核失败
            pendingStatus = ArticleReviewStatus.FAILED.type;
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }

        // 保存到数据库，更改文章的状态为审核成功或者失败
        articleService.updateArticleStatus(articleId, pendingStatus);

        if (pendingStatus == ArticleReviewStatus.SUCCESS.type) {
            // 审核成功，生成文章详情页静态html
            try {
                String articleMongoId = createArticleHTMLToGridFS(articleId);
                // 存储到对应的文章，进行关联保存
                articleService.updateArticleToGridFS(articleId, articleMongoId);

                // 调用消费端，执行下载html
//                doDownloadArticleHTML(articleId, articleMongoId);

                // 发送消息到mq队列，让消费者监听并且执行下载html
                doDownloadArticleHTMLByKafka(articleId, articleMongoId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return GraceJSONResult.ok();
    }



    private void doDownloadArticleHTMLByKafka(String articleId, String articleMongoId) {
        kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new NewsKafkaMessage(
                0,articleId+"," + articleMongoId
        )));
    }


    @Autowired
    private GridFSBucket gridFSBucket;

    // 文章生成HTML
    public String createArticleHTMLToGridFS(String articleId) throws Exception {

        Configuration cfg = new Configuration(Configuration.getVersion());
        String classpath = this.getClass().getResource("/").getPath();
        cfg.setDirectoryForTemplateLoading(new File(classpath + "templates"));

        Template template = cfg.getTemplate("detail.ftl", "utf-8");

        // 获得文章的详情数据
        ArticleDetailVO detailVO = getArticleDetail(articleId);
        Map<String, Object> map = new HashMap<>();
        map.put("articleDetail", detailVO);

        String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        InputStream inputStream = IOUtils.toInputStream(htmlContent);
        ObjectId fileId = gridFSBucket.uploadFromStream(detailVO.getId() + ".html",inputStream);
        return fileId.toString();
    }


    // 发起远程调用rest，获得文章详情数据
    public ArticleDetailVO getArticleDetail(String articleId) {
        String url
                = "http://www.imoocnews.com:8001/portal/article/detail?articleId=" + articleId;
        ResponseEntity<GraceJSONResult> responseEntity
                = restTemplate.getForEntity(url, GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        ArticleDetailVO detailVO = null;
        if (bodyResult.getStatus() == 200) {
            String detailJson = JsonUtils.objectToJson(bodyResult.getData());
            detailVO = JsonUtils.jsonToPojo(detailJson, ArticleDetailVO.class);
        }
        return detailVO;
    }

    @Override
    public GraceJSONResult delete(String userId, String articleId) {
        articleService.deleteArticle(userId, articleId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult withdraw(String userId, String articleId) {
        articleService.withdrawArticle(userId, articleId);
        return GraceJSONResult.ok();
    }
}
