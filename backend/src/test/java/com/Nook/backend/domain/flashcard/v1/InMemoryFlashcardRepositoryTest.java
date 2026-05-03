package com.Nook.backend.domain.flashcard.v1;

import com.Nook.backend.domain.flashcard.Flashcard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryFlashcardRepositoryTest {

    private InMemoryFlashcardRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryFlashcardRepository();
    }

    private Flashcard buildCard(
            String id,
            String deckId,
            String front
    ) {
        return Flashcard.builder()
                .id(id)
                .deckId(deckId)
                .front(front)
                .back("Answer")
                .creatorId("user-1")
                .build();
    }

    @Test
    void save_and_findById_returnsCard() {
        Flashcard card =
                buildCard("c1","deck-1","Question");

        repository.save(card);

        var found = repository.findById("c1");

        assertThat(found).isPresent();
        assertThat(found.get().getFront())
                .isEqualTo("Question");
    }

    @Test
    void findById_whenMissing_returnsEmpty() {
        assertThat(
                repository.findById("missing")
        ).isEmpty();
    }

    @Test
    void findByDeckId_returnsOnlyMatchingCards() {
        repository.save(buildCard("c1","deck-1","Q1"));
        repository.save(buildCard("c2","deck-1","Q2"));
        repository.save(buildCard("c3","deck-2","Q3"));

        List<Flashcard> result =
                repository.findByDeckId("deck-1");

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(Flashcard::getId)
                .containsExactlyInAnyOrder("c1","c2");
    }

    @Test
    void findByDeckId_whenNoMatches_returnsEmpty() {
        repository.save(
                buildCard("c1","deck-1","Q1")
        );

        assertThat(
                repository.findByDeckId("ghost")
        ).isEmpty();
    }

    @Test
    void update_replacesExistingCard() {
        repository.save(
                buildCard("c1","deck-1","Old")
        );

        Flashcard updated =
                buildCard("c1","deck-1","New");

        repository.update(updated);

        assertThat(
                repository.findById("c1")
                        .get()
                        .getFront()
        ).isEqualTo("New");
    }

    @Test
    void update_missingId_behavesLikeInsert() {
        Flashcard card =
                buildCard("new","deck-1","Created");

        repository.update(card);

        assertThat(
                repository.findById("new")
        ).isPresent();
    }

    @Test
    void delete_removesCard() {
        repository.save(
                buildCard("c1","deck-1","Q")
        );

        repository.delete("c1");

        assertThat(
                repository.findById("c1")
        ).isEmpty();
    }

    @Test
    void delete_missingId_doesNothing() {
        repository.delete("ghost");

        assertThat(
                repository.findById("ghost")
        ).isEmpty();
    }

    @Test
    void deleteByDeckId_removesOnlyCardsFromMatchingDeck() {
        repository.save(buildCard("c1","deck-1","Q1"));
        repository.save(buildCard("c2","deck-1","Q2"));
        repository.save(buildCard("c3","deck-2","Q3"));

        repository.deleteByDeckId("deck-1");

        assertThat(
                repository.findByDeckId("deck-1")
        ).isEmpty();

        assertThat(
                repository.findByDeckId("deck-2")
        ).hasSize(1);
    }

    @Test
    void deleteByDeckId_whenNoMatches_doesNothing() {
        repository.save(
                buildCard("c1","deck-1","Q1")
        );

        repository.deleteByDeckId("missing");

        assertThat(
                repository.findByDeckId("deck-1")
        ).hasSize(1);
    }
}