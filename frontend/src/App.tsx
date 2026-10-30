import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';
import { ThemeProvider } from '@/context/ThemeContext';
import { ToastProvider } from '@/context/ToastContext';
import { ErrorBoundary } from '@/components/ErrorBoundary';
import { AppShell } from '@/components/layout/AppShell';
import { OfflineBanner } from '@/components/pwa/OfflineBanner';
import { PwaUpdatePrompt } from '@/components/pwa/PwaUpdatePrompt';
import { AdminRoute, ProtectedRoute } from '@/components/routing/ProtectedRoute';
import { LoginPage } from '@/pages/LoginPage';
import { HomePage } from '@/pages/HomePage';
import { RegisterPage } from '@/pages/RegisterPage';
import { NotesPage } from '@/pages/NotesPage';
import { CalendarPage } from '@/pages/CalendarPage';
import { AgendaPage } from '@/pages/AgendaPage';
import { NoteEditorPage } from '@/pages/NoteEditorPage';
import { NoteViewPage } from '@/pages/NoteViewPage';
import { CategoriesPage } from '@/pages/CategoriesPage';
import { TagsPage } from '@/pages/TagsPage';
import { NotificationsPage } from '@/pages/NotificationsPage';
import { SettingsPage } from '@/pages/SettingsPage';
import { AdminUsersPage } from '@/pages/admin/AdminUsersPage';
import { AdminAuditPage } from '@/pages/admin/AdminAuditPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { PublicSharePage } from '@/pages/PublicSharePage';
import { ForgotPasswordPage } from '@/pages/ForgotPasswordPage';
import { ResetPasswordPage } from '@/pages/ResetPasswordPage';
import { VerifyEmailPage } from '@/pages/VerifyEmailPage';

export default function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <ToastProvider>
          <AuthProvider>
            <BrowserRouter>
              <OfflineBanner />
              <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/share/:token" element={<PublicSharePage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
                <Route path="/verify-email" element={<VerifyEmailPage />} />
                <Route element={<ProtectedRoute />}>
                  <Route element={<AppShell />}>
                    <Route index element={<HomePage />} />
                    <Route path="notes" element={<NotesPage title="Todas las notas" />} />
                    <Route path="notes/new" element={<NoteEditorPage />} />
                    <Route path="notes/:id" element={<NoteViewPage />} />
                    <Route path="notes/:id/edit" element={<NoteEditorPage />} />
                    <Route path="calendar" element={<CalendarPage />} />
                    <Route path="today" element={<AgendaPage kind="today" />} />
                    <Route path="upcoming" element={<AgendaPage kind="upcoming" />} />
                    <Route path="archive" element={<NotesPage archived title="Archivo" />} />
                    <Route path="search" element={<NotesPage title="Buscar" />} />
                    <Route path="categories" element={<CategoriesPage />} />
                    <Route path="tags" element={<TagsPage />} />
                    <Route path="notifications" element={<NotificationsPage />} />
                    <Route path="settings" element={<SettingsPage />} />
                    <Route element={<AdminRoute />}>
                      <Route path="admin/users" element={<AdminUsersPage />} />
                      <Route path="admin/audit" element={<AdminAuditPage />} />
                    </Route>
                  </Route>
                </Route>
                <Route path="*" element={<NotFoundPage />} />
              </Routes>
              <PwaUpdatePrompt />
            </BrowserRouter>
          </AuthProvider>
        </ToastProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}
