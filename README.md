# Digital Detox Challenge Platform

A web app that helps people spend less time on their screens. Users join detox challenges, check in every day, log their screen time, build streaks, earn badges and compete with friends.

Built as a course project for **Software Engineering** at **Salim Habib University**.

- **Supervisor:** Sir Umer Mehmood
- **Team:** Shahwaiz Hussain, Narmeen Raza, Abdul Qadeer

## Table of contents

1. [Features](#features)
2. [Tech stack](#tech-stack)
3. [Project structure](#project-structure)
4. [Installation](#installation)
5. [Configuration](#configuration)
6. [Running the app](#running-the-app)
7. [Running the tests](#running-the-tests)
8. [Quick tour](#quick-tour)
9. [Troubleshooting](#troubleshooting)
10. [Team workflow](#team-workflow)
11. [Deployment](#deployment)
12. [Status and notes](#status-and-notes)

## Features

| Area | What you can do |
|---|---|
| **Accounts** | Register, log in and out, edit your profile and daily screen time goal, change your password, request a password reset from an admin |
| **Challenges** | Search and filter challenges, read the details, join, leave, or create your own. A "Recommended for you" section suggests challenges based on your screen time and past choices |
| **Tracking** | Daily check-in with screen time and an optional trigger (why you picked up your phone), check-in history, streaks, progress page with a 7-day chart and a 12-week activity heatmap |
| **Insights** | Weekly report (goal days, change since last week, time reclaimed), streak coach messages, mood versus screen time chart, phone trigger breakdown |
| **Motivation** | Points and levels, 8 badges, daily quests, reflections (journal), in-app reminders, tips and offline ideas, focus timer (15, 30 or 60 minute phone-down sessions that earn points), printable certificate when you finish a challenge |
| **Social** | Friend groups with invite codes, a weekly leaderboard and a shared team goal, comments on challenges, report a comment |
| **Data** | Download your check-ins and reflections as CSV files |
| **Admin** | Manage challenges, tips and user accounts, moderate reported comments, handle password resets, platform statistics and analytics charts |
| **Interface** | "Sunny Path" design with a left menu, a side panel with streak, level and quests, and a path of your challenges on the home page. Light and dark themes, Ctrl+K quick search, installable as an app (PWA) |

## Tech stack

- **Language:** Java 21 (set in `platform/pom.xml`)
- **Framework:** Spring Boot 4.1 (Spring Web, Spring Security, Spring Data JPA, Validation)
- **Pages:** Thymeleaf templates, HTML, CSS and plain JavaScript (no front-end build step)
- **Database:** H2 stored in a local file (development)
- **Build tool:** Maven. The Maven Wrapper is included, so you do not need to install Maven
- **Tests:** JUnit 5 (via `spring-boot-starter-test`)

## Project structure

```
digital-detox-platform/
├── README.md
└── platform/                          <- the Spring Boot project (open this folder in VS Code)
    ├── pom.xml
    ├── mvnw, mvnw.cmd                 <- Maven Wrapper
    ├── data/                          <- local database (created on first run, not in Git)
    └── src/
        ├── main/
        │   ├── java/com/detox/platform/
        │   │   ├── *Controller.java   web pages and forms
        │   │   ├── *Service.java      business logic (progress, badges, insights ...)
        │   │   ├── *Calc.java         pure calculation classes (easy to unit test)
        │   │   ├── *Repository.java   database access (Spring Data JPA)
        │   │   ├── entities           User, Challenge, CheckIn, Reflection, FocusSession ...
        │   │   ├── SecurityConfig.java   who can open which page
        │   │   └── AdminSeeder.java      creates the first admin account
        │   └── resources/
        │       ├── application.properties
        │       ├── templates/         Thymeleaf pages (fragments.html is the page shell)
        │       └── static/            css/, js/, images/, manifest.json, sw.js
        └── test/java/com/detox/platform/   unit tests
```

## Installation

### Requirements

- **JDK 21.** The number in `platform/pom.xml` (`<java.version>`) must not be higher than your installed JDK. Check yours with `java -version`. If you have JDK 17, change `<java.version>` to `17` and everything still works.
- **Git**
- **VS Code** with the Extension Pack for Java (optional but recommended)
- An internet connection for the first run, because Maven downloads the dependencies (a few minutes)

You do **not** need to install Maven, a database server or Node.js.

### Get the code

```bash
git clone https://github.com/shahwayy-codes/digital-detox-platform.git
cd digital-detox-platform/platform
```

### Using GitHub Codespaces

Open the repository on GitHub, click **Code**, then the **Codespaces** tab, then **Create codespace on main**. The terminal opens in the repository folder. Run:

```bash
cd platform
```

Codespaces runs Linux, so use `./mvnw` (not `.\mvnw`). If you get "permission denied", run `chmod +x mvnw` once.

## Configuration

Settings live in `platform/src/main/resources/application.properties`.

| Setting | Default | What it does |
|---|---|---|
| `spring.datasource.url` | `jdbc:h2:file:./data/detoxdb` | Where the database file is stored (`platform/data/`) |
| `spring.datasource.username` / `password` | `sa` / empty | Database login (local development only) |
| `spring.jpa.hibernate.ddl-auto` | `update` | Creates and updates tables automatically when the app starts |
| `spring.thymeleaf.cache` | `false` | Pages reload without a restart while developing |
| `server.port` | `8080` (Spring default) | Port the site runs on |

### Admin account

An admin account is created automatically the first time the app starts. Its details are set in `AdminSeeder.java`.

| Email | Password |
|---|---|
| `admin@detox.com` | `Admin12345` |

**Change this password in `AdminSeeder.java` before deploying anywhere public.**

Normal users register themselves on the **Register** page.

### Change the port

If 8080 is busy:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

On Windows PowerShell use `.\mvnw` instead of `./mvnw`.

### Local database

Data is stored in `platform/data/` on your own computer or Codespace. It is not part of the repository, so every team member has separate data. To start fresh, stop the app and delete that folder.

### Change the colours

The interface colours are variables at the top of `platform/src/main/resources/static/css/style.css` (`--brand`, `--bg`, `--sun`, `--good` and so on). The dark theme has its own set of values in the `:root[data-theme="dark"]` block.

## Running the app

**Windows (PowerShell):**

```powershell
cd platform
.\mvnw spring-boot:run
```

**macOS, Linux and Codespaces:**

```bash
cd platform
./mvnw spring-boot:run
```

Wait for the line `Started PlatformApplication`, then open **http://localhost:8080** in your browser. Stop the app with `Ctrl+C`.

The first run downloads the dependencies, so it needs an internet connection and can take a few minutes.

**In Codespaces:** when the app starts, a pop-up offers to open port 8080 in the browser. If you miss it, open the **Ports** tab at the bottom of VS Code, find port 8080 and click the globe icon.

After you pull new code, stop the app and start it again. Java changes only load after a restart. Hard refresh the browser (`Ctrl+F5`) so it loads the new CSS and JavaScript.

## Running the tests

```bash
cd platform
./mvnw test
```

On Windows use `.\mvnw test`. You should see `BUILD SUCCESS` with 41 unit tests passing. The tests cover the calculation classes (insights, focus rules, CSV export, recommendations, trigger tracker, team goals, analytics and daily quests).

## Quick tour

1. Open http://localhost:8080 and click **Get started** to register.
2. Open **Challenges**, join one (for example **No Social Media Weekend**).
3. Go to **Check-in**, log your screen time and choose a trigger.
4. Open **Progress** and press **Add demo data (testing only)**. This fills in a few days so you can see the streaks, heatmap, insights, certificate and mood chart without waiting.
5. Try **Focus timer**, **Insights**, **Friends** (create a group and share the invite code) and **Badges**.
6. Press `Ctrl+K` anywhere to jump to a page, and use the moon or sun button in the left menu to switch themes.
7. Log in as the admin account to see **Admin panel** and **Analytics**.

## Troubleshooting

| Problem | Fix |
|---|---|
| `release version 21 not supported` | Your JDK is older than `<java.version>` in `pom.xml`. Install JDK 21 or change that number to your JDK version (17 or newer) |
| `Port 8080 was already in use` | An old copy is still running. Press `Ctrl+C` in that terminal, or use another port (see above) |
| Database or "column not found" error when starting | Stop the app, delete the `platform/data` folder and start again. You will need to register again |
| `permission denied` for `./mvnw` | Run `chmod +x mvnw` once |
| Page looks old or half styled | Hard refresh with `Ctrl+F5`. If you installed the app as a PWA, clear site data in the browser tools (Application tab) |
| Changes do not show up in Codespaces | The files must be in the Codespace or pulled from GitHub: run `git status` and `git pull`, then restart the app |
| First run is very slow | Maven is downloading dependencies. Wait for it to finish |

## Team workflow

1. `git checkout main` then `git pull`
2. `git checkout -b feature/short-description`
3. Make your changes and test them locally
4. `git add .`, `git commit -m "Clear message"`, `git push -u origin feature/short-description`
5. Open a **Pull Request** on GitHub, ask a teammate to review it, then **merge** it
6. Everyone runs `git checkout main` and `git pull`

Always merge the Pull Request into `main` before switching back to `main`.

## Deployment

The default setup is for local development (H2 file database). A production setup with a Dockerfile and PostgreSQL settings is kept on the `feature/deployment` branch. It reads the database and admin login from environment variables (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`) and uses `PORT` for the port. Merge that branch into `main` and make sure the Java version in the Dockerfile matches `<java.version>` in `pom.xml` before deploying.

## Status and notes

All planned user stories (US-01 to US-26) are implemented, plus the extra features listed above. Remaining work: final testing, report and documentation.

This project was made for a university course and is not intended for production use.
