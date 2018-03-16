package net.chrizzly.netbeans.plugins.nbangularcli.ui.options;

import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.chrizzly.netbeans.plugins.nbangularcli.options.AngularCliOptions;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@NbBundle.Messages({"AdvancedOption_DisplayName_NgCli=Angular CLI", "AdvancedOption_Keywords_NgCli=Angular, Angular CLI"})
@OptionsPanelController.SubRegistration(
    location = AngularCliOptionsPanelController.OPTIONS_CATEGORY,
    id = AngularCliOptionsPanelController.OPTIONS_SUBCATEGORY,
    displayName = "#AdvancedOption_DisplayName_NgCli"
//    keywords = "#AdvancedOption_Keywords_NgCli", // TODO: Maybe needed for the search
//    keywordsCategory = "Html5/AngularCLI" // TODO: Maybe needed for the search and keymap
)
public final class AngularCliOptionsPanelController extends OptionsPanelController implements ChangeListener {
    public static final String OPTIONS_CATEGORY = "Html5"; // NOI18N
    public static final String OPTIONS_SUBCATEGORY = "AngularCLI"; // NOI18N
    public static final String OPTIONS_PATH = OPTIONS_CATEGORY + "/" + OPTIONS_SUBCATEGORY; // NOI18N

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private AngularCliOptionsPanel angularCliOptionsPanel;
    private volatile boolean changed = false;
    private boolean firstOpening = true;

    @Override
    public void update() {
        assert EventQueue.isDispatchThread();
        if (firstOpening || !isChanged()) { // if panel is not modified by the user and he switches back to this panel, set to default
            firstOpening = false;
            getAngularCliOptionsPanel().setAngularCli(getAngularCliOptions().getAngularCli());
        }

        changed = false;
    }

    @Override
    public void applyChanges() {
        System.out.println("Test apply");
        EventQueue.invokeLater(() -> {
            getAngularCliOptions().setAngularCli(getAngularCliOptionsPanel().getAngularCli());

            changed = false;
        });
    }

    @Override
    public void cancel() {
        if (isChanged()) {
            getAngularCliOptionsPanel().setAngularCli(getAngularCliOptions().getAngularCli());
        }
    }

    @Override
    public boolean isValid() {
        assert EventQueue.isDispatchThread();
        return getAngularCliOptionsPanel().valid();
    }

    @Override
    public boolean isChanged() {
        String saved = getAngularCliOptions().getAngularCli();
        String current = getAngularCliOptionsPanel().getAngularCli().trim();

        return saved == null ? !current.isEmpty() : !saved.equals(current);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("net.chrizzly.netbeans.plugins.nbangularcli.options.AngularCliOptionsPanel"); // NOI18N
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getAngularCliOptionsPanel();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (!changed) {
            changed = true;
            propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        propertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private AngularCliOptions getAngularCliOptions() {
        return AngularCliOptions.getInstance();
    }

    private AngularCliOptionsPanel getAngularCliOptionsPanel() {
        assert EventQueue.isDispatchThread();

        if (angularCliOptionsPanel == null) {
            angularCliOptionsPanel = new AngularCliOptionsPanel();
            angularCliOptionsPanel.addChangeListener(this);
        }

        return angularCliOptionsPanel;
    }
}