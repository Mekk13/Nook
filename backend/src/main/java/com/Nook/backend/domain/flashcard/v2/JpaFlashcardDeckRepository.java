package com.Nook.backend.domain.flashcard.v2;

import com.Nook.backend.domain.flashcard.FlashcardDeck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaFlashcardDeckRepository extends JpaRepository<FlashcardDeck, String> {
    List<FlashcardDeck> findByRoomId(String roomId);
}