package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.manage_course.dao.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: sirc_hzr
 * @date: 2019/5/31 11:31
 * @version: 1.0
 * @description:
 */
@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    public TeachplanNode findTeachplanList(String courseId) {
        return courseMapper.findTeachplanById(courseId);
    }
}
