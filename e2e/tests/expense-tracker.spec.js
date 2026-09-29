import { test, expect } from '@playwright/test';

test.beforeEach(async ({ page, request }) => {
    const response = await request.get('/api/expenses');
    const expenses = await response.json();
    for (const expense of expenses) {
        await request.delete(`/api/expenses/${expense.id}`);
    }

    await page.goto('/');
});

test('lägger till en utgift och ser den i listan och summan', async ({ page }) => {
    await page.locator('#description').fill('Lunch');
    await page.locator('#amount').fill('95.50');
    await page.locator('#category').selectOption('FOOD');
    await page.locator('#date').fill('2026-09-24');

    await page.locator('#add-button').click();

    const item = page.locator('[data-testid="expense-item"]', { hasText: 'Lunch' });
    await expect(item).toBeVisible();
    await expect(item).toContainText('95.5');

    await expect(page.locator('#total-sum')).toContainText('95.5');
});

test('tar bort en utgift och listan uppdateras', async ({ page }) => {
    await page.locator('#description').fill('Bio');
    await page.locator('#amount').fill('120');
    await page.locator('#category').selectOption('ENTERTAINMENT');
    await page.locator('#date').fill('2026-09-24');
    await page.locator('#add-button').click();

    const item = page.locator('[data-testid="expense-item"]', { hasText: 'Bio' });
    await expect(item).toBeVisible();

    await item.locator('[data-testid="delete-button"]').click();

    await expect(item).not.toBeVisible();
});