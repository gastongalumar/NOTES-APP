import { describe, expect, it } from 'vitest';
import { formatBytes, formatDate, toDateTimeLocal, fromDateTimeLocal } from './format';

describe('format helpers', () => {
  it('formats bytes', () => {
    expect(formatBytes(512)).toBe('512 B');
    expect(formatBytes(2048)).toBe('2.0 KB');
    expect(formatBytes(2 * 1024 * 1024)).toBe('2.0 MB');
  });

  it('formats dates and round-trips local datetime', () => {
    expect(formatDate('2026-10-29T12:00:00Z')).not.toBe('—');
    const local = toDateTimeLocal('2026-10-29T12:00:00Z');
    expect(local).toHaveLength(16);
    expect(fromDateTimeLocal(local)).toMatch(/Z$/);
  });
});
