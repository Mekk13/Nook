import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import './ResetPasswordPage.css';

const ResetPasswordPage = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const navigate = useNavigate();

    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [status, setStatus] = useState({ type: '', msg: '' });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (password !== confirmPassword) {
            setStatus({ type: 'error', msg: 'Passwords do not match!' });
            return;
        }

        const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{10,}$/;
        if (!passwordRegex.test(password)) {
            setStatus({ 
                type: 'error', 
                msg: 'Password must be 10+ characters with Upper, Lower, Number, and Special char.' 
            });
            return;
        }

        try {
            const response = await fetch(`${import.meta.env.VITE_API_URL}/api/auth/reset-password`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ token, newPassword: password })
            });

            if (response.ok) {
                setStatus({ type: 'success', msg: 'Success! Redirecting to login...' });
                setTimeout(() => navigate('/login'), 3000);
            } else {
                setStatus({ type: 'error', msg: 'This link is expired or invalid.' });
            }
        } catch (err) {
            setStatus({ type: 'error', msg: 'Connection failed. Is the server running?' });
        }
    };

    return (
        <div className="rp-page-wrapper">
            <div className="rp-modal-card">
                <h2 className="rp-modal-title">New Password</h2>
                
                <p className="rp-modal-instruction">
                    Enter your new secret code below!
                </p>

                {status.msg && (
                    <div className={`rp-status-msg rp-status-${status.type}`}>
                        {status.msg}
                    </div>
                )}

                <form className="rp-form-container" onSubmit={handleSubmit}>
                    <div className="rp-input-section">
                        <label className="rp-input-label">New Password</label>
                        <input 
                            className="rp-input-field"
                            type="password" 
                            placeholder="••••••••" 
                            value={password}
                            onChange={(e) => setPassword(e.target.value)} 
                            required 
                        />
                    </div>

                    <div className="rp-input-section">
                        <label className="rp-input-label">Confirm Password</label>
                        <input 
                            className="rp-input-field"
                            type="password" 
                            placeholder="••••••••" 
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)} 
                            required 
                        />
                    </div>

                    <div className="rp-button-wrapper">
                        <button type="submit" className="rp-submit-btn">
                            Update!
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ResetPasswordPage;