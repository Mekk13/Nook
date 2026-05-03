import { useEffect } from "react";
import { useRoomStore } from "../stores/roomStore";

export function useRoomDetail() {
  const rooms = useRoomStore((state) => state.rooms);
  const selectedId = useRoomStore((state) => state.selectedRoomId);
  const fetchRoomSessions = useRoomStore((state) => state.fetchRoomSessions);
  const connectWebSocket = useRoomStore((state) => state.connectWebSocket);
  const room = rooms.find((r) => r.id === selectedId);

  useEffect(() => {
    if (!selectedId) return;
    fetchRoomSessions(selectedId);
    connectWebSocket(selectedId);
  }, [selectedId]);

  const displayDate = room?.createdAt
    ? new Date(room.createdAt).toLocaleDateString("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
      })
    : "No date available";

  return { room, displayDate };
}