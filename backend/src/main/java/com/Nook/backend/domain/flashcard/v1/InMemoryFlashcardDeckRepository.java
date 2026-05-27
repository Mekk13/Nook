package com.Nook.backend.domain.flashcard.v1;

import com.Nook.backend.domain.flashcard.FlashcardDeck;
import com.Nook.backend.domain.flashcard.IFlashcardDeckRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Profile("v1")
@Repository
public class InMemoryFlashcardDeckRepository implements IFlashcardDeckRepository {

    private final Map<String, FlashcardDeck> store = new ConcurrentHashMap<>();

    @Override
    public FlashcardDeck save(FlashcardDeck deck) {
        store.put(deck.getId(), deck);
        return deck;
    }

    @Override
    public Optional<FlashcardDeck> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<FlashcardDeck> findByRoomId(String roomId) {
        return store.values().stream()
                .filter(d -> d.getRoomId().equals(roomId))
                .toList();
    }

    @Override
    public void update(FlashcardDeck deck) {
        store.put(deck.getId(), deck);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }
}