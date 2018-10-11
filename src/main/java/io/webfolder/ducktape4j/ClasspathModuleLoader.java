package io.webfolder.ducktape4j;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class ClasspathModuleLoader implements ModuleLoader {

    private final Duktape context;

    public ClasspathModuleLoader(Duktape context) {
        this.context = context;
    }

    @Override
    public void init() {
        String name = getClass().getSimpleName();
        context.set(name, ModuleLoader.class, this);
        context.evaluate(format("Duktape.modSearch = function(id) { return %s.search(id); }",
                            name),
                            name + ".js");
    }

    @Override
    public String search(String id) {
        InputStream is = null;
        try {
            is = getClassLoader().getResourceAsStream(id + ".js");
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
