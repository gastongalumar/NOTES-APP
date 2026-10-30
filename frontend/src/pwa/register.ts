import { registerSW } from 'virtual:pwa-register';
import { registerUpdateApplier, setPwaState } from './updates';

const UPDATE_CHECK_INTERVAL = 60 * 60 * 1000;

export function registerServiceWorker(): void {
  if (import.meta.env.DEV || !('serviceWorker' in navigator)) return;

  const updateSW = registerSW({
    immediate: true,
    onNeedRefresh() {
      setPwaState({ needRefresh: true });
    },
    onRegisteredSW(_swUrl, registration) {
      if (!registration) return;
      window.setInterval(() => {
        void registration.update();
      }, UPDATE_CHECK_INTERVAL);
    },
    onRegisterError(error) {
      console.error('[pwa] no se pudo registrar el service worker', error);
    },
  });

  registerUpdateApplier(() => {
    void updateSW(true);
  });
}
