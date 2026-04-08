package com.truckoptimization.task.controller;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.truckoptimization.common.security.UserAccountService;
import com.truckoptimization.dto.database.nosql.feature.compiledRouteResult.CompiledRouteResultDocument;
import com.truckoptimization.dto.database.nosql.feature.compiledRouteResult.CompiledRouteResultService;
import com.truckoptimization.dto.database.nosql.feature.routeResult.RouteResultDocument;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
public class HomeController {
    //controller to handle basic requests

    private static final DateTimeFormatter HISTORY_DATE_FORMAT = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final UserAccountService userAccountService;
    private final CompiledRouteResultService compiledRouteResultService;

    public HomeController(UserAccountService userAccountService,
            CompiledRouteResultService compiledRouteResultService) {
        this.userAccountService = userAccountService;
        this.compiledRouteResultService = compiledRouteResultService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/history")
    public String history(Model model) {
        List<HistoryItem> historyItems = compiledRouteResultService.findAllDocuments().stream()
                .map(this::toHistoryItem)
                .toList();

        model.addAttribute("historyItems", historyItems);
        return "history";
    }

    @PostMapping("/signup")
    public String register(@RequestParam("username") String username,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {
        try {
            userAccountService.registerUser(username, password);
            redirectAttributes.addFlashAttribute("signupSuccess", "Account created. Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("signupError", exception.getMessage());
            return "redirect:/signup";
        }
    }

    private HistoryItem toHistoryItem(CompiledRouteResultDocument document) {
        List<RouteResultDocument> routeResults = document.getResults() == null ? List.of() : document.getResults();
        int routeCount = routeResults.size();
        long totalDistance = routeResults.stream().mapToLong(RouteResultDocument::getTotalDistance).sum();
        int totalStops = routeResults.stream()
                .map(RouteResultDocument::getRoute)
                .filter(route -> route != null)
                .mapToInt(List::size)
                .sum();

        String createdAt = document.getCreatedAt() == null
                ? "Unknown"
                : HISTORY_DATE_FORMAT.format(document.getCreatedAt());

        return new HistoryItem(document.getId(), createdAt, routeCount, totalStops, totalDistance);
    }

    public record HistoryItem(String id, String createdAt, int routeCount, int totalStops, long totalDistance) {
    }


}
