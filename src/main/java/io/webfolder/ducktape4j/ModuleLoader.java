package io.webfolder.ducktape4j;

public interface ModuleLoader {

    void init();

    String search(String id);

    boolean isIndex(String id);
}
