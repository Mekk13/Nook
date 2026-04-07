import { useRoomStore } from "../stores/roomStore";

export function useRoomDetail() {
  const rooms = useRoomStore((state) => state.rooms);
  const selectedId = useRoomStore((state) => state.selectedRoomId);

  const room = rooms.find((r) => r.id === selectedId);

  const displayDate = room?.createdAt || "No date available";

  return {
    room,
    displayDate,
  };
}