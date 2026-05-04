package com.Nook.backend.domain.flashcard.v2;

import com.Nook.backend.domain.flashcard.Flashcard;
import com.Nook.backend.domain.flashcard.IFlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Profile("v2")
@Repository
@RequiredArgsConstructor
public class JpaFlashcardRepositoryAdapter implements IFlashcardRepository {

    private final JpaFlashcardRepository jpa;

    @Override
    public Flashcard save(Flashcard card) { return jpa.save(card); }

    @Override
    public Optional<Flashcard> findById(String id) { return jpa.findById(id); }

    @Override
    public List<Flashcard> findByDeckId(String deckId) { return jpa.findByDeckId(deckId); }

    @Override
    public void update(Flashcard card) { jpa.save(card); }

    @Override
    public void delete(String id) { jpa.deleteById(id); }

    @Override
    @Transactional
    public void deleteByDeckId(String deckId) { jpa.deleteByDeckId(deckId); }
}