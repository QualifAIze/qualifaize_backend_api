package org.qualifaizebackendapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "document")
public class Document extends SoftDeletable {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, name = "file_name")
    @JsonProperty("document_name")
    private String fileName;

    @Column(nullable = false, unique = true)
    private String secondaryFileName;

    @Column(nullable = false, name = "subsections_count")
    @JsonProperty("subsections_count")
    private int subsectionsCount = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private User uploadedByUser;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subsection> subsections = new ArrayList<>();

    public String getTableOfContentsText() {
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString()).append(" ");

        if (subsections == null || subsections.isEmpty()) {
            sb.append("Document contains no sections");
        } else {
            sb.append("Document contains ").append(subsections.size())
                    .append(" root section").append(subsections.size() > 1 ? "s" : "")
                    .append(" which are: ");

            for (int i = 0; i < subsections.size(); i++) {
                if (i > 0) {
                    sb.append(" and ");
                }
                sb.append(subsections.get(i).getSubsectionInfo());
            }
        }

        return sb.toString();
    }
}