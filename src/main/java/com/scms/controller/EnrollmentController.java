package com.scms.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.scms.dto.EnrollmentRequest;
import com.scms.entity.Course;
import com.scms.entity.Enrollment;
import com.scms.entity.Student;
import com.scms.repository.CourseRepository;
import com.scms.repository.EnrollmentRepository;
import com.scms.repository.StudentRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepo;
    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;

    public EnrollmentController(EnrollmentRepository enrollmentRepo, StudentRepository studentRepo,
                                CourseRepository courseRepo) {
        this.enrollmentRepo = enrollmentRepo;
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
    }

    @GetMapping
    public List<Enrollment> getAll() {
        return enrollmentRepo.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Enrollment enroll(@Valid @RequestBody EnrollmentRequest req) {
        Student student = studentRepo.findById(req.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        Course course = courseRepo.findById(req.getCourseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (enrollmentRepo.existsByStudentIdAndCourseId(student.getId(), course.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Student is already enrolled in this course");
        }

        Enrollment e = new Enrollment();
        e.setStudent(student);
        e.setCourse(course);
        e.setEnrollmentDate(LocalDate.now());
        return enrollmentRepo.save(e);
    }
}