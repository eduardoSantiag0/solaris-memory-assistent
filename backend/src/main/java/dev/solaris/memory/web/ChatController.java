package dev.solaris.memory.web;

import dev.solaris.memory.dto.ChatQueryRequest;
import dev.solaris.memory.dto.ChatQueryResponse;
import dev.solaris.memory.service.ChatQueryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatQueryService chatQueryService;

    public ChatController(ChatQueryService chatQueryService) {
        this.chatQueryService = chatQueryService;
    }

    @PostMapping("/query")
    public ChatQueryResponse query(@Valid @RequestBody ChatQueryRequest request) {
        return chatQueryService.answer(request);
    }
}
