import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Professors may view and update assigned courses, and view enrolled students. */
public final class ProfessorService {
    private ProfessorService() { }

    public static void menu(Professor professor) {
        while (true) {
            System.out.println("\n===== PROFESSOR DASHBOARD: " + professor.name + " =====");
            System.out.println("1. View my assigned courses");
            System.out.println("2. Update a course I teach");
            System.out.println("3. View enrolled students and academic standing");
            System.out.println("0. Logout");
            int choice = AppInput.readInt("Choose: ");
            switch (choice) {
                case 1: viewCourses(professor); break;
                case 2: updateCourse(professor); break;
                case 3: viewEnrolledStudents(professor); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static List<Course> assignedCourses(Professor professor) {
        return DataStore.coursesForProfessor(professor.email);
    }

    private static Course chooseCourse(Professor professor) {
        List<Course> list = assignedCourses(professor);
        if (list.isEmpty()) {
            System.out.println("No courses assigned to you yet. Ask the administrator to assign a course.");
            return null;
        }
        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ". " + list.get(i).displayCode() + " - " + list.get(i).title);
        }
        int number = AppInput.readInt("Select course: ");
        if (number < 1 || number > list.size()) { System.out.println("Invalid selection."); return null; }
        return list.get(number - 1);
    }

    private static void viewCourses(Professor professor) {
        List<Course> list = assignedCourses(professor);
        if (list.isEmpty()) { System.out.println("No courses assigned."); return; }
        for (Course course : list) course.display();
    }

    private static void updateCourse(Professor professor) {
        Course c = chooseCourse(professor);
        if (c == null) return;
        while (true) {
            System.out.println("\nUPDATE COURSE: " + c.displayCode() + " - " + c.title);
            System.out.println("1. Course title");
            System.out.println("2. Syllabus");
            System.out.println("3. Class day and timing");
            System.out.println("4. Classroom / location");
            System.out.println("5. Credits");
            System.out.println("6. Prerequisite course code");
            System.out.println("7. Enrollment limit");
            System.out.println("8. Office hours");
            System.out.println("9. Total / scheduled hours");
            System.out.println("0. Finish updating");
            int choice = AppInput.readInt("Choose field: ");
            switch (choice) {
                case 1: c.title = AppInput.read("New title: "); break;
                case 2: c.syllabus = AppInput.read("Syllabus details: "); break;
                case 3:
                    c.day = AppInput.read("Day (e.g. Monday): ");
                    c.timing = AppInput.read("Time range (e.g. 09:00-10:00): ");
                    break;
                case 4: c.room = AppInput.read("Room/location: "); break;
                case 5:
                    double credits = AppInput.readPositiveDouble("New credits: ");
                    c.credits = credits;
                    System.out.println("Credits updated. Existing student registrations are not automatically removed if the limit is exceeded.");
                    break;
                case 6: c.prerequisite = AppInput.read("Prerequisite course code (blank for none): ").toUpperCase(); break;
                case 7:
                    int limit = AppInput.readInt("New enrollment limit: ");
                    if (limit > 0) c.enrollmentLimit = limit; else System.out.println("Limit must be positive.");
                    break;
                case 8: c.officeHours = AppInput.read("Office hours: "); break;
                case 9:
                    c.totalHours = AppInput.readPositiveDouble("Total hours: ");
                    c.scheduledHours = AppInput.readPositiveDouble("Scheduled hours: ");
                    break;
                case 0:
                    DataStore.saveAll();
                    System.out.println("Course updated.");
                    return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void viewEnrolledStudents(Professor professor) {
        List<Course> list = assignedCourses(professor);
        if (list.isEmpty()) { System.out.println("No courses assigned."); return; }
        Course course = chooseCourse(professor);
        if (course == null) return;
        boolean found = false;
        System.out.println("\nENROLLED STUDENTS - " + course.displayCode());
        for (Student student : DataStore.allLearners()) {
            Set<String> registered = student.registrations.getOrDefault(course.semester, new LinkedHashSet<>());
            if (registered.contains(course.uniqueKey())) {
                found = true;
                System.out.println("Name: " + student.name + " | Email: " + student.email
                        + " | Phone: " + student.phone + " | Current semester: " + student.currentSemester
                        + " | CGPA: " + String.format("%.2f", StudentService.calculateCGPA(student)));
                Double grade = student.grades.get(course.uniqueKey());
                System.out.println("Grade in this course: " + (grade == null ? "Not assigned" : grade + "/10"));
            }
        }
        if (!found) System.out.println("No students enrolled in this course/batch.");
    }
}