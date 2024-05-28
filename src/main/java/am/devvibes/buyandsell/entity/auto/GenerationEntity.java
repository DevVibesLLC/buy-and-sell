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
public class GenerationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int generationNumber;

    @ManyToOne
    @JoinColumn(name = "auto_model_id")
    private AutoModelEntity autoModel;

    @OneToMany(mappedBy = "generation", cascade = CascadeType.ALL)
    private List<GenerationItemEntity> items;

}