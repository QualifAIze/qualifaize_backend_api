package org.qualifaizebackendapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.qualifaizebackendapi.DTO.request.interview.CreateInterviewRequest;
import org.qualifaizebackendapi.mapper.InterviewMapper;
import org.qualifaizebackendapi.model.Document;
import org.qualifaizebackendapi.model.Interview;
import org.qualifaizebackendapi.model.User;
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
    public UUID createInterview(CreateInterviewRequest request) {
        Document documentToCreateTheInterview = pdfService.findDocumentByIdOrThrow(request.getDocumentId());
        User assignedToUser = userService.fetchUserOrThrow(request.getAssignedToUserId());
        User createdByUser = SecurityUtils.getCurrentUser();

        Interview interviewToSave = interviewMapper.toInterviewFromCreateInterviewRequest(request, documentToCreateTheInterview, assignedToUser, createdByUser);
        return interviewRepository.save(interviewToSave).getId();
    }
}
