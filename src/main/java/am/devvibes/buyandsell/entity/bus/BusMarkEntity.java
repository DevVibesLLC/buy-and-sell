package am.devvibes.buyandsell.entity.bus;

import am.devvibes.buyandsell.entity.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusMarkEntity extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "busMark", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<BusModelEntity> models;

}
