package io.kpen.web;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class S3 {
    public static final String S3_CACHE_DIR = "s3cache";
    public static final Regions AWS_REGON = Regions.US_EAST_2;

    private AmazonS3 client;

    public S3() {
        client = AmazonS3ClientBuilder.standard()
                .withRegion(AWS_REGON)
                .build();
    }

    public static String getUrl(String bucketName, String keyName) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + keyName;
    }

    public void uploadDir(String bucketName, String keyPrefix, Path dirToUpload) throws IOException {
        for (Path path : Files.walk(dirToUpload).collect(Collectors.toList())) {
            File file = path.toFile();
            if (file.isDirectory()) continue;

            String subKey = dirToUpload.relativize(path).toString();
            String key = keyPrefix + "/" + subKey;
            System.out.println(key);

            PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
            request.setCannedAcl(CannedAccessControlList.PublicRead);
            client.putObject(request);
        }
    }

    public File get(String bucketName, String bucketKey, String fileRelativePath) throws IOException {
        File bucketDir = new File(S3_CACHE_DIR + "/" + bucketKey);
        if (!bucketDir.exists()) downloadDir( bucketName, bucketKey);

        File cachedFile = new File(S3_CACHE_DIR + "/" + bucketKey + "/" + fileRelativePath);
        if (!cachedFile.exists()) {
            String content = getContent(bucketName, bucketKey + "/" + fileRelativePath);
            FileUtils.write(cachedFile, content, Charset.defaultCharset());
        }

        return cachedFile;
    }

    public String getContent(String bucketName, String bucketKey) throws IOException {
        GetObjectRequest req = new GetObjectRequest(bucketName, bucketKey);
        S3Object obj = client.getObject(req);
        String content;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(obj.getObjectContent()))) {
            content = buffer.lines().collect(Collectors.joining("\n"));
        }
        return content;
    }

    public void downloadDir(String bucketName, String keyPrefix) throws IOException {
        File bucketDir = new File(S3_CACHE_DIR + "/" + bucketName);
        if (!bucketDir.exists()) {
            bucketDir.mkdirs();
        }

        ObjectListing listing = client.listObjects( bucketName, keyPrefix );
        List<S3ObjectSummary> summaries = listing.getObjectSummaries();
        while (listing.isTruncated()) {
            listing = client.listNextBatchOfObjects (listing);
            summaries.addAll (listing.getObjectSummaries());
        }

        for (S3ObjectSummary s : summaries) {
            String content = getContent(bucketName, s.getKey());
            String relativeKey = s.getKey().replaceAll(keyPrefix, "");
            System.out.println("Relative key: " + relativeKey);
            File file = new File(bucketDir.getAbsolutePath() + "/" + relativeKey);
            FileUtils.write(file, content, Charset.defaultCharset());
        }
    }
}
