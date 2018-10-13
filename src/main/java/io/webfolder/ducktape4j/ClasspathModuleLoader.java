package io.webfolder.ducktape4j;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ClasspathModuleLoader implements ModuleLoader {

    private final Duktape context;

    private final String prefix;

    public ClasspathModuleLoader(Duktape context) {
        this(context, "");
    }

    public ClasspathModuleLoader(Duktape context, String prefix) {
        this.context = context;
        this.prefix = prefix;
    }

    @Override
    public void init() {
        String name = ModuleLoader.class.getSimpleName();
        context.set(name, ModuleLoader.class, this);
        context.evaluate(format("(function() { var _require = require; require = function(id) { return ModuleLoader.isIndex(id) ? _require(id + '/index.js') : _require(id); } })(); Duktape.modSearch = function(id) { return %s.search(id); }",
                            name),
                            name + ".js");
    }

    @Override
    public String search(String id) {
        InputStream is = null;
        ClassLoader cl = getClassLoader();
        try {
            is = cl.getResourceAsStream(prefix + (id.endsWith(".js") ? id : id + ".js"));
            if (is == null) {
                throw new ModuleNotFoundException(id);
            }
            String script = toString(is);
            return script;
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public boolean isIndex(String id) {
        InputStream is = null;
        ClassLoader cl = getClassLoader();
        try {
            is = cl.getResourceAsStream(prefix + (id.endsWith(".js") ? id : id + ".js"));
            if (is == null) {
                is = cl.getResourceAsStream(prefix + (id + "/index.js"));
                if ( is != null ) {
                    return true;
                }
            }
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return false;
    }

    protected String toString(InputStream is) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(is);
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        } finally {
            if ( scanner != null ) {
                scanner.close();
            }
        }
    }

    protected ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}
