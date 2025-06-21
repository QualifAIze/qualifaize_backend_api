package org.qualifaizebackendapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.DTO.response.interview.ChangeInterviewStatusResponse;
import org.qualifaizebackendapi.DTO.response.interview.CreateInterviewResponse;
import org.qualifaizebackendapi.DTO.response.interview.question.QuestionToAsk;
import org.qualifaizebackendapi.DTO.response.interview.question.SubmitAnswerResponse;
import org.qualifaizebackendapi.exception.ErrorResponse;
import org.qualifaizebackendapi.model.enums.InterviewStatus;
import org.qualifaizebackendapi.service.InterviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Interview Operations", description = "Endpoint for interview process")
@RequiredArgsConstructor
@RequestMapping("/api/v1/interview")
@RestController
@Slf4j
public class InterviewController {

    private final InterviewService interviewService;

    @Operation(
            summary = "Create a new interview",
            description = "Creates a new interview based on an existing document and returns the interview ID"
    )
    @PostMapping
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Interview created successfully",
                    content = @Content(schema = @Schema(implementation = CreateInterviewResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters or validation errors",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Referenced document or assigned user not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions to create interview",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Interview with the same name already exists for this document",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<CreateInterviewResponse> createInterview(
            @Parameter(description = "Interview creation request containing all necessary information", required = true)
            @Valid @RequestBody CreateInterviewRequest request
    ) {
        log.info("Creating new interview with name: '{}' for document ID: {}",
                request.getName(), request.getDocumentId());

        CreateInterviewResponse response = interviewService.createInterview(request);
        log.info("Successfully created interview with ID: {} and name: '{}'", response.getInterviewId(), request.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @Operation(
            summary = "Change interview status",
            description = "Updates the status of an existing interview and returns the updated status along with the interview ID"
    )
    @GetMapping("/{interviewId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = ChangeInterviewStatusResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid interviewId or status value",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Interview not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions to change status",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<ChangeInterviewStatusResponse> changeInterviewStatus(
            @Parameter(description = "ID of the interview to update", required = true)
            @PathVariable UUID interviewId,
            @Parameter(description = "New status for the interview", required = true)
            @RequestParam InterviewStatus newStatus
    ) {
        log.info("Changing status of interview {} to {}", interviewId, newStatus);

        ChangeInterviewStatusResponse response = interviewService.updateInterviewStatus(interviewId, newStatus);

        log.info("Interview {} status changed to {}", interviewId, response.getInterviewStatus());

        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "Get next interview question",
            description = "Retrieves the next question to ask in the specified interview"
    )
    @GetMapping("/next/{interviewId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Next question retrieved successfully",
                    content = @Content(schema = @Schema(implementation = QuestionToAsk.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid interview ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Interview not found or no more questions available",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions to access interview",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<QuestionToAsk> getNextInterviewQuestion(
            @Parameter(description = "ID of the interview to get next question for", required = true)
            @PathVariable UUID interviewId
    ) {
        log.info("Getting next question for interview {}", interviewId);

        QuestionToAsk nextQuestion = interviewService.getNextInterviewQuestion(interviewId);

        log.info("Retrieved next question for interview {}: {}", interviewId, nextQuestion.getTitle());

        return ResponseEntity.ok(nextQuestion);
    }

    @Operation(
            summary = "Submit answer to interview question",
            description = "Submits an answer for a specific question and returns the result with feedback"
    )
    @GetMapping("/answer/{questionId}")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Answer submitted successfully",
                    content = @Content(schema = @Schema(implementation = SubmitAnswerResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid question ID or answer format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Question not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Insufficient permissions to submit answer",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<SubmitAnswerResponse> submitAnswer(
            @Parameter(description = "ID of the question being answered", required = true)
            @PathVariable UUID questionId,
            @Parameter(description = "The user's answer (A, a, B, b, C, c, D or d)", required = true)
            @RequestParam String correctAnswer
    ) {
        log.info("Submitting answer '{}' for question {}", correctAnswer, questionId);

        SubmitAnswerResponse response = interviewService.submitAnswer(questionId, correctAnswer);

        log.info("Answer submitted for question {}: {} (Correct: {})",
                questionId, response.getSubmittedAnswer(), response.isCorrect());

        return ResponseEntity.ok(response);
    }
}