
export interface StudySession {
    id: string;
    subject: string;
    hours: number;
    date: string;
}

export interface Participant {
    id: string;
    name: string;
    sessions: StudySession[];
    studyStatus: 'Studying' | 'Idle' | 'OnBreak';
}

export interface Room {
    id: string;
    name: string;
    creator: string;
    maxParticipants: number; // Changed from string "3/5" to a number
    participants: Participant[]; // This is where the real data lives
    description?: string;
    status: 'Public' | 'Private';
    createdAt: string;
}