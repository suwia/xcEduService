package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.dao.TeachplanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author: sirc_hzr
 * @date: 2019/5/31 14:58
 * @version: 1.0
 * @description:
 */

@Service
public class TeachplanService {

    @Autowired
    private TeachplanRepository teachplanRepository;

    @Autowired
    private CourseBaseRepository courseBaseRepository;

    public static final Logger LOGGER = LoggerFactory.getLogger("TeachplanService");

    /**
     * 添加教学计划
     * @param teachplan
     * @return
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan){
        if (teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) || StringUtils.isEmpty(teachplan.getPname())) {
            LOGGER.error("addTeachplan error: "+ CommonCode.INVALID_PARAM.message());
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        String courseid = teachplan.getCourseid();
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)) {
            parentid = getRootId(courseid);
        }

        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(parentid);
        if (!teachplanOptional.isPresent()) {
            LOGGER.error(CommonCode.INVALID_PARAM.message());
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        Teachplan teachplanOld = teachplanOptional.get();
        String grade = teachplanOld.getGrade();
        Teachplan teachplanNew = new Teachplan();
        BeanUtils.copyProperties(teachplan, teachplanNew);
        teachplanNew.setParentid(parentid);

        if (grade.equals("1"))  {
            teachplanNew.setGrade("2");
        } else{
            teachplanNew.setGrade("3");
        }

        teachplanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);

    }

    private String getRootId(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            return null;
        }

        CourseBase courseBase = optional.get();
        List<Teachplan> teachplans = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if (teachplans == null || teachplans.size() <= 0) {
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setStatus("0");
            teachplan.setParentid("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }

        return teachplans.get(0).getId();
    }
}
