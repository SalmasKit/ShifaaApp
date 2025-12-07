# Shifaa Project

A JavaFX-based healthcare management application for managing patients, doctors, appointments, and treatments.

## Overview

Shifaa is a comprehensive healthcare management system built with JavaFX that provides:
- User authentication and session management
- Patient management and records
- Doctor management
- Appointment scheduling
- Treatment tracking
- PDF report generation

## Technology Stack

- **Java 11+** (with Java 23 preview features)
- **JavaFX 21** - UI framework
- **MySQL 8.0.29** - Database connectivity
- **OpenPDF 1.3.30** - PDF generation
- **Maven** - Build management

## Prerequisites

- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.6+
- MySQL Server 8.0+
- JavaFX SDK 21

## Installation

1. Clone the repository
2. Configure your MySQL database
3. Update database connection settings in `src/main/java/utils/Database.java`
4. Build the project: `mvn clean install`
5. Run the application: `mvn clean javafx:run`

## Features

- **Authentication**: Secure login system with session management
- **Patient Management**: Add, edit, and search patient records
- **Doctor Management**: Manage doctor profiles and schedules
- **Appointments**: Schedule and manage patient appointments
- **Treatments**: Track patient treatments and medications
- **Reports**: Generate PDF reports for patient data

## Database Setup

Create a MySQL database and configure the connection in the Database utility class.

## Usage

1. Launch the application
2. Login with your credentials
3. Navigate through different modules using the main interface
4. Manage patients, appointments, and treatments as needed

## Dependencies

All dependencies are managed through Maven in [pom.xml](cci:7://file:///c:/Users/Perfect%20PC/OneDrive/Documents/MY%20PROJECTS/SHIFAA/ShifaaProject/pom.xml:0:0-0:0):
- mysql-connector-java:8.0.29
- javafx-controls:21
- javafx-fxml:21
- openpdf:1.3.31

## Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn clean javafx:run

# Debug mode
mvn clean javafx:run@debug
```

## Project Structure

```
ShifaaProject/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/               # Controller classes
│   │   │   │   ├── AppointmentsController.java
│   │   │   │   ├── BaseController.java
│   │   │   │   ├── LogInController.java
│   │   │   │   ├── MainController.java
│   │   │   │   ├── Patients2Controller.java
│   │   │   │   ├── PatientsController.java
│   │   │   │   ├── SignUpController.java
│   │   │   │   └── TreatmentsController.java
│   │   │   │
│   │   │   ├── model/                   # Model classes
│   │   │   │   ├── Doctor.java
│   │   │   │   ├── DoctorDAO.java
│   │   │   │   ├── LogIn.java
│   │   │   │   ├── LogInDAO.java
│   │   │   │   ├── Patient.java
│   │   │   │   ├── PatientDAO.java
│   │   │   │   ├── Patients2.java
│   │   │   │   ├── Patients2DAO.java
│   │   │   │   ├── SignUp.java
│   │   │   │   ├── SignUpDAO.java
│   │   │   │   ├── Treatment.java
│   │   │   │   └── TreatmentDAO.java
│   │   │   │
│   │   │   ├── utils/                   # Utility classes
│   │   │   │   ├── Database.java
│   │   │   │   └── Session.java
│   │   │   │
│   │   │   └── main/
│   │   │       └── Main.java            # Application entry point
│   │   │
│   │   └── resources/
│   │       ├── Css/                     # CSS files
│   │       │
│   │       ├── images/                  # Application images
│   │       │
│   │       ├── lang/                    # Internationalization
│   │       │   └── messages.properties
│   │       │
│   │       └── view/                    # FXML views
│   │           ├── Appointments.fxml
│   │           ├── Dashboard.fxml
│   │           ├── Login.fxml
│   │           ├── Patients.fxml
│   │           ├── Patients2.fxml
│   │           ├── SignUp.fxml
│   │           └── Treatments.fxml
│   │
│   └── test/                            # Test directory
│       └── java/
│
└── pom.xml                              # Maven configuration