import { describe, expect, it } from "vitest";
import { createPkcePair } from "@/lib/auth/pkce";

describe("pkce utils", () => {
  it("creates verifier and challenge", async () => {
    const result = await createPkcePair();
    expect(result.codeVerifier.length).toBeGreaterThanOrEqual(43);
    expect(result.codeChallenge.length).toBeGreaterThan(10);
  });
});
