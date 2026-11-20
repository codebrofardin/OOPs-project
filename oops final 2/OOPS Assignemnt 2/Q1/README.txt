COLLEGE COURSE REGISTRATION SYSTEM (MULTI-FILE JAVA PROJECT)

FILES
-----
Main.java              Application entry point
AppInput.java          Common console input methods
Student.java           Student model
Professor.java         Professor model
Course.java            Course model
Complaint.java         Complaint model
DataStore.java         Shared application cache and initial course catalogue
DbUtility.java         Type-4 MySQL JDBC driver loading and Connection creation
CourseRegistrationDao.java  JDBC table creation, SELECT/ResultSet loading and DML saving
database_setup.sql     One-time MySQL database creation script
AuthService.java       Signup/login routing for Students, Professors, Admin and TAs
StudentService.java    Student dashboard and functions
ProfessorService.java  Professor course management and student lists
AdminService.java      Administrator dashboard and functions
TeachingAssistant.java TA subclass that extends Student
TeachingAssistantService.java      TA dashboard and grading assistance
TeachingAssistantAdminService.java Admin assignment/unassignment of TA courses

DATABASE SETUP, COMPILE AND RUN
-------------------------------
1. Install MySQL Server and start it.
2. Open MySQL and run `database_setup.sql` once. It creates the
   `course_registration_db` database; the Java program creates the tables.
3. Download MySQL Connector/J (`mysql-connector-j-<version>.jar`). Put the JAR
   in this same folder, or note its path.
4. Open `DbUtility.java` and set `USER` and `PASSWORD` to your own MySQL login.
   The default URL is `jdbc:mysql://localhost:3306/course_registration_db`.
5. Open a terminal in this folder and run (replace `<version>` with your JAR's
   version):

    javac -cp ".:mysql-connector-j-<version>.jar" *.java
    java -cp ".:mysql-connector-j-<version>.jar" Main

   On Windows, replace `:` in the classpath with `;`.

JDBC FLOW USED IN THIS PROJECT
------------------------------
The database code follows the JDBC notes directly:

1. `DbUtility.getConnection()` loads `com.mysql.cj.jdbc.Driver` using
   `Class.forName()` and opens a `Connection` with `DriverManager`.
2. `CourseRegistrationDao.createTables()` uses a `Statement` and DDL
   `CREATE TABLE` statements.
3. `CourseRegistrationDao.loadData()` runs `SELECT` queries and loops through a
   `ResultSet` using `while (resultSet.next())`.
4. `CourseRegistrationDao.saveData()` uses DML `INSERT` and `DELETE` statements
   after every add, update, registration, grade, complaint, or assignment change.
5. The `Statement` is closed before the parent `Connection` in a `finally` block.

There are no frameworks, ORM libraries, web APIs, or advanced database features.
The only added dependency is the standard MySQL JDBC driver JAR.

ADMIN LOGIN
-----------
Email:    admin@college.com
Password: Admin@123

TEACHING ASSISTANT LOGIN (PRELOADED DEMO ACCOUNTS)
--------------------------------------------------
Kush Patel:       kush.patel@college.com
Aesha Shah:       aesha.shah@college.com
Meet Christian:   meet.christian@college.com
Password for all three demo TAs: TA@123
These accounts exist only in the Teaching Assistant role. They are not normal
Student or Professor accounts. TA signup is disabled; the three accounts are
added on the first database run and then saved in MySQL.

IMPLEMENTED FUNCTIONALITY
-------------------------
STUDENT
- Sign up and log in
- View courses only for current semester
- Register for a course/batch with prerequisite, 20-credit cap, enrollment-limit,
  and timetable-clash checks
- View weekly schedule
- View grades, SGPA and CGPA for completed semesters only
- Drop an ongoing-semester course
- Submit complaints and view status/resolution

PROFESSOR
- Sign up and log in
- View courses assigned by admin
- Update title, syllabus, class day/time, room, credits, prerequisites,
  enrollment limit, office hours, and course hours
- View enrolled students, email/phone, current semester, CGPA and course grade
- Professors cannot add/delete catalog courses or assign grades

TEACHING ASSISTANT
- Kush Patel, Aesha Shah and Meet Christian have TA-only login accounts.
- TeachingAssistant extends Student; Student.java is not changed.
- TAs can open the existing Student dashboard and use student operations.
- TAs can view only the courses assigned to them by the administrator.
- TAs can view enrolled learners and current grades in assigned courses.
- TAs can assign/update grade points (0-10) only for learners enrolled in their
  assigned courses. TAs cannot grade themselves and cannot change grades after
  that learner's semester has been finalized.
- TAs cannot add/delete/edit courses, assign professors, manage complaints,
  update other student personal records, or finalize semesters.
- The administrator remains responsible for finalizing each semester after all
  registered course grades have been entered.

ADMINISTRATOR
- View/add/delete/edit course catalog
- View student records, update student personal information
- Assign/update grades and complete a semester only after every registered
  course has a grade
- Assign registered professor accounts to courses
- Manage TA course assignments (assign/unassign courses for each TA)
- View complaints, filter by status/date, and update status with resolution details

DEMONSTRATION SETUP
-------------------
1. Use Student -> Sign up to create at least three normal student accounts.
2. Use Professor -> Sign up to create at least two professor accounts.
3. Log in as administrator and assign the professors to catalogue courses.
4. The timetable image supplied for this project contains Semester 2 courses only.
   Add the institution's Semester 1 courses before demonstrating the full semester
   progression for newly created students.
5. As administrator, open Manage Teaching Assistants and assign one or more
   catalogue courses to Kush, Aesha or Meet.
6. Log in as a TA, view assigned courses, and view or enter grades for learners
   who are enrolled in those assigned course/batches.
7. Log back in as administrator and finalize the student's semester after all
   registered courses have grades.

OOP IMPLEMENTATION / TA INHERITANCE
-----------------------------------
- Inheritance: TeachingAssistant extends Student using `extends Student`.
- Constructor chaining: the TA constructor calls `super(name, email, password)`.
- Reuse/polymorphic substitution: TeachingAssistant objects are passed to
  StudentService.menu(Student), so they use inherited student functionality
  through the Student type without duplicating the student code.
- Encapsulation: the TA course assignment set is private and exposed through
  controlled TA assignment methods.
- The existing Student.java and Professor.java model classes are unchanged.
  There is no separate Administrator model class in this project; administrator
  behavior is implemented by AdminService. Its menu has one added entry to
  manage TA course assignments.

DATA NOTES / IMPORTANT
----------------------
1. Semester 2 course entries, short names/batches, faculty, credits, total hours,
   scheduled hours, and visible remarks are transcribed from the supplied image.
2. Repeated course codes for lab batches are stored as separate catalogue rows
   using the short name (e.g. CSP202 + DSAL-B1 / DSAL-B2) as a unique batch key.
3. The screenshot does not provide weekdays, exact class timings, room numbers,
   prerequisites, syllabus, office hours, or enrollment limits. Timing/room/syllabus
   fields are therefore initially unset/defaulted and can be filled by a professor
   or administrator; prerequisites start as none.
4. The screenshot's credits (including 3, 1, 2.5 and 0.5) are retained exactly.
   This differs from the assignment paragraph that says courses should be 2 or 4
   credits. If the instructor strictly enforces only 2/4 credits, adjust the values
   in DataStore.java. The 20-credit semester cap is enforced.
5. Semester 1 courses were not present in the supplied image. Students start in
   semester 1 as required; the administrator must add the real semester 1 courses
   through Manage course catalog before students can register and progress.
6. Initial timetable rows are marked as Semester 2 based on their 2xx course codes.
7. `DataStore` keeps collections while the program is running, but every change is
   saved through JDBC to MySQL. Accounts, course registrations, grades, complaints,
   course edits, professor assignments, and TA assignments therefore remain after exit.