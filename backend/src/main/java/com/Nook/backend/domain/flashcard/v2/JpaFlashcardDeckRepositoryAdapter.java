package com.Nook.backend.domain.flashcard.v2;

import com.Nook.backend.domain.flashcard.FlashcardDeck;
import com.Nook.backend.domain.flashcard.IFlashcardDeckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Profile("v2")
@Repository
@RequiredArgsConstructor
public class JpaFlashcardDeckRepositoryAdapter implements IFlashcardDeckRepository {

    private final JpaFlashcardDeckRepository jpa;

    @Override
    public FlashcardDeck save(FlashcardDeck deck) { return jpa.save(deck); }

    @Override
    public Optional<FlashcardDeck> findById(String id) { return jpa.findById(id); }

    @Override
    public List<FlashcardDeck> findByRoomId(String roomId) { return jpa.findByRoomId(roomId); }

    @Override
    public void update(FlashcardDeck deck) { jpa.save(deck); }

    @Override
    public void delete(String id) { jpa.deleteById(id); }
}