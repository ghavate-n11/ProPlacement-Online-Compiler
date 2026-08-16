async function test() {
  const res = await fetch('http://localhost:8080/api/compile', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, Programiz Java Compiler!");
    }
}`,
      input: ""
    })
  });
  const data = await res.json();
  console.log("Compile Result:", data);
}
test();
