package com.hackathon3.grummang_hack.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class OrgSaasResponse {

    // about Error
    private Integer errorCode;
    private String errorMessage;
    // OrgSaas
    private String name;    // saasName
    private Integer status;
    private String securityScore;
    // Workspace_config
    private Integer id;     // configId
    private String alias;
    private String adminEmail;
    private String apiToken;
    private String webhookUrl;
    private Timestamp registerDate;
    // token(email) valid
    private Boolean validation;


    // POST(valid)
    public OrgSaasResponse(Integer errorCode, String errorMessage, Boolean validation) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.validation = validation;
    }
    // POST(regi, delete)
    public OrgSaasResponse(Integer errorCode, String errorMessage, Integer id, Timestamp registerDate) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.id = id;
        this.registerDate = registerDate;
    }
    // GET(list)
    public OrgSaasResponse(Integer id, String name, String alias, Integer status,
                           String adminEmail, String apiToken,
                           String webhookUrl, Timestamp registerDate) {

        this.id = id;
        this.name = name;
        this.alias = alias;
        this.status = status;
        this.adminEmail = adminEmail;
        this.apiToken = apiToken;
        this.webhookUrl = webhookUrl;
        this.registerDate = registerDate;
    }
}
