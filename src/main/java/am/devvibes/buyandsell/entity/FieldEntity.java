package am.devvibes.buyandsell.entity;

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

	private String fieldValue;

	@ManyToOne
	private FieldNameEntity fieldName;

}