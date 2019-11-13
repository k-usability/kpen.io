package io.kpen.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.*;
import io.kpen.web.XferMgrProgress;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class S3 {
    public static final String S3_CACHE_DIR = "s3cache";
    public static final Regions AWS_REGON = Regions.US_EAST_2;

    public static String getUrl(String bucketName, String keyName) {
        return "https://" + bucketName + ".s3.amazonaws.com/" + keyName;
    }

    private static AmazonS3 newS3Client() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(AWS_REGON)
                .withCredentials(new ProfileCredentialsProvider("kpen"))
                .build();
    }

    private static TransferManager newXferManager() {
        TransferManager xfer_mgr = TransferManagerBuilder
                .standard()
                .withS3Client(newS3Client())
                .build();
       return xfer_mgr;
    }

    public interface XferRunnable<T> {
        Transfer xfer(TransferManager mgr) throws AmazonServiceException;
    }

    public static void xfermgr(XferRunnable r) throws AmazonServiceException {
        TransferManager mgr = newXferManager();
        Transfer x = r.xfer(mgr);
        XferMgrProgress.showTransferProgress(x);
        XferMgrProgress.waitForCompletion(x);
        mgr.shutdownNow();
    }

    public static void uploadDir(String bucketName, String keyPrefix, Path dirToUpload) throws IOException {
        xfermgr( mgr -> mgr.uploadDirectory(bucketName, keyPrefix, dirToUpload.toFile(), true));
    }

    public static String downloadDir(String bucketName, String keyPrefix) throws IOException {
        File bucketDir = new File(S3_CACHE_DIR + "/" + bucketName);
        if (!bucketDir.exists()) {
            bucketDir.mkdirs();
        }

        xfermgr( mgr -> mgr.downloadDirectory(bucketName, keyPrefix, bucketDir, true));

        return bucketDir.getAbsolutePath() + "/" + keyPrefix;
    }

    public static File get(String bucketName, String bucketKey, String fileRelativePath) throws IOException {
        File bucketDir = new File(S3_CACHE_DIR + "/" + bucketKey);
        if (!bucketDir.exists()) downloadDir( bucketName, bucketKey);

        File cachedFile = new File(S3_CACHE_DIR + "/" + bucketKey + "/" + fileRelativePath);
        if (!cachedFile.exists()) {
            String content = getContent(bucketName, bucketKey + "/" + fileRelativePath);
            FileUtils.write(cachedFile, content, Charset.defaultCharset());
        }

        return cachedFile;
    }

    public static String getContent(String bucketName, String bucketKey) throws IOException {
        AmazonS3 client = newS3Client();
        GetObjectRequest req = new GetObjectRequest(bucketName, bucketKey);
        S3Object obj = client.getObject(req);
        String content;
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(obj.getObjectContent()))) {
            content = buffer.lines().collect(Collectors.joining("\n"));
        }
        return content;
    }

}
