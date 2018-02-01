package net.chrizzly.netbeans.plugins.nbangularcli.projectwizard;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

/**
 *
 * @author Chrl
 */
public class AngularCliSetupPanelVisual extends JPanel implements DocumentListener {

    public AngularCliSetupPanelVisual(AngularCliSetupPanel aThis) {
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    boolean valid(WizardDescriptor wizardDescriptor) {
        // TODO: Check the checkboxes?

        return true;
    }

    void read(WizardDescriptor settings) {
        // TODO: Read settings from checkboxes.
    }

    void store(WizardDescriptor d) {
        // TODO: Store settings.
    }

    void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }
}