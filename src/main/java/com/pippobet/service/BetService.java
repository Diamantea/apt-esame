package com.pippobet.service;

import com.pippobet.model.Bet;
import com.pippobet.repository.BetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BetService {
    private final BetRepository betRepository;

    public BetService(BetRepository betRepository) {
        this.betRepository = betRepository;
    }
    public List<Bet> findAll() {
        return betRepository.findAllBets();
    }
}
