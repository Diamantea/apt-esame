package com.pippobet.repository;

import com.pippobet.model.Bet;
import java.util.List;

public interface BetRepository
{
    List<Bet> findAllBets();
}
