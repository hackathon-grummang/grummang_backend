package com.hackathon3.grummang_hack.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class Result {
    private boolean success;
    private String message;

    @Builder
    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}

