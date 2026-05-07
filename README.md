# Automated Team Formation System

This project is a Java-based application designed to automatically generate balanced teams based on participant data, including skills, personality traits, and role preferences.

---

## 📌 Project Overview

Manual team formation is time-consuming and often leads to unbalanced groups. This system automates the process using structured data and algorithms to ensure fairness and efficiency.

---

## 🎯 Key Features

- Load participant data from CSV files
- Collect participant survey inputs (skills, roles, personality)
- Automatically generate balanced teams
- Multi-threaded team formation for improved performance
- View teams and participant details
- Export teams to CSV format
- Input validation and error handling
- Logging system for debugging and tracking

---

## ⚙️ System Functionalities

### Organizer Mode
- Load participants from CSV
- Set team size
- View participants
- Form balanced teams
- View formed teams
- Save teams to CSV

### Participant Mode
- Enter personal details
- Answer personality questions
- Select roles and skills
- Data stored for team formation

---

## 🧠 Team Formation Logic

Teams are created based on:
- Skill distribution
- Personality types (Leader, Thinker, Balanced)
- Role preferences
- Game preferences

The system ensures fair distribution and handles edge cases such as:
- Insufficient participants
- Unbalanced constraints

---

## 🏗️ System Design

The system follows Object-Oriented Programming principles:
- Encapsulation
- Modularity
- Separation of concerns

### Main Components:
- OrganizerMode
- ParticipantMode
- TeamBuilder
- Participant
- Team
- CSVHandler
- Logger

---

## 🔄 UML Diagrams

This project includes:
- Use Case Diagram
- Activity Diagrams
- Sequence Diagrams
- Class Diagram

---

## 🧪 Testing

The system was tested using multiple scenarios:
- Valid and invalid inputs
- CSV file loading errors
- Team formation edge cases
- Concurrent processing validation

All major test cases passed successfully.

---

## 🛠️ Technologies Used

- Java
- Object-Oriented Programming (OOP)
- CSV File Handling
- Multi-threading
- UML Design (Visual Paradigm)

---

## 🚀 Key Learnings

- Designing scalable OOP systems
- Handling real-world constraints in algorithms
- Implementing multi-threaded logic
- Input validation and error handling
- Software design using UML diagrams


## 📌 Future Improvements

- Add GUI interface (JavaFX)
- Improve team optimization algorithm
- Add database support
- Web-based version

---

## 👨‍💻 Author
Ravindu Edirisingha
