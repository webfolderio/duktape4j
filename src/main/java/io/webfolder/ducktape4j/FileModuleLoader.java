package io.webfolder.ducktape4j;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import java.io.IOException;
import java.nio.file.Path;

public class FileModuleLoader implements ModuleLoader {

    private final Duktape context;

    public FileModuleLoader(Duktape context) {
        this.context = context;
    }

    public void init() {
        String name = getClass().getSimpleName();
        context.set(name, ModuleLoader.class, this);
        context.evaluate(format("Duktape.modSearch = function(id) { return %s.search(id); }",
                            name),
                            name + ".js");
    }

    @Override
    public String search(String id) {
        Path path = get(id + ".js");
        if ( ! exists(path) ) {
            throw new ModuleNotFoundException(id);
        }
        byte[] content;
        try {
            content = readAllBytes(path);
        } catch (IOException e) {
            throw new ModuleNotFoundException(e.getMessage());
        }
        String script = new String(content, UTF_8);
        return script;
    }        
}
