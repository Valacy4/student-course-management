package com.scms.dto;

import jakarta.validation.constraints.NotNull;

public class EnrollmentRequest {

    @NotNull(message = "Student is required")
    private Long studentId;

    @NotNull(message = "Course is required")
    private Long courseId;

	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public Long getCourseId() {
		return courseId;
	}

	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}

   
}