package com.hackathon3.grummang_hack.service.slack;



import com.hackathon3.grummang_hack.model.entity.ChannelList;
import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import com.hackathon3.grummang_hack.model.mapper.slack.SlackChannelMapper;
import com.hackathon3.grummang_hack.model.mapper.slack.SlackFileMapper;
import com.hackathon3.grummang_hack.repository.ChannelListRepo;
import com.hackathon3.grummang_hack.repository.OrgSaaSRepo;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackChannelService {
    private final SlackApiService slackApiService;
    private final SlackChannelMapper slackChannelMapper;
    private final SlackFileMapper slackFileMapper;
    private final ChannelListRepo slackChannelRepository;
    private final OrgSaaSRepo orgSaaSRepo;
    

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Void> slackFirstChannels(int workspace_config_id) {
        return CompletableFuture.runAsync(() -> {
            try {
                OrgSaaS orgSaaS = orgSaaSRepo.findById(workspace_config_id).orElse(null); //흠..
                List<Conversation> conversations = slackApiService.fetchConversations(workspace_config_id);
                List<ChannelList> slackChannels = slackChannelMapper.toEntity(conversations, orgSaaS);

                // 중복된 channel_id를 제외하고 저장할 채널 목록 생성
                List<ChannelList> filteredChannels = slackChannels.stream()
                        .filter(channel -> !slackChannelRepository.existsByChannelId(channel.getChannelId()))
                        .collect(Collectors.toList());

                slackChannelRepository.saveAll(filteredChannels);
            } catch (IOException | SlackApiException e) {
                log.error("Error fetching conversations", e);
            }
        });
    }

    @Async("threadPoolTaskExecutor")
    public CompletableFuture<Void> addChannel(Conversation conversation, OrgSaaS orgSaaS) {
        return CompletableFuture.runAsync(() -> {
                ChannelList channel = slackChannelMapper.toEntity(conversation, orgSaaS);
            if (!slackChannelRepository.existsByChannelId(channel.getChannelId())) {
                slackChannelRepository.save(channel);
            }
        });
    }
}
