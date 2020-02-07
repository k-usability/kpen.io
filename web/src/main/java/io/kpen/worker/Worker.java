package io.kpen.worker;

import io.github.cdimascio.dotenv.Dotenv;
import io.kpen.jooq.tables.records.JobRecord;
import io.kpen.util.S3;
import io.kpen.util.Tx;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.spi.LoggerContext;
import org.jooq.DSLContext;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.kpen.jooq.Tables.JOB;

public class Worker {

    private static Object run(Dotenv env, DSLContext ctx, int jobId) throws IOException {
        JobRecord job = ctx.fetchOne(JOB, JOB.ID.eq(jobId));
        String downDir = S3.downloadDir(job.getS3Bucket(), job.getS3Key());
        String genDir = downDir + "/generated";
        String appKPath = env.get("APP_K_PATH");

        String kpath = appKPath + "/" + job.getSemantics() + "/deps/k";
        String sempath = appKPath + "/" + job.getSemantics() + "/.build/defn/java";

        String logKeyPrefix = job.getS3Key() + "/generated";
        String stdoutFn = "stdout.txt";
        String stderrFn = "stderr.txt";
        Kw.Result res = Kw.run(env, genDir, job.getSpecFilename(), kpath, sempath, job.getTimeoutSec(), job.getMemlimitMb(), stdoutFn, stderrFn);

        List<File> files = new ArrayList();
        files.add(new File(res.getOutputFilePath()));
        files.add(new File(res.getErrorFilePath()));

        S3.xfermgr(mgr -> mgr.uploadFileList(job.getS3Bucket(), logKeyPrefix, new File(genDir), files));

        job.setOutputLogS3Key(logKeyPrefix + "/" + stdoutFn);
        job.setErrorLogS3Key(logKeyPrefix + "/" + stderrFn);
        job.setStatusCode(res.getStatusCode());
        job.setCompletedDt(OffsetDateTime.now());
        job.setTimedOut(res.getTimedOut());
        job.setProved(res.getProved());
        job.store();

        return null;
    }

    private static JobRecord popJob(DSLContext ctx) {
        List<JobRecord> jobs = ctx.fetch(JOB, JOB.PROCESSING_DT.isNull());
        if (jobs.isEmpty()) {
            return null;
        }
        JobRecord j = jobs.get(0);
        j.setProcessingDt(OffsetDateTime.now());
        j.store();
        return j;
    }

    public static void main(String[] args) {
        Dotenv env = Dotenv.load();

        while(true) {
            final JobRecord r = Tx.run(Worker::popJob);
            if (r == null) {
                System.out.println("No unprocessed jobs found. Sleeping...");
                safeSleep(3000);
            } else {
                try {
                    Tx.runex(ctx -> run(env, ctx, r.getId()));
                } catch(Throwable t) {
                    System.out.println("Transaction failed. Trying again...");
                    System.out.println(ExceptionUtils.getStackTrace(t));
                    safeSleep(3000);
                }
            }
        }
    }

    public static void safeSleep(long m) {
        try {Thread.sleep(m);} catch (Throwable t) {}
    }
}
