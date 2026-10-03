package com.scms.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scms.repository.CourseRepository;
import com.scms.repository.EnrollmentRepository;
import com.scms.repository.StudentRepository;

@RestController
public class DashboardController {

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;

    public DashboardController(StudentRepository studentRepo, CourseRepository courseRepo,
                               EnrollmentRepository enrollmentRepo) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        List<Map<String, Object>> perCourse = courseRepo.countEnrollmentsPerCourse().stream()
                .map(row -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("courseName", row[0]);
                    m.put("enrollments", row[1]);
                    return m;
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalStudents", studentRepo.count());
        result.put("totalCourses", courseRepo.count());
        result.put("totalEnrollments", enrollmentRepo.count());
        result.put("courseWiseEnrollments", perCourse);
        return result;
    }
}