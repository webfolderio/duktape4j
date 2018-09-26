/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.webfolder.ducktape4j;

import static java.io.File.pathSeparator;
import static java.lang.System.getProperty;
import static java.lang.System.load;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Locale.ENGLISH;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

/** A simple EMCAScript (Javascript) interpreter. */
public final class Duktape implements Closeable {

    private static final String  OS      = getProperty("os.name").toLowerCase(ENGLISH);

    private static final boolean WINDOWS = ";".equals(pathSeparator);

    private static final boolean LINUX   = "linux".contains(OS);

    private static final boolean MAC     = OS.contains("mac");

    static {
    	String path = getProperty("duktape4j.library.path");
    	if ( path != null && ! path.trim().isEmpty() ) {
    		load(path);
    	} else {
            if (WINDOWS) {
                loadLibrary("META-INF/duktape.dll");
            } else if (LINUX) {
                loadLibrary("META-INF/duktape.so");
            } else if (MAC) {
                loadLibrary("META-INF/duktape.dylib");
            }
    	}
    }

  /**
   * Create a new interpreter instance. Calls to this method <strong>must</strong> matched with
   * calls to {@link #close()} on the returned instance to avoid leaking native memory.
   */
  public static Duktape create() {
    long context = createContext();
    if (context == 0) {
      throw new OutOfMemoryError("Cannot create Duktape instance");
    }
    return new Duktape(context);
  }

  private long context;

  private Duktape(long context) {
    this.context = context;
  }

  /**
   * Evaluate {@code script} and return a result. {@code fileName} will be used in error
   * reporting. Note that the result must be one of the supported Java types or the call will
   * return null.
   *
   * @throws DuktapeException if there is an error evaluating the script.
   */
  public synchronized Object evaluate(String script, String fileName) {
    return evaluate(context, script, fileName);
  }
  /**
   * Evaluate {@code script} and return a result. Note that the result must be one of the
   * supported Java types or the call will return null.
   *
   * @throws DuktapeException if there is an error evaluating the script.
   */
  public synchronized Object evaluate(String script) {
    return evaluate(context, script, "?");
  }

  /**
   * Provides {@code object} to JavaScript as a global object called {@code name}. {@code type}
   * defines the interface implemented by {@code object} that will be accessible to JavaScript.
   * {@code type} must be an interface that does not extend any other interfaces, and cannot define
   * any overloaded methods.
   * <p>Methods of the interface may return {@code void} or any of the following supported argument
   * types: {@code boolean}, {@link Boolean}, {@code int}, {@link Integer}, {@code double},
   * {@link Double}, {@link String}.
   */
  public synchronized <T> void set(String name, Class<T> type, T object) {
    if (!type.isInterface()) {
      throw new UnsupportedOperationException("Only interfaces can be bound. Received: " + type);
    }
    if (type.getInterfaces().length > 0) {
      throw new UnsupportedOperationException(type + " must not extend other interfaces");
    }
    if (!type.isInstance(object)) {
      throw new IllegalArgumentException(object.getClass() + " is not an instance of " + type);
    }
    LinkedHashMap<String, Method> methods = new LinkedHashMap<String, Method>();
    for (Method method : type.getMethods()) {
      if (methods.put(method.getName(), method) != null) {
        throw new UnsupportedOperationException(method.getName() + " is overloaded in " + type);
      }
    }
    set(context, name, object, methods.values().toArray());
  }

  /**
   * Attaches to a global JavaScript object called {@code name} that implements {@code type}.
   * {@code type} defines the interface implemented in JavaScript that will be accessible to Java.
   * {@code type} must be an interface that does not extend any other interfaces, and cannot define
   * any overloaded methods.
   * <p>Methods of the interface may return {@code void} or any of the following supported argument
   * types: {@code boolean}, {@link Boolean}, {@code int}, {@link Integer}, {@code double},
   * {@link Double}, {@link String}.
   */
  @SuppressWarnings("unchecked")
public synchronized <T> T get(final String name, final Class<T> type) {
    if (!type.isInterface()) {
      throw new UnsupportedOperationException("Only interfaces can be proxied. Received: " + type);
    }
    if (type.getInterfaces().length > 0) {
      throw new UnsupportedOperationException(type + " must not extend other interfaces");
    }
    LinkedHashMap<String, Method> methods = new LinkedHashMap<String, Method>();
    for (Method method : type.getMethods()) {
      if (methods.put(method.getName(), method) != null) {
        throw new UnsupportedOperationException(method.getName() + " is overloaded in " + type);
      }
    }

    final long instance = get(context, name, methods.values().toArray());
    @SuppressWarnings("resource")
	final Duktape duktape = this;

    Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{ type },
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // If the method is a method from Object then defer to normal invocation.
            if (method.getDeclaringClass() == Object.class) {
              return method.invoke(this, args);
            }
            synchronized (duktape) {
              return call(duktape.context, instance, method, args);
            }
          }

          @Override
          public String toString() {
            return String.format("DuktapeProxy{name=%s, type=%s}", name, type.getName());
          }
        });
    return (T) proxy;
  }

  /**
   * Release the native resources associated with this object. You <strong>must</strong> call this
   * method for each instance to avoid leaking native memory.
   */
  @Override public synchronized void close() {
    if (context != 0) {
      long contextToClose = context;
      context = 0;
      destroyContext(contextToClose);
    }
  }

  @Override protected synchronized void finalize() throws Throwable {
    if (context != 0) {
      Logger.getLogger(getClass().getName()).warning("Duktape instance leaked!");
    }
  }

  private static native long createContext();
  private static native void destroyContext(long context);
  private static native Object evaluate(long context, String sourceCode, String fileName);
  private static native void set(long context, String name, Object object, Object[] methods);
  private static native long get(long context, String name, Object[] methods);
  private static native Object call(long context, long instance, Object method, Object[] args);

  private static void loadLibrary(String lib) {
      Path libFile;
      ClassLoader cl = Duktape.class.getClassLoader();
      String os = getProperty("os.name").toLowerCase(ENGLISH);
      boolean win = os.contains("windows");
      InputStream is = null;
      try {
    	  is = cl.getResourceAsStream(lib);
          libFile = createTempFile("duktape", win ? ".dll" : ".so");
          copy(is, libFile, REPLACE_EXISTING);
      } catch (IOException e) {
          throw new RuntimeException(e);
      } finally {
    	  if ( is != null ) {
    		  try {
				is.close();
			} catch (IOException e) {
				// ignore
			}
    	  }
      }
      libFile.toFile().deleteOnExit();
      load(libFile.toAbsolutePath().toString());
  }
}
