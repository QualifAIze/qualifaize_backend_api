package org.qualifaizebackendapi.DTO.response.interview;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.qualifaizebackendapi.model.enums.InterviewStatus;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after changing an interview's status")
public class ChangeInterviewStatusResponse {

    @Schema(description = "The ID of the interview", example = "d290f1ee-6c54-4b01-90e6-d701748f0851")
    private UUID interviewId;

    @Schema(description = "The updated status of the interview", example = "COMPLETED")
    private InterviewStatus interviewStatus;
}