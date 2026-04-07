import "./RoomCreate.css";
import MainLayout from "../../components/MainLayout/MainLayout";
import { useNavigation } from "../../services/NavigationContext";
import { useRoomCreate } from "../../hooks/useRoomCreate";
import SuccessWindow from "../../components/SuccessWindow/SuccessWindow";
import { useState } from "react";

function RoomCreate() {
  const { navigateTo } = useNavigation();
  const { maxParticipants, increment, decrement, submit, errors } = useRoomCreate();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [status, setStatus] = useState("Public");
  const [showSuccess, setShowSuccess] = useState(false);

  const handleCreate = () => {
    // If submit works, show the popup
    if (submit(name, description, status as 'Public' | 'Private')) {
      setShowSuccess(true);
    }
  };

  return (
    <MainLayout>
      <div className="create-room-container">
        <div className="create-card">
          <div className="create-header">
            <button className="nav-btn cancel" onClick={() => navigateTo("rooms")}>
              Cancel
            </button>
            <h1 className="create-title">CREATE ROOM</h1>
            <button className="nav-btn submit" onClick={handleCreate}>
              Create Room
            </button>
          </div>

          <div className="form-content">
            {/* Room Name */}
            <div className="form-group">
              <label>Room Name:</label>
              <input
                type="text"
                className={`form-input ${errors.name ? "input-error" : ""}`}
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter room name..."
              />
              {errors.name && <span className="error-message">{errors.name}</span>}
            </div>

            {/* Max Participants */}
            <div className="form-group">
              <label>Max Participants:</label>
              <div className="number-stepper">
                <button className="step-btn" onClick={decrement}>-</button>
                <div className="step-display">{maxParticipants}</div>
                <button className="step-btn" onClick={increment}>+</button>
              </div>
            </div>

            {/* Description */}
            <div className="form-group top-align">
              <label>Description:</label>
              <textarea
                className={`form-textarea ${errors.desc ? "input-error" : ""}`}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="What are we studying?"
              ></textarea>
              {errors.desc && <span className="error-message">{errors.desc}</span>}
            </div>

            {/* Status */}
            <div className="form-group">
              <label>Status:</label>
              <div className="select-wrapper">
                <select
                  className="form-select"
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                >
                  <option value="Public">Public</option>
                  <option value="Private">Private</option>
                </select>
              </div>
            </div>
          </div>
        </div>
      </div>

      <SuccessWindow
        isOpen={showSuccess}
        onClose={() => navigateTo("rooms")} 
        message="Your cozy study lounge is ready!"
      />
    </MainLayout>
  );
}

export default RoomCreate;