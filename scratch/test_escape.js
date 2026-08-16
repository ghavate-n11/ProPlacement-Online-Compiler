async function test() {
  const code = `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello\\nWorld");
    }
}`;
  const res = await fetch('http://localhost:8080/api/compile', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ code, input: '' })
  });
  console.log(await res.json());
}
test();
