package am.devvibes.buyandsell.entity.auto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private Integer generationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_model_id")
    private AutoModelEntity autoModel;

    @OneToMany(mappedBy = "generation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonIgnore
    private List<GenerationItemEntity> items;

}