package io.webfolder.ducktape4j;

public interface ModuleLoader {

    public void init();

    String search(String id);
}
