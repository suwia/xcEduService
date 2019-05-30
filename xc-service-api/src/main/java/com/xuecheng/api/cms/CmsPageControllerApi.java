package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
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

    @ApiOperation("根据id查询页面信息")
    @ApiImplicitParam(name = "id", value = "页面id",required=true,paramType="path",dataType="string")
    CmsPage findById(String id);

    @ApiOperation("根据id修改页面信息")
    @ApiImplicitParam(name = "id", value = "页面id",required=true,paramType="path",dataType="string")
    CmsPageResult update(String id, CmsPage cmsPage);

    @ApiOperation("根据id删除页面")
    @ApiImplicitParam(name = "id", value = "页面id",required=true,paramType="path",dataType="string")
    ResponseResult delete(String id);

    @ApiOperation("页面发布")
    @ApiImplicitParam(name = "id", value = "页面id",required=true,paramType="path",dataType="string")
    ResponseResult post(String id);
}
