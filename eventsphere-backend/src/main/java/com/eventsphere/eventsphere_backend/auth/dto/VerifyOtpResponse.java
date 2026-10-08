package com.eventsphere.eventsphere_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpResponse {

    private String message;

    private boolean verified;

    private boolean nextStepRequired;
}