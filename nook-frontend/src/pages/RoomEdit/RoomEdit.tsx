import "./RoomEdit.css";
import MainLayout from "../../components/MainLayout/MainLayout";
import { useNavigation } from "../../services/NavigationContext";
import { useRoomEdit } from "../../hooks/useRoomEdit";
import EditRoomParticipants from "../../components/EditRoomParticipants/EditRoomParticipants";
import { useState } from "react";

function RoomEdit() {
  const { navigateTo } = useNavigation();
  const [isModalOpen, setModalOpen] = useState(false);
  const {
    roomToEdit,
    name,
    setName,
    description,
    setDescription,
    maxParticipants,
    increment,
    decrement,
    status,
    setStatus,
    save,
    errors,
  } = useRoomEdit();

  if (!roomToEdit) {
    return (
      <MainLayout>
        <div style={{ color: "white", textAlign: "center", marginTop: "50px" }}>
          Room Not Found
        </div>
      </MainLayout>
    );
  }

  const handleSave = () => {
    if (save()) navigateTo("rooms");
  };

  return (
    <MainLayout>
      <div className="edit-room-container">
        <div className="edit-card">
          <div className="edit-header">
            <button
              className="nav-btn cancel"
              onClick={() => navigateTo("rooms")}
            >
              Cancel
            </button>
            <h1 className="edit-title">EDIT ROOM</h1>
            <button className="nav-btn save-btn" onClick={handleSave}>
              Save Changes
            </button>
          </div>

          <div className="form-content">
            <div className="form-group">
              <label>Room Name:</label>
              <input
                type="text"
                className={`form-input ${errors.name ? "input-error" : ""}`}
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
              {errors.name && (
                <span className="error-message">{errors.name}</span>
              )}
            </div>

            <div className="form-group">
              <label>Data Management:</label>
              <button 
                type="button"
                className="manage-data-btn" 
                onClick={() => setModalOpen(true)}
              >
                Edit Participants & Sessions
              </button>
            </div>

            <div className="form-group">
              <label>Max Participants:</label>
              <div className="number-stepper">
                <button className="step-btn" onClick={decrement}>
                  -
                </button>
                <div className="step-display">{maxParticipants}</div>
                <button className="step-btn" onClick={increment}>
                  +
                </button>
              </div>
            </div>

            <div className="form-group top-align">
              <label>Description:</label>
              <textarea
                className={`form-textarea ${errors.desc ? "input-error" : ""}`}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              ></textarea>
              {errors.desc && (
                <span className="error-message">{errors.desc}</span>
              )}
            </div>

            <div className="form-group">
              <label>Status:</label>
              <select
                className="form-input"
                value={status}
                onChange={(e) =>
                  setStatus(e.target.value as "Public" | "Private")
                }
              >
                <option value="Public">Public</option>
                <option value="Private">Private</option>
              </select>
            </div>
          </div>
        </div>
      </div>
      <EditRoomParticipants 
        isOpen={isModalOpen} 
        onClose={() => setModalOpen(false)} 
      />
    </MainLayout>
  );
}

export default RoomEdit;
