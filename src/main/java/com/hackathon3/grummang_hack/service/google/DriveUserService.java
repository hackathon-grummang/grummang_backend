package com.hackathon3.grummang_hack.service.google;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.hackathon3.grummang_hack.model.entity.MonitoredUsers;
import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import com.hackathon3.grummang_hack.model.mapper.google.DriveUserMapper;
import com.hackathon3.grummang_hack.repository.MonitoredUsersRepo;
import com.hackathon3.grummang_hack.repository.OrgSaaSRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class DriveUserService {

    private final GoogleUtilService googleUtilService;
    private final OrgSaaSRepo orgSaaSRepo;
    private final DriveApiService driveApiService;
    private final DriveUserMapper driveUserMapper;
    private final MonitoredUsersRepo monitoredUserRepo;

    @Autowired
    public DriveUserService(GoogleUtilService googleUtilService, OrgSaaSRepo orgSaaSRepo,
                            DriveApiService driveApiService, DriveUserMapper driveUserMapper,
                            MonitoredUsersRepo monitoredUserRepo) {
        this.googleUtilService = googleUtilService;
        this.orgSaaSRepo = orgSaaSRepo;
        this.driveApiService = driveApiService;
        this.driveUserMapper = driveUserMapper;
        this.monitoredUserRepo = monitoredUserRepo;
    }

    @Transactional
    public CompletableFuture<Void> fetchUser(int workspaceId) {
        return CompletableFuture.runAsync(() -> {
            try {
                Drive service = googleUtilService.getDriveService(workspaceId);
                OrgSaaS orgSaaSObject = orgSaaSRepo.findById(workspaceId).orElse(null);

                String spaceId = orgSaaSRepo.getSpaceID(workspaceId);
                PermissionList permissionList = driveApiService.fetchUser(service, spaceId);

                if (permissionList != null && !permissionList.isEmpty()) {
                    for (Permission permission : permissionList.getPermissions()) {
                        if (permission.getEmailAddress() != null) {
                            MonitoredUsers user = driveUserMapper.toEntity(permission, orgSaaSObject);
                            if (!monitoredUserRepo.existsByUserId(permission.getId(), Objects.requireNonNull(orgSaaSObject).getId())) {
                                monitoredUserRepo.save(user);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("An error occurred while fetching users: {}", e.getMessage(), e);
            }
        });
    }
}
