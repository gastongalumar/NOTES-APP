export type AuthMessage =
  | { type: 'token'; accessToken: string }
  | { type: 'logout' };

const channel =
  typeof BroadcastChannel !== 'undefined'
    ? new BroadcastChannel('notasapp-auth')
    : null;

export function broadcastAuth(message: AuthMessage): void {
  channel?.postMessage(message);
}

export function subscribeAuth(handler: (message: AuthMessage) => void): () => void {
  if (!channel) {
    return () => undefined;
  }
  const listener = (event: MessageEvent<AuthMessage>) => handler(event.data);
  channel.addEventListener('message', listener);
  return () => channel.removeEventListener('message', listener);
}
