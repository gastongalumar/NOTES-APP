import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import type { User } from '@/types/api';
import * as authApi from '@/api/auth';
import { refreshSession } from '@/api/http';
import { broadcastAuth, subscribeAuth } from '@/api/authChannel';
import {
  clearAccessToken,
  setAccessToken,
} from '@/auth/tokenStore';

type AuthStatus = 'loading' | 'authenticated' | 'anonymous';

interface AuthContextValue {
  user: User | null;
  status: AuthStatus;
  isAdmin: boolean;
  login: (username: string, password: string, totp?: string) => Promise<void>;
  register: (input: authApi.RegisterInput) => Promise<User>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [status, setStatus] = useState<AuthStatus>('loading');

  const clearSession = useCallback(() => {
    clearAccessToken();
    setUser(null);
    setStatus('anonymous');
  }, []);

  useEffect(() => {
    let alive = true;
    refreshSession()
      .then((response) => {
        if (!alive) return;
        setUser(response.user);
        setStatus('authenticated');
      })
      .catch(() => {
        if (alive) setStatus('anonymous');
      });
    return () => {
      alive = false;
    };
  }, []);

  useEffect(() => {
    const onForcedLogout = () => clearSession();
    window.addEventListener('notasapp:logout', onForcedLogout);
    const unsubscribe = subscribeAuth((message) => {
      if (message.type === 'token') {
        setAccessToken(message.accessToken);
      } else {
        clearSession();
      }
    });
    return () => {
      window.removeEventListener('notasapp:logout', onForcedLogout);
      unsubscribe();
    };
  }, [clearSession]);

  const login = useCallback(async (username: string, password: string, totp?: string) => {
    const response = await authApi.login({ username, password, totp });
    setAccessToken(response.accessToken);
    broadcastAuth({ type: 'token', accessToken: response.accessToken });
    setUser(response.user);
    setStatus('authenticated');
  }, []);

  const register = useCallback(async (input: authApi.RegisterInput) => {
    return authApi.register(input);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } finally {
      clearAccessToken();
      broadcastAuth({ type: 'logout' });
      setUser(null);
      setStatus('anonymous');
    }
  }, []);

  const refreshUser = useCallback(async () => {
    const current = await authApi.me();
    setUser(current);
  }, []);

  const value = useMemo(
    () => ({
      user,
      status,
      isAdmin: Boolean(user?.roles.includes('ADMIN')),
      login,
      register,
      logout,
      refreshUser,
    }),
    [user, status, login, register, logout, refreshUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
}
