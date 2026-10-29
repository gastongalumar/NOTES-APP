import { Button } from './Button';

interface PaginationProps {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}

export function Pagination({ page, totalPages, onChange }: PaginationProps) {
  if (totalPages <= 1) return null;
  return (
    <nav className="pagination" aria-label="Paginación">
      <Button small onClick={() => onChange(page - 1)} disabled={page <= 0}>
        Anterior
      </Button>
      <span className="subtle">
        Página {page + 1} de {totalPages}
      </span>
      <Button small onClick={() => onChange(page + 1)} disabled={page >= totalPages - 1}>
        Siguiente
      </Button>
    </nav>
  );
}
