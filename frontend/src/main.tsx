import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './styles/tokens.css';
import './styles/global.css';
import './styles/app.css';
import './components/layout/shell.css';
import { registerServiceWorker } from './pwa/register';
import { initInstallPrompt } from './pwa/installPrompt';

initInstallPrompt();
registerServiceWorker();

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
