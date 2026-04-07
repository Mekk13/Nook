import "./RoomDetail.css";
import MainLayout from "../../components/MainLayout/MainLayout";
import { useNavigation } from "../../services/NavigationContext";
import { useRoomDetail } from "../../hooks/useRoomDetail";
import RoomStatistics from "../../components/RoomStatistics/RoomStatistics";
import { useState } from "react";

function RoomDetail() {
  const { navigateTo } = useNavigation();
  const { room, displayDate } = useRoomDetail();
  const [isStatsOpen, setStatsOpen] = useState(false);

  if (!room) {
    return (
      <MainLayout>
        <div className="view-room-container">
          <div className="view-card" style={{ textAlign: "center" }}>
            <h2 className="view-title">Room not found!</h2>
            <button
              className="nav-btn cancel"
              onClick={() => navigateTo("rooms")}
            >
              Go Back
            </button>
          </div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="view-room-container">
        <div className="view-card">
          <div className="view-header">
            <button
              className="nav-btn cancel"
              onClick={() => navigateTo("rooms")}
            >
              Back
            </button>
            <h1 className="view-title underline">{room.name}</h1>
            <button className="nav-btn submit join">Join Room</button>
          </div>

          <div className="view-content">
            <div className="view-row">
              <div className="detail-item">
                <label>Creator:</label>
                <div className="detail-box">{room.creator}</div>
              </div>
              <div className="detail-item">
                <label>Created:</label>
                <div className="detail-box">{displayDate}</div>
              </div>
            </div>

            <div className="view-row">
              <div className="detail-item">
                <label>Participants:</label>
                {/* Change this line: */}
                <div className="detail-box">
                  {room.participants.length} / {room.maxParticipants}
                </div>
              </div>
              <div className="detail-item">
                <label>Permissions:</label>
                <div className="detail-box">{room.status}</div>
              </div>
            </div>

            <div className="view-row full-width">
              <div className="detail-item">
                <label>Description:</label>
                <div className="detail-box description">
                  {room.description || "No description provided."}
                </div>
              </div>
            </div>
          </div>

          <button className="stats-btn" onClick={() => setStatsOpen(true)}>
            Statistics
          </button>

          <RoomStatistics
            isOpen={isStatsOpen}
            onClose={() => setStatsOpen(false)}
          />
        </div>
      </div>
    </MainLayout>
  );
}

export default RoomDetail;
