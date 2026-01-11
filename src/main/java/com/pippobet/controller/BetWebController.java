package com.pippobet.controller;

import com.pippobet.service.BetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bets")
public class BetWebController {
    private static final String ATTRIBUTE_BETS = "bets";

    private final BetService betService;

    public BetWebController(BetService betService) {
        this.betService = betService;
    }

    @GetMapping
    public String viewBets(Model model) {
        model.addAttribute(ATTRIBUTE_BETS, betService.findAll());
        return ATTRIBUTE_BETS;
    }
}