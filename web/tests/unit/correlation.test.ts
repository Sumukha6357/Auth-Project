import { describe, expect, it } from "vitest";
import { createCorrelationId } from "@/lib/utils/correlation";

describe("correlation id generator", () => {
  it("generates uuid v4 string", () => {
    const id = createCorrelationId();
    expect(id).toMatch(/^[0-9a-fA-F-]{36}$/);
  });
});
