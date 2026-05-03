package com.Nook.backend.domain.flashcard.v1;

import com.Nook.backend.domain.flashcard.FlashcardDeck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryFlashcardDeckRepositoryTest {

    private InMemoryFlashcardDeckRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFlashcardDeckRepository();
    }

    private FlashcardDeck buildDeck(
            String id,
            String roomId,
            String name
    ) {
        return FlashcardDeck.builder()
                .id(id)
                .roomId(roomId)
                .creatorId("user-1")
                .name(name)
                .topic("Algorithms")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void save_and_findById_returnsDeck() {
        FlashcardDeck deck =
                buildDeck("d1","room-1","Java Deck");

        repository.save(deck);

        var found = repository.findById("d1");

        assertThat(found).isPresent();
        assertThat(found.get().getName())
                .isEqualTo("Java Deck");
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        assertThat(
                repository.findById("missing")
        ).isEmpty();
    }

    @Test
    void findByRoomId_returnsOnlyMatchingDecks() {
        repository.save(buildDeck("d1","room-1","Deck A"));
        repository.save(buildDeck("d2","room-1","Deck B"));
        repository.save(buildDeck("d3","room-2","Deck C"));

        List<FlashcardDeck> result =
                repository.findByRoomId("room-1");

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(FlashcardDeck::getId)
                .containsExactlyInAnyOrder("d1","d2");
    }

    @Test
    void findByRoomId_whenNoMatches_returnsEmpty() {
        repository.save(
                buildDeck("d1","room-1","Deck")
        );

        assertThat(
                repository.findByRoomId("missing-room")
        ).isEmpty();
    }

    @Test
    void update_replacesExistingDeck() {
        repository.save(
                buildDeck("d1","room-1","Old Name")
        );

        FlashcardDeck updated =
                buildDeck("d1","room-1","New Name");

        repository.update(updated);

        assertThat(
                repository.findById("d1").get().getName()
        ).isEqualTo("New Name");
    }

    @Test
    void update_missingId_behavesLikeInsert() {
        FlashcardDeck deck =
                buildDeck("new","room-1","Created");

        repository.update(deck);

        assertThat(
                repository.findById("new")
        ).isPresent();
    }

    @Test
    void delete_removesDeck() {
        repository.save(
                buildDeck("d1","room-1","Deck")
        );

        repository.delete("d1");

        assertThat(
                repository.findById("d1")
        ).isEmpty();
    }

    @Test
    void delete_missingId_doesNothing() {
        repository.delete("ghost");

        assertThat(
                repository.findById("ghost")
        ).isEmpty();
    }
}