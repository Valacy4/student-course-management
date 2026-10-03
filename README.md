# Student Course Management System

A full stack web application to manage students, courses and course enrollments. The backend is a Spring Boot REST API backed by MySQL, and the frontend is plain HTML, CSS and JavaScript served by the same application.

## Features

- Dashboard with total students, courses and enrollments, plus course-wise enrollment counts
- Add, view, search, edit and delete students
- Create and list courses
- Enroll a student into a course and view all enrollment details
- Duplicate enrollment prevention (same student and course)
- Validation for required fields, email format and 10-digit phone number
- Consistent JSON error responses with proper HTTP status codes
- Data stored in MySQL and kept after restarts

## Tech Stack

| Layer      | Technology                             |
|------------|----------------------------------------|
| Frontend   | HTML, CSS, JavaScript (Fetch API)      |
| Backend    | Java 17, Spring Boot 3.3, Spring Web   |
| Data       | Spring Data JPA (Hibernate), MySQL 8   |
| Validation | Jakarta Bean Validation                |
| Build      | Maven                                  |

## Prerequisites

- JDK 17 or newer
- Maven (or the one bundled with Eclipse)
- MySQL Server 8 running locally

## Setup

1. Clone the repository:

```bash
   git clone https://github.com/Valacy4/student-course-management.git
   cd student-course-management
```

2. Make sure MySQL is running. The database `student_course_db` is created automatically on first run.

3. Set the database credentials as environment variables:

   | Variable      | Required | Default | Description    |
   |---------------|----------|---------|----------------|
   | `DB_PASSWORD` | Yes      | none    | MySQL password |
   | `DB_USER`     | No       | `root`  | MySQL username |

   Windows (Command Prompt, then reopen your terminal or IDE):

```
   setx DB_PASSWORD "your_mysql_password"
```

   macOS / Linux:

```bash
   export DB_PASSWORD="your_mysql_password"
```

   Eclipse: Run As, Run Configurations, select the application, open the Environment tab and add `DB_PASSWORD`.

4. Run the application:

```bash
   mvn spring-boot:run
```

   Or from Eclipse: right-click `StudentCourseApplication.java` and choose Run As, Java Application.

5. Open http://localhost:8080 in your browser.

## Project Structure

```
src/main/java/com/scms
├── StudentCourseApplication.java
├── controller
│   ├── StudentController.java
│   ├── CourseController.java
│   ├── EnrollmentController.java
│   └── DashboardController.java
├── dto
│   ├── StudentRequest.java
│   └── EnrollmentRequest.java
├── entity
│   ├── Student.java
│   ├── Course.java
│   └── Enrollment.java
├── exception
│   └── GlobalExceptionHandler.java
└── repository
    ├── StudentRepository.java
    ├── CourseRepository.java
    └── EnrollmentRepository.java

src/main/resources
├── application.properties
└── static
    ├── index.html
    ├── style.css
    └── script.js
```

## Database Design

| Table         | Columns                                                   |
|---------------|-----------------------------------------------------------|
| `students`    | id (PK), name, email (unique), phone, date_of_joining     |
| `courses`     | id (PK), name (unique), duration                          |
| `enrollments` | id (PK), student_id (FK), course_id (FK), enrollment_date |

`enrollments` links students and courses (many-to-many) and has a unique constraint on `(student_id, course_id)`. Deleting a student removes their enrollments.

## REST API

| Method | Endpoint         | Description                                          | Success |
|--------|------------------|------------------------------------------------------|---------|
| GET    | `/students`      | List students (optional `?search=` by name or email) | 200     |
| GET    | `/students/{id}` | Get one student                                      | 200     |
| POST   | `/students`      | Create a student (optional `courseId` enrolls them)  | 201     |
| PUT    | `/students/{id}` | Update a student                                     | 200     |
| DELETE | `/students/{id}` | Delete a student                                     | 204     |
| GET    | `/courses`       | List courses                                         | 200     |
| POST   | `/courses`       | Create a course                                      | 201     |
| POST   | `/enrollments`   | Enroll a student in a course                         | 201     |
| GET    | `/enrollments`   | List all enrollments                                 | 200     |
| GET    | `/dashboard`     | Totals and course-wise enrollment counts             | 200     |

### Example requests

> The curl examples work in Git Bash, macOS and Linux. On Windows Command Prompt, use Postman instead.

Create a course:

```bash
curl -X POST http://localhost:8080/courses \
  -H "Content-Type: application/json" \
  -d '{"name":"Java Full Stack","duration":"6 months"}'
```

Create a student:

```bash
curl -X POST http://localhost:8080/students \
  -H "Content-Type: application/json" \
  -d '{"name":"Asha","email":"asha@example.com","phone":"9876543210","dateOfJoining":"2026-10-01","courseId":1}'
```

Enroll a student:

```bash
curl -X POST http://localhost:8080/enrollments \
  -H "Content-Type: application/json" \
  -d '{"studentId":1,"courseId":1}'
```

### Error responses

| Status | Meaning                                                        |
|--------|----------------------------------------------------------------|
| 400    | Validation failed or malformed request body                    |
| 404    | Student or course not found                                    |
| 409    | Duplicate email, duplicate course name or duplicate enrollment |

Validation error example:

```json
{
  "error": "Validation failed",
  "fields": {
    "phone": "Phone must be exactly 10 digits"
  }
}
```

## Validation Rules

- Name, email, phone and date of joining are required
- Email must be a valid format and unique
- Phone must be exactly 10 digits
- Course name must be unique; course name and duration are required
- A student cannot be enrolled in the same course twice

## Testing

Manual test checklist:

1. Add courses, then add students with a course selected
2. Submit invalid data (bad email, short phone, empty name) and confirm errors appear
3. Try a duplicate email and a duplicate enrollment and confirm both are rejected
4. Search, edit and delete a student
5. Restart the application and confirm the data is still present

The API can also be tested with Postman or the curl examples above.

## Troubleshooting

| Problem                                       | Fix                                                             |
|-----------------------------------------------|-----------------------------------------------------------------|
| `Could not resolve placeholder 'DB_PASSWORD'` | Set the `DB_PASSWORD` environment variable and restart your IDE |
| `Access denied for user`                      | Check `DB_USER` and `DB_PASSWORD`                               |
| `Communications link failure`                 | Make sure MySQL is running on port 3306                         |
| Port 8080 already in use                      | Change `server.port` in `application.properties`                |

## Future Improvements

- Service layer and unit tests
- Pagination, sorting and course-wise filtering
- Authentication with Spring Security
- Export students and enrollments to CSV
