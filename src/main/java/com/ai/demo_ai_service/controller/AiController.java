package com.ai.demo_ai_service.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ai.demo_ai_service.service.AiService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

  @Autowired
  private AiService aiService;

  @GetMapping("/chat")
  public String chat(@RequestParam String question) {
    return aiService.chat(question);
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> stream(@RequestParam String question) {
    return aiService.streamChat(question);
  }

  @GetMapping("/audit")
  public String audit(@RequestParam String content) {
    return aiService.audit(content);
  }

  @GetMapping("/analyze")
  public String analyze(@RequestParam String data,
      @RequestParam String question) {
    return aiService.analyzeData(data, question);
  }

  @GetMapping("/knowledge")
  public String knowledge(@RequestParam String knowledge,
      @RequestParam String question) {
    return aiService.knowledgeQA(knowledge, question);
  }
}

