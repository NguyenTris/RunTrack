import { useCallback, useEffect, useState } from "react";

export function useCrudState<T>(fetcher: (filter: string) => Promise<T[]>, initialFilter = "") {
  const [items, setItems] = useState<T[]>([]);
  const [filter, setFilter] = useState<string>(initialFilter);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<T | null>(null);
  const [deleting, setDeleting] = useState<T | null>(null);

  const load = useCallback(
    async (nextFilter: string) => {
      setLoading(true);
      setError("");
      try {
        setItems(await fetcher(nextFilter));
      } catch (err) {
        setError(err instanceof Error ? err.message : "Không thể tải dữ liệu");
      } finally {
        setLoading(false);
      }
    },
    [fetcher]
  );

  useEffect(() => {
    void load(initialFilter);
  }, [load, initialFilter]);

  const mutate = useCallback(
    async (action: () => Promise<unknown>, onDone: () => void, reloadFilter: string) => {
      setBusy(true);
      setError("");
      try {
        await action();
        onDone();
        await load(reloadFilter);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Thao tác thất bại");
      } finally {
        setBusy(false);
      }
    },
    [load]
  );

  return {
    items, filter, setFilter, loading, busy, error, setError,
    modalOpen, setModalOpen, editing, setEditing, deleting, setDeleting, load, mutate,
  };
}
