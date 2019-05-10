package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value="cms页面管理接口",description = "cms页面管理接口，提供页面的增、删、改、查")
public interface CmsPageControllerApi {

    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码",required=true,paramType="path",dataType="int"),
        @ApiImplicitParam(name = "size", value = "每页记录数",required=true,paramType="path",dataType="int")
    })
    //页面查询
    QueryResponseResult findList(int size, int page, QueryPageRequest queryPageRequest);

    @ApiOperation("页面添加")
    CmsPageResult add(CmsPage cmsPage);
}
