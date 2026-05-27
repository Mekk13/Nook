package com.Nook.backend.domain.flashcard;

import java.util.List;
import java.util.Optional;

public interface IFlashcardDeckRepository {
    FlashcardDeck save(FlashcardDeck deck);
    Optional<FlashcardDeck> findById(String id);
    List<FlashcardDeck> findByRoomId(String roomId);
    void update(FlashcardDeck deck);
    void delete(String id);
}