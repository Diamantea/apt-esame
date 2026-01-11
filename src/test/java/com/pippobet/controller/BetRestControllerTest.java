package com.pippobet.controller;

import com.pippobet.dto.BetCreateDTO;
import com.pippobet.model.Bet;
import com.pippobet.service.BetService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class BetRestControllerTest {
    private BetRestController controller;
    private BetService service;


    @BeforeEach
    public void setUp() {
        this.service = Mockito.mock(BetService.class);
        this.controller = new BetRestController(service);
    }


    @Test
    void testGetAllEvents() {
        var bets = List.of(new Bet("home", "away", "X", 1.2));
        Mockito.when(service.findAll())
                .thenReturn(bets);

        var actual = controller.getAll();

        Assertions.assertEquals(bets, actual);
    }

    @Test
    void testCreateBet() {
        var betDTO = new BetCreateDTO("home", "away", "X", 1.8);
        var createdBet = new Bet("home", "away", "X", 1.8);
        Mockito.when(service.saveBet(betDTO))
                .thenReturn(createdBet);

        var actual = controller.createBet(betDTO);

        Assertions.assertEquals(createdBet, actual);
        Mockito.verify(service).saveBet(betDTO);
    }
}
