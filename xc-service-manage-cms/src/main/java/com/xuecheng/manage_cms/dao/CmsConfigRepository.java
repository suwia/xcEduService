package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Author: sirc_hzr
 * @Date: 2019/5/8 09:14
 * @Version: 1.0
 * @Description:
 */
public interface CmsConfigRepository extends MongoRepository<CmsConfig, String> {

}
