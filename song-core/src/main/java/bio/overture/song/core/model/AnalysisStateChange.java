package bio.overture.song.core.model;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AnalysisStateChange extends DynamicData {
    private String initialState;
    private String updatedState;
    private LocalDateTime updatedAt;
}
