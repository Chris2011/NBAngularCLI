package net.chrizzly.netbeans.plugins.nbangularcli.projectwizard;

import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
//public class AngularCliLocationPanel implements WizardDescriptor.Panel,
//        WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {
public class AngularCliLocationPanel implements FinishablePanel<WizardDescriptor> {
    private WizardDescriptor wizardDescriptor;
    private AngularCliLocationPanelVisual angularCliPanelVisual;

    @Override
    public boolean isFinishPanel() {
        return true;
    }
    
    private final Set<ChangeListener> listeners = new HashSet<>(1); // or can use ChangeSupport in NB 6.0
    
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
        getComponent();
        
        return angularCliPanelVisual.valid(wizardDescriptor);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }
}