package io.webfolder.ducktape4j;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VueTest {

    private long start;

    @Before
    public void before() {
        start = System.currentTimeMillis();
    }

    @After
    public void end() {
        System.out.println("elapsed time: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void test() throws IOException {
        Duktape context = Duktape.create();
        new ClasspathModuleLoader(context, "modules/node/").init();

        context.evaluate("global = { };"); // @see domino/config.js
        context.evaluate("console = {};");
        context.evaluate("setTimeout = function() { };");
        context.evaluate("clearTimeout = null;");
        context.evaluate("setInterval = null;");
        context.evaluate("clearInterval = null;");
        context.evaluate("var domino = require('domino');");

        context.evaluate("var domino = require('domino');");
        context.evaluate("var window = domino.createWindow('<div id=\"app\">{{ message }}</div>', 'http://example.com');");
        context.evaluate("var document = window.document;");

        byte[] content = Files.readAllBytes(Paths.get("src/test/resources/vue.js"));
        String script = new String(content, UTF_8);
        context.evaluate(script);

        context.evaluate("new Vue({el: '#app', data: { message: 'Hello Vue!' }});");

        assertEquals("Hello Vue!", context.evaluate("document.querySelector('div').innerHTML"));
        
        context.close();
    }
}
