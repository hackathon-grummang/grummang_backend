package com.hackathon3.grummang_hack.service.register;

import com.hackathon3.grummang_hack.model.dto.OrgSaasRequest;
import com.hackathon3.grummang_hack.model.dto.OrgSaasResponse;
import com.hackathon3.grummang_hack.model.entity.Org;
import com.hackathon3.grummang_hack.model.entity.OrgSaaS;
import com.hackathon3.grummang_hack.model.entity.Saas;
import com.hackathon3.grummang_hack.model.entity.WorkspaceConfig;
import com.hackathon3.grummang_hack.repository.OrgRepo;
import com.hackathon3.grummang_hack.repository.OrgSaaSRepo;
import com.hackathon3.grummang_hack.repository.SaasRepo;
import com.hackathon3.grummang_hack.repository.WorkSpaceConfigRepo;
import com.slack.api.Slack;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrgSaasService {

    private final OrgRepo orgRepo;
    private final SaasRepo saasRepo;
    private final OrgSaaSRepo orgSaaSRepo;
    private final WorkSpaceConfigRepo workSpaceConfigRepo;
    private final SlackTeamInfo slackTeamInfo;

    @Autowired
    public OrgSaasService(OrgRepo orgRepo, SaasRepo saasRepo, OrgSaaSRepo orgSaaSRepo, WorkSpaceConfigRepo workSpaceConfigRepo, SlackTeamInfo slackTeamInfo) {
        this.orgRepo = orgRepo;
        this.saasRepo = saasRepo;
        this.orgSaaSRepo = orgSaaSRepo;
        this.workSpaceConfigRepo = workSpaceConfigRepo;
        this.slackTeamInfo = slackTeamInfo;
    }

    public OrgSaasResponse register(OrgSaasRequest orgSaasRequest) {
        OrgSaaS orgSaas = new OrgSaaS();
        WorkspaceConfig workspaceConfig = new WorkspaceConfig();

        if(orgSaasRequest.getSaasId() == 6) {
            Org org = orgRepo.findById(orgSaasRequest.getOrgId()).orElseThrow(() -> new RuntimeException("Org not found"));
            Saas saas = saasRepo.findById(orgSaasRequest.getSaasId()).orElseThrow(() -> new RuntimeException("SaaS not found"));

            String alias = orgSaasRequest.getAlias();
            String adminEmail = orgSaasRequest.getAdminEmail();
            String apiToken = orgSaasRequest.getApiToken();
            String webhookUrl = orgSaasRequest.getWebhookUrl();
            Timestamp ts = Timestamp.valueOf(LocalDateTime.now());

            orgSaas.setOrg(org);
            orgSaas.setSaas(saas);
            orgSaas.setSpaceId("TEMP");
            OrgSaaS regiOrgSaas = orgSaaSRepo.save(orgSaas);

            // ID 수동 설정 부분 제거
            // workspaceConfig.setId(regiOrgSaas.getId());

            workspaceConfig.setWorkspaceName("TEMP");
            workspaceConfig.setAlias(alias);
            workspaceConfig.setSaasAdminEmail(adminEmail);
            workspaceConfig.setToken("TEMP");
            workspaceConfig.setWebhook(webhookUrl);
            workspaceConfig.setRegisterDate(ts);

            workspaceConfig.setOrgSaas(regiOrgSaas);  // OrgSaaS 객체를 설정

            WorkspaceConfig regiWorkspace = workSpaceConfigRepo.save(workspaceConfig);

            return new OrgSaasResponse(200, "Waiting Google Drive", regiOrgSaas.getId(), regiWorkspace.getRegisterDate());
        }

        try {
            List<String> slackInfo = slackTeamInfo.getTeamInfo(orgSaasRequest.getApiToken());

            Org org = orgRepo.findById(orgSaasRequest.getOrgId()).orElseThrow(() -> new RuntimeException("Org not found"));
            Saas saas = saasRepo.findById(orgSaasRequest.getSaasId()).orElseThrow(() -> new RuntimeException("SaaS not found"));

            String spaceId = slackInfo.get(1);
            String spaceName = slackInfo.get(0);
            String alias = orgSaasRequest.getAlias();
            String adminEmail = orgSaasRequest.getAdminEmail();
            String apiToken = orgSaasRequest.getApiToken();
            String webhookUrl = orgSaasRequest.getWebhookUrl();
            Timestamp ts = Timestamp.valueOf(LocalDateTime.now());

            orgSaas.setOrg(org);
            orgSaas.setSaas(saas);
            orgSaas.setSpaceId(spaceId);
            OrgSaaS regiOrgSaas = orgSaaSRepo.save(orgSaas);

            // ID 수동 설정 부분 제거
            // workspaceConfig.setId(regiOrgSaas.getId());

            workspaceConfig.setWorkspaceName(spaceName);
            workspaceConfig.setAlias(alias);
            workspaceConfig.setSaasAdminEmail(adminEmail);
            workspaceConfig.setToken(apiToken);
            workspaceConfig.setWebhook(webhookUrl);
            workspaceConfig.setRegisterDate(ts);

            workspaceConfig.setOrgSaas(regiOrgSaas);  // OrgSaaS 객체를 설정

            workSpaceConfigRepo.save(workspaceConfig);

            return new OrgSaasResponse(200, null, regiOrgSaas.getId(), ts);
        } catch (Exception e ) {
            e.printStackTrace(); // 에러 로그 추가
            return new OrgSaasResponse(199, "Token Invalid, Nothing Stored", null, null);
        }
    }

    public OrgSaasResponse delete(OrgSaasRequest orgSaasRequest) {
        Optional<OrgSaaS> optionalOrgSaaS = orgSaaSRepo.findById(orgSaasRequest.getId());

        if (optionalOrgSaaS.isPresent()) {
            OrgSaaS orgSaaS = optionalOrgSaaS.get();

            orgSaaSRepo.delete(orgSaaS);

            return new OrgSaasResponse( 200, null, null,null);
        } else {
            return new OrgSaasResponse( 199, "Not found for ID", null,null);
        }
    }

    public void updateOrgSaasGD(List<String[]> drives, String accessToken) {
        List<OrgSaaS> tempOrgSaasList = orgSaaSRepo.findBySpaceId("TEMP");

        if (tempOrgSaasList.isEmpty()) {
            return;
        }

        // 드라이브 리스트 순회
        for (int i = 0; i < drives.size(); i++) {
            String[] driveInfo = drives.get(i);  // [0]: 드라이브 ID, [1]: 드라이브 이름

            // DELETE 상태인 드라이브 처리
            if ("DELETE".equals(driveInfo[0])) {
                // spaceId가 TEMP인 튜플 모두 삭제
                for (OrgSaaS orgSaas : tempOrgSaasList) {
                    orgSaaSRepo.delete(orgSaas);

                    Optional<WorkspaceConfig> optionalWorkspace = workSpaceConfigRepo.findById(orgSaas.getId());
                    optionalWorkspace.ifPresent(workSpaceConfigRepo::delete);
                }

                return;
            }

            OrgSaaS orgSaas;
            WorkspaceConfig workspace;

            if (i < tempOrgSaasList.size()) {
                // 기존 TEMP 튜플 업데이트
                orgSaas = tempOrgSaasList.get(i);
            } else {
                // TEMP 튜플을 복제
                OrgSaaS originalOrgSaas = tempOrgSaasList.get(0);  // 첫 번째 TEMP 튜플을 기준으로 복사
                orgSaas = new OrgSaaS();
                orgSaas.setOrg(originalOrgSaas.getOrg());
                orgSaas.setSaas(originalOrgSaas.getSaas());
                orgSaas.setSpaceId("TEMP");  // 나중에 업데이트될 것이므로 우선 TEMP로 설정
                orgSaas = orgSaaSRepo.save(orgSaas);  // 복제된 튜플 저장

                Optional<WorkspaceConfig> originalWorkspaceOpt = workSpaceConfigRepo.findById(originalOrgSaas.getId());
                if (originalWorkspaceOpt.isPresent()) {
                    WorkspaceConfig originalWorkspace = originalWorkspaceOpt.get();
                    workspace = new WorkspaceConfig();
                    workspace.setId(orgSaas.getId());
                    workspace.setAlias(originalWorkspace.getAlias());
                    workspace.setSaasAdminEmail(originalWorkspace.getSaasAdminEmail());
                    workspace.setToken(originalWorkspace.getToken());
                    workspace.setWebhook(originalWorkspace.getWebhook());
                    workspace.setRegisterDate(originalWorkspace.getRegisterDate());
                    workSpaceConfigRepo.save(workspace);
                } else {
                    continue;
                }
            }

            orgSaas.setSpaceId(driveInfo[0]);
            OrgSaaS saveOrgSaas = orgSaaSRepo.save(orgSaas);

            Optional<WorkspaceConfig> optionalWorkspace = workSpaceConfigRepo.findById(orgSaas.getId());
            if (optionalWorkspace.isPresent()) {
                workspace = optionalWorkspace.get();
                workspace.setWorkspaceName(driveInfo[1]);
                workspace.setToken(optionalWorkspace.get().getToken());
                workSpaceConfigRepo.save(workspace);
            }

//            rabbitTemplate.convertAndSend(rabbitMQConfig.getExchangeName(), rabbitMQConfig.getRoutingKey(), saveOrgSaas.getId());
        }
    }
}
