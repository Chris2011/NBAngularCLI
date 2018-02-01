package net.chrizzly.netbeans.plugins.nbangularcli.projectwizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Chrl
 */
public class AngularCliSetupPanel implements WizardDescriptor.Panel,
        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizardDescriptor;
    private AngularCliSetupPanelVisual component;

    public AngularCliSetupPanel() {
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new AngularCliSetupPanelVisual(this);
            component.setName(NbBundle.getMessage(AngularCliLocationPanel.class, "LBL_ExecuteAngularCliStep"));
        }

        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.js.vuejs2.wizard.VueJsCliSetupPanel");
    }

    @Override
    public void readSettings(Object data) {
//        wizardDescriptor = (WizardDescriptor) data;
//        component.read(wizardDescriptor);

        // HINT: Nothing to read.
    }

    @Override
    public void storeSettings(Object data) {
//        WizardDescriptor d = (WizardDescriptor) data;
//        component.store(d);

        // HINT: Nothing to store.
    }

    @Override
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }

    private final Set<ChangeListener> listeners = new HashSet<>(1); // or can use ChangeSupport in NB 6.0

    @Override
    public void addChangeListener(ChangeListener cl) {
        synchronized (listeners) {
            listeners.add(cl);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        synchronized (listeners) {
            listeners.remove(cl);
        }
    }

    protected final void fireChangeEvent() {
        Set<ChangeListener> ls;
        synchronized (listeners) {
            ls = new HashSet<>(listeners);
        }
        ChangeEvent ev = new ChangeEvent(this);
        ls.forEach((l) -> {
            l.stateChanged(ev);
        });
    }

    @Override
    public void validate() throws WizardValidationException {
        getComponent();

        component.validate(wizardDescriptor);
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }
}
