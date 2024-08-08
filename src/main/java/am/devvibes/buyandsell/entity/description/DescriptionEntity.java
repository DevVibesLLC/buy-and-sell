package am.devvibes.buyandsell.entity.description;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import am.devvibes.buyandsell.entity.field.FieldNameEntity;
import am.devvibes.buyandsell.util.DescriptionNameEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DescriptionEntity extends BaseEntity{

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DescriptionNameEnum header;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "description_id")
	private List<FieldNameEntity> fields;

}