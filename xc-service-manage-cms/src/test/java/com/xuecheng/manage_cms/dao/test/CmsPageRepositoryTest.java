package com.xuecheng.manage_cms.dao.test;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Author: sirc_hzr
 * @Date: 2019/5/8 09:19
 * @ClassName: CmsPageRepositoryTest
 * @Version: 1.0
 * @Description:
 */


@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Test
    public void test1() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<CmsPage> pages = cmsPageRepository.findAll(pageable);
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }
}



