import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { unreadCount } from '@/api/notifications';
import { Icon } from '@/components/icons/Icon';
import './NotificationBell.css';

interface NotificationBellProps {
  compact?: boolean;
}

export function NotificationBell({ compact = false }: NotificationBellProps) {
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
      className={`notification-bell${compact ? ' notification-bell--compact' : ''}`}
      onClick={() => navigate('/notifications')}
      aria-label={count > 0 ? `Notificaciones: ${count} sin leer` : 'Notificaciones'}
    >
      <Icon name="bell" size={compact ? 20 : 18} />
      {compact ? null : <span>Notificaciones</span>}
      {count > 0 ? (
        <span className="notification-bell__badge">{count > 99 ? '99+' : count}</span>
      ) : null}
    </button>
  );
}
