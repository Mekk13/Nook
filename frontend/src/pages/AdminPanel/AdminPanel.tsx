import { useEffect, useState } from "react";
import { useAuthStore } from "../../stores/useAuthStore";
import "./AdminPanel.css";

interface WatchlistEntry {
  id: string;
  userId: string;
  username: string;
  reason: string;
  flaggedAt: string;
}

export default function AdminPanel() {
  const token = useAuthStore.getState().token;
  const [watchlist, setWatchlist] = useState<WatchlistEntry[]>([]);
  const [loading, setLoading] = useState(true);

  const handleRemove = async (id: string) => {
    await fetch(`${import.meta.env.VITE_API_URL}/api/admin/watchlist/${id}`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${token}` },
    });
    setWatchlist((prev) => prev.filter((e) => e.id !== id));
  };

  useEffect(() => {
    fetch(`${import.meta.env.VITE_API_URL}/api/admin/watchlist`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => {
        setWatchlist(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  return (
    <div className="admin-container">
      <div className="admin-card">
        <h1 className="admin-title">Admin Panel</h1>
        <h2 className="admin-subtitle">Suspicious Users Watchlist</h2>

        {loading ? (
          <p className="admin-empty">Loading...</p>
        ) : watchlist.length === 0 ? (
          <p className="admin-empty">No suspicious users detected.</p>
        ) : (
          <div className="watchlist-table">
            <div className="watchlist-header">
              <span>Username</span>
              <span>Reason</span>
              <span>Flagged At</span>
            </div>
            {watchlist.map((entry) => (
              <div key={entry.id} className="watchlist-row">
                <span>{entry.username}</span>
                <span>{entry.reason}</span>
                <span>{new Date(entry.flaggedAt).toLocaleString()}</span>
                <button
                  className="remove-btn"
                  onClick={() => handleRemove(entry.id)}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
