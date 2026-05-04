package com.Nook.backend.domain.flashcard.v2;

import com.Nook.backend.domain.flashcard.Flashcard;
import com.Nook.backend.domain.flashcard.FlashcardDeck;
import com.Nook.backend.domain.flashcard.IFlashcardDeckRepository;
import com.Nook.backend.domain.flashcard.IFlashcardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JpaFlashcardAdaptersTest {

    @Autowired
    private IFlashcardRepository flashcardRepository;

    @Autowired
    private IFlashcardDeckRepository deckRepository;

    @BeforeEach
    void setUp() {
        flashcardRepository.findByDeckId("d1")
                .forEach(c -> flashcardRepository.delete(c.getId()));

        deckRepository.findByRoomId("r1")
                .forEach(d -> deckRepository.delete(d.getId()));
    }

    private FlashcardDeck buildDeck(String id) {
        return FlashcardDeck.builder()
                .id(id)
                .roomId("r1")
                .creatorId("u1")
                .name("Deck")
                .topic("topic")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Flashcard buildCard(String id, String deckId) {
        return Flashcard.builder()
                .id(id)
                .deckId(deckId)
                .front("front")
                .back("back")
                .creatorId("u1")
                .build();
    }

    // -------------------- DECK TESTS --------------------

    @Test
    void deck_save_and_findById() {
        deckRepository.save(buildDeck("d1"));

        Optional<FlashcardDeck> result = deckRepository.findById("d1");

        assertThat(result).isPresent();
        assertThat(result.get().getRoomId()).isEqualTo("r1");
    }

    @Test
    void deck_findByRoomId() {
        deckRepository.save(buildDeck("d1"));
        deckRepository.save(buildDeck("d2"));

        List<FlashcardDeck> result = deckRepository.findByRoomId("r1");

        assertThat(result).hasSize(2);
    }

    @Test
    void deck_update() {
        deckRepository.save(buildDeck("d1"));

        FlashcardDeck updated = buildDeck("d1");
        updated.setName("Updated");

        deckRepository.update(updated);

        assertThat(deckRepository.findById("d1").get().getName())
                .isEqualTo("Updated");
    }

    @Test
    void deck_delete() {
        deckRepository.save(buildDeck("d1"));

        deckRepository.delete("d1");

        assertThat(deckRepository.findById("d1")).isEmpty();
    }

    // -------------------- FLASHCARD TESTS --------------------

    @Test
    void card_save_and_findById() {
        deckRepository.save(buildDeck("d1"));
        flashcardRepository.save(buildCard("c1", "d1"));

        Optional<Flashcard> result = flashcardRepository.findById("c1");

        assertThat(result).isPresent();
        assertThat(result.get().getDeckId()).isEqualTo("d1");
    }

    @Test
    void card_findByDeckId() {
        deckRepository.save(buildDeck("d1"));
        flashcardRepository.save(buildCard("c1", "d1"));
        flashcardRepository.save(buildCard("c2", "d1"));

        List<Flashcard> result = flashcardRepository.findByDeckId("d1");

        assertThat(result).hasSize(2);
    }

    @Test
    void card_update() {
        deckRepository.save(buildDeck("d1"));
        flashcardRepository.save(buildCard("c1", "d1"));

        Flashcard updated = buildCard("c1", "d1");
        updated.setFront("updated front");

        flashcardRepository.update(updated);

        assertThat(flashcardRepository.findById("c1").get().getFront())
                .isEqualTo("updated front");
    }

    @Test
    void card_delete() {
        deckRepository.save(buildDeck("d1"));
        flashcardRepository.save(buildCard("c1", "d1"));

        flashcardRepository.delete("c1");

        assertThat(flashcardRepository.findById("c1")).isEmpty();
    }

    @Test
    void deleteByDeckId() {
        deckRepository.save(buildDeck("d1"));
        flashcardRepository.save(buildCard("c1", "d1"));
        flashcardRepository.save(buildCard("c2", "d1"));

        flashcardRepository.deleteByDeckId("d1");

        assertThat(flashcardRepository.findByDeckId("d1")).isEmpty();
    }
}