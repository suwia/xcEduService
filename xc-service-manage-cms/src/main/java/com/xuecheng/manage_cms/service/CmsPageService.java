package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

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

        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

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

    public CmsPageResult add(CmsPage cmsPage) {
        //根据页面名称、站点id、页面访问路径查询,检查添加页面的唯一性
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());

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
}
