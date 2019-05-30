package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryTemplateRequest;
import com.xuecheng.framework.domain.cms.response.CmsTemplateResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value="cms模板管理接口",description = "cms模板管理接口，提供模板的增、删、改、查")
public interface CmsTemplateControllerApi {

    @ApiOperation("分页查询模板列表")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", value = "页码",required=true,paramType="path",dataType="int"),
        @ApiImplicitParam(name = "size", value = "每页记录数",required=true,paramType="path",dataType="int")
    })
    //页面查询
    QueryResponseResult findList(int size, int page, QueryTemplateRequest queryTemplateRequest);

    @ApiOperation("模板添加")
    CmsTemplateResult add(CmsTemplate cmsTemplate);

    @ApiOperation("根据id查询模板信息")
    @ApiImplicitParam(name = "id", value = "模板id",required=true,paramType="path",dataType="string")
    CmsTemplate findById(String id);

    @ApiOperation("根据id修改页面信息")
    @ApiImplicitParam(name = "id", value = "模板id",required=true,paramType="path",dataType="string")
    CmsTemplateResult update(String id, CmsTemplate cmsTemplate);

    @ApiOperation("根据id删除模板")
    @ApiImplicitParam(name = "id", value = "模板id",required=true,paramType="path",dataType="string")
    ResponseResult delete(String id);
}
