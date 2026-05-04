import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { Room, StudySession } from "../types/room";
import { useAuthStore } from "./useAuthStore";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

type OperationType = "add" | "update" | "remove" | "removeSession";

interface QueuedOperation {
  id: string;
  type: OperationType;
  payload: any;
}

const API_URL = `${import.meta.env.VITE_API_URL}/api/rooms`;
const SESSIONS_URL = `${import.meta.env.VITE_API_URL}/api/sessions`;
const FAKER_URL = `${import.meta.env.VITE_API_URL}/api/faker`;
const WS_URL = `${import.meta.env.VITE_API_URL}/ws`;

function getToken() {
  return useAuthStore.getState().token;
}

function isOnline(): boolean {
  return (window as any).__networkOnline ?? navigator.onLine;
}

interface EndSessionRequest {
  name: string;
  subject: string;
}

interface RoomStore {
  rooms: Room[];
  totalPages: number;
  selectedRoomId: string | null;
  offlineQueue: QueuedOperation[];
  fakerRunning: boolean;
  stompClient: Client | null;

  setSelectedRoom: (id: string) => void;
  getAll: () => Room[];
  getById: (id: string) => Room | undefined;

  fetchRooms: (page?: number, size?: number) => Promise<void>;
  fetchRoomSessions: (roomId: string) => Promise<void>;
  add: (room: {
    name: string;
    description: string;
    maxParticipants: number;
    isPrivate: boolean;
  }) => Promise<void>;
  remove: (id: string) => Promise<void>;
  update: (
    id: string,
    updatedRoom: {
      name?: string;
      description?: string;
      maxParticipants?: number;
      isPrivate?: boolean;
    },
  ) => Promise<void>;
  flushQueue: () => Promise<void>;

  startSession: (roomId: string) => Promise<StudySession | null>;
  endSession: (
    sessionId: string,
    roomId: string,
    participantId: string,
    req: EndSessionRequest,
  ) => Promise<void>;
  startBreak: (
    sessionId: string,
    roomId: string,
    participantId: string,
  ) => Promise<void>;
  endBreak: (
    sessionId: string,
    roomId: string,
    participantId: string,
  ) => Promise<void>;
  deleteSession: (
    sessionId: string,
    roomId: string,
    participantId: string,
  ) => Promise<void>;

  updateParticipantStatus: (
    roomId: string,
    participantId: string,
    newStatus: "Studying" | "Idle" | "OnBreak",
  ) => void;

  startFaker: (
    roomId: string,
    intervalSeconds?: number,
    batchSize?: number,
  ) => Promise<void>;
  connectWebSocket: (roomId: string) => void;
  stopFaker: () => Promise<void>;
  checkFakerStatus: () => Promise<void>;
  disconnectWebSocket: () => void;

  activeSession: {
    sessionId: string;
    startedAt: number; // Date.now() timestamp
    state: "studying" | "paused";
    pausedAt: number | null;
    roomId: string;
  } | null;
  setActiveSession: (
    s: {
      sessionId: string;
      startedAt: number;
      state: "studying" | "paused";
      pausedAt: number | null;
      roomId: string;
    } | null,
  ) => void;
}

function applySessionUpdate(
  rooms: Room[],
  roomId: string,
  participantId: string,
  updatedSession: StudySession,
): Room[] {
  return rooms.map((room) =>
    room.id !== roomId
      ? room
      : {
          ...room,
          participants: (room.participants ?? []).map((p) =>
            p.userId !== participantId
              ? p
              : {
                  ...p,
                  sessions: (p.sessions ?? []).some(
                    (s) => s.id === updatedSession.id,
                  )
                    ? p.sessions.map((s) =>
                        s.id === updatedSession.id ? updatedSession : s,
                      )
                    : [...(p.sessions ?? []), updatedSession],
                },
          ),
        },
  );
}

export const useRoomStore = create<RoomStore>()(
  persist(
    (set, get) => ({
      rooms: [],
      totalPages: 1,
      selectedRoomId: null,
      offlineQueue: [],
      fakerRunning: false,
      stompClient: null,

      setSelectedRoom: (id) => set({ selectedRoomId: id }),
      getAll: () => get().rooms,
      getById: (id) => get().rooms.find((r) => r.id === id),

      fetchRooms: async (page = 0, size = 5) => {
        const token = getToken();
        try {
          const res = await fetch(`${API_URL}/my?page=${page}&size=${size}`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          const data = await res.json();
          const rooms = (data.content ?? []).map((r: any) => ({
            ...r,
            status: r.isPrivate ? "Private" : "Public",
          }));
          set({ rooms, totalPages: data.totalPages ?? 1 });
        } catch {
          console.warn("fetchRooms failed — using cached data");
        }
      },

      fetchRoomSessions: async (roomId) => {
        const token = getToken();
        try {
          const res = await fetch(`${SESSIONS_URL}/room/${roomId}`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          const data = await res.json();
          console.log("raw sessions response:", data);
          const sessions: StudySession[] = data.content ?? data;
          set((state) => ({
            rooms: state.rooms.map((r) => {
              console.log("participants:", r.participants);
              console.log("sessions:", sessions);
              if (r.id !== roomId) return r;

              const updatedParticipants = (r.participants ?? []).map((p) => ({
                ...p,
                sessions: sessions.filter((s) => s.userId === p.userId),
              }));

              return { ...r, participants: updatedParticipants };
            }),
          }));
        } catch (e) {
          console.warn(`fetchRoomSessions failed for room ${roomId}`, e);
        }
      },

      add: async (room) => {
        const token = getToken();
        const tempId = `temp-${Date.now()}`;
        const optimisticRoom: Room = {
          ...room,
          id: tempId,
          creatorName: useAuthStore.getState().user?.username ?? "",
          createdAt: new Date().toISOString(),
          memberCount: 1,
          participants: [],
          status: room.isPrivate ? "Private" : "Public",
        };

        set((state) => ({ rooms: [...state.rooms, optimisticRoom] }));

        if (!isOnline()) {
          set((state) => ({
            offlineQueue: [
              ...state.offlineQueue,
              { id: tempId, type: "add", payload: room },
            ],
          }));
          return;
        }

        try {
          await fetch(API_URL, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(room),
          });
          get().fetchRooms(0, 5);
        } catch {
          set((state) => ({
            offlineQueue: [
              ...state.offlineQueue,
              { id: tempId, type: "add", payload: room },
            ],
          }));
        }
      },

      remove: async (id) => {
        const token = getToken();
        set((state) => ({ rooms: state.rooms.filter((r) => r.id !== id) }));

        if (!isOnline()) {
          set((state) => ({
            offlineQueue: [
              ...state.offlineQueue,
              { id: `op-${Date.now()}`, type: "remove", payload: { id } },
            ],
          }));
          return;
        }

        try {
          await fetch(`${API_URL}/${id}`, {
            method: "DELETE",
            headers: { Authorization: `Bearer ${token}` },
          });
          get().fetchRooms(0, 5);
        } catch {
          set((state) => ({
            offlineQueue: [
              ...state.offlineQueue,
              { id: `op-${Date.now()}`, type: "remove", payload: { id } },
            ],
          }));
        }
      },

      update: async (id, updatedRoom) => {
        const token = getToken();
        set((state) => ({
          rooms: state.rooms.map((r) =>
            r.id === id
              ? {
                  ...r,
                  ...updatedRoom,
                  status:
                    updatedRoom.isPrivate !== undefined
                      ? updatedRoom.isPrivate
                        ? "Private"
                        : "Public"
                      : r.status,
                }
              : r,
          ),
        }));

        if (!isOnline()) {
          set((state) => ({
            offlineQueue: [
              ...state.offlineQueue,
              {
                id: `op-${Date.now()}`,
                type: "update",
                payload: { id, updatedRoom },
              },
            ],
          }));
          return;
        }

        try {
          await fetch(`${API_URL}/${id}`, {
            method: "PUT",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(updatedRoom),
          });
          get().fetchRooms(0, 5);
        } catch {
          set((state) => ({
            offlineQueue: [
              ...state.offlineQueue,
              {
                id: `op-${Date.now()}`,
                type: "update",
                payload: { id, updatedRoom },
              },
            ],
          }));
        }
      },

      startSession: async (roomId) => {
        const token = getToken();
        try {
          await fetch(`${SESSIONS_URL}/force-end`, {
            method: "POST",
            headers: { Authorization: `Bearer ${token}` },
          });

          const res = await fetch(`${SESSIONS_URL}/start`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({ roomId }),
          });
          const created: StudySession = await res.json();
          return created;
        } catch {
          console.warn("startSession failed");
          return null;
        }
      },

      endSession: async (sessionId, roomId, participantId, req) => {
        const token = getToken();
        try {
          const res = await fetch(`${SESSIONS_URL}/${sessionId}/end`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify(req),
          });
          if (!res.ok) {
            const body = await res.text();
            console.warn("endSession failed:", res.status, body);
            return;
          }
          const updated: StudySession = await res.json();
          set((state) => ({
            rooms: applySessionUpdate(
              state.rooms,
              roomId,
              participantId,
              updated,
            ),
          }));
        } catch {
          console.warn("endSession failed");
        }
      },

      startBreak: async (sessionId, roomId, participantId) => {
        const token = getToken();
        try {
          const res = await fetch(`${SESSIONS_URL}/${sessionId}/break/start`, {
            method: "POST",
            headers: { Authorization: `Bearer ${token}` },
          });
          if (!res.ok) {
            const body = await res.text();
            console.warn("startBreak failed:", res.status, body);
            return;
          }
          const updated: StudySession = await res.json();
          set((state) => ({
            rooms: applySessionUpdate(
              state.rooms,
              roomId,
              participantId,
              updated,
            ),
          }));
        } catch (e) {
          console.warn("startBreak network error:", e);
        }
      },

      endBreak: async (sessionId, roomId, participantId) => {
        const token = getToken();
        try {
          const res = await fetch(`${SESSIONS_URL}/${sessionId}/break/end`, {
            method: "POST",
            headers: { Authorization: `Bearer ${token}` },
          });
          const updated: StudySession = await res.json();
          set((state) => ({
            rooms: applySessionUpdate(
              state.rooms,
              roomId,
              participantId,
              updated,
            ),
          }));
        } catch {
          console.warn("endBreak failed");
        }
      },

      deleteSession: async (sessionId, roomId, participantId) => {
        const token = getToken();
        set((state) => ({
          rooms: state.rooms.map((room) =>
            room.id !== roomId
              ? room
              : {
                  ...room,
                  participants: (room.participants ?? []).map((p) =>
                    p.userId !== participantId
                      ? p
                      : {
                          ...p,
                          sessions: p.sessions.filter(
                            (s) => s.id !== sessionId,
                          ),
                        },
                  ),
                },
          ),
        }));

        if (!isOnline()) {
          set((state) => ({
            offlineQueue: [
              ...state.offlineQueue,
              {
                id: `op-${Date.now()}`,
                type: "removeSession",
                payload: { roomId, participantId, sessionId },
              },
            ],
          }));
          return;
        }

        try {
          await fetch(`${SESSIONS_URL}/${sessionId}`, {
            method: "DELETE",
            headers: { Authorization: `Bearer ${token}` },
          });
        } catch {
          set((state) => ({
            offlineQueue: [
              ...state.offlineQueue,
              {
                id: `op-${Date.now()}`,
                type: "removeSession",
                payload: { roomId, participantId, sessionId },
              },
            ],
          }));
        }
      },

      updateParticipantStatus: (roomId, participantId, studyStatus) =>
        set((state) => ({
          rooms: state.rooms.map((room) =>
            room.id !== roomId
              ? room
              : {
                  ...room,
                  participants: (room.participants ?? []).map((p) =>
                    p.userId !== participantId ? p : { ...p, studyStatus },
                  ),
                },
          ),
        })),

      startFaker: async (
        roomId: string,
        intervalSeconds = 5,
        batchSize = 3,
      ) => {
        console.log("Starting Faker for room:", roomId);
        const token = getToken();
        console.log("Token found:", !!token);

        try {
          const url = `${FAKER_URL}/start?roomId=${roomId}&intervalSeconds=${intervalSeconds}&batchSize=${batchSize}`;
          console.log("Fetching URL:", url);

          const response = await fetch(url, {
            method: "POST",
            headers: { Authorization: `Bearer ${token}` },
          });

          console.log("Response Status:", response.status); // Log 4
          set({ fakerRunning: true });
        } catch (error) {
          console.error("startFaker failed at fetch:", error);
        }
      },

      stopFaker: async () => {
        const token = getToken();
        try {
          await fetch(`${FAKER_URL}/stop`, {
            method: "POST",
            headers: { Authorization: `Bearer ${token}` },
          });
          set({ fakerRunning: false });
        } catch {
          console.warn("stopFaker failed");
        }
      },

      checkFakerStatus: async () => {
        const token = getToken();
        try {
          const res = await fetch(`${FAKER_URL}/status`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          const running: boolean = await res.json();
          set({ fakerRunning: running });
        } catch {
          console.warn("checkFakerStatus failed");
        }
      },

      connectWebSocket: (roomId: string) => {
        // Accept roomId as an argument
        const existing = get().stompClient;
        if (existing?.active) {
          existing.deactivate();
        }

        const client = new Client({
          webSocketFactory: () => new SockJS(WS_URL),
          reconnectDelay: 5000,
          onConnect: () => {
            console.log("WS connected for room:", roomId);
            client.subscribe(`/topic/room/${roomId}`, async (message) => {
              console.log("WS message received:", message.body);
              const event = JSON.parse(message.body);
              if (event.type === "SESSION_BATCH_CREATED") {
                await get().fetchRooms(0, 5);
                await get().fetchRoomSessions(roomId);
                // Also notify the modal if it's open
                window.dispatchEvent(
                  new CustomEvent("sessions-updated", { detail: { roomId } }),
                );
              }
            });
          },
        });

        client.activate();
        set({ stompClient: client });
      },

      disconnectWebSocket: () => {
        const client = get().stompClient;
        if (client?.active) {
          client.deactivate();
        }
        set({ stompClient: null });
      },

      flushQueue: async () => {
        const { offlineQueue } = get();
        if (offlineQueue.length === 0) return;

        const token = getToken();
        const failed: QueuedOperation[] = [];

        for (const op of offlineQueue) {
          try {
            if (op.type === "add") {
              await fetch(API_URL, {
                method: "POST",
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(op.payload),
              });
            } else if (op.type === "remove") {
              await fetch(`${API_URL}/${op.payload.id}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` },
              });
            } else if (op.type === "update") {
              await fetch(`${API_URL}/${op.payload.id}`, {
                method: "PUT",
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(op.payload.updatedRoom),
              });
            } else if (op.type === "removeSession") {
              await fetch(`${SESSIONS_URL}/${op.payload.sessionId}`, {
                method: "DELETE",
                headers: { Authorization: `Bearer ${token}` },
              });
            }
          } catch {
            failed.push(op);
          }
        }

        set({ offlineQueue: failed });
        get().fetchRooms(0, 5);
      },

      activeSession: null,
      setActiveSession: (s) => set({ activeSession: s }),
    }),
    {
      name: "nook-rooms",
      partialize: (state: RoomStore) => ({
        rooms: state.rooms,
        offlineQueue: state.offlineQueue,
        selectedRoomId: state.selectedRoomId,
        activeSession: state.activeSession,
      }),
    },
  ),
);
