import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** All student dashboard functions. */
public final class StudentService {
    private StudentService() { }

    public static void menu(Student student) {
        while (true) {
            System.out.println("\n===== STUDENT DASHBOARD: " + student.name + " =====");
            System.out.println("Current semester: " + student.currentSemester);
            System.out.println("1. View available courses");
            System.out.println("2. Register for a course");
            System.out.println("3. View weekly schedule");
            System.out.println("4. Track academic progress (grades / SGPA / CGPA)");
            System.out.println("5. Drop a course");
            System.out.println("6. Submit a complaint");
            System.out.println("7. View my complaints");
            System.out.println("0. Logout");
            int choice = AppInput.readInt("Choose: ");
            switch (choice) {
                case 1: viewAvailableCourses(student); break;
                case 2: registerCourse(student); break;
                case 3: viewSchedule(student); break;
                case 4: academicProgress(student); break;
                case 5: dropCourse(student); break;
                case 6: submitComplaint(student); break;
                case 7: viewComplaints(student); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static int selectCurrentSemester(Student student) {
        System.out.println("Your current semester is " + student.currentSemester + ".");
        int semester = AppInput.readInt("Select semester: ");
        if (semester != student.currentSemester) {
            System.out.println("You can access courses only in your current semester."); return -1;
        }
        return semester;
    }

    private static void viewAvailableCourses(Student student) {
        int semester = selectCurrentSemester(student);
        if (semester < 0) return;
        List<Course> list = DataStore.coursesForSemester(semester);
        System.out.println("\nAVAILABLE COURSES - SEMESTER " + semester);
        if (list.isEmpty()) {
            System.out.println("No courses have been added for this semester yet. Contact the administrator.");
            return;
        }
        for (Course course : list) course.display();
    }

    private static void registerCourse(Student student) {
        int semester = selectCurrentSemester(student);
        if (semester < 0) return;
        List<Course> list = DataStore.coursesForSemester(semester);
        if (list.isEmpty()) { System.out.println("No courses available for this semester."); return; }
        System.out.println("\nCOURSES AVAILABLE FOR REGISTRATION");
        for (int i = 0; i < list.size(); i++) {
            System.out.print((i + 1) + ". "); list.get(i).display();
        }
        int number = AppInput.readInt("Select course number: ");
        if (number < 1 || number > list.size()) { System.out.println("Invalid course selection."); return; }
        Course course = list.get(number - 1);
        LinkedHashSet<String> registered = student.registrations.computeIfAbsent(semester, k -> new LinkedHashSet<>());
        if (registered.contains(course.uniqueKey())) { System.out.println("Already registered for this course/batch."); return; }
        if (!prerequisiteMet(student, course)) {
            System.out.println("Prerequisite is not completed: " + course.prerequisite); return;
        }
        if (enrollmentCount(course) >= course.enrollmentLimit) {
            System.out.println("This course/batch has reached its enrollment limit."); return;
        }
        double newCredits = registeredCredits(student, semester) + course.credits;
        if (newCredits > DataStore.CREDIT_LIMIT + 0.000001) {
            System.out.printf("Credit limit exceeded. Maximum: %d, current: %.2f, requested total: %.2f%n",
                    DataStore.CREDIT_LIMIT, registeredCredits(student, semester), newCredits); return;
        }
        if (hasScheduleClash(student, course, semester)) {
            System.out.println("Schedule clash detected. Ask the professor/admin to update the class timing."); return;
        }
        registered.add(course.uniqueKey());
        System.out.println("Successfully registered for " + course.title + " (" + course.shortName + ").");
        System.out.printf("Registered credits: %.2f / %d%n", newCredits, DataStore.CREDIT_LIMIT);
    }

    private static int enrollmentCount(Course course) {
        int count = 0;
        for (Student student : DataStore.allLearners()) {
            Set<String> selected = student.registrations.getOrDefault(course.semester, new LinkedHashSet<>());
            if (selected.contains(course.uniqueKey())) count++;
        }
        return count;
    }

    private static boolean prerequisiteMet(Student student, Course course) {
        if (course.prerequisite == null || course.prerequisite.trim().isEmpty()) return true;
        for (Map.Entry<String, Double> entry : student.grades.entrySet()) {
            Course completedCourse = DataStore.courses.get(entry.getKey());
            if (completedCourse != null && completedCourse.code.equalsIgnoreCase(course.prerequisite)
                    && student.completedSemesters.contains(completedCourse.semester)) return true;
        }
        return false;
    }

    private static double registeredCredits(Student student, int semester) {
        double total = 0;
        for (String key : student.registrations.getOrDefault(semester, new LinkedHashSet<>())) {
            Course course = DataStore.courses.get(key);
            if (course != null) total += course.credits;
        }
        return total;
    }

    private static boolean hasScheduleClash(Student student, Course newCourse, int semester) {
        if (newCourse.day.isEmpty() || newCourse.timing.isEmpty()) return false;
        for (String key : student.registrations.getOrDefault(semester, new LinkedHashSet<>())) {
            Course old = DataStore.courses.get(key);
            if (old != null && old.day.equalsIgnoreCase(newCourse.day)
                    && timesOverlap(old.timing, newCourse.timing)) return true;
        }
        return false;
    }

    private static boolean timesOverlap(String first, String second) {
        try {
            String[] a = first.split("-"); String[] b = second.split("-");
            if (a.length != 2 || b.length != 2) return first.equalsIgnoreCase(second);
            DateTimeFormatter format = DateTimeFormatter.ofPattern("H:mm");
            LocalTime aStart = LocalTime.parse(a[0].trim(), format);
            LocalTime aEnd = LocalTime.parse(a[1].trim(), format);
            LocalTime bStart = LocalTime.parse(b[0].trim(), format);
            LocalTime bEnd = LocalTime.parse(b[1].trim(), format);
            return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
        } catch (Exception e) {
            return first.equalsIgnoreCase(second);
        }
    }

    private static void viewSchedule(Student student) {
        Set<String> selected = student.registrations.getOrDefault(student.currentSemester, new LinkedHashSet<>());
        if (selected.isEmpty()) { System.out.println("No courses registered in the current semester."); return; }
        System.out.println("\nWEEKLY SCHEDULE - SEMESTER " + student.currentSemester);
        AppInput.line();
        for (String key : selected) {
            Course c = DataStore.courses.get(key);
            System.out.printf("%-9s %-13s %-38s | %-10s %-13s | %-15s | %s%n",
                    c.code, c.shortName, c.title, notSet(c.day), notSet(c.timing), notSet(c.room), c.professorName);
        }
        System.out.println("The source timetable did not contain days, class times or rooms; these show as Not set until updated.");
    }

    private static String notSet(String value) { return value == null || value.trim().isEmpty() ? "Not set" : value; }

    private static void dropCourse(Student student) {
        int semester = selectCurrentSemester(student);
        if (semester < 0) return;
        Set<String> registered = student.registrations.getOrDefault(semester, new LinkedHashSet<>());
        if (registered.isEmpty()) { System.out.println("No courses to drop."); return; }
        int i = 1;
        for (String key : registered) {
            Course c = DataStore.courses.get(key);
            if (c != null) System.out.println((i++) + ". " + c.displayCode() + " - " + c.title);
        }
        int selected = AppInput.readInt("Select course number to drop: ");
        if (selected < 1 || selected > registered.size()) { System.out.println("Invalid selection."); return; }
        String key = (String) registered.toArray()[selected - 1];
        registered.remove(key);
        student.grades.remove(key);
        System.out.println("Course dropped successfully.");
    }

    private static void submitComplaint(Student student) {
        String description = AppInput.read("Describe your issue: ");
        if (description.isEmpty()) { System.out.println("Complaint description cannot be empty."); return; }
        DataStore.complaints.add(new Complaint(DataStore.nextComplaintId(), student.email, description));
        System.out.println("Complaint submitted. Status: Pending.");
    }

    private static void viewComplaints(Student student) {
        boolean found = false;
        for (Complaint c : DataStore.complaints) {
            if (c.studentEmail.equalsIgnoreCase(student.email)) {
                found = true;
                System.out.println("ID: " + c.id + " | Date: " + c.dateText() + " | Status: " + c.status);
                System.out.println("Description: " + c.description);
                if (!c.resolutionDetails.isEmpty()) System.out.println("Resolution: " + c.resolutionDetails);
            }
        }
        if (!found) System.out.println("No complaints submitted.");
    }

    private static void academicProgress(Student student) {
        System.out.println("\nACADEMIC PROGRESS");
        boolean found = false;
        for (int semester : new TreeSet<>(student.completedSemesters)) {
            System.out.println("\nSemester " + semester + " grades:");
            for (Map.Entry<String, Double> entry : student.grades.entrySet()) {
                Course course = DataStore.courses.get(entry.getKey());
                if (course != null && course.semester == semester) {
                    found = true;
                    System.out.printf("%s (%s) - %s : %.2f / 10%n",
                            course.code, course.shortName, course.title, entry.getValue());
                }
            }
            System.out.printf("SGPA: %.2f%n", calculateSGPA(student, semester));
        }
        if (!found) System.out.println("No completed-course grades yet. Grades/GPA are shown after admin completes a semester.");
        System.out.printf("CGPA: %.2f%n", calculateCGPA(student));
    }

    public static double calculateSGPA(Student student, int semester) {
        if (!student.completedSemesters.contains(semester)) return 0;
        double weighted = 0, credits = 0;
        for (Map.Entry<String, Double> entry : student.grades.entrySet()) {
            Course course = DataStore.courses.get(entry.getKey());
            if (course != null && course.semester == semester) {
                weighted += entry.getValue() * course.credits; credits += course.credits;
            }
        }
        return credits == 0 ? 0 : weighted / credits;
    }

    public static double calculateCGPA(Student student) {
        double weighted = 0, credits = 0;
        for (Map.Entry<String, Double> entry : student.grades.entrySet()) {
            Course course = DataStore.courses.get(entry.getKey());
            if (course != null && student.completedSemesters.contains(course.semester)) {
                weighted += entry.getValue() * course.credits; credits += course.credits;
            }
        }
        return credits == 0 ? 0 : weighted / credits;
    }
}
