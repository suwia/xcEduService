package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author Suwian
 * @version 1.0.0
 * @date 2019-05-09 17:32
 **/
@RestController
@RequestMapping("cms/page")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    private CmsPageService cmsPageService;

    /**
     *
     * @param page 分页查询的当前页
     * @param size 分页查询的每页展示的条数
     * @param queryPageRequest 分页查询条件
     * @return 分页查询内容以及状态信息、状态代码
     */
    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size,  QueryPageRequest queryPageRequest) {
        return cmsPageService.findList(page, size, queryPageRequest);
    }

    @Override
    @PostMapping("add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        return cmsPageService.add(cmsPage);
    }


}
