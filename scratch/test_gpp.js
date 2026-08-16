const { execSync } = require('child_process');
const path = require('path');

const w64Bin = path.resolve('backend/tools/w64devkit/bin');
const env = { ...process.env, PATH: `${w64Bin};${process.env.PATH}` };

console.log('Testing g++ compilation with PATH injection...');
try {
  const compileOut = execSync('g++ -O2 -std=c++20 "scratch/test_cpp.cpp" -o "scratch/test_cpp.exe"', { env, encoding: 'utf8' });
  console.log('Compilation output:', compileOut);
  
  const runOut = execSync('"scratch/test_cpp.exe"', { env, encoding: 'utf8' });
  console.log('Execution output:', runOut);
} catch (err) {
  console.error('Error:', err.message, err.stdout, err.stderr);
}
