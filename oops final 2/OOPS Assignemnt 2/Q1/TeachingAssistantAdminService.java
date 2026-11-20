import java.util.ArrayList;
import java.util.List;

/** Administrator-only controls for creating and assigning TA demo accounts. */
public final class TeachingAssistantAdminService {
    private TeachingAssistantAdminService() { }

    public static void menu() {
        while (true) {
            System.out.println("\n--- MANAGE TEACHING ASSISTANTS ---");
            System.out.println("1. View TA accounts and assigned courses");
            System.out.println("2. Assign a course to a TA");
            System.out.println("3. Remove a course assignment from a TA");
            System.out.println("0. Back");
            int choice = AppInput.readInt("Choose: ");
            switch (choice) {
                case 1: listTAs(); break;
                case 2: changeAssignment(true); break;
                case 3: changeAssignment(false); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void listTAs() {
        if (DataStore.teachingAssistants.isEmpty()) {
            System.out.println("No TA accounts are configured.");
            return;
        }
        for (TeachingAssistant ta : DataStore.teachingAssistants.values()) {
            System.out.println("\n" + ta.name + " | " + ta.email);
            if (ta.getAssignedCourseKeys().isEmpty()) {
                System.out.println("  No course assignments");
            } else {
                for (String key : ta.getAssignedCourseKeys()) {
                    Course c = DataStore.courses.get(key);
                    if (c != null) System.out.println("  " + c.displayCode() + " - " + c.title);
                }
            }
        }
    }

    private static void changeAssignment(boolean assign) {
        List<TeachingAssistant> assistants = new ArrayList<>(DataStore.teachingAssistants.values());
        if (assistants.isEmpty()) {
            System.out.println("No TA accounts are configured.");
            return;
        }
        for (int i = 0; i < assistants.size(); i++) {
            TeachingAssistant ta = assistants.get(i);
            System.out.println((i + 1) + ". " + ta.name + " | " + ta.email);
        }
        int taNumber = AppInput.readInt("Select TA: ");
        if (taNumber < 1 || taNumber > assistants.size()) {
            System.out.println("Invalid TA selection.");
            return;
        }
        TeachingAssistant ta = assistants.get(taNumber - 1);

        List<Course> courses = new ArrayList<>(DataStore.courses.values());
        if (courses.isEmpty()) {
            System.out.println("Course catalogue is empty.");
            return;
        }
        for (int i = 0; i < courses.size(); i++) {
            Course c = courses.get(i);
            System.out.println((i + 1) + ". " + c.displayCode() + " - " + c.title
                    + " | Professor: " + c.professorName);
        }
        int courseNumber = AppInput.readInt("Select course: ");
        if (courseNumber < 1 || courseNumber > courses.size()) {
            System.out.println("Invalid course selection.");
            return;
        }
        Course course = courses.get(courseNumber - 1);
        if (assign) {
            if (ta.isAssignedTo(course.uniqueKey())) {
                System.out.println("This TA is already assigned to this course.");
            } else {
                ta.assignCourse(course.uniqueKey());
                System.out.println(ta.name + " assigned to " + course.displayCode() + ".");
            }
        } else {
            if (ta.isAssignedTo(course.uniqueKey())) {
                ta.unassignCourse(course.uniqueKey());
                System.out.println("Course assignment removed.");
            } else {
                System.out.println("This TA is not assigned to that course.");
            }
        }
    }
}
