package net.chrizzly.netbeans.plugins.nbangularcli.projectwizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import net.chrizzly.netbeans.plugins.nbangularcli.options.AngularCliPanel;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.templates.TemplateRegistration;
import org.openide.WizardDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

// TODO define position attribute
@TemplateRegistration(
        folder = "Project/ClientSide",
        displayName = "#AngularCliApplicaton_displayName",
        description = "AngularCliApplicatonDescription.html",
        iconBase = "net/chrizzly/netbeans/plugins/nbangularcli/projectwizard/angular.png"
)
@Messages("AngularCliApplicaton_displayName=Angular CLI Application")
public class AngularCliWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    public AngularCliWizardIterator() {
    }

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
            NbBundle.getMessage(AngularCliWizardIterator.class, "LBL_CreateProjectStep"),
        };
    }

    @Override
    public Set instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    @Override
    public Set instantiate(final ProgressHandle handle) throws IOException {
        ProgressUtils.showProgressDialogAndRun(() -> {
            createNgCliApp(handle);
        }, "Creating Angular CLI application...");

        return Collections.emptySet();
    }

    private Process process;

    private void createNgCliApp(final ProgressHandle handle) {
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty("projdir"));
        dirF.mkdirs();

        Callable<Process> callable = () -> {
            String ngCli = NbPreferences.forModule(AngularCliPanel.class).get("ngCliExecutableLocation", "");

            process = new ExternalProcessBuilder(ngCli)
                    .addArgument(String.format("new %s", this.wiz.getProperty("name")))
                    .workingDirectory(dirF).call();

            return process;
        };

        ExecutionDescriptor descriptor = new ExecutionDescriptor()
                .frontWindow(true)
                .inputVisible(true)
                .postExecution(() -> {
                    StatusDisplayer.getDefault().setStatusText("Created: " + dirF.getPath());
                });

        ExecutionService service = ExecutionService.newService(callable, descriptor, "Anuglar CLI");
        Future<Integer> future = service.run();
        try {
            future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex.getCause());
        }

        FileObject dir = FileUtil.toFileObject(dirF);
        dir.refresh();

        Project p = FileOwnerQuery.getOwner(dir);
        OpenProjects.getDefault().open(new Project[]{p}, true, true);
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
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
        return MessageFormat.format("{0} of {1}",
                new Object[]{new Integer(index + 1), new Integer(panels.length)});
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
