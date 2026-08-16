async function testAll() {
  console.log("=== Testing All Compilers & Runtimes ===");

  // 1. Java Test
  const javaRes = await fetch('http://localhost:8080/api/compile', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: `public class Solution { public static void main(String[] args) { System.out.println("Java 21 OK: " + (10 + 20)); } }`,
      input: '',
      language: 'java'
    })
  });
  console.log("1. Java Response:", await javaRes.json());

  // 2. Python Test
  const pyRes = await fetch('http://localhost:8080/api/compile', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: `print("Python 3 OK: " + str([x**2 for x in range(5)]))`,
      input: '',
      language: 'python'
    })
  });
  console.log("2. Python Response:", await pyRes.json());

  // 3. C++ Test
  const cppRes = await fetch('http://localhost:8080/api/compile', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: `#include <iostream>\n#include <vector>\nint main(){ std::cout << "C++ 20 (G++) OK!" << std::endl; return 0; }`,
      input: '',
      language: 'cpp'
    })
  });
  console.log("3. C++ Response:", await cppRes.json());

  // 4. C Test
  const cRes = await fetch('http://localhost:8080/api/compile', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: `#include <stdio.h>\nint main(){ printf("C (TCC/GCC) OK! Sum: %d\\n", 15 + 35); return 0; }`,
      input: '',
      language: 'c'
    })
  });
  console.log("4. C Response:", await cRes.json());

  // 5. JavaScript Test
  const jsRes = await fetch('http://localhost:8080/api/compile', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: `console.log("JavaScript (Node.js) OK: " + [1,2,3,4].reduce((a,b)=>a+b));`,
      input: '',
      language: 'javascript'
    })
  });
  console.log("5. JavaScript Response:", await jsRes.json());
}
testAll();
