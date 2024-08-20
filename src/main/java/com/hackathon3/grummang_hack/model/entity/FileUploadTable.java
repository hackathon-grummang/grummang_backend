package com.hackathon3.grummang_hack.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "file_upload")
public class FileUploadTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "org_saas_id", nullable = false, referencedColumnName = "id")
    private OrgSaaS orgSaaS;

    @Column(name = "saas_file_id", nullable = false, unique = true)
    private String saasFileId;

    @Column(nullable = false, name="salted_hash")
    private String hash;

    @Column(name = "upload_ts", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "salted_hash", referencedColumnName = "salted_hash", insertable = false, updatable = false)
    private StoredFile storedFile;

    @Builder
    public FileUploadTable(OrgSaaS orgSaaS, String saasFileId, String hash, LocalDateTime timestamp, boolean deleted, StoredFile storedFile) {
        this.orgSaaS = orgSaaS;
        this.saasFileId = saasFileId;
        this.hash = hash;
        this.deleted = deleted;
        this.timestamp = timestamp;
        this.storedFile = storedFile;
    }
}
