package org.qualifaizebackendapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "subsection")
public class Subsection {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Subsection parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonProperty("subsections")
    private List<Subsection> children = new ArrayList<>();

    @Column()
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private int level;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public String getSubsectionInfo(){
        StringBuilder sb = new StringBuilder();

        sb.append("Section titled '").append(title)
                .append("' at level ").append(level)
                .append(" in position ").append(position);

        if (children == null || children.isEmpty()) {
            sb.append(" with no subsections");
        } else {
            sb.append(" containing ").append(children.size())
                    .append(" subsection").append(children.size() > 1 ? "s" : "")
                    .append(" which are: ");

            for (int i = 0; i < children.size(); i++) {
                if (i > 0) {
                    sb.append(" and ");
                }
                sb.append(children.get(i).getSubsectionInfo());
            }
        }

        return sb.toString();
    }
}