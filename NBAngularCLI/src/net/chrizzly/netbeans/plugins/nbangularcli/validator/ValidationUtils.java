package net.chrizzly.netbeans.plugins.nbangularcli.validator;

import org.openide.util.NbBundle;

public final class ValidationUtils {

    public static final String ANGULAR_CLI_PATH = "angular-cli.path"; // NOI18N

    private ValidationUtils() {
    }

    @NbBundle.Messages("ValidationUtils.ngcli.name=Angular CLI")
    public static void validateAngularCli(ValidationResult result, String angularCli) {
        String warning = ExternalExecutableValidator.validateCommand(angularCli, Bundle.ValidationUtils_ngcli_name());

        if (warning != null) {
            result.addWarning(new ValidationResult.Message(ANGULAR_CLI_PATH, warning));
        }
    }
}