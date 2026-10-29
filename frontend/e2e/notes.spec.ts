import { expect, test } from '@playwright/test';

test.describe('notes', () => {
  test('crear y abrir una nota', async ({ page }) => {
    await page.goto('/login');
    await page.getByLabel('Usuario').fill('admin');
    await page.getByLabel('Contraseña').fill('admin123');
    await page.getByRole('button', { name: 'Entrar' }).click();
    await page.getByRole('link', { name: 'Nueva nota' }).click();
    await page.getByLabel('Título').fill(`Nota E2E ${Date.now()}`);
    await page.getByLabel('Contenido').fill('Contenido de prueba E2E');
    await page.getByRole('button', { name: 'Guardar nota' }).click();
    await expect(page.getByRole('heading', { level: 1 })).toContainText('Nota E2E');
    await expect(page.getByText('Contenido de prueba E2E')).toBeVisible();
  });
});
