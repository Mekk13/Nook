package com.Nook.backend.domain.flashcard;

import com.Nook.backend.domain.flashcard.dto.*;
import com.Nook.backend.domain.membership.IMembershipRepository;
import com.Nook.backend.domain.membership.MemberRole;
import com.Nook.backend.domain.membership.RoomMembership;
import com.Nook.backend.domain.room.IRoomRepository;
import com.Nook.backend.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final IFlashcardDeckRepository deckRepository;
    private final IFlashcardRepository cardRepository;
    private final IMembershipRepository membershipRepository;
    private final IRoomRepository roomRepository;

    // ── Deck operations ──────────────────────────────────────────

    public List<DeckResponse> getDecksInRoom(String roomId) {
        assertRoomExists(roomId);
        return deckRepository.findByRoomId(roomId).stream()
                .map(d -> DeckResponse.from(d, cardRepository.findByDeckId(d.getId()).size()))
                .toList();
    }

    public DeckResponse createDeck(String userId, String roomId, DeckCreateRequest request) {
        assertRoomExists(roomId);
        assertMember(userId, roomId); // any member can create a deck

        FlashcardDeck deck = FlashcardDeck.builder()
                .id(UUID.randomUUID().toString())
                .roomId(roomId)
                .creatorId(userId)
                .name(request.name())
                .topic(request.topic())
                .createdAt(LocalDateTime.now())
                .build();

        deckRepository.save(deck);
        return DeckResponse.from(deck, 0);
    }

    public DeckResponse updateDeck(String userId, String deckId, DeckUpdateRequest request) {
        FlashcardDeck deck = findDeckOrThrow(deckId);
        assertCanManageDeck(userId, deck); // creator, OWNER, or MODERATOR

        if (request.name() != null) deck.setName(request.name());
        if (request.topic() != null) deck.setTopic(request.topic());

        deckRepository.update(deck);
        int cardCount = cardRepository.findByDeckId(deckId).size();
        return DeckResponse.from(deck, cardCount);
    }

    public void deleteDeck(String userId, String deckId) {
        FlashcardDeck deck = findDeckOrThrow(deckId);
        assertCanManageDeck(userId, deck); // creator, OWNER, or MODERATOR

        // Delete all cards in the deck first (cascading delete)
        cardRepository.deleteByDeckId(deckId);
        deckRepository.delete(deckId);
    }

    // ── Card operations ──────────────────────────────────────────

    public List<CardResponse> getCardsInDeck(String deckId) {
        findDeckOrThrow(deckId); // verify deck exists
        return cardRepository.findByDeckId(deckId).stream()
                .map(CardResponse::from)
                .toList();
    }

    public CardResponse addCard(String userId, String deckId, CardCreateRequest request) {
        FlashcardDeck deck = findDeckOrThrow(deckId);
        assertMember(userId, deck.getRoomId()); // any member can add cards

        Flashcard card = Flashcard.builder()
                .id(UUID.randomUUID().toString())
                .deckId(deckId)
                .front(request.front())
                .back(request.back())
                .creatorId(userId)
                .build();

        cardRepository.save(card);
        return CardResponse.from(card);
    }

    public CardResponse updateCard(String userId, String cardId, CardUpdateRequest request) {
        Flashcard card = findCardOrThrow(cardId);
        assertCanManageCard(userId, card); // own card or OWNER/MODERATOR

        if (request.front() != null) card.setFront(request.front());
        if (request.back() != null) card.setBack(request.back());

        cardRepository.update(card);
        return CardResponse.from(card);
    }

    public void deleteCard(String userId, String cardId) {
        Flashcard card = findCardOrThrow(cardId);
        assertCanManageCard(userId, card); // own card or OWNER/MODERATOR
        cardRepository.delete(cardId);
    }

    // ── Private helpers ──────────────────────────────────────────

    private FlashcardDeck findDeckOrThrow(String deckId) {
        return deckRepository.findById(deckId)
                .orElseThrow(() -> new NotFoundException("Deck not found"));
    }

    private Flashcard findCardOrThrow(String cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("Card not found"));
    }

    private void assertRoomExists(String roomId) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("Room not found"));
    }

    // Must be any kind of member to interact with flashcards
    private void assertMember(String userId, String roomId) {
        if (!membershipRepository.existsByUserIdAndRoomId(userId, roomId)) {
            throw new UnauthorizedException("You must be a member of this room");
        }
    }

    // Deck can be managed by its creator, or by OWNER/MODERATOR of the room
    private void assertCanManageDeck(String userId, FlashcardDeck deck) {
        if (deck.getCreatorId().equals(userId)) return; // creator can always manage their own deck
        assertOwnerOrModerator(userId, deck.getRoomId());
    }

    // Card can be managed by its creator, or by OWNER/MODERATOR of the room
    private void assertCanManageCard(String userId, Flashcard card) {
        if (card.getCreatorId().equals(userId)) return; // creator can always manage their own card

        // Find which room this card's deck belongs to
        FlashcardDeck deck = deckRepository.findById(card.getDeckId())
                .orElseThrow(() -> new NotFoundException("Deck not found"));
        assertOwnerOrModerator(userId, deck.getRoomId());
    }

    private void assertOwnerOrModerator(String userId, String roomId) {
        RoomMembership membership = membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this room"));

        if (membership.getRole() != MemberRole.OWNER && membership.getRole() != MemberRole.MODERATOR) {
            throw new UnauthorizedException("Only owners and moderators can perform this action");
        }
    }
}