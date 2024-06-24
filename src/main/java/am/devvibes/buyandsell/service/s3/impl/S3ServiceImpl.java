package am.devvibes.buyandsell.service.s3.impl;

import am.devvibes.buyandsell.exception.FileIsNullException;
import am.devvibes.buyandsell.service.s3.S3Service;
import am.devvibes.buyandsell.util.ExceptionConstants;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

	private final AmazonS3 amazonS3;

	@Value("${application.bucket.name}")
	private String bucketName;

	@Override
	public List<String> uploadFiles(List<MultipartFile> files) {
		List<String> imageNames = files.stream().map(this::uploadFile).toList();
		return getUrlsByImageNames(imageNames);
	}

	private List<String> getUrlsByImageNames(List<String> imageNames) {
		return imageNames.stream().map(this::getUrlForImage).toList();
	}

	private String getUrlForImage(String imageName) {
		return amazonS3.getUrl(bucketName, imageName).toString();
	}

	private String uploadFile(MultipartFile file) {
		File fileObj = convertMultiPartFileToFile(file);
		String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
		amazonS3.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
		fileObj.delete();
		return fileName;
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