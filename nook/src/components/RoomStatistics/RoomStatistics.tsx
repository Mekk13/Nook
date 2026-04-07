import React, { useState, useMemo } from "react";
import {
  PieChart,
  Pie,
  Cell,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import "./RoomStatistics.css";
import { useRoomStore } from "../../stores/roomStore";

// --- Sub-component for Rows ---
const ParticipantRow = ({ stat }: { stat: any }) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const subjectsEntries = Object.entries(
    stat.subjects as Record<string, number>,
  );
  const topDiscipline =
    subjectsEntries.length > 0
      ? subjectsEntries.reduce((a, b) => (a[1] > b[1] ? a : b))[0]
      : "None";

  return (
    <div className={`stats-item-container ${isExpanded ? "is-open" : ""}`}>
      <div
        className="stats-row expandable"
        onClick={() => setIsExpanded(!isExpanded)}
      >
        <span className="stats-name">{stat.name}</span>
        <span className="stats-hours-total">{stat.totalHours.toFixed(1)}h</span>
        <span className="stats-top-discipline focus-purple">
          {topDiscipline}
        </span>
        <span className="expand-arrow">{isExpanded ? "▲" : "▼"}</span>
      </div>
      {isExpanded && (
        <div className="stats-drawer">
          <div className="drawer-inner">
            {subjectsEntries.length > 0 ? (
              subjectsEntries.map(([subject, hours]) => (
                <div key={subject} className="subject-pill-cute">
                  <span className="pill-subject">{subject}</span>
                  <span className="pill-divider"></span>
                  <span className="pill-time">{hours}h</span>
                </div>
              ))
            ) : (
              <p className="no-data-msg">No sessions logged yet! ✨</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

const RoomStatistics: React.FC<{ isOpen: boolean; onClose: () => void }> = ({
  isOpen,
  onClose,
}) => {
  const { rooms, selectedRoomId } = useRoomStore();
  const [viewMode, setViewMode] = useState<"tabular" | "visual">("tabular");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 3;

  const currentRoom = rooms.find((r) => r.id === selectedRoomId);

  // 1. Process Raw Data (Real-time calculation)
  const participantStats = useMemo(() => {
    if (!currentRoom || !Array.isArray(currentRoom.participants)) {
        return [];
    }

    return currentRoom.participants.map((p) => {
      // 2. Safely get sessions (default to empty array)
      const sessions = p.sessions ?? [];

      // 3. Calculate subject totals
      const subjects = sessions.reduce(
        (acc, s) => {
          acc[s.subject] = (acc[s.subject] || 0) + s.hours;
          return acc;
        },
        {} as Record<string, number>,
      );

      // 4. Return the object Recharts needs
      return {
        name: p.name,
        totalHours: sessions.reduce((sum, s) => sum + s.hours, 0),
        subjects,
        ...subjects,
      };
    });
  }, [currentRoom]);
  const allSubjects = useMemo(() => {
    if (!currentRoom) return [];
    const subjectsSet = new Set<string>();
    currentRoom.participants?.forEach((p) => {
      p.sessions?.forEach((s) => {
        if (s.subject) subjectsSet.add(s.subject);
      });
    });
    return Array.from(subjectsSet);
  }, [currentRoom]);
  // 2. Pagination Logic for Tabular View
  const totalPages = Math.ceil(participantStats.length / itemsPerPage);
  const displayedStats = participantStats.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage,
  );

  if (!isOpen || !currentRoom) return null;

  const COLORS = ["#4A90E2", "#9F7AEA", "#F6AD55", "#ED64A6", "#48BB78"];
  ///const allSubjects = Array.from(new Set(currentRoom.participants.flatMap(p => p.sessions.map(s => s.subject))));

  return (
    <div className="stats-overlay">
      <div className="stats-modal-card">
        <button className="stats-back-btn" onClick={onClose}>
          Back
        </button>
        <h1 className="stats-main-title">Statistics</h1>
        <h3 className="stats-sub-title">
          {viewMode === "tabular" ? "Tabular View" : "Visual View"}
        </h3>

        {viewMode === "tabular" ? (
          <div className="tabular-content">
            <div className="stats-table-header">
              <span>Member</span>
              <span>Total</span>
              <span>Focus</span>
              <span></span>
            </div>
            <div className="stats-list">
              {displayedStats.map((stat, i) => (
                <ParticipantRow key={stat.name + i} stat={stat} />
              ))}
            </div>

            {/* Pagination Controls */}
            <div className="stats-pagination">
              <span
                className={currentPage === 1 ? "disabled" : ""}
                onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
              >
                &lt; Prev
              </span>
              <div className="stats-page-numbers-list">
                {Array.from({ length: totalPages }, (_, i) => (
                  <span
                    key={i + 1}
                    className={`page-num ${currentPage === i + 1 ? "active" : ""}`}
                    onClick={() => setCurrentPage(i + 1)}
                  >
                    {i + 1}
                  </span>
                ))}
              </div>
              <span
                className={currentPage === totalPages ? "disabled" : ""}
                onClick={() =>
                  setCurrentPage((p) => Math.min(totalPages, p + 1))
                }
              >
                Next &gt;
              </span>
            </div>
          </div>
        ) : (
          <div className="visual-content">
            {participantStats.length === 0 ? (
              <p className="no-data-msg">No study data yet ✨</p>
            ) : (
              <div className="charts-grid">
                <div className="chart-container">
                  <h4>Total Hours Studied</h4>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={participantStats}
                        dataKey="totalHours"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        outerRadius={70}
                        label
                      >
                        {participantStats.map((_, index) => (
                          <Cell
                            key={`cell-${index}`}
                            fill={COLORS[index % COLORS.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                <div className="chart-container">
                  <h4>Hours per Subject</h4>
                  <ResponsiveContainer width="100%" height={250}>
                    <BarChart data={participantStats}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      {allSubjects.map((sub, i) => (
                        <Bar
                          key={sub}
                          dataKey={sub as string}
                          fill={COLORS[i % COLORS.length]}
                          radius={[4, 4, 0, 0]}
                        />
                      ))}
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </div>
            )}
          </div>
        )}

        <button
          className="switch-visual-btn"
          onClick={() =>
            setViewMode((v) => (v === "tabular" ? "visual" : "tabular"))
          }
        >
          Switch to {viewMode === "tabular" ? "Visual" : "Tabular"} Statistics
        </button>
      </div>
    </div>
  );
};

export default RoomStatistics;
