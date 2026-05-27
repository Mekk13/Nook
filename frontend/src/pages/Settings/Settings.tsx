import "./Settings.css";
import { useState, useEffect } from "react";
import { useAuthStore } from "../../stores/useAuthStore";
import { useNavigate } from "react-router-dom";
import { useApi } from "../../hooks/useApi";

const AVATARS = [
  "cat",
  "dog",
  "fox",
  "rabbit",
  "bear",
  "panda",
  "owl",
  "penguin",
  "frog",
  "deer",
];

function Settings() {
  const { user, setAuth, logout } = useAuthStore();
  const { apiFetch } = useApi();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState(user?.fullName || "");
  const [username, setUsername] = useState(user?.username || "");
  const [description, setDescription] = useState("");
  const [avatar, setAvatar] = useState(user?.avatar || "default");

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [message, setMessage] = useState<{ text: string; ok: boolean } | null>(
    null,
  );
  const [isLoading, setIsLoading] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [passwordMessage, setPasswordMessage] = useState<{
    text: string;
    ok: boolean;
  } | null>(null);

  const [mfaEnabled, setMfaEnabled] = useState(user?.mfaEnabled ?? false);
  const [mfaMessage, setMfaMessage] = useState<{
    text: string;
    ok: boolean;
  } | null>(null);

  const showPasswordMsg = (text: string, ok: boolean) => {
    setPasswordMessage({ text, ok });
    setTimeout(() => setPasswordMessage(null), 4000);
  };

  useEffect(() => {
    apiFetch("/api/users/me")
      .then((r) => {
        if (!r.ok) return;
        return r.json();
      })
      .then((data) => {
        console.log(data.mfaEnabled)
        if (!data) return;
        setFullName(data.fullName || "");
        setUsername(data.username || "");
        setDescription(data.description || "");
        setAvatar(data.avatar || "default");
        setMfaEnabled(data.mfaEnabled ?? false); // 👈 add this
      })
      .catch(() => {});
  }, []);

  const showMsg = (text: string, ok: boolean) => {
    setMessage({ text, ok });
    setTimeout(() => setMessage(null), 4000);
  };

  const handleSaveProfile = async () => {
    if (!fullName.trim()) {
      showMsg("Display name can't be empty", false);
      return;
    }
    if (username.trim().length < 2) {
      showMsg("Username must be at least 2 characters", false);
      return;
    }
    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
      showMsg(
        "Username can only contain letters, numbers and underscores",
        false,
      );
      return;
    }

    setIsLoading(true);
    try {
      const res = await apiFetch("/api/users/me", {
        method: "PUT",
        body: JSON.stringify({ fullName, username, description, avatar }),
      });
      const text = await res.text();
      if (!res.ok) {
        showMsg(text || "Failed to update profile", false);
        return;
      }
      const data = JSON.parse(text);
      setAuth({ ...data, token: user?.userId, role: user?.role ?? "USER" });
      showMsg("Profile updated!", true);
    } catch {
      showMsg("Something went wrong", false);
    } finally {
      setIsLoading(false);
    }
  };

  const handleChangePassword = async () => {
    if (!currentPassword) {
      showPasswordMsg("Enter your current password", false);
      return;
    }
    if (newPassword.length < 10) {
      showPasswordMsg("Password must be at least 10 characters", false);
      return;
    }
    if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&])/.test(newPassword)) {
      showPasswordMsg(
        "Password needs uppercase, lowercase, number and special character (@$!%*?&)",
        false,
      );
      return;
    }
    if (newPassword !== confirmPassword) {
      showPasswordMsg("Passwords don't match", false);
      return;
    }

    setIsLoading(true);
    try {
      const res = await apiFetch("/api/users/me", {
        method: "PUT",
        body: JSON.stringify({ currentPassword, newPassword }),
      });
      const text = await res.text();
      if (!res.ok) {
        showPasswordMsg(text || "Failed to change password", false);
        return;
      }
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      showPasswordMsg("Password changed!", true);
    } catch {
      showPasswordMsg("Something went wrong", false);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    try {
      await apiFetch("/api/users/me", { method: "DELETE" });
      logout();
      navigate("/");
    } catch {
      showMsg("Failed to delete account", false);
    }
  };

  return (
    <div className="settings-page">
      <div className="settings-container">
        <h1 className="settings-title">Settings</h1>

        {message && (
          <div className={`settings-message ${message.ok ? "ok" : "err"}`}>
            {message.text}
          </div>
        )}

        <div className="settings-card">
          <h2 className="settings-section-title">Two-factor authentication</h2>
          <p style={{ fontSize: "14px", color: "#a0a8c0", margin: 0 }}>
            {mfaEnabled
              ? "3FA is enabled. You'll be asked for an email code and authenticator code each login."
              : "Enable 3FA to secure your account with an email code and authenticator app."}
          </p>

          {mfaMessage && (
            <div className={`settings-message ${mfaMessage.ok ? "ok" : "err"}`}>
              {mfaMessage.text}
            </div>
          )}

          {mfaEnabled ? (
            <button
              className="settings-btn settings-btn--danger"
              onClick={async () => {
                try {
                  await apiFetch("/api/auth/mfa/setup", { method: "DELETE" });
                  setMfaEnabled(false);
                  setMfaMessage({ text: "3FA disabled.", ok: true });
                } catch {
                  setMfaMessage({ text: "Something went wrong.", ok: false });
                }
              }}
            >
              Disable 3FA
            </button>
          ) : (
            <button
              className="settings-btn settings-btn--primary"
              onClick={() => navigate("/settings/mfa-setup")}
            >
              Enable 3FA
            </button>
          )}
          <h2 className="settings-section-title">Profile</h2>

          <div className="settings-field">
            <label>Display name</label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="Your full name"
            />
          </div>

          <div className="settings-field">
            <label>Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="username"
            />
          </div>

          <div className="settings-field">
            <label>Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Tell people a bit about yourself..."
              rows={3}
            />
          </div>

          <div className="settings-field">
            <label>Avatar</label>
            <div className="avatar-grid">
              {AVATARS.map((a) => (
                <button
                  key={a}
                  className={`avatar-option ${avatar === a ? "selected" : ""}`}
                  onClick={() => setAvatar(a)}
                >
                  {a}
                </button>
              ))}
            </div>
          </div>

          <button
            className="settings-btn settings-btn--primary"
            onClick={handleSaveProfile}
            disabled={isLoading}
          >
            {isLoading ? "Saving..." : "Save changes"}
          </button>
        </div>

        {passwordMessage && (
          <div
            className={`settings-message ${passwordMessage.ok ? "ok" : "err"}`}
          >
            {passwordMessage.text}
          </div>
        )}
        <div className="settings-card">
          <h2 className="settings-section-title">Change password</h2>

          <div className="settings-field">
            <label>Current password</label>
            <input
              type="password"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
            />
          </div>

          <div className="settings-field">
            <label>New password</label>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
          </div>

          <div className="settings-field">
            <label>Confirm new password</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </div>

          <button
            className="settings-btn settings-btn--primary"
            onClick={handleChangePassword}
            disabled={isLoading}
          >
            {isLoading ? "Saving..." : "Change password"}
          </button>
        </div>

        <div className="settings-card settings-card--danger">
          <h2 className="settings-section-title danger">Danger zone</h2>
          <p className="settings-danger-text">
            Deleting your account is permanent and cannot be undone.
          </p>
          {!showDeleteConfirm ? (
            <button
              className="settings-btn settings-btn--danger"
              onClick={() => setShowDeleteConfirm(true)}
            >
              Delete account
            </button>
          ) : (
            <div className="settings-delete-confirm">
              <p>Are you sure? This cannot be undone.</p>
              <div className="settings-delete-actions">
                <button
                  className="settings-btn settings-btn--danger"
                  onClick={handleDeleteAccount}
                >
                  Yes, delete
                </button>
                <button
                  className="settings-btn settings-btn--secondary"
                  onClick={() => setShowDeleteConfirm(false)}
                >
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Settings;
