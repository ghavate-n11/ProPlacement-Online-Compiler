# TECHNOLOGY Multi-Language Online Compiler & Web IDE

A modern, full-stack **Online Compiler & Web Playground** built with a **Java HTTP backend** and a **React + Monaco Editor frontend**. 

Supports compiling, running, and previewing **Java**, **Python**, **C++**, **C**, **JavaScript (Node.js)**, **React.js**, and **HTML / CSS / JS** directly in the browser with **Theme Switching** (Dark & Light) and customizable file naming (e.g. `Solution.java`).

---

## 🚀 Key Features

* **Multi-Language Support**:
  * ☕ **Java 21**: Compiles `Solution.java` via `javac` & runs via `java`.
  * 🐍 **Python 3**: Executes `solution.py` via Python runtime.
  * ⚡ **C++ / C**: Compiles and executes `solution.cpp` / `solution.c`.
  * 🟨 **JavaScript (Node.js)**: Runs backend JS scripts via `node`.
  * ⚛️ **React.js Playground**: Live interactive React component preview (`App.jsx`) with Babel standalone compilation.
  * 🌐 **HTML / CSS / JS Web Sandbox**: Live interactive browser preview (`index.html`).
* **Theme Switching**: Seamless one-click toggle between **Dark Mode** and **Light Mode** across the Monaco Editor, Output Console, and Navbar.
* **Programiz-Style Clean Workspace**: Split-screen editor on the left and output/live preview on the right.
* **Keyboard Shortcut**: Run any program instantly with <kbd>Ctrl</kbd> + <kbd>Enter</kbd>.
* **Editor Features**: Fixed 14px font, Fira Code monospace, line numbering, syntax highlighting, and auto-bracket pairing.
* **Utilities**: One-click **Copy Code**, **Copy Output**, **Clear Console**, and **Download File** (with language-specific file extensions).
* **Backend Health Monitoring**: Live status indicator in the top navigation bar.

---

## 📁 Project Structure

```text
EntryLevel JavaCompiler/
│
├── backend/                                  # Java HTTP Backend Server
│   ├── JavaCompilerServer.java               # HTTP server, multi-language compiler & subprocess runner
│   ├── JavaCompilerServer.class              # Compiled bytecode
│   ├── JavaCompilerServer$CompileHandler.class
│   ├── JavaCompilerServer$HealthHandler.class
│   └── temp_exec/                            # Sandboxed directory for temporary job executions
│
├── frontend/                                 # React Frontend (Vite + Monaco Editor)
│   ├── index.html                            # HTML root file
│   ├── package.json                          # Dependencies & scripts
│   ├── vite.config.js                        # Vite configuration
│   ├── src/
│   │   ├── main.jsx                          # React DOM entry point
│   │   ├── App.jsx                           # Main layout & compiler state management
│   │   ├── index.css                         # Global CSS & theme design variables
│   │   ├── App.css                           # Utility styles
│   │   ├── components/
│   │   │   ├── Navbar.jsx                    # Header with Language Selector, Run, Clear, Theme, Copy, Download
│   │   │   ├── CodeEditor.jsx                # Monaco Editor (14px font, dynamic languages & themes)
│   │   │   ├── ConsoleOutput.jsx             # Terminal output console (dark & light theme)
│   │   │   └── WebPreview.jsx                # Live sandboxed preview for React.js & HTML/CSS/JS
│   │   └── utils/
│   │       └── defaultTemplates.js           # Multi-language starter templates & configs
│
├── scratch/                                  # Testing & verification scripts
│   ├── test_api.js                           # Java basic test
│   ├── test_escape.js                        # Escape character test
│   ├── test_stdin.js                         # Stdin test
│   └── test_multilang.js                     # Multi-language API test
│
└── README.md                                 # Project documentation
```

---

## 📌 Main Files & Descriptions

| File | Purpose |
| :--- | :--- |
| [`backend/JavaCompilerServer.java`](file:///c:/MAJOR%20PROJECT/EntryLevel%20JavaCompiler/backend/JavaCompilerServer.java) | Standalone Java HTTP server on port 8080. Compiles/executes Java, Python, C++, C, and JavaScript in sandboxed directories with timeout protection and accurate JSON parsing. |
| [`frontend/src/App.jsx`](file:///c:/MAJOR%20PROJECT/EntryLevel%20JavaCompiler/frontend/src/App.jsx) | Main React component. Manages multi-language code buffers, execution requests, theme switching, and split layout view. |
| [`frontend/src/components/Navbar.jsx`](file:///c:/MAJOR%20PROJECT/EntryLevel%20JavaCompiler/frontend/src/components/Navbar.jsx) | Header bar with language selection dropdown, **Run** button (<kbd>Ctrl+Enter</kbd>), **Clear**, **Theme Toggle**, **Copy**, and **Download**. |
| [`frontend/src/components/CodeEditor.jsx`](file:///c:/MAJOR%20PROJECT/EntryLevel%20JavaCompiler/frontend/src/components/CodeEditor.jsx) | Monaco Editor wrapper configured for 14px font, customizable file names (`Solution.java`, `solution.py`, `App.jsx`), and theme adaptability. |
| [`frontend/src/components/ConsoleOutput.jsx`](file:///c:/MAJOR%20PROJECT/EntryLevel%20JavaCompiler/frontend/src/components/ConsoleOutput.jsx) | Programiz-style terminal output console supporting both dark and light styling with status badges and execution metrics. |
| [`frontend/src/components/WebPreview.jsx`](file:///c:/MAJOR%20PROJECT/EntryLevel%20JavaCompiler/frontend/src/components/WebPreview.jsx) | Interactive sandboxed iframe preview runner for **React.js** and **HTML / CSS / JS** web code. |
| [`frontend/src/utils/defaultTemplates.js`](file:///c:/MAJOR%20PROJECT/EntryLevel%20JavaCompiler/frontend/src/utils/defaultTemplates.js) | Defines languages, filenames, versions, and starter code for all supported languages. |

---

## ⚙️ Requirements

* **JDK 17+ or 21**
* **Node.js 18+** & **npm**
* *(Optional)* **Python 3**, **g++ / gcc** (for Python, C++, and C compilation)

---

## ▶️ How to Run

### 1. Start Backend Server
```bash
cd backend
javac -encoding UTF-8 JavaCompilerServer.java
java "-Dfile.encoding=UTF-8" JavaCompilerServer
```
*Backend runs on `http://localhost:8080`.*

### 2. Start Frontend Application
```bash
cd frontend
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173`.*

---

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
| :--- | :--- |
| <kbd>Ctrl</kbd> + <kbd>Enter</kbd> | Run & Execute Current Code |
