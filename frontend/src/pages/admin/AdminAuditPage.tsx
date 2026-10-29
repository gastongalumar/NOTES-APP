import { useCallback, useEffect, useState } from 'react';
import type { AuditEntry } from '@/types/api';
import { listAudit } from '@/api/admin';
import { formatDateTime } from '@/lib/format';
import { Button } from '@/components/ui/Button';
import { Pagination } from '@/components/ui/Pagination';

export function AdminAuditPage() {
  const [entries, setEntries] = useState<AuditEntry[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [action, setAction] = useState('');
  const [query, setQuery] = useState('');
  const [error, setError] = useState('');

  const load = useCallback(() => {
    listAudit({ page, size: 25, accion: query || undefined })
      .then((response) => {
        setEntries(response.content);
        setTotalPages(response.totalPages);
      })
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudo cargar la auditoría'));
  }, [page, query]);

  useEffect(load, [load]);

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Trazabilidad</p>
          <h1>Auditoría</h1>
        </div>
      </header>

      <form className="toolbar" onSubmit={(event) => { event.preventDefault(); setPage(0); setQuery(action.trim()); }}>
        <input className="input grow" placeholder="Filtrar por acción (ej. LOGIN_SUCCESS)…" value={action} onChange={(e) => setAction(e.target.value)} />
        <Button type="submit">Filtrar</Button>
      </form>

      {error ? <div className="error-banner" role="alert">{error}</div> : null}

      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Usuario</th>
              <th>Acción</th>
              <th>Entidad</th>
              <th>IP</th>
            </tr>
          </thead>
          <tbody>
            {entries.map((entry) => (
              <tr key={entry.id}>
                <td>{formatDateTime(entry.createdAt)}</td>
                <td>{entry.username ?? '—'}</td>
                <td>{entry.accion}</td>
                <td>{entry.entidad ?? '—'} {entry.entidadId ?? ''}</td>
                <td>{entry.ip ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </>
  );
}
