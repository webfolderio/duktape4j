package io.webfolder.ducktape4j;

import static io.webfolder.ducktape4j.Duktape.create;
import org.junit.Test;

public class TestDuktape {
    public static interface Console {
        void message(String message);
    }

    @Test
    public void testConsole() {
        try (Duktape context = create()) {
            context.set("console", Console.class, new Console() {
                @Override
                public void message(String message) {
                    System.out.println(message);
                }
            });
            context.evaluate("console.message('hello, world!');");
        }
    }

    @Test(expected = DuktapeException.class)
    public void testException() {
        try (Duktape context = create()) {
            context.evaluate("console.message('hello, world!");
        }
    }
}