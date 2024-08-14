package am.devvibes.buyandsell.dto.businessPage;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
@Builder
public class BusinessPageContactsDto {

	private String email;

	private List<String> phoneNumbers;

	private Map<String, String> socialMediaLinks;

}
