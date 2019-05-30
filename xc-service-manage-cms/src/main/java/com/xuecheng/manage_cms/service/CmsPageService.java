package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: sirc_hzr
 * @Date: 2019/5/8 10:15
 * @ClassName: CmsPageService
 * @Version: 1.0
 * @Description:
 */
@Service
public class CmsPageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger("CmsPageService");

    /**
     * 根据页面查询条件分页查询
     * @param page 页码
     * @param size 每页显示条数
     * @param queryPageRequest 查询条件
     * @return 查询结果对象
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        //对传进来的参数进行检验
        page = page - 1 <= 0 ? 0 : page-1;
        size = size <= 0 ? 0 : size;
        Pageable pageable = PageRequest.of(page, size);
        queryPageRequest = queryPageRequest == null ? new QueryPageRequest() : queryPageRequest;

        CmsPage cmsPage = new CmsPage();
        //根据站点id查询
        cmsPage.setSiteId(queryPageRequest.getSiteId());
        //根据页面id查询
        cmsPage.setPageId(queryPageRequest.getPageId());
        //根据页面别名查询
        cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        //根据模板id查询
        cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        //根据页面名称查询
        cmsPage.setPageName(queryPageRequest.getPageName());
        //根据页面类型查询（静态用“0”表示，动态用“1”表示）
        cmsPage.setPageType(queryPageRequest.getPageType());

        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                //根据页面别名模糊查询
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains())
                //根据页面名称模糊查询
                .withMatcher("pageName", ExampleMatcher.GenericPropertyMatchers.contains());

        //条件实例
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        //根据条件实例进行分页查询
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(example, pageable);

        //封装查询结果
        QueryResult<CmsPage> cmsPageQueryResult = new QueryResult<>();
        cmsPageQueryResult.setList(cmsPages.getContent());
        cmsPageQueryResult.setTotal(cmsPages.getTotalElements());

        return new QueryResponseResult(CommonCode.SUCCESS, cmsPageQueryResult);
    }

    /**
     * 新增页面
     * @param cmsPage 需要新增的页面
     * @return 操作结果
     */
    public CmsPageResult add(CmsPage cmsPage) {

        //请求参数为空
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        //根据页面名称、站点id、页面访问路径查询,检查添加页面的唯一性
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());

        //要添加的页面已经存在
        if (cmsPage1 != null) {
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //如果页面不存在就添加页面，并返回添加成功之后的页面
        if (cmsPage1 == null) {
            //让mongodb自动生成页面的主键
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);

            return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
        }

        //添加失败返回为null
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 根据页面id查询页面
     * @param id 页面id
     * @return 页面信息
     */
    public CmsPage findById(String id) {
        Optional<CmsPage> cmsPage = cmsPageRepository.findById(id);
        if(cmsPage.isPresent()) {
            return cmsPage.get();
        }
        return null;
    }

    /**
     * 根据id修改页面信息
     * @param id 页面id
     * @param cmsPage 页面信息
     * @return 操作结果
     */
    public CmsPageResult update(String id, CmsPage cmsPage) {
        CmsPage page = this.findById(id);
        if (page != null) {
            //更新模板id
            page.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            page.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            page.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            page.setPageName(cmsPage.getPageName());
            //更新访问路径
            page.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            page.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新DataUrl
            page.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            CmsPage save = cmsPageRepository.save(page);
            if (save != null) {
            //返回成功
                CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS, save);
                return cmsPageResult;
            }
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 根据id删除页面信息
     * @param id 页面id
     * @return 操作结果
     */
    public ResponseResult delete(String id) {

        CmsPage byId = this.findById(id);
        if (byId != null) {
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 根据cms_config的id来查询配置信息
     * @param id cms_config的id
     * @return 配置信息
     */
    public CmsConfig getConfigById(String id) {
        Optional<CmsConfig> byId = cmsConfigRepository.findById(id);
        if (byId.isPresent()) {
            return byId.get();
        }
        return null;
    }


    /**
     * 获取静态页面 5cd9266c27a27928cc04ebdb
     * 1、静态化程序获取页面的DataUrl
     * 2、静态化程序远程请求DataUrl获取数据模型。
     * 3、静态化程序获取页面的模板信息
     * 4、执行页面静态化
     * @param pageId 页面id
     * @return 静态化页面
     */
    public String getPageHtml(String pageId) {
        try {
            //获取数据模型
            Map modelByPageId = this.getModelByPageId(pageId);
            if (modelByPageId == null) {
                //数据模型为空
                ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
            }

            //获取页面模板
            String templateByPageId = this.getTemplateByPageId(pageId);
            if (StringUtils.isEmpty(templateByPageId)) {
                //模板为空
                ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
            }

            //执行页面静态化
            String html = this.generateHtml(templateByPageId, modelByPageId);
            if (StringUtils.isEmpty(html)) {
                //生成的静态页面为空
                ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
            }

            return html;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 页面静态化
     * @param templateStr 页面模板字符串
     * @param model 模型数据
     * @return 静态化页面
     * @throws IOException
     * @throws TemplateException
     */
    private String generateHtml(String templateStr, Map model) throws IOException, TemplateException {
        //创建配置类
        Configuration configuration=new Configuration(Configuration.getVersion());
        //加载模板
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateStr);
        configuration.setTemplateLoader(stringTemplateLoader);
        Template template = configuration.getTemplate("template","utf-8");
        //静态化
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
    }


    /**
     * 根据页面id获取页面模板
     * @param pageId 页面id
     * @return 页面模板字符串
     * @throws IOException
     */
    private String getTemplateByPageId(String pageId) throws IOException {
        CmsPage byId = this.findById(pageId);
        if (byId == null) {
            ExceptionCast.cast(CmsCode.CMS_QUERYPAGE_NOTEXIST);
        }

        String templateId = byId.getTemplateId();

        if (StringUtils.isEmpty(templateId)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        //根据模板id查询模板
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            String templateFileId = cmsTemplate.getTemplateFileId();
            //根据模板文件ID查询模板文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建gridFsResource，用于获取流对象
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //获取流中的数据
            return IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
        }

        return null;

    }

    /**
     * 获取数据模型
     * @param pageId 根据页面id获取页面的数据模型
     * @return 页面数据模型
     */
    private Map getModelByPageId(String pageId) {
        CmsPage byId = this.findById(pageId);
        if (byId == null) {
            ExceptionCast.cast(CmsCode.CMS_QUERYPAGE_NOTEXIST);
        }
        String dataUrl = byId.getDataUrl();

        if (StringUtils.isEmpty(dataUrl)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }

        //根据从页面获取到的dataUrl进行Http调用
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        return forEntity.getBody();
    }

    /**
     * 页面发布
     * @param pageId
     * @param htmlStr
     */
    public ResponseResult postPage(String pageId) {
        String pageHtml = this.getPageHtml(pageId);
        if (StringUtils.isEmpty(pageHtml)) {
            LOGGER.error("pageId: " + pageId + ", msg: " + CmsCode.CMS_GENERATEHTML_HTMLISNULL.message());
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        CmsPage cmsPage = saveHtml(pageId, pageHtml);
        sendPostPageMsg(pageId);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 发送消息到消息队列
     * @param pageId
     */
    private void sendPostPageMsg(String pageId) {
        //根据pageId获取页面信息
        CmsPage cmsPage = findById(pageId);
        if (cmsPage == null) {
            LOGGER.error("pageId: " + pageId + ", msg: " + CmsCode.CMS_GENERATEHTML_HTMLISNULL.message());
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }

        Map<String, String> msgMap = new HashMap<>();
        msgMap.put("pageId", cmsPage.getPageId());
        String jsonString = JSON.toJSONString(msgMap);

        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, cmsPage.getSiteId(), jsonString);

    }

    /**
     * 存储页面对应的静态html到gridFS
     * @param pageId 页面id
     * @param pageHtml 静态内容
     * @return 页面信息
     */
    private CmsPage saveHtml(String pageId, String pageHtml) {
        //根据pageId获取页面信息
        CmsPage cmsPage = findById(pageId);
        if (cmsPage == null) {
            LOGGER.error("pageId: " + pageId + ", msg: " + CmsCode.CMS_GENERATEHTML_HTMLISNULL.message());
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        //拿到静态html页面的id
        String htmlFileId = cmsPage.getHtmlFileId();
        //删除旧的静态html页面
        if (htmlFileId != null) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }

        ObjectId objectId = null;
        //存储新的html页面到GridFS
        try {
            InputStream inputStream = IOUtils.toInputStream(pageHtml, "utf-8");
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将返回的id存储到cmspage对象
        cmsPage.setHtmlFileId(objectId.toString());
        cmsPageRepository.save(cmsPage);

        //返回页面信息
        return cmsPage;
    }
}
