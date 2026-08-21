import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaCompilerServer {

    private static int getPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }
        return 8080;
    }

    private static final int PORT = getPort();
    private static final int TIMEOUT_SECONDS = 7;
    private static final Path TEMP_BASE_DIR = Paths.get("temp_exec");

    public static void main(String[] args) throws IOException {
        if (!Files.exists(TEMP_BASE_DIR)) {
            Files.createDirectories(TEMP_BASE_DIR);
        }

        int port = getPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        server.setExecutor(executor);

        server.createContext("/api/compile", new CompileHandler());
        server.createContext("/api/health", new HealthHandler());

        server.start();
        System.out.println("=================================================");
        System.out.println("   ProPlacement Multi-Language Compiler Server   ");
        System.out.println("          Practice • Prepare • Get Placed        ");
        System.out.println("   Listening on: http://0.0.0.0:" + port + "          ");
        System.out.println("   Java: " + System.getProperty("java.version"));
        System.out.println("=================================================");
    }

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String response = "{\"status\":\"OK\",\"javaVersion\":\"" + escapeJson(System.getProperty("java.version")) + "\"}";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    static class CompileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body;
            try (InputStream is = exchange.getRequestBody();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096];
                int read;
                while ((read = is.read(buf)) != -1) {
                    baos.write(buf, 0, read);
                }
                body = baos.toString(StandardCharsets.UTF_8);
            }

            String code = extractJsonField(body, "code");
            String input = extractJsonField(body, "input");
            String language = extractJsonField(body, "language");
            if (language == null || language.trim().isEmpty()) {
                language = "java";
            } else {
                language = language.trim().toLowerCase();
            }

            if (code == null || code.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"status\":\"ERROR\",\"error\":\"Code parameter cannot be empty\"}");
                return;
            }

            String jsonResult = executeCode(code, input != null ? input : "", language);
            sendJsonResponse(exchange, 200, jsonResult);
        }
    }

    private static String executeCode(String code, String stdinInput, String language) {
        String jobId = "job_" + UUID.randomUUID().toString().replace("-", "");
        Path jobDir = TEMP_BASE_DIR.resolve(jobId);

        long compileTimeMs = 0;
        long executionTimeMs = 0;

        try {
            Files.createDirectories(jobDir);

            if ("python".equals(language) || "py".equals(language)) {
                return executePython(jobDir, code, stdinInput);
            } else if ("javascript".equals(language) || "js".equals(language) || "node".equals(language)) {
                return executeJavaScript(jobDir, code, stdinInput);
            } else if ("cpp".equals(language) || "c++".equals(language)) {
                return executeCpp(jobDir, code, stdinInput);
            } else if ("c".equals(language)) {
                return executeC(jobDir, code, stdinInput);
            } else if ("mysql".equals(language) || "sql".equals(language)) {
                return executeMySql(jobDir, code, stdinInput);
            } else if ("oracle".equals(language) || "plsql".equals(language) || "oracle-sql".equals(language)) {
                return executeOracle(jobDir, code, stdinInput);
            } else if ("mongodb".equals(language) || "mongo".equals(language) || "nosql".equals(language)) {
                return executeMongo(jobDir, code, stdinInput);
            } else {
                // Default: Java
                return executeJava(jobDir, code, stdinInput);
            }

        } catch (Exception e) {
            return buildJsonResponse("SYSTEM_ERROR", "", "Server error: " + e.getMessage(), compileTimeMs, executionTimeMs, 500);
        } finally {
            deleteDirectory(jobDir.toFile());
        }
    }

    // MySQL Execution (100% Pure Java In-Memory Engine)
    private static String executeMySql(Path jobDir, String code, String stdinInput) {
        long start = System.currentTimeMillis();
        try {
            String output = JavaSqlEngine.execute(code, "mysql");
            long time = System.currentTimeMillis() - start;
            return buildJsonResponse("SUCCESS", output, "", 0, time, 0);
        } catch (Exception e) {
            long time = System.currentTimeMillis() - start;
            return buildJsonResponse("RUNTIME_ERROR", "", "MySQL Error: " + e.getMessage(), 0, time, 1);
        }
    }

    // Oracle Execution (100% Pure Java In-Memory Engine)
    private static String executeOracle(Path jobDir, String code, String stdinInput) {
        long start = System.currentTimeMillis();
        try {
            String output = JavaSqlEngine.execute(code, "oracle");
            long time = System.currentTimeMillis() - start;
            return buildJsonResponse("SUCCESS", output, "", 0, time, 0);
        } catch (Exception e) {
            long time = System.currentTimeMillis() - start;
            return buildJsonResponse("RUNTIME_ERROR", "", "Oracle Error: " + e.getMessage(), 0, time, 1);
        }
    }

    // MongoDB Execution (100% Pure Java In-Memory Engine)
    private static String executeMongo(Path jobDir, String code, String stdinInput) {
        long start = System.currentTimeMillis();
        try {
            String output = JavaMongoEngine.execute(code);
            long time = System.currentTimeMillis() - start;
            return buildJsonResponse("SUCCESS", output, "", 0, time, 0);
        } catch (Exception e) {
            long time = System.currentTimeMillis() - start;
            return buildJsonResponse("RUNTIME_ERROR", "", "MongoDB Error: " + e.getMessage(), 0, time, 1);
        }
    }

    // Java Execution
    private static String executeJava(Path jobDir, String code, String stdinInput) throws Exception {
        String className = detectClassName(code);
        Path javaFile = jobDir.resolve(className + ".java");
        Files.writeString(javaFile, code, StandardCharsets.UTF_8);

        long compileStart = System.currentTimeMillis();
        ProcessBuilder javacBuilder = createProcessBuilder(new String[]{"javac", "-encoding", "UTF-8", javaFile.getFileName().toString()});
        javacBuilder.directory(jobDir.toFile());
        Process javacProcess = javacBuilder.start();

        ExecutorService javacPool = Executors.newSingleThreadExecutor();
        Future<String> javacErrFuture = javacPool.submit(() -> readStream(javacProcess.getErrorStream()));

        boolean javacFinished = javacProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        long compileTimeMs = System.currentTimeMillis() - compileStart;

        if (!javacFinished) {
            javacProcess.destroyForcibly();
            javacPool.shutdownNow();
            return buildJsonResponse("COMPILATION_ERROR", "", "Java compilation timed out after " + TIMEOUT_SECONDS + "s.", compileTimeMs, 0, -1);
        }

        String javacErr = "";
        try {
            javacErr = javacErrFuture.get(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
        finally {
            javacPool.shutdown();
        }

        int javacExit = javacProcess.exitValue();
        if (javacExit != 0) {
            return buildJsonResponse("COMPILATION_ERROR", "", javacErr, compileTimeMs, 0, javacExit);
        }

        return runSubprocess(jobDir, new String[]{"java", "-Dfile.encoding=UTF-8", "-cp", ".", className}, stdinInput, compileTimeMs);
    }

    // Python Execution
    private static String executePython(Path jobDir, String code, String stdinInput) throws Exception {
        Path pyFile = jobDir.resolve("solution.py");
        Files.writeString(pyFile, code, StandardCharsets.UTF_8);
        String pythonCmd = findPythonExecutable();
        return runSubprocess(jobDir, new String[]{pythonCmd, "-u", "solution.py"}, stdinInput, 0);
    }

    // JavaScript Execution (Node.js)
    private static String executeJavaScript(Path jobDir, String code, String stdinInput) throws Exception {
        Path jsFile = jobDir.resolve("solution.js");
        Files.writeString(jsFile, code, StandardCharsets.UTF_8);
        return runSubprocess(jobDir, new String[]{"node", "solution.js"}, stdinInput, 0);
    }

    // C++ Execution
    private static String executeCpp(Path jobDir, String code, String stdinInput) throws Exception {
        Path cppFile = jobDir.resolve("solution.cpp");
        Files.writeString(cppFile, code, StandardCharsets.UTF_8);
        String outputBinary = isWindows() ? "solution.exe" : "solution";

        long compileStart = System.currentTimeMillis();
        String gppCmd = findGppExecutable();
        ProcessBuilder gppBuilder = createProcessBuilder(new String[]{gppCmd, "-O2", "-std=c++20", "solution.cpp", "-o", outputBinary});
        gppBuilder.directory(jobDir.toFile());
        
        Process gppProcess;
        try {
            gppProcess = gppBuilder.start();
        } catch (IOException e) {
            return buildJsonResponse("SYSTEM_ERROR", "", "C++ compiler (" + gppCmd + ") is not available on the server.", 0, 0, -1);
        }

        ExecutorService gppPool = Executors.newSingleThreadExecutor();
        Future<String> gppErrFuture = gppPool.submit(() -> readStream(gppProcess.getErrorStream()));
        boolean gppFinished = gppProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        long compileTimeMs = System.currentTimeMillis() - compileStart;

        if (!gppFinished) {
            gppProcess.destroyForcibly();
            gppPool.shutdownNow();
            return buildJsonResponse("COMPILATION_ERROR", "", "C++ compilation timed out.", compileTimeMs, 0, -1);
        }

        String gppErr = "";
        try { gppErr = gppErrFuture.get(2, TimeUnit.SECONDS); } catch (Exception ignored) {}
        finally { gppPool.shutdown(); }

        int gppExit = gppProcess.exitValue();
        if (gppExit != 0) {
            return buildJsonResponse("COMPILATION_ERROR", "", gppErr, compileTimeMs, 0, gppExit);
        }

        Path binaryPath = jobDir.resolve(outputBinary);
        binaryPath.toFile().setExecutable(true, false);
        return runSubprocess(jobDir, new String[]{binaryPath.toAbsolutePath().toString()}, stdinInput, compileTimeMs);
    }

    // C Execution
    private static String executeC(Path jobDir, String code, String stdinInput) throws Exception {
        Path cFile = jobDir.resolve("solution.c");
        Files.writeString(cFile, code, StandardCharsets.UTF_8);
        String outputBinary = isWindows() ? "solution.exe" : "solution";

        long compileStart = System.currentTimeMillis();
        String gccCmd = findGccExecutable();
        ProcessBuilder gccBuilder = createProcessBuilder(new String[]{gccCmd, "-o", outputBinary, "solution.c"});
        gccBuilder.directory(jobDir.toFile());

        Process gccProcess;
        try {
            gccProcess = gccBuilder.start();
        } catch (IOException e) {
            return buildJsonResponse("SYSTEM_ERROR", "", "C compiler (" + gccCmd + ") is not available on the server.", 0, 0, -1);
        }

        ExecutorService gccPool = Executors.newSingleThreadExecutor();
        Future<String> gccErrFuture = gccPool.submit(() -> readStream(gccProcess.getErrorStream()));
        boolean gccFinished = gccProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        long compileTimeMs = System.currentTimeMillis() - compileStart;

        if (!gccFinished) {
            gccProcess.destroyForcibly();
            gccPool.shutdownNow();
            return buildJsonResponse("COMPILATION_ERROR", "", "C compilation timed out.", compileTimeMs, 0, -1);
        }

        String gccErr = "";
        try { gccErr = gccErrFuture.get(2, TimeUnit.SECONDS); } catch (Exception ignored) {}
        finally { gccPool.shutdown(); }

        int gccExit = gccProcess.exitValue();
        if (gccExit != 0) {
            return buildJsonResponse("COMPILATION_ERROR", "", gccErr, compileTimeMs, 0, gccExit);
        }

        Path binaryPath = jobDir.resolve(outputBinary);
        binaryPath.toFile().setExecutable(true, false);
        return runSubprocess(jobDir, new String[]{binaryPath.toAbsolutePath().toString()}, stdinInput, compileTimeMs);
    }

    private static Path getToolsDir() {
        Path backendTools = Paths.get("backend", "tools").toAbsolutePath();
        if (Files.exists(backendTools)) {
            return backendTools;
        }
        Path rootTools = Paths.get("tools").toAbsolutePath();
        if (Files.exists(rootTools)) {
            return rootTools;
        }
        return Paths.get("backend", "tools").toAbsolutePath();
    }

    private static String findGppExecutable() {
        Path toolGpp = getToolsDir().resolve(Paths.get("w64devkit", "bin", "g++.exe"));
        if (Files.exists(toolGpp)) {
            return toolGpp.toString();
        }
        return "g++";
    }

    private static String findGccExecutable() {
        Path toolTcc = getToolsDir().resolve(Paths.get("tcc", "tcc.exe"));
        if (Files.exists(toolTcc)) {
            return toolTcc.toString();
        }
        Path toolGcc = getToolsDir().resolve(Paths.get("w64devkit", "bin", "gcc.exe"));
        if (Files.exists(toolGcc)) {
            return toolGcc.toString();
        }
        return "gcc";
    }

    private static String findPythonExecutable() {
        if (isWindows()) {
            return "python";
        }
        // In Linux/Docker, check if python3 exists first
        try {
            Process p = new ProcessBuilder("python3", "--version").start();
            if (p.waitFor(1, TimeUnit.SECONDS) && p.exitValue() == 0) {
                return "python3";
            }
        } catch (Exception ignored) {}
        return "python";
    }

    private static ProcessBuilder createProcessBuilder(String[] command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        Path toolsDir = getToolsDir();
        Path w64Bin = toolsDir.resolve(Paths.get("w64devkit", "bin"));
        Path tccBin = toolsDir.resolve("tcc");
        
        String currentPath = System.getenv("PATH");
        if (currentPath == null) currentPath = "";
        
        String extraPaths = "";
        if (Files.exists(w64Bin)) {
            extraPaths += w64Bin.toString() + File.pathSeparator;
        }
        if (Files.exists(tccBin)) {
            extraPaths += tccBin.toString() + File.pathSeparator;
        }
        
        if (!extraPaths.isEmpty()) {
            pb.environment().put("PATH", extraPaths + currentPath);
        }
        return pb;
    }

    private static String runSubprocess(Path jobDir, String[] command, String stdinInput, long compileTimeMs) throws Exception {
        long execStart = System.currentTimeMillis();
        ProcessBuilder pb = createProcessBuilder(command);
        pb.directory(jobDir.toFile());
        Process process = pb.start();

        try (OutputStream os = process.getOutputStream()) {
            if (stdinInput != null && !stdinInput.isEmpty()) {
                os.write(stdinInput.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        } catch (IOException ignored) {}

        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<String> stdoutFuture = pool.submit(() -> readStream(process.getInputStream()));
        Future<String> stderrFuture = pool.submit(() -> readStream(process.getErrorStream()));

        boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        long executionTimeMs = System.currentTimeMillis() - execStart;

        if (!finished) {
            process.destroyForcibly();
            pool.shutdownNow();
            return buildJsonResponse("TIMEOUT", "", "Execution timed out (" + TIMEOUT_SECONDS + "s limit exceeded).", compileTimeMs, executionTimeMs, -1);
        }

        String stdout = "";
        String stderr = "";
        try {
            stdout = stdoutFuture.get(2, TimeUnit.SECONDS);
            stderr = stderrFuture.get(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
        finally {
            pool.shutdown();
        }

        int exitCode = process.exitValue();
        String status = (exitCode == 0) ? "SUCCESS" : "RUNTIME_ERROR";
        return buildJsonResponse(status, stdout, stderr, compileTimeMs, executionTimeMs, exitCode);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static String detectClassName(String code) {
        String clean = code.replaceAll("//.*", "").replaceAll("/\\*([\\s\\S]*?)\\*/", "");
        Pattern publicPattern = Pattern.compile("public\\s+class\\s+([A-Za-z0-9_$]+)");
        Matcher matcher = publicPattern.matcher(clean);
        if (matcher.find()) {
            return matcher.group(1);
        }
        Pattern classPattern = Pattern.compile("class\\s+([A-Za-z0-9_$]+)");
        matcher = classPattern.matcher(clean);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Solution";
    }

    private static String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
            if (baos.size() > 500000) {
                baos.write("\n... [Output truncated at 500KB]".getBytes(StandardCharsets.UTF_8));
                break;
            }
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDirectory(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    private static String extractJsonField(String json, String field) {
        if (json == null) return null;
        String key = "\"" + field + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return null;

        int colonIndex = json.indexOf(':', keyIndex + key.length());
        if (colonIndex == -1) return null;

        int openQuote = json.indexOf('"', colonIndex + 1);
        if (openQuote == -1) return null;

        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = openQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                switch (c) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException e) {
                                sb.append('u');
                            }
                        } else {
                            sb.append('u');
                        }
                        break;
                    default:
                        sb.append(c);
                        break;
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                return sb.toString();
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    private static String buildJsonResponse(String status, String stdout, String stderr, long compileTimeMs, long executionTimeMs, int exitCode) {
        return String.format(
            "{\"status\":\"%s\",\"output\":\"%s\",\"error\":\"%s\",\"compileTimeMs\":%d,\"executionTimeMs\":%d,\"exitCode\":%d}",
            escapeJson(status), escapeJson(stdout), escapeJson(stderr), compileTimeMs, executionTimeMs, exitCode
        );
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // =========================================================================
    // 100% PURE JAVA IN-MEMORY SQL ENGINE (MySQL & Oracle Dialects)
    // =========================================================================
    public static class JavaSqlEngine {
        static class Table {
            String name;
            List<String> columns = new ArrayList<>();
            List<Map<String, Object>> rows = new ArrayList<>();

            Table(String name) { this.name = name; }
        }

        public static String execute(String sqlScript, String dialect) {
            Map<String, Table> tables = new LinkedHashMap<>();
            StringBuilder output = new StringBuilder();

            String[] statements = sqlScript.split(";");
            for (String raw : statements) {
                String stmt = cleanSql(raw);
                if (stmt.isEmpty()) continue;

                try {
                    String upper = stmt.toUpperCase();
                    if (upper.startsWith("CREATE TABLE")) {
                        handleCreateTable(stmt, tables);
                        if ("oracle".equalsIgnoreCase(dialect)) output.append("Table created.\n");
                        else output.append("Query OK, 0 rows affected\n");
                    } else if (upper.startsWith("INSERT INTO")) {
                        int count = handleInsert(stmt, tables);
                        if ("oracle".equalsIgnoreCase(dialect)) output.append(count).append(" row(s) created.\n");
                        else output.append("Query OK, ").append(count).append(" row(s) affected\n");
                    } else if (upper.startsWith("UPDATE")) {
                        int count = handleUpdate(stmt, tables);
                        if ("oracle".equalsIgnoreCase(dialect)) output.append(count).append(" row(s) updated.\n");
                        else output.append("Query OK, ").append(count).append(" row(s) affected\n");
                    } else if (upper.startsWith("DELETE FROM") || upper.startsWith("DELETE")) {
                        int count = handleDelete(stmt, tables);
                        if ("oracle".equalsIgnoreCase(dialect)) output.append(count).append(" row(s) deleted.\n");
                        else output.append("Query OK, ").append(count).append(" row(s) affected\n");
                    } else if (upper.startsWith("SELECT")) {
                        String formatted = handleSelect(stmt, tables, dialect);
                        output.append(formatted).append("\n");
                    } else if (upper.startsWith("DROP TABLE")) {
                        handleDrop(stmt, tables);
                        if ("oracle".equalsIgnoreCase(dialect)) output.append("Table dropped.\n");
                        else output.append("Query OK, 0 rows affected\n");
                    } else {
                        if ("oracle".equalsIgnoreCase(dialect)) output.append("Statement processed.\n");
                        else output.append("Query OK, 0 rows affected\n");
                    }
                } catch (Exception e) {
                    if ("oracle".equalsIgnoreCase(dialect)) output.append("ORA-00942: ").append(e.getMessage()).append("\n");
                    else output.append("ERROR 1064 (42000): ").append(e.getMessage()).append("\n");
                }
            }
            return output.toString();
        }

        private static String cleanSql(String sql) {
            StringBuilder sb = new StringBuilder();
            for (String line : sql.split("\n")) {
                String tr = line.trim();
                if (!tr.startsWith("--") && !tr.startsWith("/*")) {
                    sb.append(line).append(" ");
                }
            }
            return sb.toString().trim();
        }

        private static void handleCreateTable(String stmt, Map<String, Table> tables) {
            Pattern p = Pattern.compile("CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([a-zA-Z0-9_$]+)\\s*\\((.*)\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(stmt);
            if (!m.find()) throw new IllegalArgumentException("Invalid CREATE TABLE syntax");
            String tableName = m.group(1).toLowerCase();
            String colDefs = m.group(2);

            Table table = new Table(tableName);
            for (String colDef : colDefs.split(",")) {
                String clean = colDef.trim();
                if (clean.toUpperCase().startsWith("PRIMARY KEY") || clean.toUpperCase().startsWith("FOREIGN KEY") || clean.toUpperCase().startsWith("CONSTRAINT")) {
                    continue;
                }
                String[] parts = clean.split("\\s+");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    table.columns.add(parts[0].toLowerCase());
                }
            }
            tables.put(tableName, table);
        }

        private static int handleInsert(String stmt, Map<String, Table> tables) {
            Pattern p = Pattern.compile("INSERT\\s+INTO\\s+([a-zA-Z0-9_$]+)(?:\\s*\\(([^)]+)\\))?\\s+VALUES\\s*(.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(stmt);
            if (!m.find()) throw new IllegalArgumentException("Invalid INSERT INTO syntax");

            String tableName = m.group(1).toLowerCase();
            Table table = tables.get(tableName);
            if (table == null) throw new IllegalArgumentException("Table '" + tableName + "' doesn't exist");

            String colsPart = m.group(2);
            List<String> targetCols = new ArrayList<>();
            if (colsPart != null) {
                for (String c : colsPart.split(",")) targetCols.add(c.trim().toLowerCase());
            } else {
                targetCols.addAll(table.columns);
            }

            String valuesPart = m.group(3).trim();
            Pattern rowP = Pattern.compile("\\(([^)]+)\\)");
            Matcher rowM = rowP.matcher(valuesPart);
            int count = 0;
            while (rowM.find()) {
                String vals = rowM.group(1);
                List<Object> parsedVals = parseValues(vals);
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < targetCols.size(); i++) {
                    String col = targetCols.get(i);
                    Object val = i < parsedVals.size() ? parsedVals.get(i) : null;
                    row.put(col, val);
                }
                table.rows.add(row);
                count++;
            }
            return count;
        }

        private static List<Object> parseValues(String valStr) {
            List<Object> list = new ArrayList<>();
            StringBuilder cur = new StringBuilder();
            boolean inQuote = false;
            char quoteChar = ' ';
            for (int i = 0; i < valStr.length(); i++) {
                char c = valStr.charAt(i);
                if (inQuote) {
                    if (c == quoteChar) {
                        inQuote = false;
                    } else {
                        cur.append(c);
                    }
                } else {
                    if (c == '\'' || c == '"') {
                        inQuote = true;
                        quoteChar = c;
                    } else if (c == ',') {
                        list.add(parseScalar(cur.toString().trim()));
                        cur.setLength(0);
                    } else {
                        cur.append(c);
                    }
                }
            }
            if (cur.length() > 0 || inQuote) {
                list.add(parseScalar(cur.toString().trim()));
            }
            return list;
        }

        private static Object parseScalar(String s) {
            if (s.equalsIgnoreCase("NULL")) return null;
            if (s.equalsIgnoreCase("TRUE")) return true;
            if (s.equalsIgnoreCase("FALSE")) return false;
            try {
                if (s.contains(".")) return Double.parseDouble(s);
                return Long.parseLong(s);
            } catch (Exception e) {
                return s;
            }
        }

        private static int handleUpdate(String stmt, Map<String, Table> tables) {
            Pattern p = Pattern.compile("UPDATE\\s+([a-zA-Z0-9_$]+)\\s+SET\\s+(.*?)(?:\\s+WHERE\\s+(.*))?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(stmt);
            if (!m.find()) return 0;
            String tableName = m.group(1).toLowerCase();
            Table table = tables.get(tableName);
            if (table == null) return 0;

            String setPart = m.group(2);
            String wherePart = m.group(3);

            Map<String, Object> updates = new HashMap<>();
            for (String assign : setPart.split(",")) {
                String[] kv = assign.split("=");
                if (kv.length == 2) {
                    updates.put(kv[0].trim().toLowerCase(), parseScalar(kv[1].trim().replace("'", "").replace("\"", "")));
                }
            }

            int count = 0;
            for (Map<String, Object> row : table.rows) {
                if (wherePart == null || evalWhere(row, wherePart)) {
                    row.putAll(updates);
                    count++;
                }
            }
            return count;
        }

        private static int handleDelete(String stmt, Map<String, Table> tables) {
            Pattern p = Pattern.compile("DELETE\\s+FROM\\s+([a-zA-Z0-9_$]+)(?:\\s+WHERE\\s+(.*))?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(stmt);
            if (!m.find()) return 0;
            String tableName = m.group(1).toLowerCase();
            Table table = tables.get(tableName);
            if (table == null) return 0;

            String wherePart = m.group(2);
            int count = 0;
            Iterator<Map<String, Object>> it = table.rows.iterator();
            while (it.hasNext()) {
                Map<String, Object> row = it.next();
                if (wherePart == null || evalWhere(row, wherePart)) {
                    it.remove();
                    count++;
                }
            }
            return count;
        }

        private static void handleDrop(String stmt, Map<String, Table> tables) {
            Pattern p = Pattern.compile("DROP\\s+TABLE\\s+(?:IF\\s+EXISTS\\s+)?([a-zA-Z0-9_$]+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(stmt);
            if (m.find()) {
                tables.remove(m.group(1).toLowerCase());
            }
        }

        private static String handleSelect(String stmt, Map<String, Table> tables, String dialect) {
            Pattern p = Pattern.compile("SELECT\\s+(.*?)\\s+FROM\\s+([a-zA-Z0-9_$]+)(?:\\s+WHERE\\s+(.*?))?(?:\\s+GROUP\\s+BY\\s+(.*?))?(?:\\s+ORDER\\s+BY\\s+(.*?))?(?:\\s+LIMIT\\s+(\\d+))?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher m = p.matcher(stmt.trim());
            if (!m.find()) {
                throw new IllegalArgumentException("Unsupported or invalid SELECT statement");
            }

            String colsExpr = m.group(1).trim();
            String tableName = m.group(2).trim().toLowerCase();
            String whereClause = m.group(3);
            String groupByClause = m.group(4);
            String orderByClause = m.group(5);
            String limitClause = m.group(6);

            Table table = tables.get(tableName);
            if (table == null) throw new IllegalArgumentException("Table '" + tableName + "' doesn't exist");

            List<Map<String, Object>> filtered = new ArrayList<>();
            for (Map<String, Object> r : table.rows) {
                if (whereClause == null || evalWhere(r, whereClause.trim())) {
                    filtered.add(new LinkedHashMap<>(r));
                }
            }

            List<String> outputCols = new ArrayList<>();
            List<Map<String, Object>> resultRows = new ArrayList<>();

            if (groupByClause != null) {
                String groupCol = groupByClause.trim().toLowerCase();
                Map<Object, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
                for (Map<String, Object> r : filtered) {
                    Object k = r.get(groupCol);
                    grouped.computeIfAbsent(k, x -> new ArrayList<>()).add(r);
                }

                String[] rawCols = colsExpr.split(",");
                for (String rawCol : rawCols) {
                    String c = rawCol.trim();
                    String alias = c;
                    if (c.toUpperCase().contains(" AS ")) {
                        String[] parts = c.split("(?i)\\s+AS\\s+");
                        alias = parts[1].trim();
                    }
                    outputCols.add(alias);
                }

                for (Map.Entry<Object, List<Map<String, Object>>> entry : grouped.entrySet()) {
                    Map<String, Object> resRow = new LinkedHashMap<>();
                    List<Map<String, Object>> groupRows = entry.getValue();

                    for (String rawCol : rawCols) {
                        String c = rawCol.trim();
                        String alias = c;
                        String expr = c;
                        if (c.toUpperCase().contains(" AS ")) {
                            String[] parts = c.split("(?i)\\s+AS\\s+");
                            expr = parts[0].trim();
                            alias = parts[1].trim();
                        }

                        if (expr.equalsIgnoreCase(groupCol)) {
                            resRow.put(alias, entry.getKey());
                        } else if (expr.toUpperCase().startsWith("COUNT(")) {
                            resRow.put(alias, groupRows.size());
                        } else if (expr.toUpperCase().startsWith("SUM(")) {
                            String target = expr.substring(4, expr.length() - 1).trim().toLowerCase();
                            double sum = 0;
                            for (Map<String, Object> gr : groupRows) {
                                Object v = gr.get(target);
                                if (v instanceof Number n) sum += n.doubleValue();
                            }
                            resRow.put(alias, sum);
                        } else if (expr.toUpperCase().startsWith("AVG(")) {
                            String target = expr.substring(4, expr.length() - 1).trim().toLowerCase();
                            double sum = 0;
                            for (Map<String, Object> gr : groupRows) {
                                Object v = gr.get(target);
                                if (v instanceof Number n) sum += n.doubleValue();
                            }
                            resRow.put(alias, groupRows.isEmpty() ? 0 : Math.round((sum / groupRows.size()) * 100.0) / 100.0);
                        }
                    }
                    resultRows.add(resRow);
                }
            } else {
                boolean selectAll = colsExpr.equals("*");
                String[] selectCols = selectAll ? table.columns.toArray(new String[0]) : colsExpr.split(",");
                for (String sc : selectCols) {
                    String clean = sc.trim();
                    if (clean.toUpperCase().contains(" AS ")) {
                        clean = clean.split("(?i)\\s+AS\\s+")[1].trim();
                    }
                    outputCols.add(clean);
                }

                for (Map<String, Object> r : filtered) {
                    Map<String, Object> resRow = new LinkedHashMap<>();
                    for (int i = 0; i < selectCols.length; i++) {
                        String raw = selectCols[i].trim();
                        String alias = outputCols.get(i);
                        String colKey = raw.toLowerCase();
                        if (raw.toUpperCase().contains(" AS ")) {
                            colKey = raw.split("(?i)\\s+AS\\s+")[0].trim().toLowerCase();
                        }
                        resRow.put(alias, r.get(colKey));
                    }
                    resultRows.add(resRow);
                }
            }

            if (orderByClause != null) {
                String[] orderParts = orderByClause.trim().split("\\s+");
                String orderCol = orderParts[0].trim();
                boolean desc = orderParts.length > 1 && orderParts[1].equalsIgnoreCase("DESC");

                resultRows.sort((a, b) -> {
                    Object va = a.get(orderCol);
                    Object vb = b.get(orderCol);
                    if (va == null && vb == null) return 0;
                    if (va == null) return desc ? 1 : -1;
                    if (vb == null) return desc ? -1 : 1;
                    if (va instanceof Number na && vb instanceof Number nb) {
                        return desc ? Double.compare(nb.doubleValue(), na.doubleValue()) : Double.compare(na.doubleValue(), nb.doubleValue());
                    }
                    int cmp = va.toString().compareTo(vb.toString());
                    return desc ? -cmp : cmp;
                });
            }

            if (limitClause != null) {
                int lim = Integer.parseInt(limitClause.trim());
                if (resultRows.size() > lim) {
                    resultRows = resultRows.subList(0, lim);
                }
            }

            return formatTable(outputCols, resultRows, dialect);
        }

        private static boolean evalWhere(Map<String, Object> row, String whereClause) {
            if (whereClause.toUpperCase().contains(" AND ")) {
                for (String part : whereClause.split("(?i)\\s+AND\\s+")) {
                    if (!evalSingleCondition(row, part.trim())) return false;
                }
                return true;
            }
            if (whereClause.toUpperCase().contains(" OR ")) {
                for (String part : whereClause.split("(?i)\\s+OR\\s+")) {
                    if (evalSingleCondition(row, part.trim())) return true;
                }
                return false;
            }
            return evalSingleCondition(row, whereClause.trim());
        }

        private static boolean evalSingleCondition(Map<String, Object> row, String cond) {
            String[] ops = {">=", "<=", "!=", "=", ">", "<", " LIKE "};
            for (String op : ops) {
                int idx = cond.toUpperCase().indexOf(op);
                if (idx != -1) {
                    String left = cond.substring(0, idx).trim().toLowerCase();
                    String right = cond.substring(idx + op.length()).trim().replace("'", "").replace("\"", "");
                    Object val = row.get(left);
                    if (val == null) return false;

                    if (op.trim().equals("LIKE")) {
                        String regex = right.replace("%", ".*").replace("_", ".");
                        return val.toString().matches("(?i)" + regex);
                    }

                    if (val instanceof Number n) {
                        try {
                            double rNum = Double.parseDouble(right);
                            double lNum = n.doubleValue();
                            return switch (op) {
                                case ">=" -> lNum >= rNum;
                                case "<=" -> lNum <= rNum;
                                case "!=" -> lNum != rNum;
                                case "=" -> lNum == rNum;
                                case ">" -> lNum > rNum;
                                case "<" -> lNum < rNum;
                                default -> false;
                            };
                        } catch (Exception ignored) {}
                    }

                    int cmp = val.toString().compareTo(right);
                    return switch (op) {
                        case ">=" -> cmp >= 0;
                        case "<=" -> cmp <= 0;
                        case "!=" -> cmp != 0;
                        case "=" -> cmp == 0;
                        case ">" -> cmp > 0;
                        case "<" -> cmp < 0;
                        default -> false;
                    };
                }
            }
            return true;
        }

        private static String formatTable(List<String> cols, List<Map<String, Object>> rows, String dialect) {
            if (cols.isEmpty()) return "";

            int[] widths = new int[cols.size()];
            for (int i = 0; i < cols.size(); i++) {
                widths[i] = cols.get(i).length();
            }
            for (Map<String, Object> r : rows) {
                for (int i = 0; i < cols.size(); i++) {
                    Object v = r.get(cols.get(i));
                    String s = v == null ? "NULL" : v.toString();
                    widths[i] = Math.max(widths[i], s.length());
                }
            }

            StringBuilder sb = new StringBuilder();
            if ("oracle".equalsIgnoreCase(dialect)) {
                for (int i = 0; i < cols.size(); i++) {
                    sb.append(padRight(cols.get(i).toUpperCase(), widths[i])).append("  ");
                }
                sb.append("\n");
                for (int i = 0; i < cols.size(); i++) {
                    sb.append("-".repeat(widths[i])).append("  ");
                }
                sb.append("\n");
                for (Map<String, Object> r : rows) {
                    for (int i = 0; i < cols.size(); i++) {
                        Object v = r.get(cols.get(i));
                        sb.append(padRight(v == null ? "NULL" : v.toString(), widths[i])).append("  ");
                    }
                    sb.append("\n");
                }
                sb.append("\n").append(rows.size()).append(" rows selected.\n");
            } else {
                StringBuilder sep = new StringBuilder("+");
                for (int w : widths) {
                    sep.append("-".repeat(w + 2)).append("+");
                }
                sb.append(sep).append("\n|");
                for (int i = 0; i < cols.size(); i++) {
                    sb.append(" ").append(padRight(cols.get(i), widths[i])).append(" |");
                }
                sb.append("\n").append(sep).append("\n");
                for (Map<String, Object> r : rows) {
                    sb.append("|");
                    for (int i = 0; i < cols.size(); i++) {
                        Object v = r.get(cols.get(i));
                        sb.append(" ").append(padRight(v == null ? "NULL" : v.toString(), widths[i])).append(" |");
                    }
                    sb.append("\n");
                }
                sb.append(sep).append("\n");
                sb.append(rows.size()).append(" row(s) in set\n");
            }
            return sb.toString();
        }

        private static String padRight(String s, int n) {
            return String.format("%-" + n + "s", s);
        }
    }

    // =========================================================================
    // 100% PURE JAVA IN-MEMORY MONGODB SHELL ENGINE
    // =========================================================================
    public static class JavaMongoEngine {
        private static final Map<String, List<Map<String, Object>>> db = new LinkedHashMap<>();

        public static synchronized String execute(String script) {
            db.clear();
            StringBuilder out = new StringBuilder();

            String[] lines = script.split("\n");
            StringBuilder curStmt = new StringBuilder();

            for (String line : lines) {
                String tr = line.trim();
                if (tr.startsWith("//") || tr.isEmpty()) continue;
                curStmt.append(line).append("\n");

                if (tr.endsWith(");") || tr.endsWith(")")) {
                    String stmt = curStmt.toString().trim();
                    curStmt.setLength(0);

                    try {
                        String res = evalMongoStmt(stmt);
                        if (res != null && !res.isEmpty()) {
                            out.append(res).append("\n");
                        }
                    } catch (Exception e) {
                        out.append("MongoError: ").append(e.getMessage()).append("\n");
                    }
                }
            }
            return out.toString();
        }

        private static String evalMongoStmt(String stmt) {
            if (stmt.endsWith(";")) stmt = stmt.substring(0, stmt.length() - 1).trim();

            Pattern p = Pattern.compile("db\\.([a-zA-Z0-9_$]+)\\.([a-zA-Z0-9_$]+)\\s*\\((.*)\\)", Pattern.DOTALL);
            Matcher m = p.matcher(stmt);
            if (!m.find()) return null;

            String coll = m.group(1);
            String action = m.group(2);
            String args = m.group(3).trim();

            List<Map<String, Object>> collection = db.computeIfAbsent(coll, k -> new ArrayList<>());

            if (action.equalsIgnoreCase("insertMany")) {
                List<Map<String, Object>> docs = parseJsonList(args);
                for (Map<String, Object> d : docs) {
                    if (!d.containsKey("_id")) d.put("_id", "ObjectId(\"" + UUID.randomUUID().toString().replace("-", "").substring(0, 24) + "\")");
                    collection.add(d);
                }
                return "{\n  \"acknowledged\": true,\n  \"insertedCount\": " + docs.size() + "\n}";
            } else if (action.equalsIgnoreCase("insertOne") || action.equalsIgnoreCase("insert")) {
                Map<String, Object> doc = parseJsonMap(args);
                if (!doc.containsKey("_id")) doc.put("_id", "ObjectId(\"" + UUID.randomUUID().toString().replace("-", "").substring(0, 24) + "\")");
                collection.add(doc);
                return "{\n  \"acknowledged\": true,\n  \"insertedId\": \"" + doc.get("_id") + "\"\n}";
            } else if (action.equalsIgnoreCase("find")) {
                Map<String, Object> filter = args.isEmpty() ? new HashMap<>() : parseJsonMap(args);
                List<Map<String, Object>> matched = new ArrayList<>();
                for (Map<String, Object> d : collection) {
                    if (matchesFilter(d, filter)) {
                        matched.add(d);
                    }
                }
                return formatJsonList(matched);
            } else if (action.equalsIgnoreCase("findOne")) {
                Map<String, Object> filter = args.isEmpty() ? new HashMap<>() : parseJsonMap(args);
                for (Map<String, Object> d : collection) {
                    if (matchesFilter(d, filter)) {
                        return formatJsonMap(d, 0);
                    }
                }
                return "null";
            } else if (action.equalsIgnoreCase("aggregate")) {
                List<Map<String, Object>> pipeline = parseJsonList(args);
                List<Map<String, Object>> current = new ArrayList<>(collection);

                for (Map<String, Object> stage : pipeline) {
                    if (stage.containsKey("$match")) {
                        Map<String, Object> matchF = (Map<String, Object>) stage.get("$match");
                        current.removeIf(d -> !matchesFilter(d, matchF));
                    } else if (stage.containsKey("$group")) {
                        Map<String, Object> groupObj = (Map<String, Object>) stage.get("$group");
                        String groupField = groupObj.get("_id").toString();
                        Map<Object, Map<String, Object>> groups = new LinkedHashMap<>();

                        for (Map<String, Object> d : current) {
                            Object gKey = groupField.startsWith("$") ? d.get(groupField.substring(1)) : groupField;
                            Map<String, Object> gr = groups.computeIfAbsent(gKey, k -> {
                                Map<String, Object> n = new LinkedHashMap<>();
                                n.put("_id", k);
                                return n;
                            });

                            for (Map.Entry<String, Object> e : groupObj.entrySet()) {
                                if (e.getKey().equals("_id")) continue;
                                Map<String, Object> aggOp = (Map<String, Object>) e.getValue();
                                if (aggOp.containsKey("$sum")) {
                                    Object sumTarget = aggOp.get("$sum");
                                    double add = 1;
                                    if (sumTarget instanceof String s && s.startsWith("$")) {
                                        Object fv = d.get(s.substring(1));
                                        if (fv instanceof Number n) add = n.doubleValue();
                                    } else if (sumTarget instanceof Number n) {
                                        add = n.doubleValue();
                                    }
                                    double prev = gr.containsKey(e.getKey()) ? ((Number) gr.get(e.getKey())).doubleValue() : 0;
                                    gr.put(e.getKey(), prev + add);
                                }
                            }
                        }
                        current = new ArrayList<>(groups.values());
                    } else if (stage.containsKey("$sort")) {
                        Map<String, Object> sortObj = (Map<String, Object>) stage.get("$sort");
                        current.sort((a, b) -> {
                            for (Map.Entry<String, Object> e : sortObj.entrySet()) {
                                int dir = ((Number) e.getValue()).intValue();
                                Object va = a.get(e.getKey());
                                Object vb = b.get(e.getKey());
                                if (va instanceof Number na && vb instanceof Number nb) {
                                    int cmp = Double.compare(na.doubleValue(), nb.doubleValue());
                                    if (cmp != 0) return dir * cmp;
                                }
                            }
                            return 0;
                        });
                    }
                }
                return formatJsonList(current);
            }
            return "Command executed successfully.";
        }

        private static boolean matchesFilter(Map<String, Object> doc, Map<String, Object> filter) {
            for (Map.Entry<String, Object> e : filter.entrySet()) {
                String k = e.getKey();
                Object cond = e.getValue();
                if (cond instanceof Map map) {
                    Map<String, Object> m = (Map<String, Object>) map;
                    for (Map.Entry<String, Object> op : m.entrySet()) {
                        Object docV = doc.get(k);
                        Object targetV = op.getValue();
                        if (docV instanceof Number nd && targetV instanceof Number nt) {
                            double d = nd.doubleValue();
                            double t = nt.doubleValue();
                            switch (op.getKey()) {
                                case "$gt" -> { if (!(d > t)) return false; }
                                case "$gte" -> { if (!(d >= t)) return false; }
                                case "$lt" -> { if (!(d < t)) return false; }
                                case "$lte" -> { if (!(d <= t)) return false; }
                                case "$ne" -> { if (d == t) return false; }
                            }
                        }
                    }
                } else {
                    if (!Objects.equals(doc.get(k), cond)) return false;
                }
            }
            return true;
        }

        private static Map<String, Object> parseJsonMap(String json) {
            Map<String, Object> map = new LinkedHashMap<>();
            String clean = json.trim();
            if (clean.startsWith("{") && clean.endsWith("}")) {
                clean = clean.substring(1, clean.length() - 1).trim();
            }
            int brace = 0;
            StringBuilder cur = new StringBuilder();
            List<String> entries = new ArrayList<>();
            for (int i = 0; i < clean.length(); i++) {
                char c = clean.charAt(i);
                if (c == '{' || c == '[') brace++;
                else if (c == '}' || c == ']') brace--;
                else if (c == ',' && brace == 0) {
                    entries.add(cur.toString().trim());
                    cur.setLength(0);
                    continue;
                }
                cur.append(c);
            }
            if (cur.length() > 0) entries.add(cur.toString().trim());

            for (String entry : entries) {
                int colon = entry.indexOf(':');
                if (colon != -1) {
                    String k = entry.substring(0, colon).trim().replace("\"", "").replace("'", "");
                    String v = entry.substring(colon + 1).trim();
                    if (v.startsWith("{")) {
                        map.put(k, parseJsonMap(v));
                    } else if (v.startsWith("[")) {
                        map.put(k, parseJsonList(v));
                    } else {
                        map.put(k, parseScalar(v.replace("\"", "").replace("'", "")));
                    }
                }
            }
            return map;
        }

        private static List<Map<String, Object>> parseJsonList(String json) {
            List<Map<String, Object>> list = new ArrayList<>();
            String clean = json.trim();
            if (clean.startsWith("[") && clean.endsWith("]")) {
                clean = clean.substring(1, clean.length() - 1).trim();
            }
            int brace = 0;
            StringBuilder cur = new StringBuilder();
            for (int i = 0; i < clean.length(); i++) {
                char c = clean.charAt(i);
                if (c == '{') {
                    if (brace == 0) cur.setLength(0);
                    brace++;
                    cur.append(c);
                } else if (c == '}') {
                    brace--;
                    cur.append(c);
                    if (brace == 0) {
                        list.add(parseJsonMap(cur.toString().trim()));
                    }
                } else if (brace > 0) {
                    cur.append(c);
                }
            }
            return list;
        }

        private static Object parseScalar(String s) {
            if (s.equalsIgnoreCase("null")) return null;
            if (s.equalsIgnoreCase("true")) return true;
            if (s.equalsIgnoreCase("false")) return false;
            try {
                if (s.contains(".")) return Double.parseDouble(s);
                return Long.parseLong(s);
            } catch (Exception e) {
                return s;
            }
        }

        private static String formatJsonList(List<Map<String, Object>> list) {
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append(formatJsonMap(list.get(i), 2));
                if (i < list.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]");
            return sb.toString();
        }

        private static String formatJsonMap(Map<String, Object> map, int indent) {
            String sp = " ".repeat(indent);
            StringBuilder sb = new StringBuilder(sp).append("{\n");
            int idx = 0;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                sb.append(sp).append("  \"").append(e.getKey()).append("\": ");
                Object v = e.getValue();
                if (v instanceof String s) {
                    sb.append("\"").append(s).append("\"");
                } else if (v instanceof Map m) {
                    sb.append(formatJsonMap((Map<String, Object>) m, indent + 2));
                } else {
                    sb.append(v);
                }
                if (idx < map.size() - 1) sb.append(",");
                sb.append("\n");
                idx++;
            }
            sb.append(sp).append("}");
            return sb.toString();
        }
    }
}
