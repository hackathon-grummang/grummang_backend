package com.hackathon3.grummang_hack.controller.slack;

import lombok.Data;

@Data
public class InitRequestData {
    private int workspace_id;

    public InitRequestData() {
        this.workspace_id = -1; // 기본값 설정
    }
}
