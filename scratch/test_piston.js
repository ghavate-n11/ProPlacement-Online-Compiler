async function testPiston() {
  try {
    const res = await fetch('https://emkc.org/api/v2/piston/execute', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        language: 'cpp',
        version: '10.2.0',
        files: [{ name: 'solution.cpp', content: '#include <iostream>\nint main(){ std::cout << "C++ Working Successfully!" << std::endl; return 0; }' }]
      })
    });
    const data = await res.json();
    console.log('Piston C++ Result:', data);
  } catch(e) {
    console.log('Piston error:', e.message);
  }
}
testPiston();
