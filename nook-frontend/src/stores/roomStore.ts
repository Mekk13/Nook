import { create } from "zustand";
import type { Room, StudySession } from "../types/room";
import { persist, createJSONStorage } from "zustand/middleware";

interface RoomStore {
  rooms: Room[];

  selectedRoomId: string | null;
  setSelectedRoom: (id: string) => void;

  getAll: () => Room[];
  getById: (id: string) => Room | undefined;

  add: (room: Room) => void;
  remove: (id: string) => void;
  update: (id: string, updatedRoom: Partial<Room>) => void;
  addSession: (
    roomId: string,
    participantId: string,
    newSession: StudySession,
  ) => void;
  removeSession: (
    roomId: string,
    participantId: string,
    sessionId: string,
  ) => void;
  updateParticipantStatus: (roomId: string, participantId: string, newStatus: 'Studying' | 'Idle' | 'OnBreak') => void;
}

export const useRoomStore = create<RoomStore>()(
  persist(
    (set, get) => ({
      // inside useRoomStore.ts
      rooms: [
        {
          id: "1",
          name: "MathFocus",
          creator: "Alex",
          maxParticipants: 5,
          description: "Hardcore Calculus",
          status: "Public",
          createdAt: "26/03/2026",
          participants: [
            {
              id: "p1",
              name: "Alex",
              studyStatus: "Idle",
              sessions: [
                { id: "s1", subject: "Algebra", hours: 2, date: "2026-03-25" },
              ],
            },
            {
              id: "p2",
              name: "Jordan",
              studyStatus: "Idle",
              sessions: [
                {
                  id: "s3",
                  subject: "Geometry",
                  hours: 1.5,
                  date: "2026-03-26",
                },
              ],
            },
            {
              id: "p3",
              name: "Taylor",
              studyStatus: "Idle",
              sessions: [],
            },
          ],
        },
      ],
      selectedRoomId: null,
      setSelectedRoom: (id) => set({ selectedRoomId: id }),

      getAll: () => get().rooms,

      getById: (id) => get().rooms.find((r) => r.id === id),

      updateParticipantStatus: (roomId,participantId, studyStatus) => set((state) => ({
          rooms: state.rooms.map((room)=>
            room.id === roomId ? {
              ...room,
              participants: room.participants.map((p) =>
                p.id === participantId ?
              {
                 ...p,
                 studyStatus: studyStatus
              } : p
              ),
            } : room
          ),
      })),

      add: (room) =>
        set((state) => {
          
          const defaultCreator = "Max";

          // 2. Ensure participants is ALWAYS an array.
          // If the form sent something weird, we ignore it and start with the creator.
          const validParticipants =
            Array.isArray(room.participants) && room.participants.length > 0
              ? room.participants
              : [{ id: `p-${Date.now()}`, name: defaultCreator, sessions: [], studyStatus: "Idle" as const }];

          return {
            rooms: [
              ...state.rooms,
              {
                ...room,
                creator: defaultCreator,
                participants: validParticipants,
                // Force maxParticipants to be a number.
                // If the form sent "3/5" or nothing, it becomes 5.
                maxParticipants: Number(room.maxParticipants) || 5,
              },
            ],
          };
        }),

      remove: (id) =>
        set((state) => ({
          rooms: state.rooms.filter((r) => r.id !== id),
        })),

      update: (id, updatedRoom) =>
        set((state) => ({
          rooms: state.rooms.map((r) =>
            r.id === id ? { ...r, ...updatedRoom } : r,
          ),
        })),

      addSession: (roomId, participantId, newSession) =>
        set((state) => ({
          rooms: state.rooms.map((room) =>
            room.id === roomId
              ? {
                  ...room,
                  participants: room.participants.map((p) =>
                    p.id === participantId
                      ? {
                          ...p,
                          sessions: [...(p.sessions ?? []), newSession],
                        }
                      : p,
                  ),
                }
              : room,
          ),
        })),

      removeSession: (roomId, participantId, sessionId) =>
        set((state) => ({
          rooms: state.rooms.map((room) =>
            room.id === roomId
              ? {
                  ...room,
                  participants: room.participants.map((p) =>
                    p.id === participantId
                      ? {
                          ...p,
                          sessions: p.sessions.filter(
                            (s) => s.id !== sessionId,
                          ),
                        }
                      : p,
                  ),
                }
              : room,
          ),
        })),
    }),
    {
      name: "room-storage",
      storage: createJSONStorage(() => localStorage),
    },
  ),
);

window.addEventListener("storage", (event) => {
  if (event.key === "room-storage") {
    useRoomStore.persist.rehydrate();
  }
});
