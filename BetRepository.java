package com.pippobet.repository;

import com.pippobet.model.Bet;
import org.bson.types.ObjectId;
import java.util.List;

public interface BetRepository
{
    List<Bet> findAllBets();
    Bet saveBet(Bet bet);
    void deleteBet(ObjectId id);
}
