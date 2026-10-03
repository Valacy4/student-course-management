package com.scms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scms.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Student> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}