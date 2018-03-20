package net.chrizzly.netbeans.plugins.nbangularcli.projectwizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import net.chrizzly.netbeans.plugins.nbangularcli.options.AngularCliOptions;
import net.chrizzly.netbeans.plugins.nbangularcli.ui.options.AngularCliOptionsPanel;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

// TODO define position attribute
@TemplateRegistration(
        folder = "Project/ClientSide",
        displayName = "#AngularCliApplicaton_displayName",
        description = "AngularCliApplicatonDescription.html",
        iconBase = "net/chrizzly/netbeans/plugins/nbangularcli/projectwizard/angular.png"
)
@Messages("AngularCliApplicaton_displayName=Angular CLI Application")
public class AngularCliWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    public static AngularCliWizardIterator createIterator() {
        return new AngularCliWizardIterator();
    }

    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[]{
            new AngularCliLocationPanel()
        };
    }

    private String[] createSteps() {
        return new String[]{
            NbBundle.getMessage(AngularCliWizardIterator.class, "LBL_CreateProjectStep"),};
    }

    private class ProcessLaunch implements Callable<Process> {

        private final File folder;
        private final String projectName;

        public ProcessLaunch(File folder, String projectName) {
            this.folder = folder;
            this.projectName = projectName;
        }

        @Override
        public Process call() throws Exception {
//            final String appPath = NbPreferences.forModule(AngularCliOptionsPanel.class).get("ngCliExecutableLocation", "");
            final String appPath = AngularCliOptions.getInstance().getAngularCli();
            ProcessBuilder pb = new ProcessBuilder(appPath, "new", projectName, "--dir=.");

            pb.directory(folder); //NOI18N
            pb.redirectErrorStream(true);

            return pb.start();
        }
    }

    @Override
    public Set instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        Runnable runnable = createNgCliApp();

        // execute async in separate thread
        RequestProcessor.getDefault().post(runnable);

        return Collections.emptySet();
    }

    private Runnable createNgCliApp() {
        final File projectDir = FileUtil.normalizeFile((File) wiz.getProperty("projdir"));
        final String projectName = "" + wiz.getProperty("name");

        return new Runnable() {
            @Override
            public void run() {
                final ProgressHandle ph = ProgressHandle.createHandle("Creating project via angular-cli...");

                try {
                    ph.start();

                    ph.progress("Creating project directory");
                    File dirF = FileUtil.normalizeFile(projectDir);
                    dirF.mkdirs();

                    ExecutionDescriptor descriptor = new ExecutionDescriptor()
                        .controllable(true)
                        .frontWindow(true)
                        // disable rerun
                        .rerunCondition(new ExecutionDescriptor.RerunCondition() {
                            @Override
                            public void addChangeListener(ChangeListener cl) {
                            }

                            @Override
                            public void removeChangeListener(ChangeListener cl) {
                            }

                            @Override
                            public boolean isRerunPossible() {
                                return false;
                            }
                        })
                        // we handle the progress ourself
                        .showProgress(false);

                    // integrate as subtask in the same progress bar
                    ph.progress(String.format("Executing 'ng new %s'", projectName));

                    ExecutionService exeService = ExecutionService.newService(new ProcessLaunch(projectDir, projectName),
                            descriptor, String.format("Executing 'ng new %s'", projectName));
                    Integer exitCode = null;

                    // this will run the process
                    Future<Integer> processFuture = exeService.run();
                    try {
                        // wait for end of execution of shell command
                        exitCode = processFuture.get();
                    } catch (InterruptedException | ExecutionException ex) {
                        NotificationDisplayer.getDefault().notify("Angular CLI execution was aborted", NotificationDisplayer.Priority.HIGH.getIcon(), String.format("The execution of 'ng new %s' was aborted. Please see the IDE Log.", projectName), null);

                        return;
                    } catch (CancellationException ex) {
                        NotificationDisplayer.getDefault().notify("Angular CLI execution was canceled", NotificationDisplayer.Priority.HIGH.getIcon(), String.format("The execution of 'ng new %s' was canceled by the user.", projectName), null);

                        return;
                    }

//                    if (processFuture.isCancelled()) {
//                        JOptionPane.showMessageDialog(null, "Was canceled by user.");
//
//                        return;
//                    }
                    if (exitCode != null && exitCode != 0) {
                        NotificationDisplayer.getDefault().notify("Angular CLI execution was aborted", NotificationDisplayer.Priority.HIGH.getIcon(), String.format("The execution of 'ng new %s' was aborted. Please see the IDE Log.", projectName), null);

                        return;
                    }

                    if (exitCode != null && exitCode == 0) {
                        NotificationDisplayer.getDefault().notify(String.format("Project %s was successfully created", projectName), NotificationDisplayer.Priority.HIGH.getIcon(), String.format("The execution of 'ng new %s' was canceled by the user.", projectName), null);

                        ph.progress("Opening project");

                        FileObject dir = FileUtil.toFileObject(dirF);
                        dir.refresh();
                        // TODO show error and abort if generation failed (f.e. missing package.json whatever)

                        Project p = FileOwnerQuery.getOwner(dir);
                        if (null != p) {
                            OpenProjects.getDefault().open(new Project[]{p}, true, true);
                        } else {
                            // TODO show error and abort if no project found (can happen when JS plugins are disabled)
                        }
                    }
                } finally {
                    ph.finish();
                }
            }
        };
    }

    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();

            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }

            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                // TODO if using org.openide.dialogs >= 7.8, can use WizardDescriptor.PROP_*:
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps);
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir", null);
        this.wiz.putProperty("name", null);
        this.wiz = null;
        panels = null;
    }

    @Override
    public String name() {
        return MessageFormat.format("{0} of {1}", new Object[]{index + 1, panels.length});
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }
}
