import { expect, test } from '@playwright/test';

test.describe('auth', () => {
  test('login, reload con refresh y logout', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('Usuario').fill('admin');
    await page.getByLabel('Contraseña').fill('admin123');
    await page.getByRole('button', { name: 'Entrar' }).click();
    await expect(page.getByRole('heading', { level: 1 })).toContainText('Inicio');

    await page.reload();
    await expect(page.getByText('admin').first()).toBeVisible();

    const storage = await page.evaluate(() => ({
      local: Object.keys(localStorage),
      session: Object.keys(sessionStorage),
    }));
    expect(storage.local).not.toContain('token');
    expect(storage.session).not.toContain('token');

    await page.getByRole('button', { name: 'Cerrar sesión' }).click();
    await expect(page).toHaveURL(/\/login/);
  });
});
