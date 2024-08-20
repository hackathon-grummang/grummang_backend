package com.hackathon3.grummang_hack.model.dto.file;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class EventIdRequest {

    private long orgId;
    private long eventId;

    public EventIdRequest(long orgId, long eventId){
        this.orgId = orgId;
        this.eventId = eventId;
    }
}
