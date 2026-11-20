import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared in-memory data. Data resets when the application is closed. */
public final class DataStore {
    private DataStore() { }

    public static final int CREDIT_LIMIT = 20;
    public static final String ADMIN_EMAIL = "admin@college.com";
    public static final String ADMIN_PASSWORD = "Admin@123";

    public static final Map<String, Student> students = new LinkedHashMap<>();
    public static final Map<String, TeachingAssistant> teachingAssistants = new LinkedHashMap<>();
    public static final Map<String, Professor> professors = new LinkedHashMap<>();
    public static final Map<String, Course> courses = new LinkedHashMap<>();
    public static final List<Complaint> complaints = new ArrayList<>();
    private static int complaintSequence = 1;

    public static int nextComplaintId() { return complaintSequence++; }

    /** Returns normal students and TAs as Student objects for shared student operations. */
    public static List<Student> allLearners() {
        List<Student> result = new ArrayList<>(students.values());
        result.addAll(teachingAssistants.values());
        return result;
    }

    /** Preloaded role-specific TA accounts for the three people named by the user. */
    public static void loadTeachingAssistants() {
        addTeachingAssistant("Kush Patel", "kush.patel@college.com", "TA@123");
        addTeachingAssistant("Aesha Shah", "aesha.shah@college.com", "TA@123");
        addTeachingAssistant("Meet Christian", "meet.christian@college.com", "TA@123");
    }

    private static void addTeachingAssistant(String name, String email, String password) {
        if (!teachingAssistants.containsKey(email)) {
            teachingAssistants.put(email, new TeachingAssistant(name, email, password));
        }
    }

    public static void addCourse(Course course) {
        String key = course.uniqueKey();
        if (courses.containsKey(key)) throw new IllegalArgumentException("Course code + short name already exists.");
        courses.put(key, course);
    }

    public static List<Course> coursesForSemester(int semester) {
        List<Course> result = new ArrayList<>();
        for (Course course : courses.values()) if (course.semester == semester) result.add(course);
        return result;
    }

    public static List<Course> coursesForProfessor(String professorEmail) {
        List<Course> result = new ArrayList<>();
        for (Course course : courses.values()) {
            if (course.professorEmail.equalsIgnoreCase(professorEmail)) result.add(course);
        }
        return result;
    }

    /** Values below are transcribed from the user's timetable image. */
    public static void loadInitialCourses() {
        if (!courses.isEmpty()) return;
        // Course codes in the source are CSL/CSP/TLL/MAL/IDL-2xx. Semester 2 is used.
        addCourse(new Course("CSL201", "Data Structures and Algorithms", "DSA", 2,
                3.0, 45, 51, "Dr. Shweta Saharan", ""));

        addCourse(new Course("CSP202", "Data Structures and Algorithms Lab", "DSAL-B1", 2,
                1.0, 30, 28.5, "Dr. Shweta Saharan", ""));
        addCourse(new Course("CSP202", "Data Structures and Algorithms Lab", "DSAL-B2", 2,
                1.0, 30, 27, "Dr. Shweta Saharan", ""));

        addCourse(new Course("CSL203", "Object Oriented Programming", "OOP", 2,
                2.0, 30, 30, "Dr. Praveen Chandaliya", "Visiting Faculty"));
        addCourse(new Course("CSP213", "Mathematics for Artificial Intelligence", "MAI", 2,
                2.0, 30, 30, "Dr. Mahipal Jadeja", "Visiting Faculty"));
        addCourse(new Course("CSL210", "Pattern Recognition and Machine Learning", "PRML", 2,
                3.0, 45, 45, "Dr. Pruthwik Mishra", "Visiting Faculty"));

        addCourse(new Course("CSP211", "Programming for AI and ML Lab", "AI/MLL-B1", 2,
                1.0, 30, 30, "Dr. Praveen Chandaliya", "Visiting Faculty"));
        addCourse(new Course("CSP211", "Programming for AI and ML Lab", "AI/MLL-B2", 2,
                1.0, 30, 30, "Dr. Pruthwik Mishra", "Visiting Faculty"));

        addCourse(new Course("TLL201", "Logistics and Supply Chain management", "LSCM", 2,
                2.0, 30, 30, "Prof. Kaushik Das", ""));
        addCourse(new Course("MAL204", "Discrete Mathematics", "DM", 2,
                3.0, 45, 46.5, "Dr. Pradeep Saroj", ""));

        addCourse(new Course("IDL230", "Digital Electronics", "DE", 2,
                2.5, 38, 37.5, "Dr. Abhilasha Saxena", ""));
        addCourse(new Course("IDL230", "Digital Electronics Lab", "DEL-B1", 2,
                0.5, 15, 15, "Dr. Abhilasha Saxena", ""));
        addCourse(new Course("IDL230", "Digital Electronics Lab", "DEL-B2", 2,
                0.5, 15, 15, "Dr. Abhilasha Saxena", ""));

        addCourse(new Course("IDL424", "Entrepreneurship and Innovation", "EI", 2,
                2.0, 15, 15, "Dr. Prasiddh Trivedi", ""));
    }
}
