package am.devvibes.buyandsell.service.s3;

import java.util.Map;

import java.util.List;

public interface S3Service {

    List<String> getPresignedUrl( String keyName, Map<String, String> metadata);

}
