package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @Author: sirc_hzr
 * @Date: 2019/5/8 10:15
 * @ClassName: CmsPageService
 * @Version: 1.0
 * @Description:
 */
@Service
public class CmsTemplateService {

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;


    //根据页面查询条件分页查询
    public QueryResponseResult findList(int page, int size, QueryTemplateRequest queryTemplateRequest) {
        //对传进来的参数进行检验
        page = page - 1 <= 0 ? 0 : page-1;
        size = size <= 0 ? 0 : size;
        Pageable pageable = PageRequest.of(page, size);
        queryTemplateRequest = queryTemplateRequest == null ? new QueryTemplateRequest() : queryTemplateRequest;

        CmsTemplate cmsTemplate = new CmsTemplate();
        //根据站点id查询
        cmsTemplate.setSiteId(queryTemplateRequest.getSiteId());
        //根据模板id查询
        cmsTemplate.setTemplateId(queryTemplateRequest.getTemplateId());
        //模版名称
        cmsTemplate.setTemplateName(queryTemplateRequest.getTemplateName());
        //模版文件Id
        cmsTemplate.setTemplateFileId(queryTemplateRequest.getTemplateFileId());
        //模版参数
        cmsTemplate.setTemplateParameter(queryTemplateRequest.getTemplateParameter());


        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                //根据模版名称模糊查询
                .withMatcher("templateName", ExampleMatcher.GenericPropertyMatchers.contains());


        //条件实例
        Example<CmsTemplate> example = Example.of(cmsTemplate, exampleMatcher);

        //根据条件实例进行分页查询
        Page<CmsTemplate> templates = cmsTemplateRepository.findAll(example, pageable);

        //封装查询结果
        QueryResult<CmsTemplate> cmsPageQueryResult = new QueryResult<>();
        cmsPageQueryResult.setList(templates.getContent());
        cmsPageQueryResult.setTotal(templates.getTotalElements());

        return new QueryResponseResult(CommonCode.SUCCESS, cmsPageQueryResult);
    }

    //新增模板
    public CmsTemplateResult add(CmsTemplate cmsTemplate) {
        if (cmsTemplate == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }

        //根据模板名称、站点id、模板文件id查询,检查添加模板的唯一性
        CmsTemplate template = cmsTemplateRepository.findByTemplateNameAndSiteIdAndTemplateFileId(cmsTemplate.getTemplateName(), cmsTemplate.getSiteId(), cmsTemplate.getTemplateFileId());

        //要添加的模板已经存在
        if (template != null) {
            ExceptionCast.cast(CmsCode.CMS_ADDTEMPLATE_EXISTSNAME);
        }

        //如果页面不存在就添加页面，并返回添加成功之后的页面
        if (template == null) {
            //让mongodb自动生成页面的主键
            cmsTemplate.setTemplateId(null);
            cmsTemplateRepository.save(cmsTemplate);

            return new CmsTemplateResult(CommonCode.SUCCESS, cmsTemplate);
        }

        //添加失败返回为null
        return new CmsTemplateResult(CommonCode.FAIL, null);

    }

    //根据页面id查询页面
    public CmsTemplate findById(String id) {
        Optional<CmsTemplate> cmsTemplate = cmsTemplateRepository.findById(id);
        if(cmsTemplate.isPresent()) {
            return cmsTemplate.get();
        }
        return null;
    }

    //根据id修改页面信息
    public CmsTemplateResult update(String id, CmsTemplate cmsTemplate) {
        CmsTemplate template = this.findById(id);
        if (template != null) {
            //更新模板所属站点
            template.setSiteId(cmsTemplate.getSiteId());
            //更新模板名称
            template.setTemplateName(cmsTemplate.getTemplateName());
            //更新模板文件id
            template.setTemplateFileId(cmsTemplate.getTemplateFileId());
            //更新模板参数
            template.setTemplateParameter(cmsTemplate.getTemplateParameter());
            //执行更新
            CmsTemplate save = cmsTemplateRepository.save(template);
            if (save != null) {
                //返回成功
                CmsTemplateResult cmsTemplateResult = new CmsTemplateResult(CommonCode.SUCCESS, save);
                return cmsTemplateResult;
            }
        }
        return new CmsTemplateResult(CommonCode.FAIL, null);
    }

    //根据id删除页面信息
    public ResponseResult delete(String id) {

        CmsTemplate byId = this.findById(id);
        if (byId != null) {
            cmsTemplateRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

}
