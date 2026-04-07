import React, { useEffect, useState } from "react";
import "./EditRoomParticipants.css";
import { useRoomStore } from "../../stores/roomStore"; // Import your store
import type { StudySession }  from "../../types/room";

interface EditRoomParticipantsProps {
  isOpen: boolean;
  onClose: () => void;
}

const EditRoomParticipants: React.FC<EditRoomParticipantsProps> = ({ isOpen, onClose }) => {
  // 1. Get the current room data from the store
  const { rooms, selectedRoomId, addSession, removeSession } = useRoomStore();
  const currentRoom = rooms.find((r) => r.id === selectedRoomId);

  // 2. Local state to track WHICH participant we are currently looking at
  const [selectedParticipantId, setSelectedParticipantId] = useState<string>("");

  // Sync: Default to the first participant when the modal opens or room changes
  useEffect(() => {
    if (currentRoom && currentRoom.participants.length > 0 && !selectedParticipantId) {
      setSelectedParticipantId(currentRoom.participants[0].id);
    }
  }, [currentRoom, isOpen]);

  if (!isOpen || !currentRoom) return null;

  // Find the actual participant object based on the ID selected in the dropdown
  const activeParticipant = currentRoom.participants.find(p => p.id === selectedParticipantId);
  const sessions = activeParticipant?.sessions || [];

  const handleAddSession = () => {
    const hours = prompt("Enter study hours:");
    const subject = prompt("Enter subject:");
    if (hours && subject && selectedRoomId && selectedParticipantId) {
      const newSession: StudySession = {
        id: Date.now().toString(),
        subject,
        hours: parseFloat(hours),
        date: new Date().toISOString().split('T')[0]
      };
      addSession(selectedRoomId, selectedParticipantId, newSession);
    }
  };

  const handleDeleteSession = (sessionId: string) => {
    if (selectedRoomId && selectedParticipantId) {
      removeSession(selectedRoomId, selectedParticipantId, sessionId);
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-card">
        <button className="close-x" onClick={onClose}>&times;</button>
        
        <h2 className="modal-title">Room Data Manager</h2>
        
        <div className="name-edit-section">
          <label>Select Participant:</label>
          {/* DROPDOWN REPLACING INPUT */}
          <select 
            className="name-input" 
            value={selectedParticipantId}
            onChange={(e) => setSelectedParticipantId(e.target.value)}
          >
            {currentRoom.participants.map(p => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
            {currentRoom.participants.length === 0 && <option>No participants joined</option>}
          </select>
        </div>

        <div className="sessions-header">
          <h3>Study Sessions for {activeParticipant?.name || "..."}:</h3>
          <button className="add-session-btn" onClick={handleAddSession}>+ Add Session</button>
        </div>

        <div className="sessions-table">
          <div className="s-table-header">
            <span>Subject</span>
            <span>Hours</span>
            <span>Actions</span>
          </div>
          <div className="s-list">
            {sessions.map((session) => (
              <div key={session.id} className="s-row">
                <span>{session.subject}</span>
                <span>{session.hours}h</span>
                <div className="s-actions">
                  <button className="s-link-btn" onClick={() => handleDeleteSession(session.id)}>Delete</button>
                </div>
              </div>
            ))}
            {sessions.length === 0 && <div className="no-data">No sessions logged yet.</div>}
          </div>
        </div>

        <button className="done-btn" onClick={onClose}>Done</button>
      </div>
    </div>
  );
};

export default EditRoomParticipants;