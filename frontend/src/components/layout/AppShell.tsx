import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { ThemeToggle } from '@/components/ThemeToggle';
import { NotificationBell } from '@/components/notifications/NotificationBell';
import { Button } from '@/components/ui/Button';

export function AppShell() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">
            N
          </span>
          <span>NotasApp</span>
        </div>
        <nav className="nav" aria-label="Navegación principal">
          <NavLink className="nav-link" to="/" end>
            Inicio
          </NavLink>
          <NavLink className="nav-link" to="/notes">
            Notas
          </NavLink>
          <NavLink className="nav-link" to="/calendar">
            Calendario
          </NavLink>
          <NavLink className="nav-link" to="/today">
            Hoy
          </NavLink>
          <NavLink className="nav-link" to="/upcoming">
            Próximos 7 días
          </NavLink>
          <NavLink className="nav-link" to="/archive">
            Archivo
          </NavLink>
          <NavLink className="nav-link" to="/categories">
            Categorías
          </NavLink>
          <NavLink className="nav-link" to="/tags">
            Tags
          </NavLink>
          <NavLink className="nav-link" to="/notifications">
            Notificaciones
          </NavLink>
          {isAdmin ? (
            <>
              <NavLink className="nav-link" to="/admin/users">
                Usuarios
              </NavLink>
              <NavLink className="nav-link" to="/admin/audit">
                Auditoría
              </NavLink>
            </>
          ) : null}
          <NavLink className="nav-link" to="/settings">
            Configuración
          </NavLink>
        </nav>
        <div className="sidebar-footer">
          <NotificationBell />
          <ThemeToggle />
          <div className="user-chip">
            <strong>{user?.nombre || user?.username}</strong>
            <span className="subtle">{user?.email}</span>
          </div>
          <Button small onClick={handleLogout}>
            Cerrar sesión
          </Button>
        </div>
      </aside>
      <main className="main" id="main">
        <div className="main-inner">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
