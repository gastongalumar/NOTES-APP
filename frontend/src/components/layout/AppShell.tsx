import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { useMediaQuery } from '@/hooks/useMediaQuery';
import { displayName, initials } from '@/lib/user';
import { Icon, type IconName } from '@/components/icons/Icon';
import { NotificationBell } from '@/components/notifications/NotificationBell';
import { InstallPrompt } from '@/components/pwa/InstallPrompt';
import { ThemePicker } from '@/components/theme/ThemePicker';
import { Button } from '@/components/ui/Button';
import { MoreSheet } from './MoreSheet';

interface NavItem {
  to: string;
  label: string;
  icon: IconName;
  end?: boolean;
}

const PRIMARY_NAV: NavItem[] = [
  { to: '/', label: 'Inicio', icon: 'home', end: true },
  { to: '/notes', label: 'Notas', icon: 'note' },
  { to: '/today', label: 'Agenda', icon: 'agenda' },
  { to: '/calendar', label: 'Calendario', icon: 'calendar' },
];

const WORKSPACE_NAV: NavItem[] = [
  { to: '/archive', label: 'Archivo', icon: 'archive' },
  { to: '/categories', label: 'Categorías', icon: 'folder' },
  { to: '/tags', label: 'Etiquetas', icon: 'tag' },
  { to: '/notifications', label: 'Notificaciones', icon: 'bell' },
];

const ADMIN_NAV: NavItem[] = [
  { to: '/admin/users', label: 'Usuarios', icon: 'users' },
  { to: '/admin/audit', label: 'Auditoría', icon: 'shield' },
];

export function AppShell() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const isMobile = useMediaQuery('(max-width: 900px)');
  const [moreOpen, setMoreOpen] = useState(false);

  useEffect(() => {
    setMoreOpen(false);
  }, [location.pathname]);

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  if (!user) return null;

  const navLink = (item: NavItem) => (
    <NavLink key={item.to} className="nav-link" to={item.to} end={item.end}>
      <Icon name={item.icon} />
      <span>{item.label}</span>
    </NavLink>
  );

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <Link to="/" className="brand" aria-label="NotasApp, ir al inicio">
          <span className="brand-mark" aria-hidden="true">
            N
          </span>
          <span className="brand-text">
            <strong>NotasApp</strong>
            <small>Agenda y archivo</small>
          </span>
        </Link>

        <nav className="nav" aria-label="Navegación principal">
          <p className="nav-label">Espacio</p>
          {PRIMARY_NAV.map(navLink)}
          <p className="nav-label">Organizar</p>
          {WORKSPACE_NAV.map(navLink)}
          {isAdmin ? (
            <>
              <p className="nav-label">Administración</p>
              {ADMIN_NAV.map(navLink)}
            </>
          ) : null}
          <NavLink className="nav-link" to="/settings">
            <Icon name="settings" />
            <span>Configuración</span>
          </NavLink>
        </nav>

        <div className="sidebar-footer">
          <div className="user-chip">
            <span className="avatar" aria-hidden="true">
              {initials(user)}
            </span>
            <span className="user-chip-text">
              <strong>{displayName(user)}</strong>
              <small>{user.email}</small>
            </span>
          </div>
          <div className="sidebar-tools">
            <ThemePicker placement="up" />
            <InstallPrompt />
          </div>
          {!isMobile ? <NotificationBell /> : null}
          <Button variant="ghost" small onClick={handleLogout} className="sidebar-logout">
            <Icon name="logout" />
            Cerrar sesión
          </Button>
        </div>
      </aside>

      <div className="app-main">
        <header className="mobile-topbar">
          <Link to="/" className="brand brand-compact" aria-label="NotasApp, ir al inicio">
            <span className="brand-mark" aria-hidden="true">
              N
            </span>
            <span className="brand-text">
              <strong>NotasApp</strong>
            </span>
          </Link>
          <div className="mobile-topbar__actions">
            {isMobile ? <NotificationBell compact /> : null}
            <Link to="/settings" className="avatar avatar-link" aria-label="Configuración de la cuenta">
              {initials(user)}
            </Link>
          </div>
        </header>

        <main className="main" id="main">
          <div className="main-inner">
            <Outlet />
          </div>
        </main>

        <nav className="tabbar" aria-label="Navegación móvil">
          {PRIMARY_NAV.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.end} className="tabbar-link">
              <Icon name={item.icon} size={20} />
              <span>{item.label}</span>
            </NavLink>
          ))}
          <button
            type="button"
            className="tabbar-link"
            aria-expanded={moreOpen}
            aria-controls="more-sheet"
            onClick={() => setMoreOpen(true)}
          >
            <Icon name="more" size={20} />
            <span>Más</span>
          </button>
        </nav>
      </div>

      <MoreSheet open={moreOpen} onClose={() => setMoreOpen(false)} />
    </div>
  );
}
