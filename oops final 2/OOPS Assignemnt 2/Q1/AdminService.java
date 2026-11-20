import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Full administrator dashboard: catalog, student records, faculty assignment and complaints. */
public final class AdminService {
    private AdminService() { }

    /** Allows this class to be launched directly; delegates to the application entry point. */
    public static void main(String[] args) {
        Main.main(args);
    }

    public static void menu() {
        while (true) {
            System.out.println("\n================ ADMINISTRATOR DASHBOARD ================");
            System.out.println("1. Manage course catalog");
            System.out.println("2. Manage student records and grades");
            System.out.println("3. Assign professors to courses");
            System.out.println("4. Handle complaints");
            System.out.println("5. View registered professors");
            System.out.println("6. Manage Teaching Assistants");
            System.out.println("0. Logout");
            int choice = AppInput.readInt("Choose: ");
            switch (choice) {
                case 1: catalogMenu(); break;
                case 2: studentRecordsMenu(); break;
                case 3: assignProfessor(); break;
                case 4: complaintsMenu(); break;
                case 5: listProfessors(); break;
                case 6: TeachingAssistantAdminService.menu(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------- CATALOG ----------------
    private static void catalogMenu() {
        while (true) {
            System.out.println("\n--- MANAGE COURSE CATALOG ---");
            System.out.println("1. View all courses");
            System.out.println("2. Add a course");
            System.out.println("3. Delete a course");
            System.out.println("4. Edit course details");
            System.out.println("0. Back");
            int choice = AppInput.readInt("Choose: ");
            switch (choice) {
                case 1: listCourses(); break;
                case 2: addCourse(); break;
                case 3: deleteCourse(); break;
                case 4: editCourse(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void listCourses() {
        if (DataStore.courses.isEmpty()) { System.out.println("Course catalog is empty."); return; }
        for (Course course : DataStore.courses.values()) course.display();
    }

    private static void addCourse() {
        System.out.println("Enter details for the new course.");
        String code = AppInput.read("Course code: ").toUpperCase();
        String title = AppInput.read("Course title: ");
        String shortName = AppInput.read("Short name / batch: ").toUpperCase();
        int semester = AppInput.readInt("Semester number: ");
        double credits = AppInput.readPositiveDouble("Credits: ");
        double totalHours = AppInput.readPositiveDouble("Total hours: ");
        double scheduledHours = AppInput.readPositiveDouble("Scheduled hours: ");
        String professor = AppInput.read("Faculty name: ");
        String remarks = AppInput.read("Remarks (optional): ");
        if (code.isEmpty() || title.isEmpty() || shortName.isEmpty() || semester < 1 || professor.isEmpty()) {
            System.out.println("Required course details are missing."); return;
        }
        Course course = new Course(code, title, shortName, semester, credits,
                totalHours, scheduledHours, professor, remarks);
        course.prerequisite = AppInput.read("Prerequisite course code (blank for none): ").toUpperCase();
        course.day = AppInput.read("Class day (optional): ");
        course.timing = AppInput.read("Class time range HH:mm-HH:mm (optional): ");
        course.room = AppInput.read("Room/location (optional): ");
        course.syllabus = AppInput.read("Syllabus (optional): ");
        course.officeHours = AppInput.read("Faculty office hours (optional): ");
        int limit = AppInput.readInt("Enrollment limit: ");
        if (limit > 0) course.enrollmentLimit = limit;
        try {
            DataStore.addCourse(course);
            DataStore.saveAll();
            System.out.println("Course added successfully.");
        } catch (IllegalArgumentException e) { System.out.println(e.getMessage()); }
    }

    private static Course selectCourse() {
        List<Course> all = new ArrayList<>(DataStore.courses.values());
        if (all.isEmpty()) { System.out.println("No courses in catalog."); return null; }
        for (int i = 0; i < all.size(); i++) {
            System.out.println((i + 1) + ". " + all.get(i).displayCode() + " - "
                    + all.get(i).title + " (Semester " + all.get(i).semester + ")");
        }
        int choice = AppInput.readInt("Select course number: ");
        if (choice < 1 || choice > all.size()) { System.out.println("Invalid selection."); return null; }
        return all.get(choice - 1);
    }

    private static void deleteCourse() {
        Course course = selectCourse();
        if (course == null) return;
        String confirm = AppInput.read("Delete " + course.displayCode() + "? Type YES to confirm: ");
        if (!confirm.equalsIgnoreCase("YES")) { System.out.println("Deletion cancelled."); return; }
        String key = course.uniqueKey();
        DataStore.courses.remove(key);
        for (Student student : DataStore.allLearners()) {
            for (Set<String> registrations : student.registrations.values()) registrations.remove(key);
            student.grades.remove(key);
        }
        DataStore.saveAll();
        System.out.println("Course deleted from catalog and student registrations.");
    }

    private static void editCourse() {
        Course c = selectCourse();
        if (c == null) return;
        System.out.println("Current details:"); c.display();
        System.out.println("1. Title  2. Credits  3. Prerequisite  4. Professor display name");
        System.out.println("5. Total/scheduled hours  6. Day/time  7. Room  8. Enrollment limit");
        System.out.println("9. Syllabus  10. Office hours  11. Remarks");
        int choice = AppInput.readInt("Field to edit: ");
        switch (choice) {
            case 1: c.title = AppInput.read("New title: "); break;
            case 2: c.credits = AppInput.readPositiveDouble("New credits: "); break;
            case 3: c.prerequisite = AppInput.read("Prerequisite code (blank for none): ").toUpperCase(); break;
            case 4: c.professorName = AppInput.read("Faculty display name: "); c.professorEmail = ""; break;
            case 5:
                c.totalHours = AppInput.readPositiveDouble("Total hours: ");
                c.scheduledHours = AppInput.readPositiveDouble("Scheduled hours: "); break;
            case 6:
                c.day = AppInput.read("Day: "); c.timing = AppInput.read("Time range HH:mm-HH:mm: "); break;
            case 7: c.room = AppInput.read("Room/location: "); break;
            case 8:
                int limit = AppInput.readInt("Enrollment limit: ");
                if (limit > 0) c.enrollmentLimit = limit; else System.out.println("Limit must be positive."); break;
            case 9: c.syllabus = AppInput.read("Syllabus: "); break;
            case 10: c.officeHours = AppInput.read("Office hours: "); break;
            case 11: c.remarks = AppInput.read("Remarks: "); break;
            default: System.out.println("Invalid field."); return;
        }
        DataStore.saveAll();
        System.out.println("Course updated.");
    }

    // ---------------- STUDENT RECORDS ----------------
    private static void studentRecordsMenu() {
        while (true) {
            System.out.println("\n--- MANAGE STUDENT RECORDS ---");
            System.out.println("1. View all students");
            System.out.println("2. View student record");
            System.out.println("3. Update student personal information");
            System.out.println("4. Assign/update a course grade");
            System.out.println("5. Complete student's semester");
            System.out.println("0. Back");
            int choice = AppInput.readInt("Choose: ");
            switch (choice) {
                case 1: listStudents(); break;
                case 2: viewStudentRecord(); break;
                case 3: updateStudentInformation(); break;
                case 4: assignGrade(); break;
                case 5: finalizeSemester(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void listStudents() {
        if (DataStore.students.isEmpty()) { System.out.println("No students registered."); return; }
        for (Student s : DataStore.students.values()) {
            System.out.println(s.name + " | " + s.email + " | Phone: " + s.phone
                    + " | Current semester: " + s.currentSemester
                    + " | CGPA: " + String.format("%.2f", StudentService.calculateCGPA(s)));
        }
    }

    private static Student findStudent() {
        String email = AppInput.read("Student email: ").toLowerCase();
        Student student = DataStore.students.get(email);
        if (student == null) System.out.println("Student not found.");
        return student;
    }

    private static void viewStudentRecord() {
        Student s = findStudent();
        if (s == null) return;
        System.out.println("\nSTUDENT RECORD");
        System.out.println("Name: " + s.name + " | Email: " + s.email + " | Phone: " + s.phone);
        System.out.println("Address: " + s.address + " | Current semester: " + s.currentSemester);
        System.out.println("Completed semesters: " + s.completedSemesters);
        for (Map.Entry<String, Double> entry : s.grades.entrySet()) {
            Course c = DataStore.courses.get(entry.getKey());
            String label = c == null ? entry.getKey() : c.displayCode() + " - " + c.title;
            System.out.println(label + " : " + entry.getValue() + "/10");
        }
        System.out.printf("CGPA: %.2f%n", StudentService.calculateCGPA(s));
    }

    private static void updateStudentInformation() {
        Student s = findStudent();
        if (s == null) return;
        System.out.println("1. Name\n2. Phone\n3. Address\n4. Password");
        int choice = AppInput.readInt("Select information to update: ");
        switch (choice) {
            case 1: s.name = AppInput.read("New name: "); break;
            case 2: s.phone = AppInput.read("New phone: "); break;
            case 3: s.address = AppInput.read("New address: "); break;
            case 4:
                String password = AppInput.read("New password (minimum 4 characters): ");
                if (password.length() >= 4) s.password = password;
                else System.out.println("Password is too short.");
                break;
            default: System.out.println("Invalid choice."); return;
        }
        DataStore.saveAll();
        System.out.println("Student information updated.");
    }

    /** Grades are assigned/updated by the administrator for any course the student has taken. */
    private static void assignGrade() {
        Student s = findStudent();
        if (s == null) return;
        List<Course> list = new ArrayList<>();
        for (Map.Entry<Integer, LinkedHashSet<String>> semesterEntry : s.registrations.entrySet()) {
            for (String key : semesterEntry.getValue()) {
                Course c = DataStore.courses.get(key);
                if (c != null && !list.contains(c)) list.add(c);
            }
        }
        if (list.isEmpty()) {
            System.out.println("Student has no registered courses.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Course c = list.get(i);
            Double oldGrade = s.grades.get(c.uniqueKey());
            System.out.println((i + 1) + ". " + c.displayCode() + " - " + c.title
                    + " | Semester " + c.semester + " | Current grade: "
                    + (oldGrade == null ? "Not assigned" : oldGrade));
        }
        int number = AppInput.readInt("Select registered course: ");
        if (number < 1 || number > list.size()) { System.out.println("Invalid selection."); return; }
        Course selected = list.get(number - 1);
        double grade = AppInput.readDouble("Enter grade point from 0 to 10: ");
        if (grade < 0 || grade > 10) { System.out.println("Grade must be between 0 and 10."); return; }
        s.grades.put(selected.uniqueKey(), grade);
        DataStore.saveAll();
        System.out.println("Grade saved/updated by administrator.");
        if (s.completedSemesters.contains(selected.semester)) {
            System.out.printf("Updated CGPA: %.2f%n", StudentService.calculateCGPA(s));
        }
    }

    private static void finalizeSemester() {
        Student s = findStudent();
        if (s == null) return;
        int semester = s.currentSemester;
        Set<String> enrolled = s.registrations.getOrDefault(semester, new LinkedHashSet<>());
        if (enrolled.isEmpty()) { System.out.println("Student has no registered courses in this semester."); return; }
        List<String> pending = new ArrayList<>();
        for (String key : enrolled) {
            if (!s.grades.containsKey(key)) {
                Course c = DataStore.courses.get(key);
                pending.add(c == null ? key : c.displayCode());
            }
        }
        if (!pending.isEmpty()) {
            System.out.println("Cannot complete semester. Grades are pending for: " + pending);
            return;
        }
        s.completedSemesters.add(semester);
        double sgpa = StudentService.calculateSGPA(s, semester);
        System.out.printf("Semester %d completed. SGPA: %.2f%n", semester, sgpa);
        s.currentSemester++;
        DataStore.saveAll();
        System.out.println("Student's current semester is now " + s.currentSemester + ".");
    }

    // ---------------- PROFESSOR ASSIGNMENT ----------------
    private static void assignProfessor() {
        Course course = selectCourse();
        if (course == null) return;
        if (DataStore.professors.isEmpty()) {
            System.out.println("No professor accounts exist. A professor must sign up first."); return;
        }
        List<Professor> list = new ArrayList<>(DataStore.professors.values());
        System.out.println("Registered professors:");
        for (int i = 0; i < list.size(); i++) {
            Professor p = list.get(i);
            System.out.println((i + 1) + ". " + p.name + " | " + p.email + " | Expertise: " + p.expertise);
        }
        int number = AppInput.readInt("Select professor to assign: ");
        if (number < 1 || number > list.size()) { System.out.println("Invalid selection."); return; }
        Professor professor = list.get(number - 1);
        course.professorName = professor.name;
        course.professorEmail = professor.email;
        DataStore.saveAll();
        System.out.println(professor.name + " assigned to " + course.displayCode() + ".");
    }

    private static void listProfessors() {
        if (DataStore.professors.isEmpty()) { System.out.println("No professor accounts registered."); return; }
        for (Professor p : DataStore.professors.values()) {
            System.out.println(p.name + " | " + p.email + " | Phone: " + p.phone + " | Expertise: " + p.expertise);
        }
    }

    // ---------------- COMPLAINT MANAGEMENT ----------------
    private static void complaintsMenu() {
        while (true) {
            System.out.println("\n--- HANDLE COMPLAINTS ---");
            System.out.println("1. View all complaints");
            System.out.println("2. Filter by status");
            System.out.println("3. Filter by date");
            System.out.println("4. Update complaint status and resolution details");
            System.out.println("0. Back");
            int choice = AppInput.readInt("Choose: ");
            switch (choice) {
                case 1: showComplaints(DataStore.complaints); break;
                case 2: filterComplaintsByStatus(); break;
                case 3: filterComplaintsByDate(); break;
                case 4: updateComplaint(); break;
                case 0: return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private static void showComplaints(List<Complaint> list) {
        if (list.isEmpty()) { System.out.println("No matching complaints."); return; }
        for (Complaint c : list) {
            Student s = DataStore.students.get(c.studentEmail);
            if (s == null) s = DataStore.teachingAssistants.get(c.studentEmail);
            String studentName = s == null ? "Unknown student" : s.name;
            System.out.println("\nID: " + c.id + " | Date: " + c.dateText() + " | Status: " + c.status);
            System.out.println("Student: " + studentName + " | " + c.studentEmail);
            System.out.println("Description: " + c.description);
            System.out.println("Resolution: " + (c.resolutionDetails.isEmpty() ? "Not provided" : c.resolutionDetails));
        }
    }

    private static void filterComplaintsByStatus() {
        System.out.println("1. Pending\n2. Resolved");
        int choice = AppInput.readInt("Filter: ");
        if (choice != 1 && choice != 2) { System.out.println("Invalid status."); return; }
        String status = choice == 1 ? "Pending" : "Resolved";
        List<Complaint> filtered = new ArrayList<>();
        for (Complaint c : DataStore.complaints) if (c.status.equals(status)) filtered.add(c);
        showComplaints(filtered);
    }

    private static void filterComplaintsByDate() {
        String date = AppInput.read("Enter date (yyyy-MM-dd): ");
        try { LocalDate.parse(date); }
        catch (Exception e) { System.out.println("Invalid date. Use yyyy-MM-dd."); return; }
        List<Complaint> filtered = new ArrayList<>();
        for (Complaint c : DataStore.complaints) if (c.createdAt.toLocalDate().toString().equals(date)) filtered.add(c);
        showComplaints(filtered);
    }

    private static void updateComplaint() {
        showComplaints(DataStore.complaints);
        if (DataStore.complaints.isEmpty()) return;
        int id = AppInput.readInt("Enter complaint ID: ");
        for (Complaint c : DataStore.complaints) {
            if (c.id == id) {
                System.out.println("1. Pending\n2. Resolved");
                int choice = AppInput.readInt("New status: ");
                if (choice == 1) {
                    c.status = "Pending";
                    c.resolutionDetails = "";
                } else if (choice == 2) {
                    String details = AppInput.read("Resolution details: ");
                    if (details.isEmpty()) { System.out.println("Please enter resolution details."); return; }
                    c.status = "Resolved";
                    c.resolutionDetails = details;
                } else { System.out.println("Invalid status."); return; }
                DataStore.saveAll();
                System.out.println("Complaint updated.");
                return;
            }
        }
        System.out.println("Complaint ID not found.");
    }
}