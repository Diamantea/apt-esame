package com.pippobet.controller;

import com.pippobet.dto.BetCreateDTO;
import com.pippobet.service.BetService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping
    public String addBet(BetCreateDTO betCreateDTO, RedirectAttributes redirectAttributes) {
        betService.saveBet(betCreateDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Bet added successfully!");
        return "redirect:/bets";
    }

    @PostMapping("/{id}/delete")
    public String deleteBet(@PathVariable String id, RedirectAttributes redirectAttributes) {
        betService.deleteBet(new ObjectId(id));
        redirectAttributes.addFlashAttribute("successMessage", "Bet deleted successfully!");
        return "redirect:/bets";
    }
}