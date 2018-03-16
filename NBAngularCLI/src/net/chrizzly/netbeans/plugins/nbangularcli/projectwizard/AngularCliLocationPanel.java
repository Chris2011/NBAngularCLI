package net.chrizzly.netbeans.plugins.nbangularcli.projectwizard;

import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
//public class AngularCliLocationPanel implements WizardDescriptor.Panel,
//        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {
public class AngularCliLocationPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

    private WizardDescriptor wizardDescriptor;
    private AngularCliLocationPanelVisual angularCliPanelVisual;

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public AngularCliLocationPanelVisual getComponent() {
        if (angularCliPanelVisual == null) {
            angularCliPanelVisual = new AngularCliLocationPanelVisual(this);
            angularCliPanelVisual.setName(NbBundle.getMessage(AngularCliLocationPanel.class, "LBL_CreateProjectStep"));
        }

        return angularCliPanelVisual;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("net.chrizzly.netbeans.plugins.nbangularcli.projectwizard.AngularCliLocationPanel");
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        angularCliPanelVisual.read(settings);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        angularCliPanelVisual.store(settings);
    }

    private boolean hasText(String input) {
        return input != null && !input.trim().isEmpty();
    }

    @Override
    public boolean isValid() {
        // error
        String error = getComponent().getErrorMessage();

        if (hasText(error)) {
            setErrorMessage(error);

            return false;
        }

        // everything ok
        setErrorMessage(""); // NOI18N

        return true;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    private void setErrorMessage(String message) {
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }
}