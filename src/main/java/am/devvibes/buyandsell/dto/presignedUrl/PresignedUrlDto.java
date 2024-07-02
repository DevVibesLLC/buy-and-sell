package am.devvibes.buyandsell.dto.presignedUrl;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PresignedUrlDto {

	private String uploadUrl;
	private String downloadUrl;
	private String keyName;

}
