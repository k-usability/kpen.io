package io.kpen.web;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
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

    public static String getUrl(String bucketName, String keyName) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + keyName;
    }

    public AmazonS3 newS3Client() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(AWS_REGON)
                .withCredentials(new ProfileCredentialsProvider("kpen"))
                .build();
    }

    public TransferManager newXferManager() {
        TransferManager xfer_mgr = TransferManagerBuilder
                .standard()
                .withS3Client(newS3Client())
                .build();
       return xfer_mgr;
    }

    public void uploadDir(String bucketName, String keyPrefix, Path dirToUpload) throws IOException {
        TransferManager xfer_mgr = newXferManager();
        try {
            MultipleFileUpload xfer = xfer_mgr.uploadDirectory(bucketName, keyPrefix, dirToUpload.toFile(), true);
            // loop with Transfer.isDone()
            XferMgrProgress.showTransferProgress(xfer);
            // or block with Transfer.waitForCompletion()
            XferMgrProgress.waitForCompletion(xfer);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        xfer_mgr.shutdownNow();
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
        AmazonS3 client = newS3Client();
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

        TransferManager xfer_mgr = newXferManager();
        try {
            MultipleFileDownload xfer = xfer_mgr.downloadDirectory(bucketName, keyPrefix, bucketDir, true);
            // loop with Transfer.isDone()
            XferMgrProgress.showTransferProgress(xfer);
            // or block with Transfer.waitForCompletion()
            XferMgrProgress.waitForCompletion(xfer);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        xfer_mgr.shutdownNow();
    }
}
