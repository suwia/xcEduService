package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @Author: sirc_hzr
 * @Date: 2019/5/8 09:14
 * @Version: 1.0
 * @Description:
 */
public interface CmsPageRepository extends MongoRepository<CmsPage, String> {

    //根据页面名称、站点id、页面访问路径查询
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName, String siteId, String pageWebPath);
}
