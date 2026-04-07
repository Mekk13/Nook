import "./Lobby.css"; // <--- MAKE SURE THIS LINE EXISTS
import LobbyImg from "../../assets/Lobby.svg";
import Timer from "../../assets/Timer.svg";
import Book from "../../assets/Book.svg";
import Stats from "../../assets/Stats.svg";
import Exit from "../../assets/Exit.svg";
import { useRoomStore } from "../../stores/roomStore";
import { useNavigation } from "../../services/NavigationContext";

function Lobby() {
  const { selectedRoomId, getById, updateParticipantStatus } = useRoomStore();
  const { navigateTo } = useNavigation();
  const room = selectedRoomId ? getById(selectedRoomId) : null;
  const me = room?.participants.find((p) => p.name === "Max");

  const handleStart = () => {
    if (!room || !me) return;
    const nextStatus = me.studyStatus === "Idle" ? "Studying" : "Idle";
    updateParticipantStatus(room.id, me.id, nextStatus);
  };

  return (
    <div className="lobby-container">
      <div className="room-wrapper">
        <img src={LobbyImg} alt="LobbyBG" className="room-bg" />

        <div className="avatar-layer">{/* Avatars go here */}</div>
      </div>

      <div className="bottom-controls">
        <button className="lobby-control-item" onClick={handleStart}>
          <div className="icon-circle">
            <img src={Timer} alt="Timer" />
          </div>
          <span>{me?.studyStatus === "Studying" ? "Stop" : "Start"}</span>
        </button>

        <button className="lobby-control-item">
          <div className="icon-circle">
            <img src={Book} alt="Cards" />
          </div>
          <span>Cards</span>
        </button>

        <button className="lobby-control-item">
          <div className="icon-circle">
            <img src={Stats} alt="Stats" />
          </div>
          <span>Stats</span>
        </button>

        <button
          className="lobby-control-item"
          onClick={() => navigateTo("rooms")}
        >
          <div className="icon-circle">
            <img src={Exit} alt="Leave" />
          </div>
          <span>Leave</span>
        </button>
      </div>
    </div>
  );
}

export default Lobby;
