package am.devvibes.buyandsell.entity.auto;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenerationItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int restyling;
    private int yearStart;
    private Integer yearEnd;

    @ManyToOne
    @JoinColumn(name = "generation_id")
    private GenerationEntity generation;

    @ElementCollection
    private List<String> frames;
}
