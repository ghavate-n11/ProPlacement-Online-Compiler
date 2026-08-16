async function runFullVerification() {
  console.log("=================================================");
  console.log("   ProPlacement Multi-Language & Database Test   ");
  console.log("=================================================");

  const tests = [
    {
      lang: "java",
      name: "Java 21",
      code: `public class Solution { public static void main(String[] args) { System.out.println("Java ProPlacement OK: " + (50 * 2)); } }`
    },
    {
      lang: "mysql",
      name: "MySQL 8.0 (100% Pure Java Engine)",
      code: `
        CREATE TABLE candidates (id INT PRIMARY KEY, name VARCHAR(50), package_lpa INT);
        INSERT INTO candidates VALUES (1, 'Rohan', 32), (2, 'Ananya', 28), (3, 'Karan', 45);
        SELECT id, name, package_lpa FROM candidates WHERE package_lpa >= 30 ORDER BY package_lpa DESC;
      `
    },
    {
      lang: "oracle",
      name: "Oracle SQL 21c (100% Pure Java Engine)",
      code: `
        CREATE TABLE students (id NUMBER, name VARCHAR2(50), cgpa NUMBER(3,2));
        INSERT INTO students VALUES (101, 'Sneha', 9.40);
        INSERT INTO students VALUES (102, 'Aditya', 8.85);
        SELECT id, name, cgpa FROM students ORDER BY cgpa DESC;
      `
    },
    {
      lang: "mongodb",
      name: "MongoDB 7.0 (100% Pure Java Engine)",
      code: `
        db.interviews.insertMany([
          { candidate: "Rohan", role: "SDE-2", status: "Selected" },
          { candidate: "Priya", role: "Frontend", status: "Selected" },
          { candidate: "Amit", role: "DevOps", status: "Pending" }
        ]);
        db.interviews.find({ status: "Selected" });
      `
    },
    {
      lang: "python",
      name: "Python 3",
      code: `print("Python ProPlacement OK: " + str([x for x in range(5)]))`
    },
    {
      lang: "cpp",
      name: "C++ 20",
      code: `#include <iostream>\nint main(){ std::cout << "C++ 20 ProPlacement OK!" << std::endl; return 0; }`
    },
    {
      lang: "c",
      name: "C11",
      code: `#include <stdio.h>\nint main(){ printf("C ProPlacement OK!\\n"); return 0; }`
    },
    {
      lang: "javascript",
      name: "JavaScript (Node.js)",
      code: `console.log("JavaScript ProPlacement OK: " + (100 / 4));`
    }
  ];

  for (const t of tests) {
    try {
      const res = await fetch("http://localhost:8080/api/compile", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code: t.code, input: "", language: t.lang })
      });
      const data = await res.json();
      console.log(`\n--- [${t.name}] Status: ${data.status} (${data.executionTimeMs || 0}ms) ---`);
      if (data.output) console.log(data.output.trim());
      if (data.error) console.error("Error:", data.error.trim());
    } catch (e) {
      console.error(`Failed ${t.name}:`, e.message);
    }
  }
}

runFullVerification();
