package com.learn.ai.tools;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.learn.ai.entity.Course;
import com.learn.ai.entity.CourseReservation;
import com.learn.ai.entity.School;
import com.learn.ai.service.CourseReservationService;
import com.learn.ai.service.CourseService;
import com.learn.ai.service.SchoolService;
import com.learn.ai.tools.param.CourseQueryCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class CourseTools {

    private final CourseService courseService;
    private final SchoolService schoolService;
    private final CourseReservationService courseReservationService;

    @Tool(description = "根据条件查询课程")
    public List<Course> queryCourse(@ToolParam(required = false, description = "课程查询条件") CourseQueryCriteria criteria) {
        CourseQueryCriteria queryCriteria = Objects.requireNonNullElseGet(criteria, CourseQueryCriteria::new);
        QueryChainWrapper<Course> wrapper = courseService.query();
        wrapper.eq(queryCriteria.getType() != null, "type", queryCriteria.getType())
                .le(queryCriteria.getEdu() != null, "edu", queryCriteria.getEdu());
        if (queryCriteria.getSorts() != null) {
            for (CourseQueryCriteria.SortRule sort : queryCriteria.getSorts()) {
                wrapper.orderBy(true, sort.isAsc(), sort.getField());
            }
        }
        return wrapper.list();
    }

    @Tool(description = "查询所有校区")
    public List<School> queryAllSchools() {
        return schoolService.list();
    }

    @Tool(description = "生成课程预约单,并返回生成的预约单号")
    public String generateCourseReservation(String courseName, String studentName, String contactInfo, String school, String remark) {
        CourseReservation courseReservation = new CourseReservation();
        courseReservation.setCourse(courseName);
        courseReservation.setStudentName(studentName);
        courseReservation.setContactInfo(contactInfo);
        courseReservation.setSchool(school);
        courseReservation.setRemark(remark);
        courseReservationService.save(courseReservation);
        return String.valueOf(courseReservation.getId());
    }
}
