import { useCallback, useEffect, useState } from 'react';
import type { AdminStats, User } from '@/types/api';
import {
  deleteUser,
  listUsers,
  setUserEnabled,
  setUserRoles,
  stats,
} from '@/api/admin';
import { formatDate } from '@/lib/format';
import { useToast } from '@/context/ToastContext';
import { Button } from '@/components/ui/Button';
import { ConfirmDialog } from '@/components/ui/ConfirmDialog';
import { Pagination } from '@/components/ui/Pagination';

export function AdminUsersPage() {
  const { push } = useToast();
  const [users, setUsers] = useState<User[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [q, setQ] = useState('');
  const [query, setQuery] = useState('');
  const [summary, setSummary] = useState<AdminStats | null>(null);
  const [pendingDelete, setPendingDelete] = useState<User | null>(null);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    listUsers({ page, size: 20, q: query || undefined })
      .then((response) => {
        setUsers(response.content);
        setTotalPages(response.totalPages);
      })
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudieron cargar los usuarios'));
  }, [page, query]);

  useEffect(load, [load]);
  useEffect(() => {
    stats().then(setSummary).catch(() => undefined);
  }, []);

  const toggle = async (user: User) => {
    try {
      await setUserEnabled(user.id, !user.enabled);
      push(user.enabled ? 'Usuario deshabilitado' : 'Usuario habilitado', 'success');
      load();
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo cambiar el estado', 'error');
    }
  };

  const toggleRole = async (user: User) => {
    const next = user.roles.includes('ADMIN') ? ['USER'] : ['USER', 'ADMIN'];
    try {
      await setUserRoles(user.id, next);
      push('Roles actualizados', 'success');
      load();
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudieron cambiar los roles', 'error');
    }
  };

  const remove = async () => {
    if (!pendingDelete) return;
    try {
      await deleteUser(pendingDelete.id);
      setPendingDelete(null);
      push('Usuario eliminado', 'success');
      load();
    } catch (caught) {
      push((caught as { message?: string }).message ?? 'No se pudo eliminar', 'error');
    }
  };

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Administración</p>
          <h1>Usuarios</h1>
        </div>
      </header>

      {summary ? (
        <div className="card-grid" style={{ marginBottom: 'var(--space-6)' }}>
          <div className="card"><strong>{summary.usuarios}</strong><p className="subtle">Usuarios</p></div>
          <div className="card"><strong>{summary.notas}</strong><p className="subtle">Notas</p></div>
          <div className="card"><strong>{summary.adjuntos}</strong><p className="subtle">Adjuntos</p></div>
          <div className="card"><strong>{summary.loginsFallidos}</strong><p className="subtle">Logins fallidos</p></div>
        </div>
      ) : null}

      <form className="toolbar" onSubmit={(event) => { event.preventDefault(); setPage(0); setQuery(q.trim()); }}>
        <input className="input grow" placeholder="Buscar usuario o email…" value={q} onChange={(e) => setQ(e.target.value)} />
        <Button type="submit">Buscar</Button>
      </form>

      {error ? <div className="error-banner" role="alert">{error}</div> : null}

      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr>
              <th>Usuario</th>
              <th>Email</th>
              <th>Roles</th>
              <th>Alta</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>{user.username}</td>
                <td>{user.email}</td>
                <td>{user.roles.join(', ')}</td>
                <td>{formatDate(user.createdAt)}</td>
                <td>
                  <div className="toolbar" style={{ marginBottom: 0 }}>
                    <Button small onClick={() => toggleRole(user)}>
                      {user.roles.includes('ADMIN') ? 'Quitar admin' : 'Hacer admin'}
                    </Button>
                    <Button small onClick={() => toggle(user)}>
                      {user.enabled ? 'Deshabilitar' : 'Habilitar'}
                    </Button>
                    <Button small variant="danger" onClick={() => setPendingDelete(user)}>Eliminar</Button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />

      <ConfirmDialog
        open={Boolean(pendingDelete)}
        title="Eliminar usuario"
        message={`¿Eliminar a ${pendingDelete?.username ?? ''}? Sus notas quedarán en la base.`}
        confirmLabel="Eliminar"
        onConfirm={remove}
        onCancel={() => setPendingDelete(null)}
      />
    </>
  );
}
