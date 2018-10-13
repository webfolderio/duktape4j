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

    private final Path root;

    public FileModuleLoader(Duktape context) {
        this(context, get(".").toAbsolutePath());
    }

    public FileModuleLoader(Duktape context, Path root) {
        this.context = context;
        this.root = root;
    }

    public void init() {
        String name = ModuleLoader.class.getSimpleName();
        context.set(name, ModuleLoader.class, this);
        context.evaluate(format("(function() { var _require = require; require = function(id) { return ModuleLoader.isIndex(id) ? _require(id + '/index.js') : _require(id); } })(); Duktape.modSearch = function(id) { return %s.search(id); }",
                            name),
                            name + ".js");
    }

    @Override
    public String search(String id) {
        Path path = root.resolve(id.endsWith(".js") ? id : id + ".js");
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

    @Override
    public boolean isIndex(String id) {
        boolean found = exists(root.resolve(id.endsWith(".js") ? id : id + ".js"));
        if ( ! found ) {
            return exists(root.resolve(id).resolve("index.js"));
        }
        return false;
    }
}
