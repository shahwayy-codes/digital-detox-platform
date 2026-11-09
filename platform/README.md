# Digital Detox Challenge Platform

A web app that helps people spend less time on their screens. Users join detox challenges, check in every day, log their screen time, build streaks, earn badges and compete with friends.

Built as a course project for **Software Engineering** at **Salim Habib University**.

- **Supervisor:** Sir Umer Mehmood
- **Team:** Shahwaiz Hussain, Narmeen Raza, Abdul Qadeer

## Features

| Area | What you can do |
|---|---|
| **Accounts** | Register, log in and out, edit your profile and daily screen time goal, change your password, request a password reset from an admin |
| **Challenges** | Search and filter challenges, read the details, join, leave, or create your own |
| **Tracking** | Daily check-in, screen time logging, check-in history, streaks, and a progress dashboard with a 7-day chart |
| **Motivation** | Points and levels, 8 badges, reflections (journal), in-app daily reminders, tips and offline ideas |
| **Social** | Friend groups with invite codes and a weekly leaderboard, comments on challenges, report a comment |
| **Admin** | Manage challenges, tips and user accounts, moderate reported comments, handle password resets, see platform statistics |

## Tech stack

- **Language:** Java
- **Framework:** Spring Boot (Spring Web, Spring Security, Spring Data JPA, Validation)
- **Pages:** Thymeleaf templates, HTML, CSS and a little JavaScript
- **Database:** H2 (local file) for development
- **Build tool:** Maven (the Maven Wrapper is included, so you do not need to install Maven)

## Project structure

```
digital-detox-platform/
├── README.md
└── platform/                       <- the Spring Boot project (open this folder in VS Code)
    ├── pom.xml
    ├── mvnw, mvnw.cmd              <- Maven Wrapper
    └── src/main/
        ├── java/com/detox/platform/    controllers, entities, repositories, services
        └── resources/
            ├── application.properties
            ├── templates/              Thymeleaf pages
            └── static/                 css/ and js/
```

## Getting started

### Requirements

- **JDK 17 or newer.** The version in `platform/pom.xml` (`<java.version>`) must not be higher than your installed JDK. Check yours with `java -version`.
- **Git**
- **VS Code** with the Extension Pack for Java (optional but recommended)

### Run it

```bash
git clone https://github.com/shahwayy-codes/digital-detox-platform.git
cd digital-detox-platform/platform
```

On Windows (PowerShell):

```powershell
.\mvnw spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

Wait for the line `Started PlatformApplication`, then open **http://localhost:8080** in your browser. Stop the app with `Ctrl+C`.

The first run downloads the dependencies, so it needs an internet connection and can take a few minutes.

### Accounts

- Create a normal account from the **Register** page.
- An **admin account** is created automatically the first time the app starts. Its details are set in `AdminSeeder.java`. Change the password there before deploying.

### Local database

Data is stored in `platform/data/` on your own computer. It is not part of the repository, so every team member has their own separate data. To start fresh, stop the app and delete that folder.

## Team workflow

1. `git checkout main` then `git pull`
2. `git checkout -b feature/short-description`
3. Make your changes and test them locally
4. `git add .`, `git commit -m "Clear message"`, `git push -u origin feature/short-description`
5. Open a **Pull Request** on GitHub, ask a teammate to review it, then **merge** it
6. Everyone runs `git checkout main` and `git pull`

Always merge the Pull Request into `main` before switching back to `main`.

## Status

All planned user stories (US-01 to US-26) are implemented. Remaining work: testing, report and documentation, and deployment.

## Notes

This project was made for a university course and is not intended for production use.
