import { useEffect, useState, useCallback, useRef } from "react";
import "./MySessionsDrawer.css";
import { useAuthStore } from "../../stores/useAuthStore";
import type { StudySession, BreakResponse } from "../../types/room";

interface MySessionsDrawerProps {
  isOpen: boolean;
  onClose: () => void;
}

interface EditForm {
  name: string;
  subject: string;
}

const PAGE_SIZE = 8;

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString([], {
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatDuration(hours: number) {
  if (hours <= 0) return "—";
  const h = Math.floor(hours);
  const m = Math.round((hours - h) * 60);
  if (h > 0 && m > 0) return `${h}h ${m}m`;
  if (h > 0) return `${h}h`;
  return `${m}m`;
}

function BreakList({ breaks }: { breaks: BreakResponse[] }) {
  if (!breaks || breaks.length === 0)
    return <p className="msd-no-breaks">No breaks taken.</p>;
  return (
    <div className="msd-breaks">
      {breaks.map((b, i) => (
        <div key={b.id} className="msd-break-row">
          <span className="msd-break-label">Break {i + 1}</span>
          <span>{formatDateTime(b.startedAt)}</span>
          <span>→</span>
          <span>{b.endedAt ? formatDateTime(b.endedAt) : "Ongoing"}</span>
        </div>
      ))}
    </div>
  );
}

export default function MySessionsDrawer({
  isOpen,
  onClose,
}: MySessionsDrawerProps) {
  const token = useAuthStore.getState().token;
  const [sessions, setSessions] = useState<StudySession[]>([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editForm, setEditForm] = useState<EditForm>({ name: "", subject: "" });
  const [saving, setSaving] = useState(false);

  const pageRef = useRef(0);
  const hasMoreRef = useRef(true);
  const loadingRef = useRef(false);
  const seenIdsRef = useRef<Set<string>>(new Set());
  const sentinelRef = useRef<HTMLDivElement>(null);
  const listRef = useRef<HTMLDivElement>(null);

  const [subjectFilter, setSubjectFilter] = useState("");
  const [appliedFilter, setAppliedFilter] = useState("");
  const [subjects, setSubjects] = useState<string[]>([]);

  const fetchPage = useCallback(
    async (pageNum: number) => {
      const url = appliedFilter
        ? `${import.meta.env.VITE_API_URL}/api/sessions/my/filter?subject=${encodeURIComponent(appliedFilter)}&page=${pageNum}&size=${PAGE_SIZE}`
        : `${import.meta.env.VITE_API_URL}/api/sessions/my?page=${pageNum}&size=${PAGE_SIZE}`;
      const res = await fetch(url, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const data = await res.json();
      const content: StudySession[] = data.content ?? [];
      const totalPages: number = data.totalPages ?? 1;
      return { sessions: content, hasMore: pageNum + 1 < totalPages };
    },
    [token, appliedFilter],
  );

  const resetAndReload = useCallback((filter: string) => {
    setSessions([]);
    setHasMore(true);
    setExpandedId(null);
    pageRef.current = 0;
    hasMoreRef.current = true;
    loadingRef.current = false;
    seenIdsRef.current = new Set();
    setAppliedFilter(filter);
  }, []);

  const loadMore = useCallback(async () => {
    if (loadingRef.current || !hasMoreRef.current) return;
    loadingRef.current = true;
    setLoading(true);
    try {
      const result = await fetchPage(pageRef.current);
      const novel = result.sessions.filter(
        (s) => !seenIdsRef.current.has(s.id),
      );
      novel.forEach((s) => seenIdsRef.current.add(s.id));
      setSessions((prev) => [...prev, ...novel]);
      hasMoreRef.current = result.hasMore;
      setHasMore(result.hasMore);
      pageRef.current += 1;
    } catch {
      console.error("Failed to load sessions");
    } finally {
      loadingRef.current = false;
      setLoading(false);
    }
  }, [fetchPage]);

  useEffect(() => {
    if (!isOpen) return;
    setSessions([]);
    setHasMore(true);
    setLoading(false);
    setExpandedId(null);
    setEditingId(null);
    pageRef.current = 0;
    hasMoreRef.current = true;
    loadingRef.current = false;
    seenIdsRef.current = new Set();

    const init = async () => {
      loadingRef.current = true;
      setLoading(true);
      try {
        const [result, subjectsRes] = await Promise.all([
          fetchPage(0),
          fetch(`${import.meta.env.VITE_API_URL}/api/sessions/my/subjects`, {
            headers: { Authorization: `Bearer ${token}` },
          }),
        ]);
        const subjectList: string[] = await subjectsRes.json();
        setSubjects(subjectList);
        result.sessions.forEach((s) => seenIdsRef.current.add(s.id));
        setSessions(result.sessions);
        hasMoreRef.current = result.hasMore;
        setHasMore(result.hasMore);
        pageRef.current = 1;
      } finally {
        loadingRef.current = false;
        setLoading(false);
      }
    };

    init();
  }, [isOpen, fetchPage]);

  useEffect(() => {
    const sentinel = sentinelRef.current;
    const list = listRef.current;
    if (!sentinel || !list) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) loadMore();
      },
      { root: list, threshold: 0.1 },
    );
    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [loadMore]);

  const handleDelete = async (sessionId: string) => {
    if (!confirm("Delete this session?")) return;
    try {
      await fetch(`${import.meta.env.VITE_API_URL}/api/sessions/${sessionId}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      setSessions((prev) => prev.filter((s) => s.id !== sessionId));
      seenIdsRef.current.delete(sessionId);
    } catch {
      console.error("Failed to delete session");
    }
  };

  const handleEditOpen = (s: StudySession) => {
    setEditingId(s.id);
    setEditForm({ name: s.name ?? "", subject: s.subject ?? "" });
  };

  const handleEditSave = async (sessionId: string) => {
    setSaving(true);
    try {
      const res = await fetch(
        `${import.meta.env.VITE_API_URL}/api/sessions/${sessionId}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify({
            name: editForm.name,
            subject: editForm.subject,
          }),
        },
      );
      if (!res.ok) return;
      const updated: StudySession = await res.json();
      setSessions((prev) =>
        prev.map((s) => (s.id === sessionId ? updated : s)),
      );
      setEditingId(null);
    } catch {
      console.error("Failed to save session");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className={`msd-backdrop ${isOpen ? "open" : ""}`} onClick={onClose}>
      <div className="msd-drawer" onClick={(e) => e.stopPropagation()}>
        <div className="msd-header">
          <h2 className="msd-title">My Sessions</h2>
          <button className="msd-close" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="msd-filter-bar">
          <select
            className="msd-filter-input"
            value={subjectFilter}
            onChange={(e) => {
              setSubjectFilter(e.target.value);
              resetAndReload(e.target.value);
            }}
          >
            <option value="">All subjects</option>
            {subjects.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>

        <div className="msd-list" ref={listRef}>
          {sessions.length === 0 && !loading && (
            <p className="msd-empty">No sessions yet.</p>
          )}

          {sessions.map((s) => (
            <div key={s.id} className="msd-session-card">
              {editingId === s.id ? (
                <div className="msd-edit-form">
                  <div className="msd-edit-field">
                    <label>Name</label>
                    <input
                      className="msd-edit-input"
                      value={editForm.name}
                      onChange={(e) =>
                        setEditForm((f) => ({ ...f, name: e.target.value }))
                      }
                      minLength={2}
                    />
                  </div>
                  <div className="msd-edit-field">
                    <label>Subject</label>
                    <input
                      className="msd-edit-input"
                      value={editForm.subject}
                      onChange={(e) =>
                        setEditForm((f) => ({ ...f, subject: e.target.value }))
                      }
                      minLength={2}
                    />
                  </div>
                  <div className="msd-edit-actions">
                    <button
                      className="msd-btn cancel"
                      onClick={() => setEditingId(null)}
                    >
                      Cancel
                    </button>
                    <button
                      className="msd-btn save"
                      onClick={() => handleEditSave(s.id)}
                      disabled={saving || editForm.name.trim().length < 2}
                    >
                      {saving ? "Saving..." : "Save"}
                    </button>
                  </div>
                </div>
              ) : (
                <>
                  <div
                    className="msd-session-header"
                    onClick={() =>
                      setExpandedId(expandedId === s.id ? null : s.id)
                    }
                  >
                    <div className="msd-session-info">
                      <span className="msd-session-name">
                        {s.name ?? "Unnamed"}
                      </span>
                      <span className="msd-session-subject">
                        {s.subject ?? "—"}
                      </span>
                    </div>
                    <div className="msd-session-meta">
                      <span className="msd-session-duration">
                        {formatDuration(s.durationHours)}
                      </span>
                      <span className="msd-session-expand">
                        {expandedId === s.id ? "▲" : "▼"}
                      </span>
                    </div>
                  </div>

                  {expandedId === s.id && (
                    <div className="msd-session-details">
                      <div className="msd-detail-row">
                        <span>Started</span>
                        <span>{formatDateTime(s.startedAt)}</span>
                      </div>
                      <div className="msd-detail-row">
                        <span>Ended</span>
                        <span>
                          {s.endedAt
                            ? formatDateTime(s.endedAt)
                            : "In progress"}
                        </span>
                      </div>
                      <BreakList breaks={s.breaks} />
                      <div className="msd-session-actions">
                        <button
                          className="msd-btn edit"
                          onClick={() => handleEditOpen(s)}
                        >
                          Edit
                        </button>
                        <button
                          className="msd-btn delete"
                          onClick={() => handleDelete(s.id)}
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          ))}

          {loading && <p className="msd-loading">Loading...</p>}
          {!hasMore && sessions.length > 0 && (
            <p className="msd-end">All sessions loaded.</p>
          )}
          <div ref={sentinelRef} style={{ height: 1 }} />
        </div>
      </div>
    </div>
  );
}
