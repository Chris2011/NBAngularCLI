package net.chrizzly.netbeans.plugins.nbangularcli.executables;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import net.chrizzly.netbeans.plugins.nbangularcli.options.AngularCliOptions;
import net.chrizzly.netbeans.plugins.nbangularcli.ui.options.AngularCliOptionsPanelController;
import net.chrizzly.netbeans.plugins.nbangularcli.options.FileUtils;
import net.chrizzly.netbeans.plugins.nbangularcli.options.StringUtils;
import net.chrizzly.netbeans.plugins.nbangularcli.validator.AngularCliOptionsValidator;
import net.chrizzly.netbeans.plugins.nbangularcli.validator.ValidationResult;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


public class AngularCliExecutable {

    public static final String ANGULAR_CLI_NAME;

//    private static final String FORCE_PARAM = "--force"; // NOI18N
//    private static final String CSS_PARAM = "--css"; // NOI18N
//    private static final String LESS_PARAM = "less"; // NOI18N

    protected final Project project;
    protected final String angularCliPath;


    static {
        if (Utilities.isWindows()) {
            ANGULAR_CLI_NAME = "ng.cmd"; // NOI18N
        } else {
            ANGULAR_CLI_NAME = "ng"; // NOI18N
        }
    }

    AngularCliExecutable(String angularCliPath, @NullAllowed Project project) {
        assert angularCliPath != null;

        this.angularCliPath = angularCliPath;
        this.project = project;
    }

    @CheckForNull
    public static AngularCliExecutable getDefault(@NullAllowed Project project, boolean showOptions) {
        ValidationResult result = new AngularCliOptionsValidator()
                .validateAngularCli()
                .getResult();
        if (validateResult(result) != null) {
            if (showOptions) {
                OptionsDisplayer.getDefault().open(AngularCliOptionsPanelController.OPTIONS_PATH);
            }

            return null;
        }

        return createExecutable(AngularCliOptions.getInstance().getAngularCli(), project);
    }

    private static AngularCliExecutable createExecutable(String angularCli, Project project) {
        if (Utilities.isMac()) {
            return new AngularCliExecutable.MacAngularCliExecutable(angularCli, project);
        }
        return new AngularCliExecutable(angularCli, project);
    }

    String getCommand() {
        return angularCliPath;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "AngularCliExecutable.generate=Angular CLI ({0})",
    })
    public Future<Integer> generate(FileObject target, boolean less) {
        assert !EventQueue.isDispatchThread();
        assert project != null;

        String projectName = ProjectUtils.getInformation(project).getDisplayName();
        Future<Integer> task = getExecutable(Bundle.AngularCliExecutable_generate(projectName))
                .additionalParameters(getGenerateParams(target, less))
                .run(getDescriptor());

        assert task != null : angularCliPath;
        return task;
    }

    private ExternalExecutable getExecutable(String title) {
        assert title != null;
        return new ExternalExecutable(getCommand())
                .workDir(getWorkDir())
                .displayName(title)
                .optionsPath(AngularCliOptionsPanelController.OPTIONS_PATH)
                .noOutput(false);
    }

    private ExecutionDescriptor getDescriptor() {
        assert project != null;
        return ExternalExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .showSuspended(true)
                .optionsPath(AngularCliOptionsPanelController.OPTIONS_PATH)
                .outLineBased(true)
                .errLineBased(true)
                .postExecution(() -> project.getProjectDirectory().refresh());
    }

    private File getWorkDir() {
        if (project == null) {
            return FileUtils.TMP_DIR;
        }

        File workDir = FileUtil.toFile(project.getProjectDirectory());

        assert workDir != null : project.getProjectDirectory();

        return workDir;
    }

    private List<String> getGenerateParams(FileObject target, boolean less) {
        List<String> params = new ArrayList<>(3);
//        params.add(FORCE_PARAM);
        params.add(FileUtil.toFile(target).getAbsolutePath());
        return getParams(params);
    }

    List<String> getParams(List<String> params) {
        assert params != null;
        return params;
    }

    @CheckForNull
    private static String validateResult(ValidationResult result) {
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getFirstErrorMessage();
        }
        return result.getFirstWarningMessage();
    }

    //~ Inner classes

    private static final class MacAngularCliExecutable extends AngularCliExecutable {

        private static final String BASH_COMMAND = "/bin/bash -lc"; // NOI18N


        MacAngularCliExecutable(String angularCliPath, Project project) {
            super(angularCliPath, project);
        }

        @Override
        String getCommand() {
            return BASH_COMMAND;
        }

        @Override
        List<String> getParams(List<String> params) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("\""); // NOI18N
            sb.append(angularCliPath);
            sb.append("\" \""); // NOI18N
            sb.append(StringUtils.implode(super.getParams(params), "\" \"")); // NOI18N
            sb.append("\""); // NOI18N
            return Collections.singletonList(sb.toString());
        }

    }

}
