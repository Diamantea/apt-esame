package com.pippobet.service;

import com.pippobet.dto.BetCreateDTO;
import com.pippobet.model.Bet;
import com.pippobet.repository.BetRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class BetServiceTest {
    private BetRepository betRepository;
    private BetService betService;

    @BeforeEach
    public void setUp() {
        this.betRepository = Mockito.mock(BetRepository.class);
        this.betService = new BetService(betRepository);
    }

    @Test
    void testFindAllWithEmptyListShouldReturnEmptyList() {
        Mockito.when(betRepository.findAllBets()).thenReturn(List.of());

        var actualBets = betService.findAll();

        var expectedBets = List.of();
        Assertions.assertEquals(expectedBets, actualBets);
    }

    @Test
    void testFindAllWithMultipleBetsShouldReturnAllBets() {
        var bet1 = new Bet("Home1", "Away1", "1", 2.0);
        var bet2 = new Bet("Home2", "Away2", "X", 3.5);
        var expectedBets = List.of(bet1, bet2);
        Mockito.when(betRepository.findAllBets()).thenReturn(expectedBets);

        var actualBets = betService.findAll();

        Assertions.assertEquals(expectedBets, actualBets);
    }

    @Test
    void testCreateBetFromDTOShouldSave() {
        var betDTO = new BetCreateDTO("Manchester", "Liverpool", "1", 2.5);
        var expectedBet = new Bet("Manchester", "Liverpool", "1", 2.5);
        Mockito.when(betRepository.saveBet(Mockito.any(Bet.class))).thenReturn(expectedBet);

        var actualBet = betService.saveBet(betDTO);

        Assertions.assertEquals(expectedBet, actualBet);
    }

    @Test
    void testDeleteBetShouldCallRepository() {
        var betId = new ObjectId();

        betService.deleteBet(betId);

        Mockito.verify(betRepository).deleteBet(betId);
    }
}