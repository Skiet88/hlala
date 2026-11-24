import { useCallback, useEffect, useRef, useState } from 'react';

export interface Loaded<T> {
  data: T | null;
  error: string | null;
  loading: boolean;
  reload: () => void;
  /** Replace the data with what an action returned, without a reload. */
  set: (value: T) => void;
}

/** Loads data for a screen and keeps loading and error states honest. */
export function useLoad<T>(load: () => Promise<T>, deps: unknown[] = []): Loaded<T> {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [tick, setTick] = useState(0);
  const loadRef = useRef(load);
  loadRef.current = load;

  useEffect(() => {
    let live = true;
    setLoading(true);
    setError(null);
    loadRef
      .current()
      .then((value) => live && setData(value))
      .catch((e: unknown) => live && setError(e instanceof Error ? e.message : 'Something went wrong'))
      .finally(() => live && setLoading(false));
    return () => {
      live = false;
    };
  }, [tick, ...deps]);

  const reload = useCallback(() => setTick((t) => t + 1), []);
  return { data, error, loading, reload, set: setData };
}

/** Runs an action (pay, sign, accept) and exposes busy and error state for the button. */
export function useAction() {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const run = useCallback(async <R,>(action: () => Promise<R>): Promise<R | undefined> => {
    setBusy(true);
    setError(null);
    try {
      return await action();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong');
      return undefined;
    } finally {
      setBusy(false);
    }
  }, []);
  return { busy, error, run, clearError: () => setError(null) };
}
