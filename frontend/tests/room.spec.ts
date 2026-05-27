import {test, expect} from '@playwright/test';

test.beforeEach(async ({ page }) => {
  await page.goto('/'); 
});

test('presentation has title', async ({page})=>
{
  await page.goto('/');
  await expect(page.getByRole('heading', {name: 'Nook'})).toBeVisible();
}
);

test('presentation has button', async({page})=>{
  await expect(page.getByRole('button', {name: /Start studying!/i })).toBeVisible();
}
);

test('presentation button works', async({page})=>{
  const startStudying = page.getByRole('button', {name: /Start studying!/i});

  await startStudying.click();
}
);