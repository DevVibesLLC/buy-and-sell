package am.devvibes.buyandsell.service.s3.impl;

import am.devvibes.buyandsell.dto.presignedUrl.PresignedUrlDto;
import am.devvibes.buyandsell.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

	private final S3Presigner s3Presigner;

	@Value("${application.imagesBucket.name}")
	private String imagesBucketName;

	@Value("${application.storiesBucket.name}")
	private String storiesBucketName;

	@Value("${application.imageForSellFolder}")
	private String imageForSellFolder;

	@Value("${application.storiesFolder}")
	private String storiesFolder;

	@Value("${application.defaultFileName}")
	private String defaultFileName;

	@Override
	public PresignedUrlDto getPresignedUrlForImages(String resolution) {
		String fileName = generateFileName(resolution);
		String keyName = generateKeyName(imageForSellFolder, fileName);
		return PresignedUrlDto.builder()
				.uploadUrl(createPresignedUploadUrl(imagesBucketName, keyName))
				.downloadUrl(createPresignedDownloadUrl(imagesBucketName, keyName))
				.keyName(keyName)
				.build();
	}

	@Override
	public PresignedUrlDto getPresignedUrlForStories(String resolution) {
		String fileName = generateFileName(resolution);
		String keyName = generateKeyName(storiesFolder, fileName);
		return PresignedUrlDto.builder()
				.uploadUrl(createPresignedUploadUrl(storiesBucketName, keyName))
				.downloadUrl(createPresignedDownloadUrl(storiesBucketName, keyName))
				.keyName(keyName)
				.build();
	}

	private String generateFileName(String resolution) {
		return LocalDateTime.now() + defaultFileName + "." + resolution;
	}

	public String createPresignedUploadUrl(String bucketName, String keyName) {

		try (S3Presigner presigner = S3Presigner.create()) {
			PutObjectRequest objectRequest = PutObjectRequest.builder().bucket(bucketName).key(keyName).build();

			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(10))
					.putObjectRequest(objectRequest)
					.build();

			PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
			return presignedRequest.url().toExternalForm();
		}
	}

	public String createPresignedDownloadUrl(String bucketName, String keyName) {

		try (S3Presigner presigner = S3Presigner.create()) {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(keyName).build();

			GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(Duration.ofMinutes(10))
					.getObjectRequest(getObjectRequest)
					.build();

			PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(getObjectPresignRequest);
			return presignedRequest.url().toExternalForm();
		}
	}

	public String generateKeyName(String baseFolder, String fileName) {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		String datePath = currentDate.format(formatter);
		return baseFolder + "/" + datePath + "/" + fileName;
	}

	public List<String> getImagesPresignedDownloadUrls(List<String> keyNames) {
		return keyNames.stream().map(this::getImagePresignedDownloadUrl).toList();
	}

	public List<String> getStoriesPresignedDownloadUrls(List<String> keyNames) {
		return keyNames.stream().map(this::getStoryPresignedDownloadUrl).toList();
	}

	public String getImagePresignedDownloadUrl(String keyName) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(imagesBucketName).key(keyName).build();

		GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10))
				.getObjectRequest(getObjectRequest)
				.build();

		PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);

		return presignedGetObjectRequest.url().toString();
	}

	public String getStoryPresignedDownloadUrl(String keyName) {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(storiesBucketName).key(keyName).build();

		GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10))
				.getObjectRequest(getObjectRequest)
				.build();

		PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);

		return presignedGetObjectRequest.url().toString();
	}

}