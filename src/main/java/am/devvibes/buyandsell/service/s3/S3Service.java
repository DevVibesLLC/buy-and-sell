package am.devvibes.buyandsell.service.s3;

import am.devvibes.buyandsell.dto.presignedUrl.PresignedUrlDto;

public interface S3Service {

    PresignedUrlDto getPresignedUrlForImages(String resolution);

    PresignedUrlDto getPresignedUrlForStories(String resolution);

    PresignedUrlDto getPresignedUrlForBusinessPageBanner(String resolution);

    PresignedUrlDto getPresignedUrlForBusinessPageLogo(String resolution);
}
