"use client";

export default function ConsoleError({ error, reset }: { error: Error; reset: () => void }) {
  return (
    <div className="rounded-xl border border-destructive/30 bg-destructive/5 p-6">
      <h2 className="text-base font-semibold text-destructive">Console page failed to render</h2>
      <p className="mt-2 text-sm text-destructive/90">{error.message || "Unexpected failure"}</p>
      <button
        type="button"
        className="mt-4 rounded-md bg-destructive px-3 py-2 text-sm font-medium text-destructive-foreground"
        onClick={reset}
      >
        Retry
      </button>
    </div>
  );
}
