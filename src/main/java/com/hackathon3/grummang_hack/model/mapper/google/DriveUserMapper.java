package com.hackathon3.grummang_hack.model.mapper.google;


import com.google.api.services.drive.model.Permission;
import com.hackathon3.grummang_hack.model.entity.MonitoredUsers;
import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
@RequiredArgsConstructor
public class DriveUserMapper {
    public MonitoredUsers toEntity(Permission permission, OrgSaaS orgSaaS) {
        return MonitoredUsers.builder()
                .userId(permission.getId())
                .orgSaaS(orgSaaS)
                .email(permission.getEmailAddress())
                .userName(permission.getDisplayName())
                .timestamp(new Timestamp(System.currentTimeMillis()))
                .build();
    }
}
