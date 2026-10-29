import { useEffect, useMemo, useState } from 'react';
import {
  addMonths,
  eachDayOfInterval,
  endOfMonth,
  endOfWeek,
  format,
  isSameMonth,
  isToday,
  startOfMonth,
  startOfWeek,
} from 'date-fns';
import { es } from 'date-fns/locale';
import { Link } from 'react-router-dom';
import type { NoteSummary } from '@/types/api';
import { agenda } from '@/api/notes';
import { Button } from '@/components/ui/Button';

export function CalendarPage() {
  const [month, setMonth] = useState(() => new Date());
  const [notes, setNotes] = useState<NoteSummary[]>([]);
  const [error, setError] = useState('');

  const days = useMemo(() => {
    const start = startOfWeek(startOfMonth(month), { weekStartsOn: 1 });
    const end = endOfWeek(endOfMonth(month), { weekStartsOn: 1 });
    return eachDayOfInterval({ start, end });
  }, [month]);

  useEffect(() => {
    const start = startOfWeek(startOfMonth(month), { weekStartsOn: 1 });
    const end = endOfWeek(endOfMonth(month), { weekStartsOn: 1 });
    agenda(start.toISOString(), end.toISOString())
      .then(setNotes)
      .catch((caught) => setError((caught as { message?: string }).message ?? 'No se pudo cargar la agenda'));
  }, [month]);

  const byDay = useMemo(() => {
    const map = new Map<string, NoteSummary[]>();
    for (const note of notes) {
      if (!note.fechaVencimiento) continue;
      const key = format(new Date(note.fechaVencimiento), 'yyyy-MM-dd');
      map.set(key, [...(map.get(key) ?? []), note]);
    }
    return map;
  }, [notes]);

  return (
    <>
      <header className="page-header">
        <div>
          <p className="eyebrow">Agenda</p>
          <h1>{format(month, 'MMMM yyyy', { locale: es })}</h1>
        </div>
        <div className="toolbar" style={{ marginBottom: 0 }}>
          <Button onClick={() => setMonth(addMonths(month, -1))}>Mes anterior</Button>
          <Button onClick={() => setMonth(new Date())}>Hoy</Button>
          <Button onClick={() => setMonth(addMonths(month, 1))}>Mes siguiente</Button>
        </div>
      </header>

      {error ? <div className="error-banner" role="alert">{error}</div> : null}

      <div className="calendar" role="grid" aria-label="Calendario de vencimientos">
        {['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'].map((day) => (
          <div className="calendar-head" role="columnheader" key={day}>{day}</div>
        ))}
        {days.map((day) => {
          const key = format(day, 'yyyy-MM-dd');
          const items = byDay.get(key) ?? [];
          return (
            <div
              className={`calendar-day ${isSameMonth(day, month) ? '' : 'is-outside'} ${isToday(day) ? 'is-today' : ''}`}
              role="gridcell"
              key={key}
            >
              <span className="calendar-number">{format(day, 'd')}</span>
              {items.slice(0, 3).map((note) => (
                <Link className="calendar-note" to={`/notes/${note.id}`} key={note.id}>
                  {note.titulo}
                </Link>
              ))}
              {items.length > 3 ? <span className="subtle">+{items.length - 3} más</span> : null}
            </div>
          );
        })}
      </div>
    </>
  );
}
