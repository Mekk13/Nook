import { useState, useEffect } from "react";
import { useRoomStore } from "../stores/roomStore";

export function useRoomEdit() {
  const { rooms, selectedRoomId, update } = useRoomStore();
  const roomToEdit = rooms.find((r) => r.id === selectedRoomId);

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [maxParticipants, setMaxParticipants] = useState(5);
  const [status, setStatus] = useState<'Public' | 'Private'>('Public');

  const [errors, setErrors] = useState<{ name?: string; desc?: string }>({});

  // Sync existing room data
  useEffect(() => {
    if (roomToEdit) {
      setName(roomToEdit.name ?? "");
      setDescription(roomToEdit.description ?? "");
      setStatus(roomToEdit.status ?? "Public");

      setMaxParticipants(roomToEdit.maxParticipants ?? 5);
    }
  }, [roomToEdit]);

  const save = () => {
    if (!roomToEdit) return false;

    const newErrors: { name?: string; desc?: string } = {};

    if (!name.trim()) {
      newErrors.name = "Room name is required.";
    } else if (name.length < 3) {
      newErrors.name = "Name must be at least 3 characters.";
    }

    if (!description.trim()) {
      newErrors.desc = "Description cannot be empty.";
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return false;
    }

    setErrors({});

    update(roomToEdit.id, {
      name,
      description,
      maxParticipants, 
      status
    });

    return true;
  };

  const increment = () => setMaxParticipants((prev) => prev + 1);
  const decrement = () => setMaxParticipants((prev) => Math.max(1, prev - 1));

  return {
    roomToEdit,
    name, setName,
    description, setDescription,
    maxParticipants, increment, decrement,
    status, setStatus,
    save,
    errors
  };
}