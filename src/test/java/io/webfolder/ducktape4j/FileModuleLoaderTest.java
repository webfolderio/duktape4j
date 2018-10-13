package io.webfolder.ducktape4j;

import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FileModuleLoaderTest {

    @Test
    public void test() {
        Duktape context = Duktape.create();
        new FileModuleLoader(context, get("src/test/resources")).init();
        Object bar = context.evaluate("var dummy = require('dummy'); dummy.foo;");
        assertEquals(bar, "bar");
        context.close();
    }
}
