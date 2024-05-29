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
public class AutoModelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auto_mark_id")
    private AutoMarkEntity autoMark;

    @OneToMany(mappedBy = "autoModel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonIgnore
    private List<GenerationEntity> generations;
}
