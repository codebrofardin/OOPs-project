/** Entry point for the Course Registration System. */
public class Main {
    public static void main(String[] args) {
        DataStore.loadInitialCourses();
        DataStore.loadTeachingAssistants();
        System.out.println("============================================================");
        System.out.println("             COLLEGE COURSE REGISTRATION SYSTEM");
        System.out.println("============================================================");
        while (true) {
            System.out.println("\nMAIN MENU");
            System.out.println("1. Student");
            System.out.println("2. Professor");
            System.out.println("3. Administrator");
            System.out.println("4. Teaching Assistant");
            System.out.println("0. Exit");
            int choice = AppInput.readInt("Choose role: ");
            switch (choice) {
                case 1: AuthService.studentEntry(); break;
                case 2: AuthService.professorEntry(); break;
                case 3: AuthService.adminLogin(); break;
                case 4: AuthService.teachingAssistantLogin(); break;
                case 0:
                    System.out.println("Thank you for using the system.");
                    return;
                default: System.out.println("Invalid choice.");
            }
        }
    }
}
