import { useEffect, useRef, useState, useCallback } from "react";
import "./ChatDrawer.css";
import { useAuthStore } from "../../stores/useAuthStore";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

interface ChatMessage {
  id: string;
  roomId: string;
  userId: string;
  username: string;
  content: string;
  sentAt: string;
}

interface ChatDrawerProps {
  isOpen: boolean;
  onClose: () => void;
  roomId: string;
}

export default function ChatDrawer({
  isOpen,
  onClose,
  roomId,
}: ChatDrawerProps) {
  const token = useAuthStore.getState().token;
  const currentUserId = useAuthStore.getState().user?.userId;
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");
  const stompRef = useRef<Client | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);

  // Fetch history
  useEffect(() => {
    if (!isOpen || !roomId) return;
    fetch(`${import.meta.env.VITE_API_URL}/api/chat/room/${roomId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.json())
      .then((data) => setMessages(data))
      .catch(console.error);
  }, [isOpen, roomId]);

  // WebSocket
  useEffect(() => {
    if (!isOpen || !roomId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_URL}/ws`),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(`/topic/chat/${roomId}`, (message) => {
          const msg: ChatMessage = JSON.parse(message.body);
          setMessages((prev) => [...prev, msg]);
        });
      },
    });

    client.activate();
    stompRef.current = client;

    return () => {
      client.deactivate();
      stompRef.current = null;
    };
  }, [isOpen, roomId]);

  // Scroll to bottom on new message
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const sendMessage = useCallback(() => {
    console.log("sendMessage called, input:", input, "stomp active:", stompRef.current?.active);
    if (!input.trim() || !stompRef.current?.active) return;
    stompRef.current.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({
        roomId,
        content: input.trim(),
        userId: currentUserId,
      }),
      headers: { Authorization: `Bearer ${token}` },
    });
    setInput("");
  }, [input, roomId, token]);

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <div className={`chat-backdrop ${isOpen ? "open" : ""}`} onClick={onClose}>
      <div className="chat-drawer" onClick={(e) => e.stopPropagation()}>
        <div className="chat-header">
          <h2 className="chat-title">Room Chat</h2>
          <button className="chat-close" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="chat-messages">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`chat-bubble ${msg.userId === currentUserId ? "mine" : "theirs"}`}
            >
              {msg.userId !== currentUserId && (
                <span className="chat-username">{msg.username}</span>
              )}
              <span className="chat-content">{msg.content}</span>
              <span className="chat-time">
                {new Date(msg.sentAt).toLocaleTimeString([], {
                  hour: "2-digit",
                  minute: "2-digit",
                })}
              </span>
            </div>
          ))}
          <div ref={bottomRef} />
        </div>

        <div className="chat-input-bar">
          <input
            className="chat-input"
            placeholder="Type a message..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
          />
          <button className="chat-send-btn" onClick={sendMessage}>
            Send
          </button>
        </div>
      </div>
    </div>
  );
}
