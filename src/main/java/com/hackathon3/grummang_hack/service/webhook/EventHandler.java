package com.hackathon3.grummang_hack.service.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventHandler {

    private final SlackFileEvent slackFileEvent;
    private final SlackChannelEvent slackChannelEvent;
    private final SlackUserEvent slackUserEvent;




}
