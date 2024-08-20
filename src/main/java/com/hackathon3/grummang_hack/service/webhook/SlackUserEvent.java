package com.hackathon3.grummang_hack.service.webhook;


import com.hackathon3.grummang_hack.model.dto.slack.event.SlackUserJoinedEventDto;
import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import com.hackathon3.grummang_hack.repository.OrgSaaSRepo;
import com.hackathon3.grummang_hack.service.slack.SlackApiService;
import com.hackathon3.grummang_hack.service.slack.SlackUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackUserEvent {

    
    private final SlackApiService slackApiService;
    private final SlackUserService slackUserService;
    private final OrgSaaSRepo orgSaaSRepo;

    public void handleUserEvent(SlackUserJoinedEventDto dto) {
        log.info("Handling user event");
        try {
            dto.getTeamId();
            String spaceId = dto.getTeamId();
            String userId = dto.getJoinedUserId();
            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceIdUsingQuery(spaceId).get();
            slackUserService.addUser(slackApiService.fetchUserInfo(userId,orgSaaSObject));
            log.info("User event processed successfully");
        } catch (Exception e) {
            log.error("Unexpected error processing user event", e);
        }
    }
}
