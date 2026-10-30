import { useEffect, useState } from 'react';
import './Pwa.css';

export function OfflineBanner() {
  const [online, setOnline] = useState(() =>
    typeof navigator === 'undefined' ? true : navigator.onLine,
  );

  useEffect(() => {
    const handleOnline = () => setOnline(true);
    const handleOffline = () => setOnline(false);
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  if (online) return null;

  return (
    <p className="offline-banner" role="status" aria-live="polite">
      <span className="offline-banner__dot" aria-hidden="true" />
      Sin conexión. Los cambios no se guardan hasta reconectar.
    </p>
  );
}
