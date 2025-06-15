package org.qualifaizebackendapi.DTO.db_object;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class SubsectionRow {
    private UUID id;
    private UUID parentId;
    private String title;
    private int level;
    private int position;

    public SubsectionRow(UUID id, UUID parentId, String title, int level, int position) {
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.level = level;
        this.position = position;
    }
}
