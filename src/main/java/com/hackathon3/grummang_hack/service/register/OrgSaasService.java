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
import com.hackathon3.grummang_hack.service.util.AESUtil;
import com.slack.api.Slack;
import org.hibernate.jdbc.Work;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrgSaasService {

    private final OrgRepo orgRepo;
    private final StartScan startScan;
    private final SaasRepo saasRepo;
    private final OrgSaaSRepo orgSaaSRepo;
    private final WorkSpaceConfigRepo workSpaceConfigRepo;
    private final SlackTeamInfo slackTeamInfo;
    private final RabbitTemplate initRabbitTemplate;

//    @Value("${aes.key}")
//    private String aesKey;

    @Autowired
    public OrgSaasService(OrgRepo orgRepo, StartScan startScan, SaasRepo saasRepo, OrgSaaSRepo orgSaaSRepo, WorkSpaceConfigRepo workSpaceConfigRepo, SlackTeamInfo slackTeamInfo, RabbitTemplate initRabbitTemplate) {
        this.orgRepo = orgRepo;
        this.startScan = startScan;
        this.saasRepo = saasRepo;
        this.orgSaaSRepo = orgSaaSRepo;
        this.workSpaceConfigRepo = workSpaceConfigRepo;
        this.slackTeamInfo = slackTeamInfo;
        this.initRabbitTemplate = initRabbitTemplate;
    }

    public OrgSaasResponse register(OrgSaasRequest orgSaasRequest) {
        OrgSaaS orgSaas = new OrgSaaS();
        WorkspaceConfig workspaceConfig = new WorkspaceConfig();

        if(orgSaasRequest.getSaasId() == 6) {
            Org org = orgRepo.findById(orgSaasRequest.getOrgId()).orElseThrow(() -> new RuntimeException("Org not found"));
            Saas saas = saasRepo.findById(orgSaasRequest.getSaasId()).orElseThrow(() -> new RuntimeException("SaaS not found"));

            String alias = orgSaasRequest.getAlias();
            String adminEmail = orgSaasRequest.getAdminEmail();
            String webhookUrl = orgSaasRequest.getWebhookUrl();
            Timestamp ts = Timestamp.valueOf(LocalDateTime.now());

            orgSaas.setOrg(org);
            orgSaas.setSaas(saas);
            orgSaas.setSpaceId("TEMP");
            OrgSaaS regiOrgSaas = orgSaaSRepo.save(orgSaas);

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

            workspaceConfig.setWorkspaceName(spaceName);
            workspaceConfig.setAlias(alias);
            workspaceConfig.setSaasAdminEmail(adminEmail);
            workspaceConfig.setToken(apiToken);
            workspaceConfig.setWebhook(webhookUrl);
            workspaceConfig.setRegisterDate(ts);

            workspaceConfig.setOrgSaas(regiOrgSaas);  // OrgSaaS 객체를 설정
            WorkspaceConfig regiWorkspace = workSpaceConfigRepo.save(workspaceConfig);

            String saasName = saas.getSaasName();

            try{
                startScan.postToScan(regiWorkspace.getId(), saasName);

                return new OrgSaasResponse(200, null, regiOrgSaas.getId(), ts);
            } catch (Exception e) {
                return new OrgSaasResponse(198, e.getMessage(), null, null);
            }
        } catch (Exception e ) {
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

    public List<OrgSaasResponse> getOrgSaasList(Integer orgId) {
        List<OrgSaaS> orgSaaSList = orgSaaSRepo.findByOrgId(orgId);

        List<Integer> configIds = orgSaaSList.stream()
                .map(OrgSaaS::getId)
                .distinct()
                .collect(Collectors.toList());

        List<WorkspaceConfig> workspaceConfigList = workSpaceConfigRepo.findByIdIn(configIds);
        Map<Integer, WorkspaceConfig> workspaceConfigMap = workspaceConfigList.stream()
                .collect(Collectors.toMap(WorkspaceConfig::getId, workspaceConfig -> workspaceConfig));

        return orgSaaSList.stream().map(orgSaaS -> {
            WorkspaceConfig workspaceConfig = workspaceConfigMap.get(orgSaaS.getId());

            Optional<Saas> saasOptional = saasRepo.findById(Math.toIntExact(orgSaaS.getSaas().getId()));
            String saasName = saasOptional.map(Saas::getSaasName).orElse("Unknown");

            return new OrgSaasResponse(
                    workspaceConfig != null ? workspaceConfig.getId() : null,
                    saasName,
                    workspaceConfig != null ? workspaceConfig.getAlias() : null,
                    orgSaaS.getStatus(),
                    workspaceConfig != null ? workspaceConfig.getSaasAdminEmail() : null,
                    workspaceConfig != null ? workspaceConfig.getToken() : null,
                    workspaceConfig != null ? workspaceConfig.getWebhook() : null,
                    workspaceConfig != null ? workspaceConfig.getRegisterDate() : null
            );
        }).collect(Collectors.toList());
    }

    public void updateOrgSaasGD(List<String[]> drives, String accessToken) {
        // TEMP 상태의 OrgSaaS 객체를 가져옵니다.
        List<OrgSaaS> tempOrgSaasList = orgSaaSRepo.findBySpaceId("TEMP");

        if (tempOrgSaasList.isEmpty() || drives.isEmpty()) {
            // TEMP 상태의 OrgSaaS가 없거나, 드라이브 리스트가 비어 있으면 반환합니다.
            return;
        }

        // 첫 번째 드라이브 정보만 처리
        String[] driveInfo = drives.get(0);
//        System.out.println("Target Drive: " + Arrays.toString(driveInfo));

        // DELETE인 튜플은 삭제
        if ("DELETE".equals(driveInfo[0])) {
            for (OrgSaaS orgSaaS : tempOrgSaasList) {
                orgSaaSRepo.delete(orgSaaS);

                Optional<WorkspaceConfig> optionalWorkspaceConfig = workSpaceConfigRepo.findById(orgSaaS.getId());
                optionalWorkspaceConfig.ifPresent(workSpaceConfigRepo::delete);
            }
            return;
        }

        OrgSaaS orgSaaS = tempOrgSaasList.get(0); // 첫 번째 TEMP 상태의 OrgSaaS만 사용

        orgSaaS.setSpaceId(driveInfo[0]);
        OrgSaaS saveOrgSaas = orgSaaSRepo.save(orgSaaS);

        Optional<WorkspaceConfig> optionalWorkspaceConfig = workSpaceConfigRepo.findById(orgSaaS.getId());
        if (optionalWorkspaceConfig.isPresent()) {
            WorkspaceConfig workspaceConfig = optionalWorkspaceConfig.get();
            workspaceConfig.setWorkspaceName(driveInfo[1]);
            workspaceConfig.setToken(accessToken);
            workspaceConfig.setOrgSaas(orgSaaS); // orgSaas 필드를 설정합니다.
            workSpaceConfigRepo.save(workspaceConfig);
        }
        initRabbitTemplate.convertAndSend(saveOrgSaas.getId());
    }
}
