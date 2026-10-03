package com.scms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.scms.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByNameIgnoreCase(String name);

    // course name + number of enrollments (0 if none)
    @Query("select c.name, count(e) from Course c left join Enrollment e on e.course = c group by c.id, c.name")
    List<Object[]> countEnrollmentsPerCourse();
}