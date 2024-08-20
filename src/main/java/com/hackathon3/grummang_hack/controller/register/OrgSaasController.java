package com.hackathon3.grummang_hack.controller.register;

import com.hackathon3.grummang_hack.model.dto.OrgSaasRequest;
import com.hackathon3.grummang_hack.model.dto.OrgSaasResponse;
import com.hackathon3.grummang_hack.service.register.GoogleUtil;
import com.hackathon3.grummang_hack.service.register.OrgSaasService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/org-saas")
@Slf4j
public class OrgSaasController {

    private final OrgSaasService orgSaasService;
    private final GoogleUtil googleUtil;

    @Autowired
    public OrgSaasController(OrgSaasService orgSaasService, GoogleUtil googleUtil) {
        this.orgSaasService = orgSaasService;
        this.googleUtil = googleUtil;
    }


    @PostMapping("/register")
    public OrgSaasResponse register(@RequestBody OrgSaasRequest orgSaasRequest) {
        return orgSaasService.register(orgSaasRequest);
    }

    @PostMapping("/delete")
    public OrgSaasResponse delete(@RequestBody OrgSaasRequest orgSaasRequest) {
        return orgSaasService.delete(orgSaasRequest);
    }

    @GetMapping("/test")
    public void token(@RequestParam("code") String code){
        googleUtil.func(code);
    }
}
