package am.devvibes.buyandsell.entity.field;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldEntity extends BaseEntity implements Serializable {

	@Column(nullable = false)
	private String fieldValue;

	@ManyToOne
	@JoinColumn(nullable = false)
	private FieldNameEntity fieldName;

}