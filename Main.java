import java.util.*;
import java.io.*;

class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}

class StudentNotFoundException extends Exception {
    public StudentNotFoundException(String message) {
        super(message);
    }
}

class Loader implements Runnable {
    private String message;

    public Loader(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        try {
            System.out.print(message);
            for (int i = 0; i < 3; i++) {
                Thread.sleep(300);
                System.out.print(".");
            }
            System.out.println();
        } catch (InterruptedException e) {
            System.out.println("Loading interrupted");
        }
    }
}

abstract class Person {
    protected String name;
    protected String email;

    public Person() {}

    public Person(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public abstract void displayInfo();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

class Student extends Person {
    private int rollNo;
    private String course;
    private double marks;
    private String grade;

    public Student() {}

    public Student(int rollNo, String name, String email, String course, double marks) {
        super(name, email);
        this.rollNo = rollNo;
        this.course = course;
        this.marks = marks;
        this.grade = calculateGrade();
    }

    public void inputDetails(Scanner scanner) throws InvalidInputException {
        System.out.print("Enter Roll No: ");
        this.rollNo = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Name: ");
        this.name = scanner.nextLine().trim();
        if (this.name.isEmpty()) {
            throw new InvalidInputException("Name cannot be empty");
        }

        System.out.print("Enter Email: ");
        this.email = scanner.nextLine().trim();
        if (this.email.isEmpty()) {
            throw new InvalidInputException("Email cannot be empty");
        }

        System.out.print("Enter Course: ");
        this.course = scanner.nextLine().trim();
        if (this.course.isEmpty()) {
            throw new InvalidInputException("Course cannot be empty");
        }

        System.out.print("Enter Marks: ");
        this.marks = scanner.nextDouble();
        scanner.nextLine();
        if (this.marks < 0 || this.marks > 100) {
            throw new InvalidInputException("Marks must be between 0 and 100");
        }

        this.grade = calculateGrade();
    }

    public String calculateGrade() {
        if (marks >= 90) return "A+";
        else if (marks >= 80) return "A";
        else if (marks >= 70) return "B";
        else if (marks >= 60) return "C";
        else if (marks >= 50) return "D";
        else return "F";
    }

    @Override
    public void displayInfo() {
        System.out.println("Roll No: " + rollNo);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Course: " + course);
        System.out.println("Marks: " + marks);
    }

    public int getRollNo() {
        return rollNo;
    }

    public double getMarks() {
        return marks;
    }

    public String toFileString() {
        return rollNo + "," + name + "," + email + "," + course + "," + marks;
    }

    public static Student fromFileString(String line) {
        String[] parts = line.split(",");
        return new Student(
            Integer.parseInt(parts[0]),
            parts[1],
            parts[2],
            parts[3],
            Double.parseDouble(parts[4])
        );
    }
}

interface RecordActions {
    void addStudent(Student student);
    void deleteStudent(String name) throws StudentNotFoundException;
    void updateStudent(int rollNo, Student student) throws StudentNotFoundException;
    Student searchStudent(String name) throws StudentNotFoundException;
    void viewAllStudents();
}

class StudentManager implements RecordActions {
    private List<Student> students;
    private Map<Integer, Student> studentMap;
    private static final String FILE_NAME = "students.txt";

    public StudentManager() {
        students = new ArrayList<>();
        studentMap = new HashMap<>();
        loadFromFile();
    }

    @Override
    public void addStudent(Student student) {
        if (studentMap.containsKey(student.getRollNo())) {
            System.out.println("Student with Roll No " + student.getRollNo() + " already exists!");
            return;
        }
        students.add(student);
        studentMap.put(student.getRollNo(), student);
    }

    @Override
    public void deleteStudent(String name) throws StudentNotFoundException {
        Student toDelete = null;
        for (Student s : students) {
            if (s.getName().equalsIgnoreCase(name)) {
                toDelete = s;
                break;
            }
        }
        if (toDelete == null) {
            throw new StudentNotFoundException("Student with name " + name + " not found");
        }
        students.remove(toDelete);
        studentMap.remove(toDelete.getRollNo());
        System.out.println("Student record deleted.");
    }

    @Override
    public void updateStudent(int rollNo, Student student) throws StudentNotFoundException {
        if (!studentMap.containsKey(rollNo)) {
            throw new StudentNotFoundException("Student with Roll No " + rollNo + " not found");
        }
        Student existing = studentMap.get(rollNo);
        students.remove(existing);
        students.add(student);
        studentMap.put(rollNo, student);
    }

    @Override
    public Student searchStudent(String name) throws StudentNotFoundException {
        for (Student s : students) {
            if (s.getName().equalsIgnoreCase(name)) {
                return s;
            }
        }
        throw new StudentNotFoundException("Student with name " + name + " not found");
    }

    @Override
    public void viewAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No student records available.");
            return;
        }
        Iterator<Student> iterator = students.iterator();
        while (iterator.hasNext()) {
            Student s = iterator.next();
            s.displayInfo();
            System.out.println();
        }
    }

    public void sortByMarks() {
        Collections.sort(students, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return Double.compare(s2.getMarks(), s1.getMarks());
            }
        });
        System.out.println("Sorted Student List by Marks:");
        viewAllStudents();
    }

    public void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(FILE_NAME));
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Student student = Student.fromFileString(line);
                    students.add(student);
                    studentMap.put(student.getRollNo(), student);
                } catch (Exception e) {
                    System.out.println("Error parsing line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing file: " + e.getMessage());
            }
        }
    }

    public void saveToFile() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_NAME));
            for (Student s : students) {
                writer.write(s.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing file: " + e.getMessage());
            }
        }
    }
}

public class Main {
    public static void main(String[] args) {
        StudentManager manager = new StudentManager();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== Capstone Student Menu =====");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Search by Name");
            System.out.println("4. Delete by Name");
            System.out.println("5. Sort by Marks");
            System.out.println("6. Save and Exit");
            System.out.print("Enter choice: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        try {
                            Student student = new Student();
                            student.inputDetails(scanner);
                            Thread loader = new Thread(new Loader("Adding student"));
                            loader.start();
                            loader.join();
                            manager.addStudent(student);
                        } catch (InvalidInputException e) {
                            System.out.println("Error: " + e.getMessage());
                        } catch (InterruptedException e) {
                            System.out.println("Thread interrupted");
                        }
                        break;

                    case 2:
                        manager.viewAllStudents();
                        break;

                    case 3:
                        System.out.print("Enter name to search: ");
                        String searchName = scanner.nextLine();
                        try {
                            Student found = manager.searchStudent(searchName);
                            System.out.println("Student Info:");
                            found.displayInfo();
                        } catch (StudentNotFoundException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;

                    case 4:
                        System.out.print("Enter name to delete: ");
                        String deleteName = scanner.nextLine();
                        try {
                            manager.deleteStudent(deleteName);
                        } catch (StudentNotFoundException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;

                    case 5:
                        manager.sortByMarks();
                        break;

                    case 6:
                        Thread saver = new Thread(new Loader("Saving records"));
                        saver.start();
                        try {
                            saver.join();
                        } catch (InterruptedException e) {
                            System.out.println("Thread interrupted");
                        }
                        manager.saveToFile();
                        System.out.println("Saved and exiting.");
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }

        scanner.close();
    }
}
