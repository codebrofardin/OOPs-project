import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** TA dashboard. A TA has student features plus restricted grading assistance. */
public final class TeachingAssistantService {
    private TeachingAssistantService() { }

    public static void menu(TeachingAssistant ta) {
        while (true) {
            System.out.println("\n===== TEACHING ASSISTANT DASHBOARD: " + ta.name + " =====");
            System.out.println("1. Open student dashboard (all student features)");
            System.out.println("2. View assigned courses");
            System.out.println("3. View enrolled students and grades");
            System.out.println("4. Assign/update grades for assigned courses");
            System.out.println("0. Logout");
            int choice = AppInput.readInt("Choose: ");
            switch (choice) {
                case 1:
                    // Inheritance: TA is a Student and can use the existing student service.
                    StudentService.menu(ta);
                    break;
                case 2: viewAssignedCourses(ta); break;
                case 3: viewEnrolledStudentsAndGrades(ta); break;
                case 4: assignOrUpdateGrade(ta); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static List<Course> assignedCourses(TeachingAssistant ta) {
        List<Course> result = new ArrayList<>();
        for (String key : ta.getAssignedCourseKeys()) {
            Course course = DataStore.courses.get(key);
            if (course != null) result.add(course);
        }
        return result;
    }

    private static Course chooseAssignedCourse(TeachingAssistant ta) {
        List<Course> list = assignedCourses(ta);
        if (list.isEmpty()) {
            System.out.println("No courses are assigned to you. Contact the administrator.");
            return null;
        }
        System.out.println("Your assigned courses:");
        for (int i = 0; i < list.size(); i++) {
            Course c = list.get(i);
            System.out.println((i + 1) + ". " + c.displayCode() + " - " + c.title
                    + " | Professor: " + c.professorName);
        }
        int number = AppInput.readInt("Select course: ");
        if (number < 1 || number > list.size()) {
            System.out.println("Invalid course selection.");
            return null;
        }
        return list.get(number - 1);
    }

    private static void viewAssignedCourses(TeachingAssistant ta) {
        List<Course> list = assignedCourses(ta);
        if (list.isEmpty()) {
            System.out.println("No courses are assigned to you yet.");
            return;
        }
        for (Course course : list) course.display();
    }

    private static void viewEnrolledStudentsAndGrades(TeachingAssistant ta) {
        Course course = chooseAssignedCourse(ta);
        if (course == null) return;
        boolean found = false;
        System.out.println("\nSTUDENT GRADES - " + course.displayCode());
        for (Student student : DataStore.allLearners()) {
            Set<String> registered = student.registrations.getOrDefault(
                    course.semester, new LinkedHashSet<String>());
            if (registered.contains(course.uniqueKey())) {
                found = true;
                Double grade = student.grades.get(course.uniqueKey());
                System.out.println("Name: " + student.name + " | Email: " + student.email
                        + " | Grade: " + (grade == null ? "Not assigned" : grade + "/10")
                        + " | Semester: " + student.currentSemester);
            }
        }
        if (!found) System.out.println("No learners are enrolled in this course/batch.");
    }

    private static void assignOrUpdateGrade(TeachingAssistant ta) {
        Course course = chooseAssignedCourse(ta);
        if (course == null) return;

        List<Student> enrolledLearners = new ArrayList<>();
        for (Student student : DataStore.allLearners()) {
            if (student.email.equalsIgnoreCase(ta.email)) continue; // No self-grading.
            Set<String> registered = student.registrations.getOrDefault(
                    course.semester, new LinkedHashSet<String>());
            if (registered.contains(course.uniqueKey())) enrolledLearners.add(student);
        }

        if (enrolledLearners.isEmpty()) {
            System.out.println("No other learners are registered for this course.");
            return;
        }

        System.out.println("Enrolled learners:");
        for (int i = 0; i < enrolledLearners.size(); i++) {
            Student student = enrolledLearners.get(i);
            Double oldGrade = student.grades.get(course.uniqueKey());
            System.out.println((i + 1) + ". " + student.name + " | " + student.email
                    + " | Current grade: " + (oldGrade == null ? "Not assigned" : oldGrade));
        }
        int number = AppInput.readInt("Select learner: ");
        if (number < 1 || number > enrolledLearners.size()) {
            System.out.println("Invalid learner selection.");
            return;
        }

        Student selected = enrolledLearners.get(number - 1);
        if (selected.completedSemesters.contains(course.semester)) {
            System.out.println("This semester is already completed. TA grade updates are locked.");
            return;
        }

        double grade = AppInput.readDouble("Enter grade point (0 to 10): ");
        if (grade < 0 || grade > 10) {
            System.out.println("Grade must be between 0 and 10.");
            return;
        }
        selected.grades.put(course.uniqueKey(), grade);
        System.out.println("Grade saved/updated for " + selected.name + ".");
        System.out.println("The administrator still completes the semester after all course grades are entered.");
    }
}
