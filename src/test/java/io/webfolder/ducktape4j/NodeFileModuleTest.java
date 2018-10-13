package io.webfolder.ducktape4j;

import static java.nio.file.Paths.get;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NodeFileModuleTest {

    @Test
    public void test() {
        Duktape context = Duktape.create();
        new FileModuleLoader(context, get("src/test/resources/modules/node")).init();

        context.evaluate("global = { };");
        context.evaluate("console = {};");
        context.evaluate("setTimeout = null;");
        context.evaluate("clearTimeout = null;");
        context.evaluate("setInterval = null;");
        context.evaluate("clearInterval = null;");
        context.evaluate("var domino = require('domino');");

        context.evaluate("var window = domino.createWindow('<h1>Hello world</h1>', 'http://example.com');");
        context.evaluate("var document = window.document;");
        context.evaluate("var h1 = document.querySelector('h1');");

        Object html = context.evaluate("h1.innerHTML");
        Object instanceOf = context.evaluate("h1 instanceof window.Element");

        assertEquals("Hello world", (String) html);
        assertEquals(true, instanceOf);

        context.close();
    }
}
