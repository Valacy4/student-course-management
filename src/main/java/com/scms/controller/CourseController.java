package com.scms.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.scms.entity.Course;
import com.scms.repository.CourseRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseRepository courseRepo;

    public CourseController(CourseRepository courseRepo) {
        this.courseRepo = courseRepo;
    }

    @GetMapping
    public List<Course> getAll() {
        return courseRepo.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Course create(@Valid @RequestBody Course course) {
        if (courseRepo.existsByNameIgnoreCase(course.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Course already exists");
        }
        course.setId(null);
        course.setName(course.getName().trim());
        course.setDuration(course.getDuration().trim());
        return courseRepo.save(course);
    }
}