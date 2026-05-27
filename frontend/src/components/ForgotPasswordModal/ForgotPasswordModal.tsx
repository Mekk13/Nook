import React, { useState } from 'react';
import { useForgotPassword } from '../../hooks/useForgotPassword';
import './ForgotPasswordModal.css';

interface Props {
  isOpen: boolean;
  onClose: () => void;
}

const ForgotPasswordModal: React.FC<Props> = ({ isOpen, onClose }) => {
  const [email, setEmail] = useState("");
  const { sendResetEmail, isLoading, message } = useForgotPassword();

  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="reset-card">
        <button className="close-button" onClick={onClose}>×</button>
        <h2 className="reset-title">Reset Password</h2>
        
        {message ? (
          <div className={`message-${message.type}`}>
            <p>{message.text}</p>
            <button className="send-reset-button" onClick={onClose}>Back to Login</button>
          </div>
        ) : (
          <>
            <p className="reset-instruction">Enter your email for a recovery link.</p>
            <div className="reset-field-group">
  <label>Email</label>
  <input 
    type="email" 
    value={email} 
    onChange={(e) => setEmail(e.target.value)} 
    placeholder="your@email.com"
  />
</div>
            <button 
              className="send-reset-button" 
              onClick={() => sendResetEmail(email)}
              disabled={isLoading}
            >
              {isLoading ? "Sending..." : "Send Link"}
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default ForgotPasswordModal;