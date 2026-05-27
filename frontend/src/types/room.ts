export interface BreakResponse {
    id: string;
    sessionId: string;
    startedAt: string;
    endedAt: string | null;
    inProgress: boolean;
}

export interface StudySession {
    id: string;
    userId: string;
    roomId: string;
    name: string | null;
    subject: string | null;
    startedAt: string;
    endedAt: string | null;
    inProgress: boolean;
    durationHours: number; // Add this line here!
    breaks: BreakResponse[];
}

export interface Participant {
    userId: string;
    username: string;
    avatar: string;
    role: string;
    status: string;
    sessions: StudySession[];
    studyStatus?: 'Studying' | 'Idle' | 'OnBreak';
}

export interface Room {
    id: string;
    name: string;
    creatorName: string;
    maxParticipants: number;
    participants: Participant[];
    description?: string;
    status: 'Public' | 'Private';
    createdAt: string;
    memberCount: number;
    roomCode?: string;
}

export interface Member {
    userId: string;
    username: string;
    avatar: string;
    role: string;
    status: string;
}