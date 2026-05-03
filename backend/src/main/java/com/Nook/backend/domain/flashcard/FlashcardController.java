package com.Nook.backend.domain.flashcard;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.flashcard.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FlashcardController {

    private final FlashcardService flashcardService;

    // GET /api/rooms/{roomId}/decks
    @GetMapping("/api/rooms/{roomId}/decks")
    public ResponseEntity<List<DeckResponse>> getDecksInRoom(@PathVariable String roomId) {
        return ResponseEntity.ok(flashcardService.getDecksInRoom(roomId));
    }

    // POST /api/rooms/{roomId}/decks
    @PostMapping("/api/rooms/{roomId}/decks")
    public ResponseEntity<DeckResponse> createDeck(
            @PathVariable String roomId,
            @Valid @RequestBody DeckCreateRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(flashcardService.createDeck(userId, roomId, request));
    }

    // PUT /api/decks/{deckId}
    @PutMapping("/api/decks/{deckId}")
    public ResponseEntity<DeckResponse> updateDeck(
            @PathVariable String deckId,
            @Valid @RequestBody DeckUpdateRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(flashcardService.updateDeck(userId, deckId, request));
    }

    // DELETE /api/decks/{deckId}
    @DeleteMapping("/api/decks/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable String deckId) {
        String userId = SecurityUtils.getCurrentUserId();
        flashcardService.deleteDeck(userId, deckId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/decks/{deckId}/cards
    @GetMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<List<CardResponse>> getCardsInDeck(@PathVariable String deckId) {
        return ResponseEntity.ok(flashcardService.getCardsInDeck(deckId));
    }

    // POST /api/decks/{deckId}/cards
    @PostMapping("/api/decks/{deckId}/cards")
    public ResponseEntity<CardResponse> addCard(
            @PathVariable String deckId,
            @Valid @RequestBody CardCreateRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(flashcardService.addCard(userId, deckId, request));
    }

    // PUT /api/cards/{cardId}
    @PutMapping("/api/cards/{cardId}")
    public ResponseEntity<CardResponse> updateCard(
            @PathVariable String cardId,
            @Valid @RequestBody CardUpdateRequest request
    ) {
        String userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(flashcardService.updateCard(userId, cardId, request));
    }

    // DELETE /api/cards/{cardId}
    @DeleteMapping("/api/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable String cardId) {
        String userId = SecurityUtils.getCurrentUserId();
        flashcardService.deleteCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }
}