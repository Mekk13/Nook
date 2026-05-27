import React, { useState, useMemo, useEffect } from "react";
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

const ParticipantRow = ({
  stat,
  isExpanded,
  onToggle,
}: {
  stat: any;
  isExpanded: boolean;
  onToggle: () => void;
}) => {
  const subjectsEntries = Object.entries(
    stat.subjects as Record<string, number>,
  );
  const topDiscipline =
    subjectsEntries.length > 0
      ? subjectsEntries.reduce((a, b) => (a[1] > b[1] ? a : b))[0]
      : "None";

  return (
    <div className={`stats-item-container ${isExpanded ? "is-open" : ""}`}>
      <div className="stats-row expandable" onClick={onToggle}>
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
                  <span className="pill-time">
                    {(hours as number).toFixed(1)}h
                  </span>
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
  const [expandedStat, setExpandedStat] = useState<string | null>(null);
  const itemsPerPage = 3;

  const currentRoom = rooms.find((r) => r.id === selectedRoomId);

  useEffect(() => {
    if (!isOpen || !selectedRoomId) return;
    useRoomStore.getState().fetchRoomSessions(selectedRoomId);
  }, [isOpen, selectedRoomId]);

  const participantStats = useMemo(() => {
    if (!currentRoom || !Array.isArray(currentRoom.participants)) return [];
    return currentRoom.participants.map((p) => {
      const sessions = p.sessions ?? [];
      const subjects = sessions.reduce(
        (acc, s) => {
          const duration = s.durationHours || 0;
          const subKey = s.subject ?? "General";
          acc[subKey] = (acc[subKey] || 0) + duration;
          return acc;
        },
        {} as Record<string, number>,
      );
      return {
        name: p.username ?? "Unknown",
        totalHours: sessions.reduce(
          (sum, s) => sum + (s.durationHours || 0),
          0,
        ),
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

  const totalPages = Math.max(
    1,
    Math.ceil(participantStats.length / itemsPerPage),
  );
  const displayedStats = participantStats.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage,
  );

  if (!isOpen || !currentRoom) return null;

  const COLORS = ["#4A90E2", "#9F7AEA", "#F6AD55", "#ED64A6", "#48BB78"];

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
                <ParticipantRow
                  key={stat.name + i}
                  stat={stat}
                  isExpanded={expandedStat === stat.name}
                  onToggle={() =>
                    setExpandedStat(
                      expandedStat === stat.name ? null : stat.name,
                    )
                  }
                />
              ))}
            </div>
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
                  <ResponsiveContainer width="100%" height={280}>
                    <PieChart>
                      <Pie
                        data={participantStats}
                        dataKey="totalHours"
                        nameKey="name"
                        cx="50%"
                        cy="50%"
                        outerRadius={80}
                        label={false}
                      >
                        {participantStats.map((_, index) => (
                          <Cell
                            key={`cell-${index}`}
                            fill={COLORS[index % COLORS.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip
                        formatter={(value, name) => [
                          `${Number(value).toFixed(1)}h`,
                          name,
                        ]}
                      />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </div>

                <div className="chart-container">
                  <h4>Hours per Subject</h4>
                  <ResponsiveContainer width="100%" height={280}>
                    <BarChart data={participantStats}>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} />
                      <XAxis dataKey="name" padding={{ left: 30, right: 30 }} />
                      <YAxis />
                      <Tooltip
                        formatter={(value, name) => [
                          `${Number(value).toFixed(1)}h`,
                          name,
                        ]}
                      />
                      {allSubjects.map((sub, i) => (
                        <Bar
                          key={sub}
                          dataKey={sub}
                          stackId="a"
                          fill={COLORS[i % COLORS.length]}
                          radius={
                            i === allSubjects.length - 1
                              ? [4, 4, 0, 0]
                              : [0, 0, 0, 0]
                          }
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
