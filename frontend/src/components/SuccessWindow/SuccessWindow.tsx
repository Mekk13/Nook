import React from 'react';
import './SuccessWindow.css';
import successCat from "../../assets/party.png";

interface SuccessWindowProps {
  isOpen: boolean;     
  onClose: () => void;  
  message: string;      
}

const SuccessWindow: React.FC<SuccessWindowProps> = ({ isOpen, onClose, message }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        {/* Animated Stars */}
        <div className="star-container">
          <span className="star s1">⭐</span>
          <span className="star s2">✨</span>
          <span className="star s3">⭐</span>
          <span className="star s4">✨</span>
          <span className="star s5">⭐</span>
        </div>

        <div className="success-icon">
          <img src={successCat} className='party-cat' alt="yay" />
        </div>
        <h2 style={{ color: '#b2b2ff', marginBottom: '0.5rem' }}>Success!</h2>
        <p style={{ color: '#666', fontSize: '1.1rem' }}>{message}</p>
        
        <button className="confirm-btn" onClick={onClose}>
          Awesome!
        </button>
      </div>
    </div>
  );
};

export default SuccessWindow;