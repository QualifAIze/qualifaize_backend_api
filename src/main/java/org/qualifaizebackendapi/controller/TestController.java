package org.qualifaizebackendapi.controller;

import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.repository.InterviewRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final InterviewRepository interviewRepository;

    @GetMapping()
    public UUID getData(){
        List<String> data = interviewRepository.findQuestionTitlesByInterviewId(UUID.fromString("9d858b8a-d7fb-4569-882b-e7cd2cafcc45"));
        return UUID.randomUUID();
    }

}
