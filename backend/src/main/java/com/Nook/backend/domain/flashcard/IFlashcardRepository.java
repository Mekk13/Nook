package com.Nook.backend.domain.flashcard;

import java.util.List;
import java.util.Optional;

public interface IFlashcardRepository {
    Flashcard save(Flashcard card);
    Optional<Flashcard> findById(String id);
    List<Flashcard> findByDeckId(String deckId);
    void update(Flashcard card);
    void delete(String id);

    void deleteByDeckId(String deckId);
}