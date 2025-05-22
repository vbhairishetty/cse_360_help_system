# Compass360 Help System

Compass360 is a modular and secure help system developed for the ASU course CSE 360 - Introduction to Software Engineering. It supports educational stakeholders including **Admins**, **Instructors**, **TAs**, **Students**, and **Past Students**, offering an encrypted, role-based platform for managing and retrieving help articles, user queries, and system support.

## 🔍 Project Overview

The project was developed over four phases:

- **Phase 1**: Secure account creation, multi-role login, session-based authentication.
- **Phase 2**: Article management with backup/restore capabilities and metadata-rich search.
- **Phase 3**: Encrypted special access groups, student feedback system, detailed role-specific dashboards.
- **Phase 4**: Integration of MVC architecture, JUnit testing, and screencast demos.

## 🧱 Architecture

Compass360 follows the **Model-View-Controller (MVC)** architecture:

- **Model**: Business logic, user-role enforcement, encrypted content handling.
- **View**: JavaFX-based GUI tailored for each user role.
- **Controller**: Handles input, updates model, and refreshes view accordingly.

## 👥 User Roles and Functionalities

| Role             | Key Functionalities                                                                  |
|------------------|--------------------------------------------------------------------------------------|
| **Admin**        | Invite users, reset/delete accounts, manage roles/groups, backup/restore articles.   |
| **Instructor**   | Create/edit/delete articles, manage special groups, backup/restore content.          |
| **TA**           | Handle student questions (tokens), update token status, escalate to instructors.     |
| **Student**      | Search/view articles, filter by content level/group, send feedback.                  |
| **Past Student** | View and respond to questions (read-only interactions).                              |

## 🔐 Security Features

- OTP-based password reset (valid for 10 mins).
- Invitation-only user registration.
- Encrypted article content (especially for special access groups).
- Role-based access control with session management.

## 📦 Features

- **Article Search**: Filter by group, author, keywords, or content level (beginner to expert).
- **Backup/Restore**: Admins & Instructors can perform merge/replace backups for groups or all articles.
- **Encrypted Groups**: Restricted access groups ensure article privacy.
- **Feedback Loop**: Students can raise issues and send generic or specific queries to improve help content.
- **Automated Testing**: JUnit test suite for critical components to ensure code reliability and scalability.

## 🧪 Testing

JUnit and manual tests were conducted to validate:
- User registration and login flows
- Article creation and encryption
- Group access control and feedback handling

## 📁 Folder Structure
├── src/ │ ├── controllers/ # Admin, Instructor, Student controllers │ ├── model/ # HelpArticle, Group, User, Role classes │ ├── util/ # EncryptionUtility, SessionManager │ └── view/ # JavaFX views ├── backup/ # Backup and restore files ├── test/ # JUnit test cases └── README.md


## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/Sai-Vignesh/CSE360_Help_System.git
   cd CSE360_Help_System

2. Run the JavaFX project in your IDE (e.g., IntelliJ or Eclipse).

3. First user will be prompted to create an Admin account.

📌 Requirements
    - Java 11+
    - JavaFX SDK
    - SQLite or embedded DB
    - JUnit 5 (for testing)

📄 License
This project is developed as part of ASU CSE360 course. For academic and educational use only.


