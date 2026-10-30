export interface PwaState {
  needRefresh: boolean;
}

type Listener = (state: PwaState) => void;

let state: PwaState = { needRefresh: false };
let updateApplier: (() => void) | null = null;
const listeners = new Set<Listener>();

export function getPwaState(): PwaState {
  return state;
}

export function setPwaState(patch: Partial<PwaState>): void {
  state = { ...state, ...patch };
  listeners.forEach((listener) => listener(state));
}

export function subscribePwaState(listener: Listener): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

export function registerUpdateApplier(applier: () => void): void {
  updateApplier = applier;
}

export function applyPwaUpdate(): void {
  if (updateApplier) {
    updateApplier();
    return;
  }
  window.location.reload();
}
