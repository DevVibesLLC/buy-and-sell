package am.devvibes.buyandsell.service.s3.impl;

import am.devvibes.buyandsell.exception.FileIsNullException;
import am.devvibes.buyandsell.service.s3.S3Service;
import am.devvibes.buyandsell.util.ExceptionConstants;
import com.amazonaws.services.s3.AmazonS3;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

	private final String imageForSellFolder = "images/sell";

	private final AmazonS3 amazonS3;
	private final S3Presigner s3Presigner;

	@Value("${application.imagesBucket.name}")
	private String imagesBucketName;

	@Value("${application.storiesBucket.name}")
	private String storiesBucketName;




	public String generateKeyName(String baseFolder, String fileName) {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		String datePath = currentDate.format(formatter);
		return baseFolder + "/" + datePath + "/" + fileName;
	}

	@Override
	public List<String> getPresignedUrl(String fileName, Map<String, String> metadata) {
		return List.of(createPresignedUploadUrl(imagesBucketName,imageForSellFolder,fileName,metadata),
			createPresignedDownloadUrl(imagesBucketName, imageForSellFolder, fileName)
			);

	}

	public String createPresignedUploadUrl(String bucketName, String baseFolder, String fileName,
		Map<String, String> metadata) {
		String keyName = generateKeyName(baseFolder, fileName);

		try (S3Presigner presigner = S3Presigner.create()) {
			PutObjectRequest objectRequest = PutObjectRequest.builder()
				.bucket(bucketName)
				.key(keyName)
				.metadata(metadata)
				.build();

			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
				.putObjectRequest(objectRequest)
				.build();

			PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
			String myURL = presignedRequest.url().toString();
			log.info("Presigned URL to upload a file to: [{}]", myURL);
			log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

			return presignedRequest.url().toExternalForm();
		}
	}

	public String createPresignedDownloadUrl(String bucketName, String baseFolder, String fileName) {
		String keyName = generateKeyName(baseFolder, fileName);

		try (S3Presigner presigner = S3Presigner.create()) {
			GetObjectRequest getObjectRequest = GetObjectRequest.builder()
				.bucket(bucketName)
				.key(keyName)
				.build();

			GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
				.signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
				.getObjectRequest(getObjectRequest)
				.build();

			PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(getObjectPresignRequest);
			String myURL = presignedRequest.url().toString();
			log.info("Presigned URL to download a file from: [{}]", myURL);
			log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

			return presignedRequest.url().toExternalForm();
		}
	}

	private List<String> getUrlsByItemImageNames(List<String> imageNames) {
		return imageNames.stream().map(this::getUrlForItemImage).toList();
	}

	private String getUrlForItemImage(String imageName) {
		return amazonS3.getUrl(imagesBucketName, imageName).toString();
	}

	private String getUrlForStory(String storyName) {
		return amazonS3.getUrl(storiesBucketName, storyName).toString();
	}


	private File convertMultiPartFileToFile(MultipartFile file) {
		if (file == null) {
			throw new FileIsNullException(ExceptionConstants.FILE_IS_NULL);
		}

		File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
		try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
			fos.write(file.getBytes());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return convertedFile;
	}

}