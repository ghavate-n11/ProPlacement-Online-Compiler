export const LANGUAGE_CONFIG = {
  java: {
    id: 'java',
    name: 'Java',
    version: 'Java 21',
    fileName: 'Solution.java',
    monacoLang: 'java',
    type: 'backend',
    defaultCode: `// Java Solution - ProPlacement Practice
import java.util.*;

public class Solution {
    public static void main(String[] args) {
        System.out.println("Welcome to ProPlacement Online Compiler!");
        System.out.println("Practice • Prepare • Get Placed\\n");
        
        int[] numbers = {10, 25, 45, 80, 95};
        int sum = Arrays.stream(numbers).sum();
        System.out.println("Sample Data Sum: " + sum);
    }
}`
  },
  python: {
    id: 'python',
    name: 'Python',
    version: 'Python 3',
    fileName: 'solution.py',
    monacoLang: 'python',
    type: 'backend',
    defaultCode: `# Python Solution - ProPlacement Practice
def solve():
    print("Welcome to ProPlacement Python Compiler!")
    print("Practice • Prepare • Get Placed\\n")
    
    candidates = [
        {"name": "Alice", "score": 92, "role": "SDE-1"},
        {"name": "Bob", "score": 88, "role": "Backend"},
        {"name": "Charlie", "score": 95, "role": "Fullstack"}
    ]
    
    top = [c for c in candidates if c["score"] >= 90]
    print(f"Top Interview Candidates: {top}")

solve()
`
  },
  cpp: {
    id: 'cpp',
    name: 'C++',
    version: 'C++ 20',
    fileName: 'solution.cpp',
    monacoLang: 'cpp',
    type: 'backend',
    defaultCode: `// C++ 20 Solution - ProPlacement Practice
#include <iostream>
#include <vector>
#include <numeric>

int main() {
    std::cout << "Welcome to ProPlacement C++ Compiler!" << std::endl;
    std::cout << "Practice • Prepare • Get Placed\\n" << std::endl;
    
    std::vector<int> nums = {10, 20, 30, 40, 50};
    int total = std::accumulate(nums.begin(), nums.end(), 0);
    
    std::cout << "Vector Sum: " << total << std::endl;
    return 0;
}
`
  },
  c: {
    id: 'c',
    name: 'C',
    version: 'C11',
    fileName: 'solution.c',
    monacoLang: 'c',
    type: 'backend',
    defaultCode: `// C Solution - ProPlacement Practice
#include <stdio.h>

int main() {
    printf("Welcome to ProPlacement C Compiler!\\n");
    printf("Practice • Prepare • Get Placed\\n\\n");
    
    int a = 15, b = 35;
    printf("Result of %d + %d = %d\\n", a, b, a + b);
    return 0;
}
`
  },
  mysql: {
    id: 'mysql',
    name: 'MySQL',
    version: 'MySQL 8.0',
    fileName: 'query.sql',
    monacoLang: 'sql',
    type: 'backend',
    defaultCode: `-- MySQL Database Query - ProPlacement Practice
-- Practice SQL queries for interviews & placement rounds

CREATE TABLE employees (
    id INT PRIMARY KEY,
    name VARCHAR(50),
    department VARCHAR(50),
    salary DECIMAL(10, 2)
);

INSERT INTO employees VALUES (101, 'Alex Johnson', 'Engineering', 95000.00);
INSERT INTO employees VALUES (102, 'Priya Sharma', 'Product', 105000.00);
INSERT INTO employees VALUES (103, 'David Lee', 'Design', 88000.00);
INSERT INTO employees VALUES (104, 'Sara Khan', 'Engineering', 92000.00);

-- Query: High-paying candidates by department
SELECT id, name, department, salary 
FROM employees 
WHERE salary >= 90000 
ORDER BY salary DESC;

-- Departmental analytics
SELECT department, COUNT(*) AS total_employees, AVG(salary) AS avg_salary
FROM employees
GROUP BY department;
`
  },
  oracle: {
    id: 'oracle',
    name: 'Oracle SQL',
    version: 'Oracle 21c',
    fileName: 'schema.sql',
    monacoLang: 'sql',
    type: 'backend',
    defaultCode: `-- Oracle SQL - ProPlacement Practice
-- Oracle SQL*Plus schema and queries for placement exams

CREATE TABLE students (
    student_id NUMBER PRIMARY KEY,
    name VARCHAR2(50),
    branch VARCHAR2(50),
    cgpa NUMBER(3, 2)
);

INSERT INTO students VALUES (1001, 'Rohan Gupta', 'CSE', 9.20);
INSERT INTO students VALUES (1002, 'Ananya Sen', 'IT', 8.90);
INSERT INTO students VALUES (1003, 'Karan Mehta', 'CSE', 9.50);
INSERT INTO students VALUES (1004, 'Sneha Rao', 'ECE', 8.40);

-- Select eligible students for placement drives
SELECT student_id, name, branch, cgpa
FROM students
WHERE cgpa >= 8.50
ORDER BY cgpa DESC;

-- Aggregate placement metrics by branch
SELECT branch, COUNT(*) AS candidates, AVG(cgpa) AS avg_cgpa
FROM students
GROUP BY branch;
`
  },
  mongodb: {
    id: 'mongodb',
    name: 'MongoDB',
    version: 'MongoDB 7.0',
    fileName: 'query.js',
    monacoLang: 'javascript',
    type: 'backend',
    defaultCode: `// MongoDB NoSQL Shell - ProPlacement Practice
// Practice document queries, filtering & aggregation pipelines

// 1. Insert placement candidate profiles
db.placements.insertMany([
  { name: "Rahul Verma", company: "Google", package_lpa: 32, branch: "CSE" },
  { name: "Ananya Roy", company: "Amazon", package_lpa: 28, branch: "IT" },
  { name: "Karan Patel", company: "Microsoft", package_lpa: 36, branch: "CSE" },
  { name: "Sneha Gupta", company: "Adobe", package_lpa: 24, branch: "ECE" }
]);

// 2. Find offers with package >= 28 LPA
db.placements.find({ package_lpa: { $gte: 28 } });

// 3. Aggregation Pipeline: Statistics by Branch
db.placements.aggregate([
  { $match: { package_lpa: { $gte: 20 } } },
  { $group: { _id: "$branch", count: { $sum: 1 }, total_package: { $sum: "$package_lpa" } } },
  { $sort: { total_package: -1 } }
]);
`
  },
  javascript: {
    id: 'javascript',
    name: 'JavaScript',
    version: 'Node.js 20',
    fileName: 'solution.js',
    monacoLang: 'javascript',
    type: 'backend',
    defaultCode: `// JavaScript (Node.js) - ProPlacement Practice
console.log("Welcome to ProPlacement JavaScript Runtime!");
console.log("Practice • Prepare • Get Placed\\n");

const candidates = [
    { id: 1, name: "Alice", skills: ["Java", "DSA", "SQL"] },
    { id: 2, name: "Bob", skills: ["Python", "Machine Learning"] },
    { id: 3, name: "Charlie", skills: ["React", "Node.js", "MongoDB"] }
];

console.log("Candidate Profiles:");
candidates.forEach(c => console.log(\`- \${c.name}: \${c.skills.join(', ')}\`));
`
  },
  react: {
    id: 'react',
    name: 'React.js',
    version: 'React 18',
    fileName: 'App.jsx',
    monacoLang: 'javascript',
    type: 'frontend_react',
    defaultCode: `// React.js Interactive Component Preview
import React, { useState } from 'react';

export default function App() {
  const [count, setCount] = useState(0);
  const [todos, setTodos] = useState(['Practice DSA Arrays', 'Solve SQL Joins', 'Build React Portfolio']);
  const [text, setText] = useState('');

  const addTodo = () => {
    if (!text.trim()) return;
    setTodos([...todos, text]);
    setText('');
  };

  return (
    <div style={{
      fontFamily: 'system-ui, -apple-system, sans-serif',
      padding: '2rem',
      maxWidth: '500px',
      margin: '0 auto',
      color: '#1e293b'
    }}>
      <div style={{
        background: 'linear-gradient(135deg, #3b82f6 0%, #6366f1 100%)',
        padding: '1.5rem',
        borderRadius: '12px',
        color: '#fff',
        marginBottom: '1.5rem',
        boxShadow: '0 10px 25px -5px rgba(59, 130, 246, 0.3)'
      }}>
        <h2 style={{ margin: 0, fontSize: '1.4rem' }}>⚡ ProPlacement React Playground</h2>
        <p style={{ margin: '0.5rem 0 0', opacity: 0.9, fontSize: '0.9rem' }}>
          Practice • Prepare • Get Placed
        </p>
      </div>

      <div style={{
        background: '#ffffff',
        border: '1px solid #e2e8f0',
        borderRadius: '12px',
        padding: '1.25rem',
        marginBottom: '1.25rem',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)'
      }}>
        <h3 style={{ marginTop: 0, fontSize: '1.1rem' }}>Placement Prep Progress</h3>
        <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#2563eb', margin: '0.5rem 0' }}>
          {count} Problems Solved
        </p>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            onClick={() => setCount(count + 1)}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#3b82f6',
              color: '#fff',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: 600
            }}
          >
            + Solve Problem
          </button>
          <button
            onClick={() => setCount(0)}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#f1f5f9',
              color: '#475569',
              border: '1px solid #cbd5e1',
              borderRadius: '6px',
              cursor: 'pointer'
            }}
          >
            Reset
          </button>
        </div>
      </div>

      <div style={{
        background: '#ffffff',
        border: '1px solid #e2e8f0',
        borderRadius: '12px',
        padding: '1.25rem',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.05)'
      }}>
        <h3 style={{ marginTop: 0, fontSize: '1.1rem' }}>Interview Checklist</h3>
        <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
          <input
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Add interview topic..."
            style={{
              flex: 1,
              padding: '0.5rem 0.75rem',
              borderRadius: '6px',
              border: '1px solid #cbd5e1',
              fontSize: '0.9rem'
            }}
          />
          <button
            onClick={addTodo}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#10b981',
              color: '#fff',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontWeight: 600
            }}
          >
            Add
          </button>
        </div>
        <ul style={{ paddingLeft: '1.2rem', margin: 0 }}>
          {todos.map((item, i) => (
            <li key={i} style={{ padding: '0.3rem 0', color: '#334155' }}>
              {item}
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
`
  },
  html: {
    id: 'html',
    name: 'HTML / CSS / JS',
    version: 'HTML5',
    fileName: 'index.html',
    monacoLang: 'html',
    type: 'frontend_web',
    defaultCode: `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>ProPlacement Web Sandbox</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      font-family: 'Segoe UI', system-ui, sans-serif;
      background: #0f172a;
      color: #f8fafc;
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1.5rem;
    }
    .card {
      background: rgba(30, 41, 59, 0.8);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.1);
      padding: 2.5rem;
      border-radius: 16px;
      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
      text-align: center;
      max-width: 450px;
      width: 100%;
    }
    h1 {
      font-size: 1.8rem;
      background: linear-gradient(135deg, #38bdf8, #818cf8);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-bottom: 0.5rem;
    }
    p { color: #94a3b8; font-size: 0.95rem; margin-bottom: 1.5rem; }
    .btn {
      background: linear-gradient(135deg, #3b82f6, #6366f1);
      color: white;
      border: none;
      padding: 0.75rem 1.75rem;
      font-size: 0.95rem;
      font-weight: 600;
      border-radius: 8px;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 20px rgba(59, 130, 246, 0.4);
    }
    .status {
      margin-top: 1.25rem;
      font-size: 0.85rem;
      color: #38bdf8;
      font-family: monospace;
    }
  </style>
</head>
<body>
  <div class="card">
    <h1>🚀 ProPlacement Web Studio</h1>
    <p>Practice • Prepare • Get Placed</p>
    <button class="btn" id="clickBtn">Click Me ✨</button>
    <div class="status" id="statusText">Interactive Clicks: 0</div>
  </div>

  <script>
    let count = 0;
    document.getElementById('clickBtn').addEventListener('click', () => {
      count++;
      document.getElementById('statusText').innerText = 'Interactive Clicks: ' + count;
    });
  </script>
</body>
</html>`
  }
};
