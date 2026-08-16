import React, { useEffect, useRef, useState } from 'react';
import { Globe, RefreshCw, Layers, ExternalLink } from 'lucide-react';

export default function WebPreview({ code, language, theme }) {
  const iframeRef = useRef(null);
  const [reloadKey, setReloadKey] = useState(0);

  const generateSrcDoc = () => {
    const isDark = theme === 'dark';

    if (language === 'react') {
      // Clean code: remove import and export statements so Babel standalone executes cleanly
      const sanitizedCode = code
        .replace(/import\s+.*?from\s+['"].*?['"];?/g, '')
        .replace(/export\s+default\s+/g, '')
        .replace(/export\s+/g, '');

      return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>React Live Preview</title>
  <!-- React 18 & Babel Standalone -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/react/18.2.0/umd/react.development.js" crossorigin="anonymous"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/react-dom/18.2.0/umd/react-dom.development.js" crossorigin="anonymous"></script>
  <script src="https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/7.23.5/babel.min.js" crossorigin="anonymous"></script>
  <style>
    * { box-sizing: border-box; }
    body {
      margin: 0;
      padding: 0;
      font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
      background-color: ${isDark ? '#0f172a' : '#f8fafc'};
      color: ${isDark ? '#f8fafc' : '#0f172a'};
      min-height: 100vh;
    }
    #error-display {
      padding: 1.5rem;
      background: #450a0a;
      color: #fecaca;
      border: 1px solid #dc2626;
      border-radius: 8px;
      margin: 1.5rem;
      font-family: monospace;
      font-size: 13px;
      line-height: 1.5;
    }
  </style>
</head>
<body>
  <div id="root"></div>

  <script type="text/babel">
    // Expose standard React hooks to scope
    const {
      useState,
      useEffect,
      useRef,
      useMemo,
      useCallback,
      useReducer,
      useContext,
      createContext
    } = React;

    try {
      ${sanitizedCode}

      // Render root component
      const targetComponent = typeof App !== 'undefined' ? <App /> : (typeof Solution !== 'undefined' ? <Solution /> : null);
      
      if (targetComponent) {
        const root = ReactDOM.createRoot(document.getElementById('root'));
        root.render(targetComponent);
      } else {
        document.getElementById('root').innerHTML = '<div id="error-display"><h3>Component Error</h3><p>Please make sure to define an <code>App()</code> or <code>Solution()</code> component function.</p></div>';
      }
    } catch (err) {
      document.getElementById('root').innerHTML = '<div id="error-display"><h3>Runtime Error</h3><pre>' + err.message + '</pre></div>';
    }
  </script>
</body>
</html>`;
    }

    // Standard HTML / CSS / JS Preview
    return code;
  };

  const handleRefresh = () => {
    setReloadKey(prev => prev + 1);
  };

  useEffect(() => {
    if (iframeRef.current) {
      iframeRef.current.srcdoc = generateSrcDoc();
    }
  }, [code, language, theme, reloadKey]);

  const handleOpenExternal = () => {
    const blob = new Blob([generateSrcDoc()], { type: 'text/html' });
    const url = URL.createObjectURL(blob);
    window.open(url, '_blank');
  };

  const isDark = theme === 'dark';

  return (
    <div className={`w-full h-full flex flex-col border-l ${isDark ? 'bg-[#0b0f19] border-[#1f2937]' : 'bg-[#f8fafc] border-[#e2e8f0]'}`}>
      {/* Top Header */}
      <div className={`h-9 px-4 border-b flex items-center justify-between select-none ${isDark ? 'bg-[#111827] border-[#1f2937]' : 'bg-[#ffffff] border-[#e2e8f0]'}`}>
        <div className="flex items-center space-x-2">
          {language === 'react' ? (
            <Layers className="w-3.5 h-3.5 text-cyan-400" />
          ) : (
            <Globe className="w-3.5 h-3.5 text-blue-500" />
          )}
          <span className={`text-xs font-semibold ${isDark ? 'text-gray-200' : 'text-gray-800'}`}>
            {language === 'react' ? 'React Live Preview' : 'Web Browser Preview'}
          </span>
          <span className="px-1.5 py-0.5 text-[10px] font-medium rounded bg-emerald-500/10 text-emerald-500 border border-emerald-500/20">
            Interactive
          </span>
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={handleOpenExternal}
            className={`p-1 rounded transition-all ${isDark ? 'text-gray-400 hover:text-white hover:bg-[#1f2937]' : 'text-gray-600 hover:text-black hover:bg-gray-100'}`}
            title="Open Preview in New Tab"
          >
            <ExternalLink className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={handleRefresh}
            className={`p-1 rounded transition-all ${isDark ? 'text-gray-400 hover:text-white hover:bg-[#1f2937]' : 'text-gray-600 hover:text-black hover:bg-gray-100'}`}
            title="Reload Preview"
          >
            <RefreshCw className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Sandboxed Interactive Frame */}
      <div className="flex-1 w-full h-full bg-white relative">
        <iframe
          key={reloadKey}
          ref={iframeRef}
          srcDoc={generateSrcDoc()}
          title="Sandbox Preview"
          sandbox="allow-scripts allow-modals allow-same-origin allow-forms"
          className="w-full h-full border-0"
        />
      </div>
    </div>
  );
}
