package io.webfolder.ducktape4j;

public class ModuleNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -9213820383113132340L;

    public ModuleNotFoundException(String message) {
        super(message);
    }
}
