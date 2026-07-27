package com.example.shinhangaecheokja.onboarding.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OnboardingPageController {

  @GetMapping("/")
  public String showOnboarding() {
    return "forward:/onboarding/index.html";
  }
}
