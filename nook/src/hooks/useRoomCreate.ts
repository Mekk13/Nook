import { useState } from "react";
import { useRoomStore } from "../stores/roomStore";

export function useRoomCreate() {
  const [maxParticipants, setMaxParticipants] = useState(5);
  const [errors, setErrors] = useState<{ name?: string; desc?: string }>({});
  const addRoom = useRoomStore((state) => state.add);

  const increment = () => setMaxParticipants((prev) => prev + 1);
  const decrement = () => setMaxParticipants((prev) => Math.max(1, prev - 1));

  const submit = (name: string, desc: string, status: "Public" | "Private") => {
    const newErrors: { name?: string; desc?: string } = {};

    if (!name.trim()) {
      newErrors.name = "Room name is required.";
    } else if (name.length < 3) {
      newErrors.name = "Name must be at least 3 characters.";
    }

    if (!desc.trim()) {
      newErrors.desc = "Description cannot be empty.";
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return false; 
    }

    setErrors({});
    const createdAt = new Date().toLocaleDateString("en-GB", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });

    addRoom({
      id: Math.random().toString(),
      name,
      maxParticipants: maxParticipants,
      participants: [],
      description: desc,
      status,
      creator: "You",
      createdAt,
    });
    
    return true;
  };

  return { maxParticipants, increment, decrement, submit, errors };
}