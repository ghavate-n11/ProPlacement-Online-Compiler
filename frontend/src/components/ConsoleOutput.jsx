import React from "react";
import { Terminal, Copy, Trash2 } from "lucide-react";

export default function ConsoleOutput({
  outputResult,
  onClearOutput,
  isRunning,
  theme = "dark",
}) {
  const isDark = theme === "dark";

  const handleCopy = () => {
    const output =
      (outputResult?.output || "") +
      (outputResult?.error || "");

    if (output) {
      navigator.clipboard.writeText(output);
    }
  };

  return (
    <div
      className={`h-full flex flex-col border-l ${isDark
          ? "bg-gray-950 border-gray-700"
          : "bg-white border-gray-200"
        }`}
    >
      {/* Header */}
      <div
        className={`h-9 px-4 flex items-center justify-between border-b ${isDark
            ? "bg-gray-900 border-gray-700"
            : "bg-gray-100 border-gray-200"
          }`}
      >
        <div className="flex items-center gap-2">
          <Terminal size={14} />

          <span className="text-xs font-semibold">
            Output
          </span>

          {outputResult?.status === "SUCCESS" && (
            <span className="text-green-500 text-xs">
              ● Success
            </span>
          )}

          {outputResult?.status === "COMPILATION_ERROR" && (
            <span className="text-red-500 text-xs">
              ● Error
            </span>
          )}
        </div>

        <div className="flex gap-2">
          <button onClick={handleCopy}>
            <Copy size={14} />
          </button>

          <button onClick={onClearOutput}>
            <Trash2 size={14} />
          </button>
        </div>
      </div>

      {/* Output */}
      <div
        className={`flex-1 p-4 overflow-auto font-mono text-sm ${isDark
            ? "bg-black text-gray-100"
            : "bg-white text-gray-900"
          }`}
      >
        {isRunning ? (
          <p className="text-blue-500">
            Compiling and running...
          </p>
        ) : outputResult ? (
          <>
            {outputResult.output && (
              <pre className="whitespace-pre-wrap">
                {outputResult.output}
              </pre>
            )}

            {outputResult.error && (
              <pre className="text-red-500 whitespace-pre-wrap">
                {outputResult.error}
              </pre>
            )}
          </>
        ) : (
          <p className="text-gray-500">
            Click Run to see the output.
          </p>
        )}
      </div>
    </div>
  );
}