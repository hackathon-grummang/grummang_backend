package com.hackathon3.grummang_hack.service.google;


import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.PermissionList;
import com.hackathon3.grummang_hack.model.mapper.google.DriveUserMapper;
import com.hackathon3.grummang_hack.repository.MonitoredUsersRepo;
import com.hackathon3.grummang_hack.repository.OrgSaaSRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DriveApiService {

    public FileList fetchFiles(Drive service, String DriveId){
        try {
            FileList result = service.files().list()
                    .setDriveId(DriveId)
                    .setIncludeItemsFromAllDrives(true)
                    .setSupportsAllDrives(true)
                    .setCorpora("drive")
                    .setQ("trashed = false")
                    .setFields("files")
                    .execute();
            return result;
        } catch (Exception e) {
            log.error("An error occurred while listing files: {}", e.getMessage(), e);
            return null;
        }
    }

    public PermissionList fetchUser(Drive service, String DriveId) {
        try {
            return service.permissions().list(DriveId)
                    .setSupportsAllDrives(true)
                    .setUseDomainAdminAccess(true)
                    .setFields("permissions(id,emailAddress,displayName,role)")
                    .execute();
        } catch (Exception e){
            log.error("An error occurred while listing users: {}", e.getMessage(), e);
            return null;
        }
    }
}
