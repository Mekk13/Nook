import "./RoomMaster.css";

import { useNavigate } from "react-router-dom";
import { useRoomMaster } from "../../hooks/useRoomMaster";
import { useRoomStore } from "../../stores/roomStore";
import { useEffect, useState } from "react";

function RoomMaster() {
  const fetchRooms = useRoomStore((state) => state.fetchRooms);
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [joinCode, setJoinCode] = useState("");
  const joinByCode = useRoomStore((state) => state.joinByCode);
  const [joinError, setJoinError] = useState<string | null>(null);

  useEffect(() => {
    fetchRooms();
  }, [fetchRooms]);
  const navigate = useNavigate();

  // Connect to our new logic hook
  const {
    currentRooms,
    currentPage,
    totalPages,
    isPrevDisabled,
    isNextDisabled,
    goToNextPage,
    goToPrevPage,
    deleteRoom,
    setSelectedRoom,
  } = useRoomMaster(5);

  const handleAction = (
    roomId: string,
    target: "detail" | "edit" | "lobby",
  ) => {
    setSelectedRoom(roomId); // keep this for store compatibility
    if (target === "detail") navigate(`/rooms/${roomId}`);
    if (target === "edit") navigate(`/rooms/${roomId}/edit`);
    if (target === "lobby") navigate(`/rooms/${roomId}/lobby`);
  };

  const handleJoin = async () => {
    if (!joinCode.trim()) return;
    const error = await joinByCode(joinCode.trim().toUpperCase());
    if (error) {
      setJoinError(error);
      return;
    }
    setShowJoinModal(false);
    setJoinCode("");
    setJoinError(null);
  };

  return (
    <div className="room-master-container">
      <div className="main-card">
        <div className="card-header">
          <h1 className="main-title">Your Rooms</h1>
          <button
            className="create-room-btn"
            onClick={() => navigate("/rooms/create")}
          >
            + Create Room
          </button>
          <button
            className="join-room-btn"
            onClick={() => setShowJoinModal(true)}
          >
            + Join Room
          </button>
          <button className="back-btn" onClick={() => navigate("/")}>
            ← Back
          </button>
        </div>

        <div className="table-header">
          <span>Room Name</span>
          <span>Creator</span>
          <span>Participants</span>
          <span>Actions</span>
        </div>

        <div className="room-list">
          {currentRooms.map((room) => (
            <div key={room.id} className="room-row">
              <span className="room-name">{room.name}</span>
              <span className="room-creator">
                {room.creatorName ?? "Unknown"}
              </span>
              <span className="room-count">
                {room.memberCount ?? 0} / {room.maxParticipants}
              </span>
              <div className="actions">
                <button
                  className="link-btn join-link" // Added a specific class for styling
                  onClick={() => handleAction(room.id, "lobby")}
                >
                  Join
                </button>
                <button
                  className="link-btn"
                  onClick={() => handleAction(room.id, "detail")}
                >
                  View
                </button>
                <span className="divider">/</span>
                <button
                  className="link-btn"
                  onClick={() => handleAction(room.id, "edit")}
                >
                  Edit
                </button>
                <span className="divider">/</span>
                <button
                  className="link-btn"
                  onClick={() => deleteRoom(room.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>

        <div className="pagination">
          <span
            className={isPrevDisabled ? "disabled" : ""}
            onClick={goToPrevPage}
          >
            &lt; Prev
          </span>

          <span className="page-nums">
            Page {currentPage} of {totalPages || 1}
          </span>

          <span
            className={isNextDisabled ? "disabled" : ""}
            onClick={goToNextPage}
          >
            Next &gt;
          </span>
        </div>
      </div>
      {showJoinModal && (
        <div className="modal-overlay">
          <div className="modal-card">
            <button className="close-x" onClick={() => setShowJoinModal(false)}>
              &times;
            </button>
            <h2 className="modal-title">Join a Room</h2>
            <div className="join-form">
              <input
                className="join-code-input"
                placeholder="Enter room code..."
                value={joinCode}
                onChange={(e) => setJoinCode(e.target.value.toUpperCase())}
                maxLength={6}
              />
              {joinError && <p className="join-error">{joinError}</p>}
              <button className="done-btn" onClick={handleJoin}>
                Join
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default RoomMaster;
