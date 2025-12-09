import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

class Employee {
    String name;
    int age;
    String designation;
    double salary;

    public Employee(String name, int age, String designation, double salary) {
        this.name = name;
        this.age = age;
        this.designation = designation;
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Name: " + name +
                ", Age: " + age +
                ", Designation: " + designation +
                ", Salary: " + salary;
    }
}




public class Bank {

    static Scanner sc = new Scanner(System.in);

 
    private static final String URL = "jdbc:mysql://localhost:3306/testdb"; 
    private static final String USER = "root";      
    private static final String PASSWORD = "root";  

  
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {

        int choice;

        do {
            showMenu();
            choice = readInt("Enter your choice: ");

            if (choice == 1) {
                createEmployees();
            } else if (choice == 2) {
                displayEmployees();
            } else if (choice == 3) {
                raiseSalary();
            } else if (choice == 4) {
                System.out.println("Thank you for using application.");
            } else {
                System.out.println("Invalid choice. Try again!");
            }

        } while (choice != 4);
    }

 
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void showMenu() {
        System.out.println("\n===== EMPLOYEE MENU =====");
        System.out.println("1) Create Employee");
        System.out.println("2) Display Employees");
        System.out.println("3) Raise Salary");
        System.out.println("4) Exit");
    }

    public static void createEmployees() {
        char ch;
        do {

          
            String name;
            while (true) {
                System.out.print("Enter Name");
                name = sc.nextLine().trim();

                
                name = name.replaceAll("\\s+", " ");

                int spaceCount = 0;
                for (char c : name.toCharArray()) {
                    if (c == ' ') spaceCount++;
                }

                if (spaceCount <= 2) {
                    break;
                } else {
                    System.out.println("Invalid name! Name must contain at most 2 spaces. Try again.");
                }
            }

            int age;
            while (true) {
                age = readInt("Enter Age (20-60): ");
                if (age >= 20 && age <= 60)
                    break;
                System.out.println("Age must be between 20 and 60.");
            }

            String desig;
            double salary = 0.0;

            while (true) {
                System.out.print("Enter Designation (P/M/T)");
                desig = sc.nextLine().trim().toUpperCase();

                if (desig.equals("P")) {
                    salary = 20000;
                    break;
                } else if (desig.equals("M")) {
                    salary = 25000;
                    break;
                } else if (desig.equals("T")) {
                    salary = 15000;
                    break;
                } else {
                    System.out.println("Invalid designation. Try again.");
                }
            }

            // === INSERT INTO DATABASE ===
            String insertSql =
                    "INSERT INTO employees (name, age, designation, salary) VALUES (?, ?, ?, ?)";

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(insertSql)) {

                ps.setString(1, name);
                ps.setInt(2, age);
                ps.setString(3, desig);
                ps.setDouble(4, salary);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    System.out.println("Employee created successfully!");
                } else {
                    System.out.println("Failed to create employee.");
                }

            } catch (SQLException e) {
                System.out.println("Error while inserting employee.");
                e.printStackTrace();
            }

            System.out.print("Create another employee? (y/N): ");
            String ans = sc.nextLine().trim().toLowerCase();
            ch = ans.isEmpty() ? 'n' : ans.charAt(0);

        } while (ch == 'y');
    }

    public static void displayEmployees() {

        String selectSql = "SELECT name, age, designation, salary FROM employees";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(selectSql);
             ResultSet rs = ps.executeQuery()) {

            boolean any = false;
            System.out.println("\n------- Employee List -------");

            while (rs.next()) {
                any = true;
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String desig = rs.getString("designation");
                double salary = rs.getDouble("salary");

                Employee e = new Employee(name, age, desig, salary);
                System.out.println(e);
            }

            if (!any) {
                System.out.println("No employees to display.");
            }

        } catch (SQLException e) {
            System.out.println("Error while fetching employees.");
            e.printStackTrace();
        }
    }

    public static void raiseSalary() {

        System.out.print("Enter employee name: ");
        String searchName = sc.nextLine().trim();

        int percent;
        while (true) {
            percent = readInt("Enter raise percentage (1-10): ");
            if (percent >= 1 && percent <= 10) break;
            System.out.println("Percentage must be between 1–10.");
        }

        String selectSql = "SELECT salary FROM employees WHERE LOWER(name) = LOWER(?)";
        String updateSql = "UPDATE employees SET salary = salary + (salary * ? / 100.0) " +
                           "WHERE LOWER(name) = LOWER(?)";

        try (Connection con = getConnection()) {

            double oldSalary = 0.0;

            // Get existing salary
            try (PreparedStatement psSel = con.prepareStatement(selectSql)) {
                psSel.setString(1, searchName);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Employee not found.");
                        return;
                    }
                    oldSalary = rs.getDouble("salary");
                }
            }

            // Update salary
            try (PreparedStatement psUpd = con.prepareStatement(updateSql)) {
                psUpd.setInt(1, percent);
                psUpd.setString(2, searchName);
                int rows = psUpd.executeUpdate();

                if (rows > 0) {
                    double newSalary = oldSalary + (oldSalary * percent / 100.0);
                    System.out.println("Salary updated for " + searchName +
                            " from " + oldSalary + " to " + newSalary);
                } else {
                    System.out.println("Salary update failed.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error while updating salary.");
            e.printStackTrace();
        }
    }

    public static int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(sc.nextLine());
            } catch (Exception e) {
                System.out.println("Invalid input. Enter a number.");
            }
        }
    }
}