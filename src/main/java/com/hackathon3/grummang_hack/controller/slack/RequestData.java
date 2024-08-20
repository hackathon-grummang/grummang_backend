package com.hackathon3.grummang_hack.controller.slack;

import lombok.Data;

@Data
public class RequestData {
    private String email;
    private int workespace_id;

    public RequestData() {
        this.email = null; // 기본값 설정
        this.workespace_id = -1; // 기본값 설정
    }
}
