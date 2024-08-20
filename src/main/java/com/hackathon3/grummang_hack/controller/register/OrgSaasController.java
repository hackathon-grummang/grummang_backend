package com.hackathon3.grummang_hack.controller.register;

import com.hackathon3.grummang_hack.model.dto.OrgSaasRequest;
import com.hackathon3.grummang_hack.model.dto.OrgSaasResponse;
import com.hackathon3.grummang_hack.service.register.GoogleUtil;
import com.hackathon3.grummang_hack.service.register.OrgSaasService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{orgId}")
    public List<OrgSaasResponse> getOrgSaasList(@PathVariable Integer orgId) {
        return orgSaasService.getOrgSaasList(orgId);
    }

    @GetMapping("/test")
    public ResponseEntity<String> token(@RequestParam("code") String code){
        googleUtil.func(code);

        // Return the HTML page with auto-close functionality
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body("<!DOCTYPE html>" +
                        "<html lang='en'>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<title>Authorization Complete</title>" +
                        "<script type='text/javascript'>" +
                        "function closeTab() {" +
                        "    alert('Google Drive 연동 성공');" +
                        "    window.close();" +
                        "}" +
                        "window.onload = closeTab;" +
                        "</script>" +
                        "</head>" +
                        "<body>" +
                        "<p>Google Drive 연동 성공, 탭을 닫아주세요.</p>" +
                        "</body>" +
                        "</html>");
    }
}
