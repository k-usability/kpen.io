package io.kpen.web;

import io.kpen.jooq.tables.records.JobRecord;
import io.kpen.jooq.tables.records.PersonRecord;
import io.kpen.jooq.tables.records.ProjectRecord;
import io.kpen.util.Auth;
import io.kpen.util.S3;
import io.kpen.util.Tx;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.jooq.DSLContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static io.kpen.jooq.Tables.JOB;
import static io.kpen.jooq.Tables.PROJECT;
import static io.kpen.web.GetProjectController.ProjectState.*;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3010", "https://kpen.io"})
@RestController
@RequestMapping(path = "api", produces = MediaType.APPLICATION_JSON_VALUE)
public class GetProjectController {

    public enum Color {
        Green("#d4edda"),
        Red("#f8d7da"),
        Yellow("#fff3cd");

        public final String hex;

        Color(String hex) {
            this.hex = hex;
        }
    }

    public enum ProjectState {
        CompilationError(Color.Red, "danger", "Compilation error"),
        Pending(Color.Yellow, "warning", "Proof in-progress"),
        Failed(Color.Red, "danger", "Proof failed"),
        Succeeded(Color.Green, "success", "Proof succeeded");

        public final Color color;
        public final String alertType;
        public final String label;

        ProjectState(Color color, String alertType, String label) {
            this.color = color;
            this.alertType = alertType;
            this.label = label;
        }
    }

    @GetMapping(value = "/project")
    public GetProjectResp getProject(@RequestParam Integer projectId) throws Throwable {
        return Tx.runex(ctx -> getProject(ctx, projectId));
    }

    @PostMapping(value = "/projects")
    public GetProjectsResp getProjects(final Authentication auth) throws Throwable {
        return Tx.runex(ctx -> getProjects(ctx, Auth.getPersonRecord(ctx, auth)));
    }

    public static GetProjectsResp getProjects(DSLContext ctx, PersonRecord person) throws IOException {
        List<JobView> jobviews = new ArrayList();

        List<JobRecord> jobs = ctx.fetch(JOB.join(PROJECT).on(JOB.PROJECT_ID.eq(PROJECT.ID)),
                                    PROJECT.USER_ID.eq(person.getId()))
                                    .into(JOB)
                                    .sortDesc(JOB.REQUEST_DT);

        Set<ProjectState> stateSet = new HashSet();
        for (JobRecord job : jobs) {
            JobView view = toView(job);
            stateSet.add(view.state);
            jobviews.add(toView(job));
        }

        return new GetProjectsResp(jobviews);
    }

    public static GetProjectResp getProject(DSLContext ctx, Integer projectId) throws IOException {
        ProjectRecord proj = ctx.fetchOne(PROJECT, PROJECT.ID.eq(projectId));
        S3 s3 = new S3();
        File progFile = s3.get(proj.getS3Bucket(), proj.getS3Key(), proj.getProgramFilename());
        File specFile = s3.get(proj.getS3Bucket(), proj.getS3Key(), proj.getSpecFilename());

        String program = FileUtils.readFileToString(progFile, Charset.defaultCharset());
        String spec = FileUtils.readFileToString(specFile, Charset.defaultCharset());

        List<JobView> jobs = new ArrayList();

        Set<ProjectState> stateSet = new HashSet();
        for (JobRecord job : ctx.fetch(JOB, JOB.PROJECT_ID.eq(proj.getId()))) {
            JobView view = toView(job);
            stateSet.add(view.state);
            jobs.add(toView(job));
        }

        ProjectState projectState;
        String label;
        if (proj.getIsCompilationError()) {
            projectState = CompilationError;
            label = "Compilation failed: " + proj.getCompilationErrorMessage();
        } else {
            if (stateSet.isEmpty()) {
                projectState = Pending;
            } else {
                if (stateSet.contains(Failed)) {
                    projectState = Failed;
                } else if (stateSet.contains(Pending)) {
                    projectState = Pending;
                } else {
                    projectState = Succeeded;
                }
            }
            label = projectState.label;
        }

        if (projectState.equals(Pending)) {
            label += ". Elapsed time: " + Duration.between(proj.getCreationDt(), ZonedDateTime.now()).toMillis() / 1000 + " seconds.";
        }

        return new GetProjectResp(program, spec, jobs, projectState.name(), label, projectState.color.hex, projectState.alertType);
    }

    private static String toString(DateTimeFormatter f, OffsetDateTime o) {
        return o == null ? "" : f.format(o);
    }

    private static Long getProcessingDuration(ChronoUnit u, JobRecord r) {
        if (r.getProcessingDt() == null || r.getCompletedDt() == null) return null;
        return u.between(r.getProcessingDt(), r.getCompletedDt());
    }

    public static JobView toView(JobRecord r) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        JobView view = new JobView();
        view.id = Integer.toString(r.getId());
        view.completedDt = toString(f, r.getCompletedDt());
        view.requestDt = toString(f, r.getRequestDt());
        view.processingDt = toString(f, r.getProcessingDt());
        view.processingSecs = Objects.toString(getProcessingDuration(ChronoUnit.SECONDS, r));
        view.processingMins = Objects.toString(getProcessingDuration(ChronoUnit.MINUTES, r));
        view.benchmarkName = r.getBenchmarkName();
        view.specName = r.getSpecName();
        view.statusCode = Objects.toString(r.getStatusCode());
        view.outUrl = r.getOutputLogS3Key() == null ? null : S3.getUrl(r.getS3Bucket(), r.getOutputLogS3Key());
        view.errUrl = r.getErrorLogS3Key() == null ? null : S3.getUrl(r.getS3Bucket(), r.getErrorLogS3Key());
        view.kUrl = S3.getUrl(r.getS3Bucket(), r.getS3Key() + "/generated/" + r.getSpecFilename());

        if (r.getProjectId() != null) {
            view.projectUrl = "/project/" + r.getProjectId();
        }

        if (r.getCompletedDt() != null) {
            if (Objects.equals(r.getProved(), true)) {
                view.state = ProjectState.Succeeded;
                view.result = "Proved true";
                view.resultColor = Color.Green.hex;
                view.resultStyle = "success";
            } else if (Objects.equals(r.getProved(), false)) {
                view.state = Failed;
                view.result = "Proved false";
                view.resultColor = Color.Red.hex;
                view.resultStyle = "danger";
            } else {
                if (Objects.equals(r.getTimedOut(), true)) {
                    view.state = Failed;
                    view.result = "Timeout";
                    view.resultColor = Color.Red.hex;
                    view.resultStyle = "danger";
                } else {
                    view.state = Failed;
                    view.result = "Error";
                    view.resultColor = Color.Red.hex;
                    view.resultStyle = "danger";
                }
            }
        } else {
            view.state = ProjectState.Pending;
            view.result = "Pending";
            view.resultColor = Color.Yellow.hex;
            view.resultStyle = "warning";
        }

        return view;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JobView {
        public String id;
        public String completedDt;
        public String requestDt;
        public String processingDt;
        public String processingSecs;
        public String processingMins;
        public String benchmarkName;
        public String specName;
        public String statusCode;
        public String outUrl;
        public String errUrl;
        public String kUrl;
        public String result;
        public String resultColor;
        public String resultStyle;
        public transient ProjectState state;
        public String projectUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class GetProjectReq {
        private Integer projectId;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class GetProjectResp {
        private String programText;
        private String specText;
        private List<JobView> jobs;
        private String stateType;
        private String state;
        private String stateColor;
        private String stateAlertType;
    }


    @Data
    @Builder
    @AllArgsConstructor
    public static class GetProjectsResp {
        private List<JobView> jobs;
    }
}
