import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { initials, displayName } from '@/lib/user';
import { Dialog } from '@/components/ui/Dialog';
import { Button } from '@/components/ui/Button';
import { Icon, type IconName } from '@/components/icons/Icon';
import { ThemeOptions } from '@/components/theme/ThemeOptions';
import { InstallPrompt } from '@/components/pwa/InstallPrompt';

interface MoreSheetProps {
  open: boolean;
  onClose: () => void;
}

interface SheetLink {
  to: string;
  label: string;
  icon: IconName;
}

export function MoreSheet({ open, onClose }: MoreSheetProps) {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  const links: SheetLink[] = [
    { to: '/archive', label: 'Archivo', icon: 'archive' },
    { to: '/categories', label: 'Categorías', icon: 'folder' },
    { to: '/tags', label: 'Etiquetas', icon: 'tag' },
    { to: '/notifications', label: 'Notificaciones', icon: 'bell' },
    { to: '/settings', label: 'Configuración', icon: 'settings' },
    ...(isAdmin
      ? ([
          { to: '/admin/users', label: 'Usuarios', icon: 'users' },
          { to: '/admin/audit', label: 'Auditoría', icon: 'shield' },
        ] as SheetLink[])
      : []),
  ];

  const handleLogout = async () => {
    onClose();
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <Dialog
      open={open}
      title="Más"
      variant="sheet"
      id="more-sheet"
      onClose={onClose}
      footer={
        <Button variant="ghost" onClick={handleLogout}>
          <Icon name="logout" />
          Cerrar sesión
        </Button>
      }
    >
      <div className="more-sheet">
        <div className="more-sheet__user">
          <span className="avatar" aria-hidden="true">
            {initials(user)}
          </span>
          <span className="more-sheet__identity">
            <strong>{displayName(user)}</strong>
            <small>{user.email}</small>
          </span>
        </div>

        <nav className="more-sheet__links" aria-label="Más secciones">
          {links.map((link) => (
            <NavLink key={link.to} to={link.to} className="more-sheet__link" onClick={onClose}>
              <Icon name={link.icon} />
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="more-sheet__section">
          <p className="nav-label">Tema</p>
          <ThemeOptions name="theme-more-sheet" onSelect={onClose} />
        </div>

        <div className="more-sheet__section">
          <InstallPrompt />
        </div>
      </div>
    </Dialog>
  );
}
