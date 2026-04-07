import { test, expect } from "@playwright/test";

test.describe("Study Rooms E2E", () => {

  test.beforeEach(async ({ page }) => {
    await page.goto("http://localhost:5173/");
  });

 test("Create Room - Updated for UI", async ({ page }) => {
 
  await page.getByPlaceholder('Enter room name...').fill('Study Group Alpha');
  const plusButton = page.locator('text=+');
  await plusButton.click(); 
  await expect(page.locator('text=6')).toBeVisible();

  await page.getByPlaceholder('What are we studying?').fill('Focusing on Playwright E2E tests.');

  await page.click('text=Public'); 
 
  await page.locator('button', { hasText: 'Create Room' }).click();

  await expect(page.locator('text=Study Group Alpha')).toBeVisible();
});

  test("Navigate between pages", async ({ page }) => {
  
    await page.click('text=Your Rooms'); 
    await expect(page.locator('h1')).toHaveText('Your Rooms');

    await page.click('text=+ Create Room');
    await expect(page.locator('h1')).toContainText('Create Room');

    await page.click('text=← Back');
    await expect(page.locator('h1')).toHaveText('Your Rooms');
  });

  test("Pagination works with mocked data", async ({ page }) => {
  // Intercept the API call that fetches rooms
  await page.route('**/api/rooms*', async (route) => {
    const mockRooms = Array.from({ length: 10 }, (_, i) => ({
      id: i,
      name: `Room ${i + 1}`,
      description: 'Mock Description',
      maxParticipants: 5
    }));
    
    await route.fulfill({
      contentType: 'application/json',
      body: JSON.stringify({
        rooms: mockRooms,
        totalCount: 10,
        currentPage: 1,
        totalPages: 2
      }),
    });
  });

  await page.goto("http://localhost:5173/");

  const nextButton = page.getByRole('button', { name: 'Next >' });
  await expect(nextButton).toBeEnabled();
  await nextButton.click();
  
  await expect(page.locator('.page-nums')).toHaveText(/Page 2 of 2/);
});
});