import { expect, test } from '@playwright/test';

test.describe('pwa', () => {
  test('registra el service worker, publica el manifest y abre offline', async ({
    page,
    context,
  }) => {
    await page.goto('/login');

    const manifestResponse = await page.request.get('/manifest.webmanifest');
    expect(manifestResponse.ok()).toBe(true);
    const manifest = (await manifestResponse.json()) as {
      name: string;
      display: string;
      icons: Array<{ sizes: string }>;
    };
    expect(manifest.name).toContain('NotasApp');
    expect(manifest.display).toBe('standalone');
    expect(manifest.icons.some((icon) => icon.sizes === '512x512')).toBe(true);

    const registration = await page.evaluate(async () => {
      const ready = await navigator.serviceWorker.ready;
      return { scope: ready.scope, active: Boolean(ready.active) };
    });
    expect(registration.active).toBe(true);
    expect(registration.scope).toMatch(/\/$/);

    await page.reload();
    await page.waitForFunction(() => Boolean(navigator.serviceWorker.controller));

    await context.setOffline(true);
    try {
      await page.goto('/login');
      await expect(page.getByRole('heading', { name: 'Entrar' })).toBeVisible();
    } finally {
      await context.setOffline(false);
    }
  });
});
