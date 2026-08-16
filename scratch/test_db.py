import sqlite3
import json
import sys

def execute_sql(sql_code, dialect="mysql"):
    conn = sqlite3.connect(":memory:")
    cursor = conn.cursor()
    
    statements = [s.strip() for s in sql_code.split(';') if s.strip()]
    output_lines = []
    
    for stmt in statements:
        lines = [l for l in stmt.split('\n') if not l.strip().startswith('--')]
        clean_stmt = '\n'.join(lines).strip()
        if not clean_stmt:
            continue
            
        try:
            cursor.execute(clean_stmt)
            if cursor.description:
                columns = [col[0] for col in cursor.description]
                rows = cursor.fetchall()
                
                col_widths = [len(c) for c in columns]
                for r in rows:
                    for i, val in enumerate(r):
                        col_widths[i] = max(col_widths[i], len(str(val)))
                
                if dialect == "mysql":
                    sep = "+" + "+".join(["-" * (w + 2) for w in col_widths]) + "+"
                    header = "| " + " | ".join([c.ljust(col_widths[i]) for i, c in enumerate(columns)]) + " |"
                    output_lines.append(sep)
                    output_lines.append(header)
                    output_lines.append(sep)
                    for r in rows:
                        row_str = "| " + " | ".join([str(val if val is not None else 'NULL').ljust(col_widths[i]) for i, val in enumerate(r)]) + " |"
                        output_lines.append(row_str)
                    output_lines.append(sep)
                    output_lines.append(f"{len(rows)} row(s) in set\n")
                elif dialect == "oracle":
                    header = "  ".join([c.upper().ljust(col_widths[i]) for i, c in enumerate(columns)])
                    sep = "  ".join(["-" * max(w, len(columns[i])) for i, w in enumerate(col_widths)])
                    output_lines.append(header)
                    output_lines.append(sep)
                    for r in rows:
                        row_str = "  ".join([str(val if val is not None else 'NULL').ljust(col_widths[i]) for i, val in enumerate(r)])
                        output_lines.append(row_str)
                    output_lines.append(f"\n{len(rows)} rows selected.\n")
            else:
                conn.commit()
                if dialect == "mysql":
                    output_lines.append(f"Query OK, {cursor.rowcount} row(s) affected")
                elif dialect == "oracle":
                    if clean_stmt.upper().startswith("CREATE"):
                        output_lines.append("Table created.")
                    elif clean_stmt.upper().startswith("INSERT"):
                        output_lines.append(f"{max(1, cursor.rowcount)} row(s) created.")
                    elif clean_stmt.upper().startswith("UPDATE"):
                        output_lines.append(f"{cursor.rowcount} row(s) updated.")
                    elif clean_stmt.upper().startswith("DELETE"):
                        output_lines.append(f"{cursor.rowcount} row(s) deleted.")
                    else:
                        output_lines.append("Statement processed.")
        except Exception as e:
            if dialect == "mysql":
                output_lines.append(f"ERROR 1064 (42000): {str(e)}")
            else:
                output_lines.append(f"ORA-00942: {str(e)}")
                
    conn.close()
    return "\n".join(output_lines)

test_mysql = """
CREATE TABLE employees (
    id INT PRIMARY KEY,
    name VARCHAR(50),
    department VARCHAR(50),
    salary DECIMAL(10, 2)
);

INSERT INTO employees VALUES (101, 'Alex Johnson', 'Engineering', 95000.00);
INSERT INTO employees VALUES (102, 'Priya Sharma', 'Product', 105000.00);
INSERT INTO employees VALUES (103, 'David Lee', 'Design', 88000.00);

SELECT * FROM employees WHERE salary >= 90000;
"""

print("=== MySQL Test ===")
print(execute_sql(test_mysql, "mysql"))
print("\n=== Oracle Test ===")
print(execute_sql(test_mysql, "oracle"))
