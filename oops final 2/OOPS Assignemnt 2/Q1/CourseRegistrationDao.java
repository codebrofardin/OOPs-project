import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * JDBC DAO for the course-registration data. It deliberately uses the basic
 * JDBC objects taught in class: Connection, Statement and ResultSet.
 */
public final class CourseRegistrationDao {
    private CourseRegistrationDao() { }

    /** Creates the project tables when they are not already present. */
    public static void createTables() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DbUtility.getConnection();
            statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS students ("
                    + "email VARCHAR(100) PRIMARY KEY, name VARCHAR(100), password VARCHAR(100), "
                    + "phone VARCHAR(40), address VARCHAR(255), current_semester INT, role VARCHAR(20))");
            statement.execute("CREATE TABLE IF NOT EXISTS professors ("
                    + "email VARCHAR(100) PRIMARY KEY, name VARCHAR(100), password VARCHAR(100), "
                    + "phone VARCHAR(40), expertise VARCHAR(150))");
            statement.execute("CREATE TABLE IF NOT EXISTS courses ("
                    + "course_key VARCHAR(120) PRIMARY KEY, code VARCHAR(30), title VARCHAR(150), "
                    + "short_name VARCHAR(50), professor_name VARCHAR(100), professor_email VARCHAR(100), "
                    + "prerequisite VARCHAR(30), semester INT, credits DOUBLE, total_hours DOUBLE, "
                    + "scheduled_hours DOUBLE, remarks VARCHAR(255), day_name VARCHAR(30), timing VARCHAR(50), "
                    + "room VARCHAR(80), syllabus VARCHAR(255), office_hours VARCHAR(100), enrollment_limit INT)");
            statement.execute("CREATE TABLE IF NOT EXISTS registrations ("
                    + "student_email VARCHAR(100), semester INT, course_key VARCHAR(120), "
                    + "PRIMARY KEY(student_email, semester, course_key))");
            statement.execute("CREATE TABLE IF NOT EXISTS grades ("
                    + "student_email VARCHAR(100), course_key VARCHAR(120), grade_point DOUBLE, "
                    + "PRIMARY KEY(student_email, course_key))");
            statement.execute("CREATE TABLE IF NOT EXISTS completed_semesters ("
                    + "student_email VARCHAR(100), semester INT, PRIMARY KEY(student_email, semester))");
            statement.execute("CREATE TABLE IF NOT EXISTS complaints ("
                    + "id INT PRIMARY KEY, student_email VARCHAR(100), description VARCHAR(255), "
                    + "created_at VARCHAR(30), status VARCHAR(20), resolution_details VARCHAR(255))");
            statement.execute("CREATE TABLE IF NOT EXISTS ta_assignments ("
                    + "ta_email VARCHAR(100), course_key VARCHAR(120), PRIMARY KEY(ta_email, course_key))");
        } finally {
            close(statement, connection);
        }
    }

    /** Reads database rows into the application's maps and lists using ResultSet. */
    public static void loadData() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DbUtility.getConnection();
            statement = connection.createStatement();

            resultSet = statement.executeQuery("SELECT * FROM students");
            while (resultSet.next()) {
                Student student;
                if ("TA".equals(resultSet.getString("role"))) {
                    student = new TeachingAssistant(resultSet.getString("name"), resultSet.getString("email"), resultSet.getString("password"));
                    DataStore.teachingAssistants.put(student.email, (TeachingAssistant) student);
                } else {
                    student = new Student(resultSet.getString("name"), resultSet.getString("email"), resultSet.getString("password"));
                    DataStore.students.put(student.email, student);
                }
                student.phone = resultSet.getString("phone");
                student.address = resultSet.getString("address");
                student.currentSemester = resultSet.getInt("current_semester");
            }
            closeResultSet(resultSet);

            resultSet = statement.executeQuery("SELECT * FROM professors");
            while (resultSet.next()) {
                Professor professor = new Professor(resultSet.getString("name"), resultSet.getString("email"), resultSet.getString("password"));
                professor.phone = resultSet.getString("phone");
                professor.expertise = resultSet.getString("expertise");
                DataStore.professors.put(professor.email, professor);
            }
            closeResultSet(resultSet);

            resultSet = statement.executeQuery("SELECT * FROM courses");
            while (resultSet.next()) {
                Course course = new Course(resultSet.getString("code"), resultSet.getString("title"),
                        resultSet.getString("short_name"), resultSet.getInt("semester"),
                        resultSet.getDouble("credits"), resultSet.getDouble("total_hours"),
                        resultSet.getDouble("scheduled_hours"), resultSet.getString("professor_name"), resultSet.getString("remarks"));
                course.professorEmail = resultSet.getString("professor_email");
                course.prerequisite = resultSet.getString("prerequisite");
                course.day = resultSet.getString("day_name");
                course.timing = resultSet.getString("timing");
                course.room = resultSet.getString("room");
                course.syllabus = resultSet.getString("syllabus");
                course.officeHours = resultSet.getString("office_hours");
                course.enrollmentLimit = resultSet.getInt("enrollment_limit");
                DataStore.courses.put(course.uniqueKey(), course);
            }
            closeResultSet(resultSet);

            resultSet = statement.executeQuery("SELECT * FROM registrations");
            while (resultSet.next()) {
                Student student = DataStore.findLearner(resultSet.getString("student_email"));
                if (student != null) student.registrations.computeIfAbsent(resultSet.getInt("semester"), key -> new LinkedHashSet<String>())
                        .add(resultSet.getString("course_key"));
            }
            closeResultSet(resultSet);

            resultSet = statement.executeQuery("SELECT * FROM grades");
            while (resultSet.next()) {
                Student student = DataStore.findLearner(resultSet.getString("student_email"));
                if (student != null) student.grades.put(resultSet.getString("course_key"), resultSet.getDouble("grade_point"));
            }
            closeResultSet(resultSet);

            resultSet = statement.executeQuery("SELECT * FROM completed_semesters");
            while (resultSet.next()) {
                Student student = DataStore.findLearner(resultSet.getString("student_email"));
                if (student != null) student.completedSemesters.add(resultSet.getInt("semester"));
            }
            closeResultSet(resultSet);

            resultSet = statement.executeQuery("SELECT * FROM complaints");
            while (resultSet.next()) {
                Complaint complaint = new Complaint(resultSet.getInt("id"), resultSet.getString("student_email"),
                        resultSet.getString("description"), LocalDateTime.parse(resultSet.getString("created_at")));
                complaint.status = resultSet.getString("status");
                complaint.resolutionDetails = resultSet.getString("resolution_details");
                DataStore.complaints.add(complaint);
                DataStore.updateComplaintSequence(complaint.id);
            }
            closeResultSet(resultSet);

            resultSet = statement.executeQuery("SELECT * FROM ta_assignments");
            while (resultSet.next()) {
                TeachingAssistant ta = DataStore.teachingAssistants.get(resultSet.getString("ta_email"));
                if (ta != null) ta.assignCourse(resultSet.getString("course_key"));
            }
        } finally {
            closeResultSet(resultSet);
            close(statement, connection);
        }
    }

    /** Writes the current maps and lists to the database with DML statements. */
    public static void saveData() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DbUtility.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM ta_assignments");
            statement.executeUpdate("DELETE FROM completed_semesters");
            statement.executeUpdate("DELETE FROM grades");
            statement.executeUpdate("DELETE FROM registrations");
            statement.executeUpdate("DELETE FROM complaints");
            statement.executeUpdate("DELETE FROM courses");
            statement.executeUpdate("DELETE FROM professors");
            statement.executeUpdate("DELETE FROM students");

            for (Student student : DataStore.students.values()) insertStudent(statement, student, "STUDENT");
            for (TeachingAssistant ta : DataStore.teachingAssistants.values()) insertStudent(statement, ta, "TA");
            for (Professor professor : DataStore.professors.values()) {
                statement.executeUpdate("INSERT INTO professors VALUES (" + text(professor.email) + "," + text(professor.name) + ","
                        + text(professor.password) + "," + text(professor.phone) + "," + text(professor.expertise) + ")");
            }
            for (Course course : DataStore.courses.values()) {
                statement.executeUpdate("INSERT INTO courses VALUES (" + text(course.uniqueKey()) + "," + text(course.code) + ","
                        + text(course.title) + "," + text(course.shortName) + "," + text(course.professorName) + ","
                        + text(course.professorEmail) + "," + text(course.prerequisite) + "," + course.semester + ","
                        + course.credits + "," + course.totalHours + "," + course.scheduledHours + "," + text(course.remarks) + ","
                        + text(course.day) + "," + text(course.timing) + "," + text(course.room) + "," + text(course.syllabus) + ","
                        + text(course.officeHours) + "," + course.enrollmentLimit + ")");
            }
            for (Student learner : DataStore.allLearners()) {
                for (Map.Entry<Integer, LinkedHashSet<String>> entry : learner.registrations.entrySet()) {
                    for (String courseKey : entry.getValue()) statement.executeUpdate("INSERT INTO registrations VALUES ("
                            + text(learner.email) + "," + entry.getKey() + "," + text(courseKey) + ")");
                }
                for (Map.Entry<String, Double> entry : learner.grades.entrySet()) statement.executeUpdate("INSERT INTO grades VALUES ("
                        + text(learner.email) + "," + text(entry.getKey()) + "," + entry.getValue() + ")");
                for (Integer semester : learner.completedSemesters) statement.executeUpdate("INSERT INTO completed_semesters VALUES ("
                        + text(learner.email) + "," + semester + ")");
            }
            for (Complaint complaint : DataStore.complaints) statement.executeUpdate("INSERT INTO complaints VALUES (" + complaint.id + ","
                    + text(complaint.studentEmail) + "," + text(complaint.description) + "," + text(complaint.createdAt.toString()) + ","
                    + text(complaint.status) + "," + text(complaint.resolutionDetails) + ")");
            for (TeachingAssistant ta : DataStore.teachingAssistants.values()) for (String courseKey : ta.getAssignedCourseKeys()) {
                statement.executeUpdate("INSERT INTO ta_assignments VALUES (" + text(ta.email) + "," + text(courseKey) + ")");
            }
        } finally {
            close(statement, connection);
        }
    }

    private static void insertStudent(Statement statement, Student student, String role) throws SQLException {
        statement.executeUpdate("INSERT INTO students VALUES (" + text(student.email) + "," + text(student.name) + ","
                + text(student.password) + "," + text(student.phone) + "," + text(student.address) + ","
                + student.currentSemester + "," + text(role) + ")");
    }

    /** Escapes a simple text value because this introductory project uses Statement. */
    private static String text(String value) {
        return "'" + (value == null ? "" : value.replace("'", "''")) + "'";
    }

    private static void closeResultSet(ResultSet resultSet) {
        try { if (resultSet != null) resultSet.close(); } catch (SQLException exception) { }
    }

    /** Child Statement is closed before its parent Connection, as in the JDBC notes. */
    private static void close(Statement statement, Connection connection) {
        try { if (statement != null) statement.close(); } catch (SQLException exception) { }
        try { if (connection != null) connection.close(); } catch (SQLException exception) { }
    }
}