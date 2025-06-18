package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.DTO.response.interview.ChangeInterviewStatusResponse;
import org.qualifaizebackendapi.DTO.response.interview.CreateInterviewResponse;
import org.qualifaizebackendapi.exception.ResourceNotFoundException;
import org.qualifaizebackendapi.mapper.InterviewMapper;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.User;
import org.qualifaizebackendapi.model.enums.InterviewStatus;
import org.qualifaizebackendapi.repository.InterviewRepository;
import org.qualifaizebackendapi.service.InterviewService;
import org.qualifaizebackendapi.service.PdfService;
import org.qualifaizebackendapi.service.UserService;
import org.qualifaizebackendapi.utils.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final PdfService pdfService;
    private final UserService userService;
    private final InterviewRepository interviewRepository;
    private final InterviewMapper interviewMapper;

    @Override
    public CreateInterviewResponse createInterview(CreateInterviewRequest request) {
        Document documentToCreateTheInterview = pdfService.findDocumentByIdOrThrow(request.getDocumentId());
        User assignedToUser = userService.fetchUserOrThrow(request.getAssignedToUserId());
        User createdByUser = SecurityUtils.getCurrentUser();

        Interview interviewToSave = interviewMapper.toInterviewFromCreateInterviewRequest(request, documentToCreateTheInterview, assignedToUser, createdByUser);
        return new CreateInterviewResponse(interviewRepository.save(interviewToSave).getId());
    }

    @Override
    public ChangeInterviewStatusResponse updateInterviewStatus(UUID interviewId, InterviewStatus newStatus) {
        Interview interviewToUpdate = this.fetchInterviewOrThrow(interviewId);
        interviewToUpdate.setStatus(newStatus);
        Interview updatedInterview = interviewRepository.save(interviewToUpdate);
        return new ChangeInterviewStatusResponse(updatedInterview.getId(), updatedInterview.getStatus());
    }

    public Interview fetchInterviewOrThrow(UUID interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Interview with Id %s was now found!", interviewId)
                ));
    }
}
