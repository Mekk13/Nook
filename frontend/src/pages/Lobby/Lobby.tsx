import "./Lobby.css";
import LobbyImg from "../../assets/Lobby.svg";
import Timer from "../../assets/Timer.svg";
import Book from "../../assets/Book.svg";
import Stats from "../../assets/Stats.svg";
import Exit from "../../assets/Exit.svg";
import { useEffect, useRef, useState } from "react";
import { useRoomStore } from "../../stores/roomStore";
import { useNavigation } from "../../services/NavigationContext";
import { useAuthStore } from "../../stores/useAuthStore";
import type { Member } from "../../types/room";
import EndSessionModal from "../../components/EndSession/EndSessionModal";
import RoomStatistics from "../../components/RoomStatistics/RoomStatistics";
import MySessionsDrawer from "../../components/MySessionsDrawer/MySessionsDrawer";

type SessionState = "idle" | "studying" | "paused";

function formatTime(seconds: number) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  if (h > 0) return `${h}h ${String(m).padStart(2, "0")}m ${String(s).padStart(2, "0")}s`;
  if (m > 0) return `${m}m ${String(s).padStart(2, "0")}s`;
  return `${s}s`;
}

function Lobby() {
  const { selectedRoomId } = useRoomStore();
  const { navigateTo } = useNavigation();
  const token = useAuthStore.getState().token;
  const currentUserId = useAuthStore.getState().user?.userId;

  const [_members, setMembers] = useState<Member[]>([]);
  const [sessionState, setSessionState] = useState<SessionState>("idle");
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [elapsed, setElapsed] = useState(0);
  const [showEndModal, setShowEndModal] = useState(false);
  const [showStats, setShowStats] = useState(false);
  const [showMySessions, setShowMySessions] = useState(false);

  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const startSession = useRoomStore((state) => state.startSession);
  const endSession = useRoomStore((state) => state.endSession);
  const startBreak = useRoomStore((state) => state.startBreak);
  const endBreak = useRoomStore((state) => state.endBreak);
  const activeSession = useRoomStore((state) => state.activeSession);
  const setActiveSession = useRoomStore((state) => state.setActiveSession);

  // Rehydrate session state from store on mount
  useEffect(() => {
    if (!activeSession) return;
    if (activeSession.roomId !== selectedRoomId) {
      setActiveSession(null);
      return;
    }
    setSessionId(activeSession.sessionId);
    setSessionState(activeSession.state);
    if (activeSession.state === "paused" && activeSession.pausedAt) {
      setElapsed(Math.floor((activeSession.pausedAt - activeSession.startedAt) / 1000));
    } else {
      setElapsed(Math.floor((Date.now() - activeSession.startedAt) / 1000));
    }
  }, []);

  // Fetch members
  useEffect(() => {
    if (!selectedRoomId) return;
    fetch(`${import.meta.env.VITE_API_URL}/api/rooms/${selectedRoomId}/members`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => setMembers(data.content ?? data))
      .catch(console.error);
  }, [selectedRoomId]);

  // Timer tick
  useEffect(() => {
    if (sessionState === "studying") {
      timerRef.current = setInterval(() => setElapsed((e) => e + 1), 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
    }
    return () => { if (timerRef.current) clearInterval(timerRef.current); };
  }, [sessionState]);

  const handleStart = async () => {
    if (!selectedRoomId) return;
    const created = await startSession(selectedRoomId);
    if (!created) return;
    setSessionId(created.id);
    setElapsed(0);
    setSessionState("studying");
    setActiveSession({ sessionId: created.id, startedAt: Date.now(), state: "studying", pausedAt: null, roomId: selectedRoomId });
  };

  const handlePause = async () => {
    if (!sessionId || !selectedRoomId || !currentUserId) return;
    await startBreak(sessionId, selectedRoomId, currentUserId);
    setSessionState("paused");
    setActiveSession({ sessionId, startedAt: Date.now() - elapsed * 1000, state: "paused", pausedAt: Date.now(), roomId: selectedRoomId });
  };

  const handleResume = async () => {
    if (!sessionId || !selectedRoomId || !currentUserId) return;
    await endBreak(sessionId, selectedRoomId, currentUserId);
    setSessionState("studying");
    setActiveSession({ sessionId, startedAt: Date.now() - elapsed * 1000, state: "studying", pausedAt: null, roomId: selectedRoomId });
  };

  const handleStopPress = () => {
    if (timerRef.current) clearInterval(timerRef.current);
    setShowEndModal(true);
  };

  const handleEndConfirm = async (name: string, subject: string) => {
    if (!sessionId || !selectedRoomId || !currentUserId) return;
    await endSession(sessionId, selectedRoomId, currentUserId, { name, subject });
    setActiveSession(null);
    setShowEndModal(false);
    setSessionId(null);
    setElapsed(0);
    setSessionState("idle");
  };

  const handleEndCancel = () => {
    if (sessionState === "studying") {
      timerRef.current = setInterval(() => setElapsed((e) => e + 1), 1000);
    }
    setShowEndModal(false);
  };

  return (
    <div className="lobby-container">
      <div className="room-wrapper">
        <img src={LobbyImg} alt="LobbyBG" className="room-bg" />
        <div className="avatar-layer">
          {/* TODO: render member avatars with positions and pfp circles */}
        </div>
      </div>

      {sessionState !== "idle" && (
        <div className="session-timer">
          <span className="timer-dot" data-paused={sessionState === "paused"} />
          <span className="timer-text">{formatTime(elapsed)}</span>
          {sessionState === "paused" && <span className="timer-paused-label">Paused</span>}
        </div>
      )}

      <div className="bottom-controls">
        {sessionState === "idle" && (
          <button className="lobby-control-item" onClick={handleStart}>
            <div className="icon-circle"><img src={Timer} alt="Timer" /></div>
            <span>Start</span>
          </button>
        )}

        {sessionState === "studying" && (
          <button className="lobby-control-item" onClick={handlePause}>
            <div className="icon-circle"><img src={Timer} alt="Timer" /></div>
            <span>Pause</span>
          </button>
        )}

        {sessionState === "paused" && (
          <>
            <button className="lobby-control-item" onClick={handleResume}>
              <div className="icon-circle"><img src={Timer} alt="Timer" /></div>
              <span>Resume</span>
            </button>
            <button className="lobby-control-item stop" onClick={handleStopPress}>
              <div className="icon-circle stop"><img src={Timer} alt="Stop" /></div>
              <span>Stop</span>
            </button>
          </>
        )}

        <button className="lobby-control-item">
          <div className="icon-circle"><img src={Book} alt="Cards" /></div>
          <span>Cards</span>
        </button>

        <button className="lobby-control-item" onClick={() => setShowMySessions(true)}>
          <div className="icon-circle"><img src={Book} alt="My Sessions" /></div>
          <span>Sessions</span>
        </button>

        <button className="lobby-control-item" onClick={() => setShowStats(true)}>
          <div className="icon-circle"><img src={Stats} alt="Stats" /></div>
          <span>Stats</span>
        </button>

        <button className="lobby-control-item" onClick={() => navigateTo("home")}>
          <div className="icon-circle"><img src={Exit} alt="Leave" /></div>
          <span>Leave</span>
        </button>
      </div>

      <EndSessionModal
        isOpen={showEndModal}
        elapsedSeconds={elapsed}
        onConfirm={handleEndConfirm}
        onCancel={handleEndCancel}
      />

      <RoomStatistics
        isOpen={showStats}
        onClose={() => setShowStats(false)}
      />

      <MySessionsDrawer
        isOpen={showMySessions}
        onClose={() => setShowMySessions(false)}
      />
    </div>
  );
}

export default Lobby;