package com.pippobet.controller;

import com.pippobet.model.Bet;
import com.pippobet.service.BetService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/bets")
public class BetRestController {
    private BetService betService;

	public BetRestController(BetService betService) {
        this.betService = betService;
    }

	@GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<Bet> getAll() {
		return betService.findAll();
	}
}
