import React from "react";
import { Play, RotateCcw, Copy, Download, Sun, Moon } from "lucide-react";
import { LANGUAGE_CONFIG } from "../utils/defaultTemplates";

export default function Navbar({
  selectedLanguage,
  onSelectLanguage,
  onRun,
  isRunning,
  onClearOutput,
  onCopy,
  copied,
  onDownload,
  theme,
  setTheme,
}) {
  const isDark = theme === "dark";

  return (
    <header
      className={`h-14 px-4 flex items-center justify-between border-b ${isDark
          ? "bg-gray-900 border-gray-700 text-white"
          : "bg-white border-gray-200 text-gray-900"
        }`}
    >
      {/* Logo */}
      <div className="flex items-center gap-2">
        <div className="w-8 h-8 rounded bg-blue-600 flex items-center justify-center text-white font-bold">
          {"</>"}
        </div>

        <div>
          <h1 className="font-bold text-sm">
            <span className="text-blue-500">Pro</span>Placement
          </h1>

          <p className="text-[10px] text-gray-500">
            Practice • Prepare • Get Placed
          </p>
        </div>

        {/* Language */}
        <select
          value={selectedLanguage}
          onChange={(e) => onSelectLanguage(e.target.value)}
          className={`ml-3 px-3 py-1.5 rounded border text-xs ${isDark
              ? "bg-gray-800 border-gray-600 text-white"
              : "bg-gray-100 border-gray-300 text-gray-900"
            }`}
        >
          {Object.values(LANGUAGE_CONFIG).map((lang) => (
            <option key={lang.id} value={lang.id}>
              {lang.name}
            </option>
          ))}
        </select>
      </div>

      {/* Run / Clear */}
      <div className="flex gap-2">
        <button
          onClick={onRun}
          disabled={isRunning}
          className="flex items-center gap-2 px-4 py-2 rounded bg-blue-600 text-white text-xs font-semibold hover:bg-blue-700"
        >
          <Play size={14} />

          {isRunning ? "Running..." : "Run"}
        </button>

        <button
          onClick={onClearOutput}
          className={`flex items-center gap-2 px-3 py-2 rounded border text-xs ${isDark
              ? "bg-gray-800 border-gray-600 text-gray-300"
              : "bg-gray-100 border-gray-300 text-gray-700"
            }`}
        >
          <RotateCcw size={14} />

          Clear
        </button>
      </div>

      {/* Right buttons */}
      <div className="flex items-center gap-2">
        <button
          onClick={onCopy}
          className={`px-3 py-2 rounded border text-xs ${isDark
              ? "bg-gray-800 border-gray-600"
              : "bg-gray-100 border-gray-300"
            }`}
        >
          <Copy size={14} className="inline mr-1" />

          {copied ? "Copied" : "Copy"}
        </button>

        <button
          onClick={onDownload}
          className={`px-3 py-2 rounded border text-xs ${isDark
              ? "bg-gray-800 border-gray-600"
              : "bg-gray-100 border-gray-300"
            }`}
        >
          <Download size={14} className="inline mr-1" />

          Download
        </button>

        <button
          onClick={() => setTheme(isDark ? "light" : "dark")}
          className={`p-2 rounded border ${isDark
              ? "bg-gray-800 border-gray-600"
              : "bg-gray-100 border-gray-300"
            }`}
        >
          {isDark ? <Sun size={16} /> : <Moon size={16} />}
        </button>
      </div>
    </header>
  );
}