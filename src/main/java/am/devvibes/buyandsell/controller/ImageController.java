package am.devvibes.buyandsell.controller;

import am.devvibes.buyandsell.service.s3.S3Service;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/image")
public class ImageController {


    private final S3Service s3Service;

    @GetMapping("/get-presigned-url")
    public List<String>  getPresignedUrl(@RequestParam String fileName,@RequestParam Map<String, String> metadata) {
        return s3Service.getPresignedUrl(fileName, metadata);
    }

}

