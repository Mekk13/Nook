package com.Nook.backend.domain.flashcard;

import com.Nook.backend.domain.flashcard.dto.*;
import com.Nook.backend.domain.membership.IMembershipRepository;
import com.Nook.backend.domain.membership.MemberRole;
import com.Nook.backend.domain.membership.RoomMembership;
import com.Nook.backend.domain.room.IRoomRepository;
import com.Nook.backend.domain.room.Room;
import com.Nook.backend.exception.NotFoundException;
import com.Nook.backend.exception.UnauthorizedException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock IFlashcardDeckRepository deckRepository;
    @Mock IFlashcardRepository cardRepository;
    @Mock IMembershipRepository membershipRepository;
    @Mock IRoomRepository roomRepository;

    @InjectMocks
    FlashcardService service;


    private Room room() {
        return Room.builder()
                .id("room-1")
                .name("Room")
                .build();
    }

    private FlashcardDeck deck() {
        return FlashcardDeck.builder()
                .id("deck-1")
                .roomId("room-1")
                .creatorId("user-1")
                .name("Algorithms")
                .topic("Graphs")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Flashcard card() {
        return Flashcard.builder()
                .id("card-1")
                .deckId("deck-1")
                .creatorId("user-1")
                .front("Q")
                .back("A")
                .build();
    }

    private RoomMembership ownerMembership() {
        return RoomMembership.builder()
                .userId("mod")
                .roomId("room-1")
                .role(MemberRole.OWNER)
                .build();
    }


    @Test
    void getDecksInRoom_returnsDecks() {
        when(roomRepository.findById("room-1"))
                .thenReturn(Optional.of(room()));

        when(deckRepository.findByRoomId("room-1"))
                .thenReturn(List.of(deck()));

        when(cardRepository.findByDeckId("deck-1"))
                .thenReturn(List.of(card()));

        var result = service.getDecksInRoom("room-1");

        assertThat(result).hasSize(1);
    }

    @Test
    void getDecksInRoom_missingRoom_throws() {
        when(roomRepository.findById("room-1"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.getDecksInRoom("room-1")
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createDeck_success() {
        when(roomRepository.findById("room-1"))
                .thenReturn(Optional.of(room()));

        when(membershipRepository.existsByUserIdAndRoomId(
                "user-1","room-1"
        )).thenReturn(true);

        DeckCreateRequest req =
                new DeckCreateRequest("DSA","Trees");

        var result =
                service.createDeck("user-1","room-1",req);

        assertThat(result.name()).isEqualTo("DSA");

        verify(deckRepository).save(any());
    }

    @Test
    void createDeck_notMember_throws() {
        when(roomRepository.findById("room-1"))
                .thenReturn(Optional.of(room()));

        when(membershipRepository.existsByUserIdAndRoomId(
                "user-1","room-1"
        )).thenReturn(false);

        assertThatThrownBy(() ->
                service.createDeck(
                        "user-1",
                        "room-1",
                        new DeckCreateRequest("X","Y")
                )
        ).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateDeck_creatorAllowed() {
        when(deckRepository.findById("deck-1"))
                .thenReturn(Optional.of(deck()));

        when(cardRepository.findByDeckId("deck-1"))
                .thenReturn(List.of());

        var result =
                service.updateDeck(
                        "user-1",
                        "deck-1",
                        new DeckUpdateRequest(
                                "Updated",
                                "NewTopic"
                        )
                );

        assertThat(result.name())
                .isEqualTo("Updated");

        verify(deckRepository).update(any());
    }

    @Test
    void updateDeck_ownerAllowedEvenNotCreator() {
        FlashcardDeck deck = deck();
        deck.setCreatorId("someone-else");

        when(deckRepository.findById("deck-1"))
                .thenReturn(Optional.of(deck));

        when(membershipRepository.findByUserIdAndRoomId(
                "mod","room-1"
        )).thenReturn(Optional.of(ownerMembership()));

        when(cardRepository.findByDeckId("deck-1"))
                .thenReturn(List.of());

        service.updateDeck(
                "mod",
                "deck-1",
                new DeckUpdateRequest("X","Y")
        );

        verify(deckRepository).update(any());
    }

    @Test
    void deleteDeck_deletesCardsThenDeck() {
        when(deckRepository.findById("deck-1"))
                .thenReturn(Optional.of(deck()));

        service.deleteDeck("user-1","deck-1");

        verify(cardRepository)
                .deleteByDeckId("deck-1");

        verify(deckRepository)
                .delete("deck-1");
    }

    @Test
    void getCardsInDeck_returnsCards() {
        when(deckRepository.findById("deck-1"))
                .thenReturn(Optional.of(deck()));

        when(cardRepository.findByDeckId("deck-1"))
                .thenReturn(List.of(card()));

        assertThat(
                service.getCardsInDeck("deck-1")
        ).hasSize(1);
    }

    @Test
    void addCard_success() {
        when(deckRepository.findById("deck-1"))
                .thenReturn(Optional.of(deck()));

        when(membershipRepository.existsByUserIdAndRoomId(
                "user-1","room-1"
        )).thenReturn(true);

        var response =
                service.addCard(
                        "user-1",
                        "deck-1",
                        new CardCreateRequest(
                                "Question",
                                "Answer"
                        )
                );

        assertThat(response.front())
                .isEqualTo("Question");

        verify(cardRepository).save(any());
    }

    @Test
    void updateCard_creatorAllowed() {
        when(cardRepository.findById("card-1"))
                .thenReturn(Optional.of(card()));

        var response =
                service.updateCard(
                        "user-1",
                        "card-1",
                        new CardUpdateRequest(
                                "New Front",
                                "New Back"
                        )
                );

        assertThat(response.front())
                .isEqualTo("New Front");

        verify(cardRepository).update(any());
    }

    @Test
    void updateCard_ownerAllowed() {
        Flashcard card = card();
        card.setCreatorId("someone-else");

        when(cardRepository.findById("card-1"))
                .thenReturn(Optional.of(card));

        when(deckRepository.findById("deck-1"))
                .thenReturn(Optional.of(deck()));

        when(membershipRepository.findByUserIdAndRoomId(
                "mod","room-1"
        )).thenReturn(Optional.of(ownerMembership()));

        service.updateCard(
                "mod",
                "card-1",
                new CardUpdateRequest("F","B")
        );

        verify(cardRepository).update(any());
    }

    @Test
    void deleteCard_deletes() {
        when(cardRepository.findById("card-1"))
                .thenReturn(Optional.of(card()));

        service.deleteCard("user-1","card-1");

        verify(cardRepository).delete("card-1");
    }

    @Test
    void updateCard_notFound_throws() {
        when(cardRepository.findById("bad"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateCard(
                        "u",
                        "bad",
                        new CardUpdateRequest("a","b")
                )
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateDeck_nonModeratorForbidden() {
        FlashcardDeck deck = deck();
        deck.setCreatorId("other");

        RoomMembership member =
                RoomMembership.builder()
                        .userId("plain-user")
                        .roomId("room-1")
                        .role(MemberRole.MEMBER)
                        .build();

        when(deckRepository.findById("deck-1"))
                .thenReturn(Optional.of(deck));

        when(membershipRepository.findByUserIdAndRoomId(
                "plain-user","room-1"
        )).thenReturn(Optional.of(member));

        assertThatThrownBy(() ->
                service.updateDeck(
                        "plain-user",
                        "deck-1",
                        new DeckUpdateRequest("x","y")
                )
        ).isInstanceOf(UnauthorizedException.class);
    }
}