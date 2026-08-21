async function test() {
  const res = await fetch('https://pro-placement-online-compiler-bc3y.vercel.app//api/compile', {
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
