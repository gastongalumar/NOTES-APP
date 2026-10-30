import { useEffect, useState } from 'react';
import type { BeforeInstallPromptEvent } from '@/types/pwa';
import {
  clearDeferredInstallPrompt,
  getDeferredInstallPrompt,
  subscribeInstallPrompt,
} from '@/pwa/installPrompt';
import './Pwa.css';

function isStandalone(): boolean {
  const iosStandalone = (navigator as Navigator & { standalone?: boolean }).standalone === true;
  const displayStandalone =
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(display-mode: standalone)').matches;
  return iosStandalone || displayStandalone;
}

export function InstallPrompt() {
  const [promptEvent, setPromptEvent] = useState<BeforeInstallPromptEvent | null>(
    () => getDeferredInstallPrompt(),
  );
  const [installed, setInstalled] = useState(isStandalone);

  useEffect(() => {
    const unsubscribe = subscribeInstallPrompt(setPromptEvent);
    const handleInstalled = () => {
      setInstalled(true);
      setPromptEvent(null);
    };

    window.addEventListener('appinstalled', handleInstalled);
    return () => {
      unsubscribe();
      window.removeEventListener('appinstalled', handleInstalled);
    };
  }, []);

  if (installed || !promptEvent) return null;

  const install = async () => {
    const event = promptEvent;
    clearDeferredInstallPrompt();
    try {
      await event.prompt();
      await event.userChoice;
    } catch (error) {
      console.warn('[pwa] el navegador rechazó el prompt de instalación', error);
    }
  };

  return (
    <button type="button" className="install-prompt" onClick={install}>
      <svg
        aria-hidden="true"
        viewBox="0 0 24 24"
        width="14"
        height="14"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.8"
        strokeLinecap="round"
        strokeLinejoin="round"
      >
        <path d="M12 4v10" />
        <path d="m8 10 4 4 4-4" />
        <path d="M5 19h14" />
      </svg>
      Instalar app
    </button>
  );
}
