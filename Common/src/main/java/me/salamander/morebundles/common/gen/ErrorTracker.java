package me.salamander.morebundles.common.gen;

import java.util.ArrayList;
import java.util.List;

public class ErrorTracker {
    private boolean failed;
    private final List<String> errors;
    private final List<String> warnings;
    private final boolean failOnWarning;

    private ErrorTracker parent;

    public ErrorTracker(boolean failOnWarning) {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.failOnWarning = failOnWarning;
    }

    private ErrorTracker(List<String> errors, List<String> warnings, boolean failOnWarning, ErrorTracker parent) {
        this.errors = errors;
        this.warnings = warnings;
        this.failOnWarning = failOnWarning;
        this.parent = parent;
    }

    public void addError(final String error) {
        this.errors.add(error);
        fail();
    }

    public void addWarning(final String warning) {
        this.warnings.add(warning);
        failOnWarning();
    }

    private void fail(){
        this.failed = true;
        if(this.parent != null) {
            this.parent.fail();
        }
    }

    private void failOnWarning(){
        if(failOnWarning) {
            this.failed = true;
        }

        if(this.parent != null) {
            this.parent.failOnWarning();
        }
    }

    public List<String> errors() {
        return this.errors;
    }

    public List<String> warnings() {
        return this.warnings;
    }

    public boolean failed() {
        return this.failed;
    }

    public ErrorTracker sub(boolean failOnWarning){
        return new ErrorTracker(errors, warnings, failOnWarning, this);
    }
}
