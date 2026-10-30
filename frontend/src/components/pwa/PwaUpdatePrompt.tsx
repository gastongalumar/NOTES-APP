import { useEffect, useState } from 'react';
import { applyPwaUpdate, getPwaState, setPwaState, subscribePwaState } from '@/pwa/updates';
import { Button } from '@/components/ui/Button';
import './Pwa.css';

export function PwaUpdatePrompt() {
  const [state, setState] = useState(getPwaState);

  useEffect(() => subscribePwaState(setState), []);

  if (!state.needRefresh) return null;

  return (
    <div className="pwa-update" role="status" aria-live="polite">
      <div className="pwa-update__body">
        <p className="pwa-update__title">Hay una versión nueva</p>
        <p className="pwa-update__text">
          Actualizá para ver los últimos cambios. Tus notas no se modifican.
        </p>
      </div>
      <div className="pwa-update__actions">
        <Button small variant="primary" onClick={applyPwaUpdate}>
          Actualizar
        </Button>
        <Button small variant="ghost" onClick={() => setPwaState({ needRefresh: false })}>
          Ahora no
        </Button>
      </div>
    </div>
  );
}
