import React, { useEffect, useState, useRef, useCallback } from "react";
import "./EditRoomParticipants.css";
import { useRoomStore } from "../../stores/roomStore";
import { useAuthStore } from "../../stores/useAuthStore";

interface SessionResponse {
  id: string;
  userId: string;
  name: string;
  subject: string;
  startedAt: string;
  endedAt: string | null;
  inProgress: boolean;
  durationHours: number;
}

interface EditRoomParticipantsProps {
  isOpen: boolean;
  onClose: () => void;
}

const PAGE_SIZE = 10;

const EditRoomParticipants: React.FC<EditRoomParticipantsProps> = ({ isOpen, onClose }) => {
  const selectedRoomId = useRoomStore((state) => state.selectedRoomId);
  const token = useAuthStore.getState().token;

  const [sessions, setSessions] = useState<SessionResponse[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);

  const pageRef = useRef(0);
  const hasMoreRef = useRef(true);
  const loadingRef = useRef(false);
  const seenIdsRef = useRef<Set<string>>(new Set());
  const sentinelRef = useRef<HTMLDivElement>(null);
  const listRef = useRef<HTMLDivElement>(null);
  const fetchPage = useCallback(async (pageNum: number) => {
  const res = await fetch(
    `http://localhost:8080/api/sessions/room/${selectedRoomId}?page=${pageNum}&size=${PAGE_SIZE}`,
    { headers: { Authorization: `Bearer ${token}` } }
  );
  const data = await res.json();
  console.log(`fetchPage(${pageNum}) →`, {
    totalElements: data.totalElements,
    totalPages: data.totalPages,
    contentLength: data.content?.length,
    hasMore: pageNum + 1 < (data.totalPages ?? 1)
  });
  const content: SessionResponse[] = data.content ?? [];
  const totalPages: number = data.totalPages ?? 1;
  return { sessions: content, hasMore: pageNum + 1 < totalPages };
}, [selectedRoomId, token]);

  const appendSessions = useCallback((incoming: SessionResponse[]) => {
    const novel = incoming.filter((s) => !seenIdsRef.current.has(s.id));
    if (novel.length === 0) return;
    novel.forEach((s) => seenIdsRef.current.add(s.id));
    setSessions((prev) => [...prev, ...novel]);
  }, []);

  const loadMore = useCallback(async () => {
    if (loadingRef.current || !hasMoreRef.current) return;
    loadingRef.current = true;
    setLoading(true);
    try {
      const result = await fetchPage(pageRef.current);
      appendSessions(result.sessions);
      hasMoreRef.current = result.hasMore;
      setHasMore(result.hasMore);
      pageRef.current += 1;
    } catch {
      console.error("Failed to load sessions");
    } finally {
      loadingRef.current = false;
      setLoading(false);
    }
  }, [fetchPage, appendSessions]);

  const handleSessionsUpdated = useCallback(async (e: Event) => {
  
  const { roomId } = (e as CustomEvent).detail;
  if (roomId !== selectedRoomId) return;

  try {
    const res = await fetch(
      `http://localhost:8080/api/sessions/room/${selectedRoomId}?page=${pageRef.current}&size=${PAGE_SIZE}`,
      { headers: { Authorization: `Bearer ${token}` } }
    );
    const data = await res.json();
    const incoming: SessionResponse[] = data.content ?? [];
    const totalPages: number = data.totalPages ?? 1;

    appendSessions(incoming);

    if (pageRef.current + 1 < totalPages) {
      hasMoreRef.current = true;
      setHasMore(true);
      pageRef.current += 1;
    }
  } catch {
    console.error("Failed to fetch new sessions from WS event");
  }
}, [selectedRoomId, token, appendSessions]);

  // Reset and load first page on open
  useEffect(() => {
    if (!isOpen || !selectedRoomId) return;

    setSessions([]);
    setHasMore(true);
    setLoading(false);
    pageRef.current = 0;
    hasMoreRef.current = true;
    loadingRef.current = false;
    seenIdsRef.current = new Set();

    const init = async () => {
      loadingRef.current = true;
      setLoading(true);
      try {
        const result = await fetchPage(0);
        result.sessions.forEach((s) => seenIdsRef.current.add(s.id));
        setSessions(result.sessions);
        hasMoreRef.current = result.hasMore;
        setHasMore(result.hasMore);
        pageRef.current = 1;
      } catch {
        console.error("Failed to load sessions");
      } finally {
        loadingRef.current = false;
        setLoading(false);
      }
    };

    init();
  }, [isOpen, selectedRoomId, fetchPage]);

  // Subscribe to WS-triggered updates while modal is open
  useEffect(() => {
    if (!isOpen) return;
    window.addEventListener("sessions-updated", handleSessionsUpdated);
    return () => window.removeEventListener("sessions-updated", handleSessionsUpdated);
  }, [isOpen, handleSessionsUpdated]);

  // IntersectionObserver for infinite scroll
  useEffect(() => {
  const sentinel = sentinelRef.current;
  const list = listRef.current;
  if (!sentinel || !list) return;

  const observer = new IntersectionObserver(
    (entries) => { 
      if (entries[0].isIntersecting) loadMore(); },
    { 
      root: list,      
      threshold: 0.1 
    }
  );
  observer.observe(sentinel);
  return () => observer.disconnect();
}, [loadMore, isOpen, sessions]);

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-card">
        <button className="close-x" onClick={onClose}>&times;</button>
        <h2 className="modal-title">Room Sessions</h2>

        {sessions.length === 0 && !loading ? (
          <p className="no-data">No sessions recorded in this room yet.</p>
        ) : (
          <div className="sessions-table">
            <div className="s-table-header">
              <span>User</span>
              <span>Session</span>
              <span>Subject</span>
              <span>Started</span>
              <span>Ended</span>
              <span>Duration</span>
            </div>
            <div className="s-list" ref={listRef}>
              {sessions.map((s) => (
                <div key={s.id} className="s-row">
                  <span className="s-cell user">{s.name}</span>
                  <span className="s-cell session">Studying {s.subject?.split(" ")[0]}</span>
                  <span className="s-cell subject">{s.subject ?? "—"}</span>
                  <span className="s-cell time">
                    {new Date(s.startedAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                  </span>
                  <span className="s-cell time">
                    {s.endedAt
                      ? new Date(s.endedAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
                      : "Live"}
                  </span>
                  <span className="s-cell total">
                    {s.durationHours > 0 ? `${s.durationHours.toFixed(1)}h` : "—"}
                  </span>
                </div>
              ))}
              <div ref={sentinelRef} style={{ height: 1 }} />
            </div>
          </div>
        )}

        {loading && <p className="loading-more">Loading...</p>}
        {!hasMore && sessions.length > 0 && <p className="no-data">All sessions loaded.</p>}

        <button className="done-btn" onClick={onClose}>Done</button>
      </div>
    </div>
  );
};

export default EditRoomParticipants;