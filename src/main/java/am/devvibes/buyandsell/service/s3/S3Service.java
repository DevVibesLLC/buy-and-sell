package am.devvibes.buyandsell.service.s3;

import am.devvibes.buyandsell.dto.presignedUrl.PresignedUrlDto;

import java.util.Map;

import java.util.List;

public interface S3Service {

    PresignedUrlDto getPresignedUrlForImages(String resolution);

    PresignedUrlDto getPresignedUrlForStories(String resolution);


}
