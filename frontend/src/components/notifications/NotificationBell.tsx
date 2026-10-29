import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { unreadCount } from '@/api/notifications';
import './NotificationBell.css';

export function NotificationBell() {
  const navigate = useNavigate();
  const [count, setCount] = useState(0);

  useEffect(() => {
    let active = true;
    const load = () => {
      unreadCount()
        .then((value) => {
          if (active) setCount(value);
        })
        .catch(() => undefined);
    };
    load();
    const interval = window.setInterval(load, 60_000);
    const onVisibility = () => {
      if (!document.hidden) load();
    };
    document.addEventListener('visibilitychange', onVisibility);
    return () => {
      active = false;
      window.clearInterval(interval);
      document.removeEventListener('visibilitychange', onVisibility);
    };
  }, []);

  return (
    <button
      type="button"
      className="notification-bell"
      onClick={() => navigate('/notifications')}
      aria-label={count > 0 ? `Notificaciones: ${count} sin leer` : 'Notificaciones'}
    >
      <svg aria-hidden="true" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.8">
        <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
        <path d="M10 21h4" />
      </svg>
      <span>Notificaciones</span>
      {count > 0 ? <span className="notification-bell__badge">{count > 99 ? '99+' : count}</span> : null}
    </button>
  );
}
