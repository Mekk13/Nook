package com.Nook.backend.domain.flashcard;

import com.Nook.backend.auth.SecurityUtils;
import com.Nook.backend.domain.flashcard.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardControllerTest {

    @Mock FlashcardService service;
    @InjectMocks FlashcardController controller;

    @Test
    void getDecks() {
        when(service.getDecksInRoom("r1"))
                .thenReturn(List.of(mock(DeckResponse.class)));

        var res = controller.getDecksInRoom("r1");

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void createDeck() {
        when(service.createDeck(any(), any(), any()))
                .thenReturn(mock(DeckResponse.class));

        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn("u1");

            var res = controller.createDeck(
                    "r1",
                    mock(DeckCreateRequest.class)
            );

            assertThat(res.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
        }
    }

    @Test
    void updateDeck() {
        when(service.updateDeck(any(), any(), any()))
                .thenReturn(mock(DeckResponse.class));

        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn("u1");

            var res = controller.updateDeck(
                    "d1",
                    mock(DeckUpdateRequest.class)
            );

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void deleteDeck() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn("u1");

            var res = controller.deleteDeck("d1");

            assertThat(res.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Test
    void getCards() {
        when(service.getCardsInDeck("d1"))
                .thenReturn(List.of(mock(CardResponse.class)));

        var res = controller.getCardsInDeck("d1");

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void addCard() {
        when(service.addCard(any(), any(), any()))
                .thenReturn(mock(CardResponse.class));

        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn("u1");

            var res = controller.addCard(
                    "d1",
                    mock(CardCreateRequest.class)
            );

            assertThat(res.getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
        }
    }

    @Test
    void updateCard() {
        when(service.updateCard(any(), any(), any()))
                .thenReturn(mock(CardResponse.class));

        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn("u1");

            var res = controller.updateCard(
                    "c1",
                    mock(CardUpdateRequest.class)
            );

            assertThat(res.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void deleteCard() {
        try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
            sec.when(SecurityUtils::getCurrentUserId).thenReturn("u1");

            var res = controller.deleteCard("c1");

            assertThat(res.getStatusCode())
                    .isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}