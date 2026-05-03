package com.Nook.backend.domain.flashcard.v1;

import com.Nook.backend.domain.flashcard.Flashcard;
import com.Nook.backend.domain.flashcard.IFlashcardRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Profile("v1")
@Repository
public class InMemoryFlashcardRepository implements IFlashcardRepository {

    private final Map<String, Flashcard> store = new ConcurrentHashMap<>();

    @Override
    public Flashcard save(Flashcard card) {
        store.put(card.getId(), card);
        return card;
    }

    @Override
    public Optional<Flashcard> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Flashcard> findByDeckId(String deckId) {
        return store.values().stream()
                .filter(c -> c.getDeckId().equals(deckId))
                .toList();
    }

    @Override
    public void update(Flashcard card) {
        store.put(card.getId(), card);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public void deleteByDeckId(String deckId) {
        // removeIf iterates and removes matching entries in one go
        store.entrySet().removeIf(entry -> entry.getValue().getDeckId().equals(deckId));
    }
}