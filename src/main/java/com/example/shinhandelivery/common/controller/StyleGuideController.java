package com.example.shinhandelivery.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StyleGuideController {

  @GetMapping("/style-guide.html")
  public String styleGuide() {
    return "style-guide";
  }
}
