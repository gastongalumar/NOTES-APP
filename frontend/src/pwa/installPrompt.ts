import type { BeforeInstallPromptEvent } from '@/types/pwa';

type InstallListener = (event: BeforeInstallPromptEvent | null) => void;

let deferredPrompt: BeforeInstallPromptEvent | null = null;
const listeners = new Set<InstallListener>();
let initialized = false;

/**
 * Se llama una sola vez, antes de renderizar React. El evento
 * `beforeinstallprompt` puede dispararse antes de que monte el componente
 * que muestra el botón; si se escucha recién ahí, se pierde.
 */
export function initInstallPrompt(): void {
  if (initialized || typeof window === 'undefined') {
    return;
  }
  initialized = true;

  window.addEventListener('beforeinstallprompt', (event) => {
    event.preventDefault();
    deferredPrompt = event as BeforeInstallPromptEvent;
    notify(deferredPrompt);
  });

  window.addEventListener('appinstalled', () => {
    deferredPrompt = null;
    notify(null);
  });
}

export function getDeferredInstallPrompt(): BeforeInstallPromptEvent | null {
  return deferredPrompt;
}

export function clearDeferredInstallPrompt(): void {
  deferredPrompt = null;
  notify(null);
}

export function subscribeInstallPrompt(listener: InstallListener): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

function notify(event: BeforeInstallPromptEvent | null): void {
  listeners.forEach((listener) => listener(event));
}
