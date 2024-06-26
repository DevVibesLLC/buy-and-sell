package am.devvibes.buyandsell.service.s3;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface S3Service {

	List<String> uploadItemImages(List<MultipartFile> files);

	String uploadStory(MultipartFile story);

}
