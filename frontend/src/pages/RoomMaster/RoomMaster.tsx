import "./RoomMaster.css";

import { useNavigation } from "../../services/NavigationContext";
import { useRoomMaster } from "../../hooks/useRoomMaster";
import { useRoomStore } from "../../stores/roomStore";
import { useEffect } from "react";

function RoomMaster() {
  const fetchRooms = useRoomStore((state) => state.fetchRooms);

  useEffect(() => {
    fetchRooms();
  }, [fetchRooms]);
  const { navigateTo } = useNavigation();

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
    setSelectedRoom(roomId);
    navigateTo(target);
  };

  return (
      <div className="room-master-container">
        <div className="main-card">
          <div className="card-header">
            <h1 className="main-title">Your Rooms</h1>
            <button
              className="create-room-btn"
              onClick={() => navigateTo("create")}
            >
              + Create Room
            </button>
            <button
              className="back-btn"
              onClick={() => navigateTo("presentation")}
            >
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
                <span className="room-creator">{room.creatorName ?? "Unknown"}</span>
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
      </div>
  );
}

export default RoomMaster;
