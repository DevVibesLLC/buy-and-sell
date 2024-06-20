package am.devvibes.buyandsell.entity.notebook;

import am.devvibes.buyandsell.entity.mobile.MobilePhoneModelEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotebookMarkEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

}
