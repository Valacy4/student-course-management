package com.scms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scms.entity.Enrollment;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}