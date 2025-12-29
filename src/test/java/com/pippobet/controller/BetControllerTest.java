package com.pippobet.controller;

import com.pippobet.model.Bet;
import com.pippobet.repository.BetRepository;
import com.pippobet.view.BetView;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class BetControllerTest
{
    private BetController controller;
    private BetView view;
    private BetRepository repository;


    @BeforeEach
    public void setUp()
    {
        this.repository = Mockito.mock(BetRepository.class);
        this.view = Mockito.mock(BetView.class);
        this.controller = new BetController(repository, view);
    }


    @Test
    void testGetAllEvents()
    {
        var bets = List.of(new Bet("home", "away", "X", 1.2));
        Mockito.when(repository.findAllBets())
            .thenReturn(bets);

        controller.showAllBets();

        Mockito.verify(view).updateBets(bets);
    }


    @Test
    void testAddNewBet()
    {
        var bet = new Bet("home", "away", "X", 1.2);
        Mockito.when(repository.findAllBets())
            .thenReturn(List.of(bet));

        controller.addBet(bet);

        Mockito.verify(repository).save(bet);
        Mockito.verify(repository).findAllBets();
        Mockito.verify(view).updateBets(List.of(bet));
    }


    @Test
    void testDeleteBet()
    {
        var bet = new Bet("home", "away", "X", 1.2);
        Mockito.when(repository.findAllBets())
            .thenReturn(List.of());

        controller.deleteBet(bet);

        Mockito.verify(repository).delete(bet);
        Mockito.verify(repository).findAllBets();
        Mockito.verify(view).updateBets(List.of());
    }
}
