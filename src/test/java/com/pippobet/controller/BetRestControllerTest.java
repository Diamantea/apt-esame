package com.pippobet.controller;

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
}
