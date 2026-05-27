package com.Nook.backend.domain.flashcard.v2;

import com.Nook.backend.domain.flashcard.Flashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaFlashcardRepository extends JpaRepository<Flashcard, String> {
    List<Flashcard> findByDeckId(String deckId);
    void deleteByDeckId(String deckId);
}