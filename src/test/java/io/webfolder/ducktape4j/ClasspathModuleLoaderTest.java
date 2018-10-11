package io.webfolder.ducktape4j;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ClasspathModuleLoaderTest {

    @Test
    public void test() {
        Duktape context = Duktape.create();
        new ClasspathModuleLoader(context).init();
        Object bar = context.evaluate("var dummy = require('dummy'); dummy.foo;");
        assertEquals(bar, "bar");
        context.close();
    }
}
