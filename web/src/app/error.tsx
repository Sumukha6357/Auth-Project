"use client";

export default function GlobalError({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <html>
      <body className="min-h-screen bg-slate-50 text-slate-900">
        <main className="mx-auto flex min-h-screen max-w-xl items-center justify-center px-6">
          <div className="w-full rounded-xl border bg-white p-6 shadow-sm">
            <h1 className="text-lg font-semibold">Application error</h1>
            <p className="mt-2 text-sm text-muted-foreground">{error.message || "An unexpected error occurred."}</p>
            <button
              type="button"
              className="mt-4 rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white"
              onClick={reset}
            >
              Try again
            </button>
          </div>
        </main>
      </body>
    </html>
  );
}
