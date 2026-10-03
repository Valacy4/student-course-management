package com.scms.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.scms.dto.StudentRequest;
import com.scms.entity.Course;
import com.scms.entity.Enrollment;
import com.scms.entity.Student;
import com.scms.repository.CourseRepository;
import com.scms.repository.EnrollmentRepository;
import com.scms.repository.StudentRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final EnrollmentRepository enrollmentRepo;

    public StudentController(StudentRepository studentRepo, CourseRepository courseRepo,
                             EnrollmentRepository enrollmentRepo) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
        this.enrollmentRepo = enrollmentRepo;
    }

    @GetMapping
    public List<Student> getAll(@RequestParam(required = false) String search) {
        if (search == null || search.isBlank()) {
            return studentRepo.findAll();
        }
        return studentRepo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search);
    }

    @GetMapping("/{id}")
    public Student getOne(@PathVariable Long id) {
        return findStudent(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Student create(@Valid @RequestBody StudentRequest req) {
        if (studentRepo.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A student with this email already exists");
        }
        Course course = null;
        if (req.getCourseId() != null) {
            course = courseRepo.findById(req.getCourseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        }

        Student student = new Student();
        copy(req, student);
        Student saved = studentRepo.save(student);

        if (course != null) {
            Enrollment e = new Enrollment();
            e.setStudent(saved);
            e.setCourse(course);
            e.setEnrollmentDate(LocalDate.now());
            enrollmentRepo.save(e);
        }
        return saved;
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @Valid @RequestBody StudentRequest req) {
        Student student = findStudent(id);
        if (studentRepo.existsByEmailAndIdNot(req.getEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another student already uses this email");
        }
        copy(req, student);
        return studentRepo.save(student);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        studentRepo.delete(findStudent(id));
    }

    private Student findStudent(Long id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    private void copy(StudentRequest req, Student s) {
        s.setName(req.getName().trim());
        s.setEmail(req.getEmail().trim().toLowerCase());
        s.setPhone(req.getPhone());
        s.setDateOfJoining(req.getDateOfJoining());
    }
}