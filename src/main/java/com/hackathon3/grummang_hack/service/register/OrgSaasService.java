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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OrgSaasService {

    private final OrgRepo orgRepo;
    private final SaasRepo saasRepo;
    private final OrgSaaSRepo orgSaaSRepo;
    private final WorkSpaceConfigRepo workSpaceConfigRepo;

    @Autowired
    public OrgSaasService(OrgRepo orgRepo, SaasRepo saasRepo, OrgSaaSRepo orgSaaSRepo, WorkSpaceConfigRepo workSpaceConfigRepo) {
        this.orgRepo = orgRepo;
        this.saasRepo = saasRepo;
        this.orgSaaSRepo = orgSaaSRepo;
        this.workSpaceConfigRepo = workSpaceConfigRepo;
    }

    public OrgSaasResponse register(OrgSaasRequest orgSaasRequest) {
        OrgSaaS orgSaas = new OrgSaaS();
        WorkspaceConfig workspaceConfig = new WorkspaceConfig();

        // org_saas table
        Org org = orgRepo.findById(orgSaasRequest.getOrgId()).orElseThrow(() -> new RuntimeException("Org not found"));
        Saas saas = saasRepo.findById(orgSaasRequest.getSaasId()).orElseThrow(() -> new RuntimeException("SaaS not found"));
        String spaceId = "spaceId";
        // workspace_config table
        Integer orgSaaSId = 1;
        String alias = orgSaasRequest.getAlias();
        String adminEmail = orgSaasRequest.getAdminEmail();
        String apiToken = orgSaasRequest.getApiToken();
        String webhookUrl = orgSaasRequest.getWebhookUrl();
        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());

        // 저장
        orgSaas.setOrg(org);
        orgSaas.setSaas(saas);
        orgSaas.setSpaceId(spaceId);
        OrgSaaS regiOrgSaas = orgSaaSRepo.save(orgSaas);
        workspaceConfig.setId(orgSaaSId);
        workspaceConfig.setAlias(alias);
        workspaceConfig.setSaasAdminEmail(adminEmail);
        workspaceConfig.setToken(apiToken);
        workspaceConfig.setWebhook(webhookUrl);
        workspaceConfig.setRegisterDate(ts);
        WorkspaceConfig regiWorkspaceConfig = workSpaceConfigRepo.save(workspaceConfig);

        return new OrgSaasResponse(200, null, orgSaaSId, ts);
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
}
