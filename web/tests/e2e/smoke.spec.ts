import { test, expect } from "@playwright/test";

test("home renders", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByText("Identity Control Center")).toBeVisible();
});

test("login renders", async ({ page }) => {
  await page.goto("/login");
  await expect(page.getByText("OIDC Login")).toBeVisible();
});

test("demo routes gracefully redirect to login without backend", async ({ page }) => {
  await page.goto("/demo/profile");
  await expect(page).toHaveURL(/\/login/);
});
