package com.pippobet.controller;

import com.pippobet.model.Bet;
import com.pippobet.service.BetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BetWebController.class)
class BetWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BetService betService;

    private List<Bet> testBets;

    @BeforeEach
    void setUp() {
        testBets = new ArrayList<>();
        testBets.add(new Bet("Manchester United", "Liverpool", "1", 1.95));
        testBets.add(new Bet("Chelsea", "Arsenal", "X", 3.50));
        testBets.add(new Bet("Manchester City", "Tottenham", "2", 4.00));
    }

    @Test
    void testViewBets_ReturnsCorrectViewName() throws Exception {
        when(betService.findAll()).thenReturn(testBets);

        mockMvc.perform(get("/bets"))
                .andExpect(status().isOk())
                .andExpect(view().name("bets"))
                .andExpect(model().attribute("bets", testBets));
    }

    @Test
    void testAddBet_SuccessfullyAddsAndRedirects() throws Exception {
        mockMvc.perform(post("/bets")
                .param("homeTeam", "Manchester United")
                .param("awayTeam", "Liverpool")
                .param("outcome", "1")
                .param("odd", "1.95"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bets"));

        verify(betService).saveBet(any());
    }
}