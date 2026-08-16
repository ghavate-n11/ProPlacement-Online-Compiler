import React from "react";
import Editor from "@monaco-editor/react";

export default function CodeEditor({
  code,
  onChange,
  onRun,
  editorRef,
  fileName = "Solution.java",
  monacoLang = "java",
  langVersion = "Java 21",
  theme = "dark",
}) {
  const handleEditorMount = (editor, monaco) => {
    if (editorRef) {
      editorRef.current = editor;
    }

    editor.addCommand(
      monaco.KeyMod.CtrlCmd | monaco.KeyCode.Enter,
      () => {
        onRun();
      }
    );
  };

  return (
    <div className="h-full flex flex-col">
      {/* Editor Header */}
      <div className="h-9 px-4 flex items-center justify-between border-b bg-gray-900 text-gray-300 text-xs">
        <span>{fileName}</span>

        <span>{langVersion}</span>
      </div>

      {/* Monaco */}
      <div className="flex-1">
        <Editor
          height="100%"
          language={monacoLang}
          theme={theme === "dark" ? "vs-dark" : "vs"}
          value={code}
          onChange={onChange}
          onMount={handleEditorMount}
          options={{
            fontSize: 14,
            minimap: {
              enabled: false,
            },
            automaticLayout: true,
            tabSize: 4,
            wordWrap: "on",
            scrollBeyondLastLine: false,
          }}
        />
      </div>
    </div>
  );
}