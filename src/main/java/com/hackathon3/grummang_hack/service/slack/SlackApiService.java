package com.hackathon3.grummang_hack.service.slack;


import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.files.FilesListResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.File;
import com.slack.api.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class SlackApiService {

    private final Slack slack;
    private final SlackUtil slackUtil;
    private String token;


    @Autowired
    public SlackApiService(SlackUtil slackUtil) {
        this.slack = Slack.getInstance();
        this.slackUtil = slackUtil;
    }

    // ConversationsList API호출 메서드
    public List<Conversation> fetchConversations(int workspaceId) throws IOException, SlackApiException {
        token = slackUtil.getToken(workspaceId);
        ConversationsListResponse conversationsResponse = slack.methods(token).conversationsList(r -> r);
        if (conversationsResponse.isOk()) {
            return conversationsResponse.getChannels();
        } else {
            throw new RuntimeException("Error fetching conversations: " + conversationsResponse.getError());
        }
    }

    // users.list API호출 메서드
    public List<User> fetchUsers(int workspaceId) throws IOException, SlackApiException {
        token = slackUtil.getToken(workspaceId);
        UsersListResponse usersListResponse = slack.methods(token).usersList(r -> r);
        if (usersListResponse.isOk()) {
            return usersListResponse.getMembers();
        } else {
            throw new RuntimeException("Error fetching users: " + usersListResponse.getError());
        }
    }
    // files.list API호출 메서드
    public List<File> fetchFiles(int workspaceId) throws IOException, SlackApiException {
        token = slackUtil.getToken(workspaceId);
        FilesListResponse filesListResponse = slack.methods(token).filesList(r -> r);
        if (filesListResponse.isOk()) {
            return filesListResponse.getFiles();
        } else {
            throw new RuntimeException("Error fetching files: " + filesListResponse.getError());
        }
    }

    // files.info API호출 메서드
    public File fetchFileInfo(String fileId, int workspaceId) throws IOException, SlackApiException {
        token = slackUtil.getToken(workspaceId);
        com.slack.api.methods.response.files.FilesInfoResponse filesInfoResponse = slack.methods(token).filesInfo(r -> r.file(fileId));
        if (filesInfoResponse.isOk()) {
            return filesInfoResponse.getFile();
        } else {
            throw new RuntimeException("Error fetching file info: " + filesInfoResponse.getError());
        }
    }

    // conversations.info API호출 메서드
    public Conversation fetchConversationInfo(String channelId, OrgSaaS orgSaaSObject) throws IOException, SlackApiException {
        token = slackUtil.tokenSelector(orgSaaSObject);
        com.slack.api.methods.response.conversations.ConversationsInfoResponse conversationsInfoResponse = slack.methods(token).conversationsInfo(r -> r.channel(channelId));
        if (conversationsInfoResponse.isOk()) {
            return conversationsInfoResponse.getChannel();
        } else {
            throw new RuntimeException("Error fetching conversation info: " + conversationsInfoResponse.getError());
        }
    }

    // users.info API호출 메서드
    public User fetchUserInfo(String userId, OrgSaaS orgSaaSObject) throws IOException, SlackApiException {
        token = slackUtil.tokenSelector(orgSaaSObject);
        com.slack.api.methods.response.users.UsersInfoResponse usersInfoResponse = slack.methods(token).usersInfo(r -> r.user(userId));
        if (usersInfoResponse.isOk()) {
            return usersInfoResponse.getUser();
        } else {
            throw new RuntimeException("Error fetching user info: " + usersInfoResponse.getError());
        }
    }
}
