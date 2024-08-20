package com.hackathon3.grummang_hack.service.webhook;

import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import com.hackathon3.grummang_hack.repository.OrgSaaSRepo;
import com.hackathon3.grummang_hack.service.slack.SlackApiService;
import com.hackathon3.grummang_hack.service.slack.SlackChannelService;
import com.hackathon3.grummang_hack.service.slack.SlackUtil;
import com.slack.api.model.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackChannelEvent {

    private final SlackChannelService slackChannelService;
    private final SlackApiService slackApiService;
    private final OrgSaaSRepo orgSaaSRepo;
    private final SlackUtil fileUtil;
    
    public void handleChannelEvent(Map<String, Object> payload) {
        log.info("Handling channel event");
        try {

            String spaceId = payload.get("teamId").toString();
            String channdlId = payload.get("channelId").toString();

            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceIdUsingQuery(spaceId).get();
            Conversation new_conversation = slackApiService.fetchConversationInfo(channdlId,orgSaaSObject);
            slackChannelService.addChannel(new_conversation,orgSaaSObject);
            log.info("Channel event processed successfully");
        } catch (Exception e) {
            log.error("Unexpected error processing channel event", e);
        }
    }
}
