async function testStdin() {
  const res = await fetch('http://localhost:8080/api/compile', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      code: `import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your name: ");
        if (sc.hasNext()) {
            String name = sc.next();
            System.out.println("Welcome, " + name + "!");
        }
    }
}`,
      input: "Alex"
    })
  });
  const data = await res.json();
  console.log("Stdin Test Result:", data);
}
testStdin();
