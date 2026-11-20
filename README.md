# OOPs Project - Course Registration System

Console-based Java course-registration project with Student, Professor,
Administrator and Teaching Assistant roles.

## Permanent storage with JDBC and MySQL

The project now uses only introductory JDBC concepts from the supplied notes:

1. Load the MySQL Type-4 JDBC driver with `Class.forName`.
2. Establish a `Connection` with `DriverManager`.
3. Use `Statement` for SQL `CREATE`, `INSERT`, `DELETE`, and `SELECT` queries.
4. Read `SELECT` rows with `ResultSet`.
5. Close JDBC resources after each database operation.

All accounts, catalogue changes, registrations, grades, complaints, and TA
assignments are restored when the program starts again. See the full setup and
run instructions in [the project README](<oops final 2/OOPS Assignemnt 2/Q1/README.txt>).

The editable UML sources are also in that folder:

- `CourseRegistrationSystem_ClassDiagram.puml`
- `Assignment2_CourseRegistration_UML.drawio`
