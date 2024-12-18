package com.example.abcd.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizManager {
    private static final Map<String, Map<String, List<QuizQuestion>>> quizzesBySubject = new HashMap<>();

    static {
        initializeQuizQuestions();
    }

    private static void initializeQuizQuestions() {
        // Semester 1
        Map<String, List<QuizQuestion>> sem1Subjects = new HashMap<>();
        
        // 101 - Communication Skills
        List<QuizQuestion> commSkills = new ArrayList<>();
        commSkills.add(new QuizQuestion(
            "What is the primary purpose of non-verbal communication?",
            new String[]{
                "To replace verbal communication",
                "To enhance and support verbal messages",
                "To confuse the receiver",
                "To waste time"
            },
            "To enhance and support verbal messages"
        ));
        commSkills.add(new QuizQuestion(
            "Which of the following is a barrier to effective communication?",
            new String[]{
                "Active listening",
                "Clear message",
                "Language differences",
                "Eye contact"
            },
            "Language differences"
        ));
        sem1Subjects.put("101 - Communication Skills", commSkills);

        // 102 - Mathematics
        List<QuizQuestion> mathQuestions = new ArrayList<>();
        mathQuestions.add(new QuizQuestion(
            "What is the derivative of x² with respect to x?",
            new String[]{
                "x²",
                "2x",
                "2",
                "x"
            },
            "2x"
        ));
        mathQuestions.add(new QuizQuestion(
            "What is the value of sin²θ + cos²θ?",
            new String[]{
                "0",
                "1",
                "2",
                "Depends on θ"
            },
            "1"
        ));
        sem1Subjects.put("102 - Mathematics", mathQuestions);

        // Continue with other semester 1 subjects...
        sem1Subjects.put("103 - Introduction to Computers", initializeIntroToComputers());
        sem1Subjects.put("104 - Computer Programming & Programming Methodology (CPPM)", initializeCPPM());
        sem1Subjects.put("105 - Data Manipulation and Analysis", initializeDataManipulation());

        quizzesBySubject.put("sem1", sem1Subjects);

        // Semester 2
        Map<String, List<QuizQuestion>> sem2Subjects = new HashMap<>();
        sem2Subjects.put("201-1 - Organizational Structure & Behavior", initializeOrgBehavior());
        sem2Subjects.put("201-2 - Introduction to Internet & HTML", initializeHTML());
        sem2Subjects.put("202-1 - Computerized Financial Accounting", initializeAccounting());
        sem2Subjects.put("202-2 - Emerging Trends and Information Technology", initializeEmergingTech());
        sem2Subjects.put("203 - Operating System - I", initializeOS());
        sem2Subjects.put("204 - Programming Skills", initializeProgrammingSkills());
        sem2Subjects.put("205 - Concepts of Relational Database Management System", initializeRDBMS());

        quizzesBySubject.put("sem2", sem2Subjects);

        // Semester 3
        Map<String, List<QuizQuestion>> sem3Subjects = new HashMap<>();
        sem3Subjects.put("301 - Statistical Methods", initializeStatisticalMethods());
        sem3Subjects.put("302 - Software Engineering", initializeSoftwareEngineering());
        sem3Subjects.put("303 - Database Handling using Python", initializeDatabaseHandlingPython());
        sem3Subjects.put("304 - OOPs and Data Structures", initializeOOPsDataStructures());
        sem3Subjects.put("305-01 - Web Designing – 1", initializeWebDesigning1());
        sem3Subjects.put("305-02 - Mobile Application Development – 1", initializeMobileAppDevelopment1());

        quizzesBySubject.put("sem3", sem3Subjects);

        // Semester 4
        Map<String, List<QuizQuestion>> sem4Subjects = new HashMap<>();
        
        // 401 - Information System
        sem4Subjects.put("401 - Information System", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is an Information System?",
                new String[]{
                    "Only hardware components",
                    "Only software components",
                    "A combination of hardware, software, and people for processing information",
                    "Only network components"
                },
                "A combination of hardware, software, and people for processing information"
            ));
            add(new QuizQuestion(
                "Which is not a component of Information System?",
                new String[]{
                    "Hardware",
                    "Software",
                    "Entertainment",
                    "Data"
                },
                "Entertainment"
            ));
        }});

        // 402 - Internet of Things (IoT)
        sem4Subjects.put("402 - Internet of Things (IoT)", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is IoT?",
                new String[]{
                    "Internet connection",
                    "Network of physical devices connected to internet",
                    "Type of computer",
                    "Programming language"
                },
                "Network of physical devices connected to internet"
            ));
            add(new QuizQuestion(
                "Which protocol is commonly used in IoT?",
                new String[]{
                    "HTTP",
                    "MQTT",
                    "FTP",
                    "SMTP"
                },
                "MQTT"
            ));
        }});

        // 403 - Java Programming
        sem4Subjects.put("403 - Java Programming", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is JVM?",
                new String[]{
                    "Java Visual Machine",
                    "Java Virtual Machine",
                    "Java Virtual Memory",
                    "Java Visual Memory"
                },
                "Java Virtual Machine"
            ));
            add(new QuizQuestion(
                "Which concept is not supported in Java?",
                new String[]{
                    "Inheritance",
                    "Multiple Inheritance through classes",
                    "Polymorphism",
                    "Encapsulation"
                },
                "Multiple Inheritance through classes"
            ));
        }});

        // 404 - .NET Programming
        sem4Subjects.put("404 - .NET Programming", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is CLR in .NET?",
                new String[]{
                    "Common Language Runtime",
                    "Common Language Register",
                    "Common Local Runtime",
                    "Common Local Register"
                },
                "Common Language Runtime"
            ));
            add(new QuizQuestion(
                "Which language is not part of .NET?",
                new String[]{
                    "C#",
                    "VB.NET",
                    "Swift",
                    "F#"
                },
                "Swift"
            ));
        }});

        // 405-01 - Web Designing – 2
        sem4Subjects.put("405-01 - Web Designing – 2", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is JavaScript primarily used for?",
                new String[]{
                    "Styling web pages",
                    "Creating dynamic content",
                    "Database management",
                    "Server configuration"
                },
                "Creating dynamic content"
            ));
            add(new QuizQuestion(
                "What is AJAX?",
                new String[]{
                    "A programming language",
                    "A cleaning product",
                    "Asynchronous JavaScript and XML",
                    "A web browser"
                },
                "Asynchronous JavaScript and XML"
            ));
        }});

        // 405-02 - Mobile Application Development – 2
        sem4Subjects.put("405-02 - Mobile Application Development – 2", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is a Fragment in Android?",
                new String[]{
                    "A complete activity",
                    "A portion of UI in an activity",
                    "A database table",
                    "A web service"
                },
                "A portion of UI in an activity"
            ));
            add(new QuizQuestion(
                "What is the purpose of Intent in Android?",
                new String[]{
                    "To store data",
                    "To communicate between components",
                    "To create layouts",
                    "To handle permissions"
                },
                "To communicate between components"
            ));
        }});

        quizzesBySubject.put("sem4", sem4Subjects);

        // Semester 5
        Map<String, List<QuizQuestion>> sem5Subjects = new HashMap<>();
        
        // 501-01 - Advanced Web Designing
        sem5Subjects.put("501-01 - Advanced Web Designing", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is React.js?",
                new String[]{
                    "A database",
                    "A JavaScript library for building user interfaces",
                    "A programming language",
                    "A web server"
                },
                "A JavaScript library for building user interfaces"
            ));
            add(new QuizQuestion(
                "What is Node.js?",
                new String[]{
                    "A front-end framework",
                    "A JavaScript runtime environment",
                    "A database system",
                    "A web browser"
                },
                "A JavaScript runtime environment"
            ));
        }});

        // 501-02 - Advanced Mobile Computing
        sem5Subjects.put("501-02 - Advanced Mobile Computing", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is Firebase?",
                new String[]{
                    "A mobile operating system",
                    "A backend development platform",
                    "A programming language",
                    "A mobile device"
                },
                "A backend development platform"
            ));
            add(new QuizQuestion(
                "What is REST API?",
                new String[]{
                    "A programming language",
                    "An architectural style for APIs",
                    "A database",
                    "A mobile framework"
                },
                "An architectural style for APIs"
            ));
        }});

        // 502 - UNIX and Shell Programming
        sem5Subjects.put("502 - UNIX and Shell Programming", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is a Shell in UNIX?",
                new String[]{
                    "A hardware component",
                    "An interface between user and kernel",
                    "A file system",
                    "A network protocol"
                },
                "An interface between user and kernel"
            ));
            add(new QuizQuestion(
                "Which command is used to list files in UNIX?",
                new String[]{
                    "list",
                    "ls",
                    "show",
                    "dir"
                },
                "ls"
            ));
        }});

        // 503 - Network Technology
        sem5Subjects.put("503 - Network Technology", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is TCP/IP?",
                new String[]{
                    "A web browser",
                    "A set of protocols for internet communication",
                    "A programming language",
                    "An operating system"
                },
                "A set of protocols for internet communication"
            ));
            add(new QuizQuestion(
                "What is a router?",
                new String[]{
                    "A device that forwards data packets between networks",
                    "A storage device",
                    "A printer",
                    "A keyboard"
                },
                "A device that forwards data packets between networks"
            ));
        }});

        // 504 - Web Framework and Services
        sem5Subjects.put("504 - Web Framework and Services", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is a Web Service?",
                new String[]{
                    "A website",
                    "A software system for machine-to-machine interaction over network",
                    "A web browser",
                    "A database"
                },
                "A software system for machine-to-machine interaction over network"
            ));
            add(new QuizQuestion(
                "What is SOAP?",
                new String[]{
                    "A cleaning product",
                    "A protocol for exchanging structured information",
                    "A programming language",
                    "A database"
                },
                "A protocol for exchanging structured information"
            ));
        }});

        // 505 - ASP .NET
        sem5Subjects.put("505 - ASP .NET", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is ASP.NET?",
                new String[]{
                    "A database",
                    "A web framework by Microsoft",
                    "A programming language",
                    "An operating system"
                },
                "A web framework by Microsoft"
            ));
            add(new QuizQuestion(
                "What is MVC in ASP.NET?",
                new String[]{
                    "Most Valuable Code",
                    "Model View Controller",
                    "Microsoft Visual Code",
                    "Multiple View Control"
                },
                "Model View Controller"
            ));
        }});

        quizzesBySubject.put("sem5", sem5Subjects);

        // Semester 6
        Map<String, List<QuizQuestion>> sem6Subjects = new HashMap<>();
        
        // 601-01 - Computer Graphics
        sem6Subjects.put("601-01 - Computer Graphics", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is a pixel?",
                new String[]{
                    "A line segment",
                    "The smallest addressable element in a display device",
                    "A color model",
                    "A file format"
                },
                "The smallest addressable element in a display device"
            ));
            add(new QuizQuestion(
                "What is RGB?",
                new String[]{
                    "A file format",
                    "A programming language",
                    "A color model using Red, Green, and Blue",
                    "A graphics card"
                },
                "A color model using Red, Green, and Blue"
            ));
        }});

        // 601-02 - Fundamentals of Cloud Computing
        sem6Subjects.put("601-02 - Fundamentals of Cloud Computing", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is Cloud Computing?",
                new String[]{
                    "Weather forecasting",
                    "Delivery of computing services over the internet",
                    "A type of computer",
                    "A programming language"
                },
                "Delivery of computing services over the internet"
            ));
            add(new QuizQuestion(
                "Which is not a cloud service model?",
                new String[]{
                    "SaaS",
                    "PaaS",
                    "IaaS",
                    "CaaS (Computer as a Service)"
                },
                "CaaS (Computer as a Service)"
            ));
        }});

        // 602 - E-Commerce and Cyber Security
        sem6Subjects.put("602 - E-Commerce and Cyber Security", new ArrayList<QuizQuestion>() {{
            add(new QuizQuestion(
                "What is E-Commerce?",
                new String[]{
                    "Electronic mail",
                    "Buying and selling goods and services over the internet",
                    "Computer hardware",
                    "A security protocol"
                },
                "Buying and selling goods and services over the internet"
            ));
            add(new QuizQuestion(
                "What is a firewall?",
                new String[]{
                    "A physical wall",
                    "A security system that monitors network traffic",
                    "An antivirus software",
                    "A type of computer"
                },
                "A security system that monitors network traffic"
            ));
        }});

        quizzesBySubject.put("sem6", sem6Subjects);
    }

    private static List<QuizQuestion> initializeIntroToComputers() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "Which component is known as the brain of the computer?",
            new String[]{
                "Hard Disk",
                "RAM",
                "CPU",
                "Monitor"
            },
            "CPU"
        ));
        questions.add(new QuizQuestion(
            "What is the full form of RAM?",
            new String[]{
                "Random Access Memory",
                "Read Access Memory",
                "Read Always Memory",
                "Random Always Memory"
            },
            "Random Access Memory"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeCPPM() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is an algorithm?",
            new String[]{
                "A programming language",
                "A step-by-step procedure to solve a problem",
                "A computer program",
                "A mathematical equation"
            },
            "A step-by-step procedure to solve a problem"
        ));
        questions.add(new QuizQuestion(
            "Which of the following is not a programming paradigm?",
            new String[]{
                "Procedural",
                "Object-Oriented",
                "Functional",
                "Systematic"
            },
            "Systematic"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeDataManipulation() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is data cleaning?",
            new String[]{
                "Deleting all data",
                "Removing duplicate and incorrect data",
                "Copying data",
                "Creating new data"
            },
            "Removing duplicate and incorrect data"
        ));
        questions.add(new QuizQuestion(
            "Which of the following is a type of data visualization?",
            new String[]{
                "SQL query",
                "Bar chart",
                "RAM",
                "CPU"
            },
            "Bar chart"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeOrgBehavior() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is organizational culture?",
            new String[]{
                "Office building design",
                "Shared values and beliefs of an organization",
                "Company's profit",
                "Number of employees"
            },
            "Shared values and beliefs of an organization"
        ));
        questions.add(new QuizQuestion(
            "Which leadership style involves employee participation in decision making?",
            new String[]{
                "Autocratic",
                "Democratic",
                "Laissez-faire",
                "None of the above"
            },
            "Democratic"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeHTML() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What does HTML stand for?",
            new String[]{
                "Hyper Text Markup Language",
                "High Text Making Language",
                "Hyper Text Making Language",
                "High Text Markup Language"
            },
            "Hyper Text Markup Language"
        ));
        questions.add(new QuizQuestion(
            "Which tag is used to create a hyperlink in HTML?",
            new String[]{
                "<link>",
                "<a>",
                "<href>",
                "<url>"
            },
            "<a>"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeAccounting() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is the purpose of a balance sheet?",
            new String[]{
                "Shows profit and loss",
                "Shows assets and liabilities",
                "Shows cash flow",
                "Shows employee salaries"
            },
            "Shows assets and liabilities"
        ));
        questions.add(new QuizQuestion(
            "Which financial statement shows the company's revenues and expenses?",
            new String[]{
                "Balance Sheet",
                "Income Statement",
                "Cash Flow Statement",
                "Owner's Equity Statement"
            },
            "Income Statement"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeEmergingTech() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is Artificial Intelligence?",
            new String[]{
                "A robot",
                "A computer program that can think like humans",
                "A database",
                "A web browser"
            },
            "A computer program that can think like humans"
        ));
        questions.add(new QuizQuestion(
            "Which of the following is an example of emerging technology?",
            new String[]{
                "Telephone",
                "Television",
                "Blockchain",
                "Radio"
            },
            "Blockchain"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeOS() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is the function of an operating system?",
            new String[]{
                "Play games",
                "Manage hardware and software resources",
                "Create documents",
                "Browse internet"
            },
            "Manage hardware and software resources"
        ));
        questions.add(new QuizQuestion(
            "Which of the following is not an operating system?",
            new String[]{
                "Windows",
                "Linux",
                "Chrome",
                "macOS"
            },
            "Chrome"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeProgrammingSkills() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is debugging?",
            new String[]{
                "Creating new programs",
                "Finding and fixing errors in programs",
                "Writing documentation",
                "Testing programs"
            },
            "Finding and fixing errors in programs"
        ));
        questions.add(new QuizQuestion(
            "Which is not a programming language?",
            new String[]{
                "Java",
                "Python",
                "Microsoft Word",
                "C++"
            },
            "Microsoft Word"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeRDBMS() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is a primary key in RDBMS?",
            new String[]{
                "A key that can be null",
                "A unique identifier for each record",
                "A foreign key reference",
                "A composite key"
            },
            "A unique identifier for each record"
        ));
        questions.add(new QuizQuestion(
            "What does SQL stand for?",
            new String[]{
                "Strong Query Language",
                "Structured Question Language",
                "Structured Query Language",
                "Simple Query Language"
            },
            "Structured Query Language"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeStatisticalMethods() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is the mean of a dataset?",
            new String[]{
                "Middle value",
                "Most frequent value",
                "Average of all values",
                "Largest value"
            },
            "Average of all values"
        ));
        questions.add(new QuizQuestion(
            "What is standard deviation used for?",
            new String[]{
                "To measure central tendency",
                "To measure spread of data",
                "To count data points",
                "To sort data"
            },
            "To measure spread of data"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeSoftwareEngineering() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is the waterfall model?",
            new String[]{
                "An agile methodology",
                "A sequential software development model",
                "A testing technique",
                "A programming language"
            },
            "A sequential software development model"
        ));
        questions.add(new QuizQuestion(
            "What is unit testing?",
            new String[]{
                "Testing the entire application",
                "Testing individual components",
                "Testing user interface",
                "Testing database"
            },
            "Testing individual components"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeDatabaseHandlingPython() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "Which Python library is commonly used for database operations?",
            new String[]{
                "NumPy",
                "Pandas",
                "SQLAlchemy",
                "Matplotlib"
            },
            "SQLAlchemy"
        ));
        questions.add(new QuizQuestion(
            "What is the purpose of cursor in database operations?",
            new String[]{
                "To point to screen",
                "To execute SQL queries",
                "To create tables",
                "To connect to database"
            },
            "To execute SQL queries"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeOOPsDataStructures() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is encapsulation in OOP?",
            new String[]{
                "Creating objects",
                "Hiding implementation details",
                "Inheriting properties",
                "Method overriding"
            },
            "Hiding implementation details"
        ));
        questions.add(new QuizQuestion(
            "Which data structure uses FIFO principle?",
            new String[]{
                "Stack",
                "Queue",
                "Tree",
                "Graph"
            },
            "Queue"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeWebDesigning1() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is CSS used for?",
            new String[]{
                "Server-side scripting",
                "Database management",
                "Styling web pages",
                "Network protocols"
            },
            "Styling web pages"
        ));
        questions.add(new QuizQuestion(
            "Which property is used to change text color in CSS?",
            new String[]{
                "text-color",
                "color",
                "font-color",
                "text-style"
            },
            "color"
        ));
        return questions;
    }

    private static List<QuizQuestion> initializeMobileAppDevelopment1() {
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(
            "What is an Activity in Android?",
            new String[]{
                "A database",
                "A screen in an app",
                "A web service",
                "A programming language"
            },
            "A screen in an app"
        ));
        questions.add(new QuizQuestion(
            "What is the purpose of AndroidManifest.xml?",
            new String[]{
                "Store user data",
                "Define app configuration",
                "Write Java code",
                "Create layouts"
            },
            "Define app configuration"
        ));
        return questions;
    }

    public static List<String> getSubjectsForSemester(String semester) {
        Map<String, List<QuizQuestion>> semesterSubjects = quizzesBySubject.get(semester);
        return semesterSubjects != null ? new ArrayList<>(semesterSubjects.keySet()) : new ArrayList<>();
    }

    public static List<QuizQuestion> getQuizQuestions(String semester, String subject) {
        Map<String, List<QuizQuestion>> semesterSubjects = quizzesBySubject.get(semester);
        if (semesterSubjects != null) {
            return semesterSubjects.getOrDefault(subject, new ArrayList<>());
        }
        return new ArrayList<>();
    }
}
