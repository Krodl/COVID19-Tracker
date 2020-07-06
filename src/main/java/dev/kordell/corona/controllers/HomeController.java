package dev.kordell.corona.controllers;

import dev.kordell.corona.models.LocationStats;
import dev.kordell.corona.services.CoronaVirusDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    CoronaVirusDataService coronaVirusDataService;

    @GetMapping("/")
    public String home(Model model) {
        List<LocationStats> allStats = coronaVirusDataService.getAllStats();
        int totalCases = allStats.stream().mapToInt(stat -> stat.getLatestReportedCases()).sum();
        model.addAttribute("locationStats", allStats);
        model.addAttribute("totalCases", totalCases);

        return "home";
    }
}
