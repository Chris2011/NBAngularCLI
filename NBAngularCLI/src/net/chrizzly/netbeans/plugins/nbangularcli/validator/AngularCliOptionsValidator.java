package net.chrizzly.netbeans.plugins.nbangularcli.validator;

import net.chrizzly.netbeans.plugins.nbangularcli.options.AngularCliOptions;

public class AngularCliOptionsValidator {

    private final ValidationResult result = new ValidationResult();

    public AngularCliOptionsValidator validateAngularCli() {
        return AngularCliOptionsValidator.this.validateAngularCli(AngularCliOptions.getInstance().getAngularCli());
    }

    public AngularCliOptionsValidator validateAngularCli(String angularCli) {
        ValidationUtils.validateAngularCli(result, angularCli);

        return this;
    }

    public ValidationResult getResult() {
        return result;
    }

}