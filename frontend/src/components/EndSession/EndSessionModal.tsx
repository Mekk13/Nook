import { useState } from "react";
import "./EndSessionModal.css";

interface EndSessionModalProps {
  isOpen: boolean;
  elapsedSeconds: number;
  onConfirm: (name: string, subject: string) => void;
  onCancel: () => void;
}

function formatTime(seconds: number) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  if (h > 0) return `${h}h ${m}m ${s}s`;
  if (m > 0) return `${m}m ${s}s`;
  return `${s}s`;
}

export default function EndSessionModal({
  isOpen,
  elapsedSeconds,
  onConfirm,
  onCancel,
}: EndSessionModalProps) {
  const [name, setName] = useState("");
  const [subject, setSubject] = useState("");

  if (!isOpen) return null;

  const handleConfirm = () => {
    if (!name.trim() || !subject.trim()) return;
    onConfirm(name.trim(), subject.trim());
    setName("");
    setSubject("");
  };

  return (
    <div className="esm-overlay">
      <div className="esm-card">
        <h2 className="esm-title">Session Complete! 🎉</h2>
        <p className="esm-duration">
          You studied for <strong>{formatTime(elapsedSeconds)}</strong>
        </p>

        <div className="esm-field">
          <label>Session Name</label>
          <input
            className="esm-input"
            type="text"
            placeholder="e.g. Morning study"
            value={name}
            onChange={(e) => setName(e.target.value)}
            minLength={2}
          />
          {name.trim().length < 2 && (
            <span className="esm-hint">At least 2 characters required</span>
          )}
        </div>

        <div className="esm-field">
          <label>Subject</label>
          <input
            className="esm-input"
            type="text"
            placeholder="e.g. Mathematics"
            value={subject}
            onChange={(e) => setSubject(e.target.value)}
            minLength={2}
          />
          {name.trim().length < 2 && (
            <span className="esm-hint">At least 2 characters required</span>
          )}
        </div>

        <div className="esm-actions">
          <button className="esm-btn cancel" onClick={onCancel}>
            Discard
          </button>
          <button
            className="esm-btn confirm"
            onClick={handleConfirm}
            disabled={name.trim().length < 2 || subject.trim().length < 2}
          >
            Save Session
          </button>
        </div>
      </div>
    </div>
  );
}
