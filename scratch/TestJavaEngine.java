import java.util.*;
import java.util.regex.*;

public class TestJavaEngine {

    public static void main(String[] args) {
        System.out.println("--- Test Java SQL Engine ---");
        String sql = """
            CREATE TABLE employees (
                id INT PRIMARY KEY,
                name VARCHAR(50),
                dept VARCHAR(50),
                salary INT
            );
            
            INSERT INTO employees VALUES (101, 'Alex Johnson', 'Engineering', 95000);
            INSERT INTO employees VALUES (102, 'Priya Sharma', 'Product', 105000);
            INSERT INTO employees VALUES (103, 'David Lee', 'Design', 88000);
            INSERT INTO employees VALUES (104, 'Sara Khan', 'Engineering', 92000);
            
            SELECT id, name, dept, salary FROM employees WHERE salary >= 90000 ORDER BY salary DESC;
            SELECT dept, COUNT(*) as count, AVG(salary) as avg_sal FROM employees GROUP BY dept;
        """;
        
        System.out.println(JavaSqlEngine.execute(sql, "mysql"));
        System.out.println(JavaSqlEngine.execute(sql, "oracle"));

        System.out.println("\n--- Test Java Mongo Engine ---");
        String mongo = """
            db.students.insertMany([
                { name: "Rahul Verma", score: 92, branch: "CSE" },
                { name: "Ananya Roy", score: 88, branch: "IT" },
                { name: "Karan Patel", score: 95, branch: "CSE" },
                { name: "Sneha Gupta", score: 79, branch: "ECE" }
            ]);
            
            db.students.find({ score: { $gte: 90 } });
            db.students.aggregate([
                { $match: { score: { $gte: 80 } } },
                { $group: { _id: "$branch", count: { $sum: 1 }, totalScore: { $sum: "$score" } } },
                { $sort: { totalScore: -1 } }
            ]);
        """;
        System.out.println(JavaMongoEngine.execute(mongo));
    }

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
                    } else if (upper.startsWith("DELETE FROM")) {
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
            // Match all (...)
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

            // Filter rows
            List<Map<String, Object>> filtered = new ArrayList<>();
            for (Map<String, Object> r : table.rows) {
                if (whereClause == null || evalWhere(r, whereClause.trim())) {
                    filtered.add(new LinkedHashMap<>(r));
                }
            }

            List<String> outputCols = new ArrayList<>();
            List<Map<String, Object>> resultRows = new ArrayList<>();

            // Group By / Aggregates
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
                // Regular Select
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

            // Order By
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

            // Limit
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
                // Oracle SQL*Plus format
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
                // MySQL format
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

    public static class JavaMongoEngine {
        private static final Map<String, List<Map<String, Object>>> db = new LinkedHashMap<>();

        public static String execute(String script) {
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
            // Parse top-level key-values
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
