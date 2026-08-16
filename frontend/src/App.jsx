import React, { useState, useEffect, useRef } from 'react';
import Navbar from './components/Navbar';
import CodeEditor from './components/CodeEditor';
import ConsoleOutput from './components/ConsoleOutput';
import WebPreview from './components/WebPreview';
import { LANGUAGE_CONFIG } from './utils/defaultTemplates';

const API_BASE_URL = 'http://localhost:8080/api';

export default function App() {
  const [selectedLanguage, setSelectedLanguage] = useState('java');
  const [codes, setCodes] = useState(() => {
    const initialCodes = {};
    Object.keys(LANGUAGE_CONFIG).forEach(key => {
      initialCodes[key] = LANGUAGE_CONFIG[key].defaultCode;
    });
    return initialCodes;
  });

  const [outputResult, setOutputResult] = useState(null);
  const [isRunning, setIsRunning] = useState(false);
  const [copied, setCopied] = useState(false);
  const [theme, setTheme] = useState('dark');
  const [serverStatus, setServerStatus] = useState('connecting');

  const editorRef = useRef(null);
  const currentLangConfig = LANGUAGE_CONFIG[selectedLanguage] || LANGUAGE_CONFIG.java;
  const currentCode = codes[selectedLanguage] || '';

  // Update theme on root DOM element
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  // Check Backend Health on Mount
  useEffect(() => {
    const checkHealth = async () => {
      try {
        const res = await fetch(`${API_BASE_URL}/health`);
        if (res.ok) {
          const data = await res.json();
          if (data.status === 'OK') {
            setServerStatus('connected');
          }
        } else {
          setServerStatus('error');
        }
      } catch (err) {
        setServerStatus('error');
      }
    };
    checkHealth();
    const interval = setInterval(checkHealth, 10000);
    return () => clearInterval(interval);
  }, []);

  // Language Change Handler
  const handleSelectLanguage = (langId) => {
    setSelectedLanguage(langId);
    setOutputResult(null);
  };

  // Code Update Handler
  const handleCodeChange = (newCode) => {
    setCodes(prev => ({
      ...prev,
      [selectedLanguage]: newCode
    }));
  };

  // Run Code Action
  const handleRunCode = async () => {
    // If it's a frontend web/react sandbox, preview updates instantly
    if (currentLangConfig.type.startsWith('frontend')) {
      setOutputResult({
        status: 'SUCCESS',
        output: `Live preview updated for ${currentLangConfig.name}.`,
        error: '',
        compileTimeMs: 0,
        executionTimeMs: 10,
        exitCode: 0
      });
      return;
    }

    if (isRunning) return;
    setIsRunning(true);

    try {
      const response = await fetch(`${API_BASE_URL}/compile`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          code: currentCode,
          input: '',
          language: selectedLanguage
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP status ${response.status}`);
      }

      const data = await response.json();
      setOutputResult(data);
    } catch (err) {
      setOutputResult({
        status: 'COMPILATION_ERROR',
        output: '',
        error: `Failed to connect to backend compiler server: ${err.message}\nMake sure JavaCompilerServer is running on port 8080.`,
        compileTimeMs: 0,
        executionTimeMs: 0,
        exitCode: -1
      });
    } finally {
      setIsRunning(false);
    }
  };

  // Clear Output Action
  const handleClearOutput = () => {
    setOutputResult(null);
  };

  // Copy Code Action
  const handleCopyCode = () => {
    navigator.clipboard.writeText(currentCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  // Download Code Action
  const handleDownloadCode = () => {
    const element = document.createElement('a');
    const file = new Blob([currentCode], { type: 'text/plain;charset=utf-8' });
    element.href = URL.createObjectURL(file);
    element.download = currentLangConfig.fileName;
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  };

  return (
    <div className={`h-screen w-screen flex flex-col overflow-hidden select-none transition-colors ${theme === 'dark' ? 'bg-[#0b0f19] text-gray-100' : 'bg-[#f8fafc] text-gray-900'}`}>
      {/* Navbar */}
      <Navbar
        selectedLanguage={selectedLanguage}
        onSelectLanguage={handleSelectLanguage}
        onRun={handleRunCode}
        isRunning={isRunning}
        onClearOutput={handleClearOutput}
        onCopy={handleCopyCode}
        copied={copied}
        onDownload={handleDownloadCode}
        theme={theme}
        setTheme={setTheme}
        serverStatus={serverStatus}
      />

      {/* Main IDE Workspace Split (Editor | Console or Web Preview) */}
      <div className="flex-1 flex flex-col md:flex-row overflow-hidden relative">
        {/* Code Editor Pane (Left) */}
        <div className="w-full md:w-1/2 lg:w-3/5 h-1/2 md:h-full flex flex-col">
          <CodeEditor
            code={currentCode}
            onChange={(val) => handleCodeChange(val || '')}
            onRun={handleRunCode}
            editorRef={editorRef}
            fileName={currentLangConfig.fileName}
            monacoLang={currentLangConfig.monacoLang}
            langVersion={currentLangConfig.version}
            theme={theme}
          />
        </div>

        {/* Right Output Pane: Console Terminal or Live Web Preview */}
        <div className="w-full md:w-1/2 lg:w-2/5 h-1/2 md:h-full flex flex-col">
          {currentLangConfig.type.startsWith('frontend') ? (
            <WebPreview
              code={currentCode}
              language={selectedLanguage}
              theme={theme}
              onReload={handleRunCode}
            />
          ) : (
            <ConsoleOutput
              outputResult={outputResult}
              onClearOutput={handleClearOutput}
              isRunning={isRunning}
              theme={theme}
            />
          )}
        </div>
      </div>
    </div>
  );
}
