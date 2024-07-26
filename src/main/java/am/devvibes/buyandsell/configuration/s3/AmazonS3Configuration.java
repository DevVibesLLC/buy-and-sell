package am.devvibes.buyandsell.configuration.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class AmazonS3Configuration {

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String accessSecret;

	@Value("${cloud.aws.region.static}")
	private String region;

	@Bean
	public S3Presigner s3Presigner() throws URISyntaxException {
		return S3Presigner.builder()
				.region(Region.of(region))
				.credentialsProvider(
						StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, accessSecret)))
				.endpointOverride(new URI("https://s3." + region + ".amazonaws.com"))
				.build();
	}

}