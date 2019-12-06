/*
 * Copyright (C) 2016 Square, Inc.
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
#include <memory>
#include <mutex>
#include <chrono>
#include <jni.h>
#include "DuktapeContext.h"
#include "java/GlobalRef.h"
#include "java/JavaExceptions.h"

#if defined(__MINGW32__) || defined(_MSC_VER)
#define timegm _mkgmtime
#endif

#include <time.h>
#include <iomanip>
#include <sstream>

#include <time.h>
#include <iomanip>
#include <sstream>
namespace {

std::unique_ptr<GlobalRef> duktapeClass;
std::function<int(double)> getTimeZoneOffset = [](double d) { return 0; };

void initialize(JNIEnv* env, jclass type) {
  duktapeClass.reset(new GlobalRef(env, type));

  auto tzClass = env->FindClass("java/util/TimeZone");
  auto getDefaultTimeZone = env->GetStaticMethodID(tzClass, "getDefault", "()Ljava/util/TimeZone;");
  auto getOffset = env->GetMethodID(tzClass, "getOffset", "(J)I");

  const GlobalRef timeZoneClass(env, tzClass);
  getTimeZoneOffset = [timeZoneClass, getDefaultTimeZone, getOffset](double time) {
    auto theEnv = timeZoneClass.getJniEnv();
    auto timeZone = theEnv->CallStaticObjectMethod(static_cast<jclass>(timeZoneClass.get()),
                                                   getDefaultTimeZone);
    const std::chrono::milliseconds offsetMillis(theEnv->CallIntMethod(timeZone, getOffset, time));
    theEnv->DeleteLocalRef(timeZone);
    return static_cast<int>(std::chrono::duration_cast<std::chrono::seconds>(offsetMillis).count());
  };
}

} // anonymous namespace

extern "C" {


#if defined(__MINGW32__) || defined(_MSC_VER)

char* strptime(const char* s,
    const char* f,
    struct tm* tm) {
    // Isn't the C++ standard lib nice? std::get_time is defined such that its
    // format parameters are the exact same as strptime. Of course, we have to
    // create a string stream first, and imbue it with the current C locale, and
    // we also have to make sure we return the right things if it fails, or
    // if it succeeds, but this is still far simpler an implementation than any
    // of the versions in any of the C standard libraries.
    std::istringstream input(s);
    input.imbue(std::locale(setlocale(LC_ALL, nullptr)));
    input >> std::get_time(tm, f);
    if (input.fail()) {
        return nullptr;
    }
    return (char*)(s + input.tellg());
}
#endif

duk_int_t android__get_local_tzoffset(duk_double_t time) {
  return getTimeZoneOffset(time);
}

/**
 * Overload the default Duktape parser (which only does "%c"/ISO8601) to handle other date formats
 * that tend to appear in JavaScript docs around parsing dates.
 */
duk_bool_t android__date_parse_string(duk_context* ctx, const char* str) {
  // Ordered by likelihood (ideally %c/ISO8601 is the format we're given).
  static const char* dateFormats[] = {
      "%c",             // 2015-03-25T23:45:12
      "%Y/%m/%d %T",    // 2015/03/25 23:45:12
      "%Y/%m/%d",
      "%m/%d/%Y %T",    // 03/25/2015 23:45:12
      "%m/%d/%Y",
      "%b %d %Y %T",    // Mar[ch] 25 2015 23:45:12
      "%b %d %Y",
      "%d %b %Y %T",    // 25 Mar[ch] 2015 23:45:12
      "%d %b %Y",
      "%a %b %d %Y %T", // Wed[nesday] Mar[ch] 25 2015 23:45:12
      "%a %b %d %Y",
  };
  tm tm;
  int timezoneOffset = android__get_local_tzoffset(0);
  for (const auto dateFormat : dateFormats) {
    memset(&tm, 0, sizeof(tm));
    if (!strptime(str, dateFormat, &tm)) {
      // No dice.
      continue;
    }
    tm.tm_isdst = -1; // Not set by strptime - unknown if DST.
    const auto t = timegm(&tm);
    if (t >= 0) {
      duk_push_number(ctx, (t - timezoneOffset) * 1000.0);
      return true;
    }
  }
  return false;
}

JNIEXPORT jlong JNICALL
Java_io_webfolder_ducktape4j_Duktape_createContext(JNIEnv* env, jclass type) {
  static std::once_flag initialized;
  std::call_once(initialized, initialize, std::ref(env), type);

  JavaVM* javaVM;
  env->GetJavaVM(&javaVM);
  try {
    return reinterpret_cast<jlong>(new DuktapeContext(javaVM));
  } catch (std::bad_alloc&) {
    return 0L;
  }
}

JNIEXPORT void JNICALL
Java_io_webfolder_ducktape4j_Duktape_destroyContext(JNIEnv *env, jclass type, jlong context) {
  delete reinterpret_cast<DuktapeContext*>(context);
}

JNIEXPORT jobject JNICALL
Java_io_webfolder_ducktape4j_Duktape_evaluate__JLjava_lang_String_2Ljava_lang_String_2(
    JNIEnv* env, jclass type, jlong context, jstring code, jstring fname) {
  DuktapeContext* duktape = reinterpret_cast<DuktapeContext*>(context);
  if (duktape == nullptr) {
    queueNullPointerException(env, "Null Duktape context - did you close your Duktape?");
    return nullptr;
  }
  try {
    return duktape->evaluate(env, code, fname);
  } catch (const std::invalid_argument& e) {
    queueIllegalArgumentException(env, e.what());
  } catch (const std::exception& e) {
    queueDuktapeException(env, e.what());
  }
  return nullptr;
}

JNIEXPORT void JNICALL
Java_io_webfolder_ducktape4j_Duktape_set(JNIEnv *env, jclass type, jlong context, jstring name,
                                       jobject object, jobjectArray methods) {
  DuktapeContext* duktape = reinterpret_cast<DuktapeContext*>(context);
  if (duktape == nullptr) {
    queueNullPointerException(env, "Null Duktape context - did you close your Duktape?");
    return;
  }
  try {
    duktape->set(env, name, object, methods);
  } catch (const std::invalid_argument& e) {
    queueIllegalArgumentException(env, e.what());
  } catch (const std::exception& e) {
    queueDuktapeException(env, e.what());
  }
}

JNIEXPORT jlong JNICALL
Java_io_webfolder_ducktape4j_Duktape_get(JNIEnv *env, jclass type, jlong context, jstring name,
                                        jobjectArray methods) {
  DuktapeContext* duktape = reinterpret_cast<DuktapeContext*>(context);
  if (duktape == nullptr) {
    queueNullPointerException(env, "Null Duktape context - did you close your Duktape?");
    return 0L;
  }

  try {
    return reinterpret_cast<jlong>(duktape->get(env, name, methods));
  } catch (const std::invalid_argument& e) {
    queueIllegalArgumentException(env, e.what());
  } catch (const std::exception& e) {
    queueDuktapeException(env, e.what());
  }
  return 0L;
}

JNIEXPORT jobject JNICALL
Java_io_webfolder_ducktape4j_Duktape_call(JNIEnv *env, jclass type, jlong context, jlong instance,
                                       jobject method, jobjectArray args) {
  // Validate our DuktapeContext first - if the context is null, we can't use the proxy.
  DuktapeContext* duktape = reinterpret_cast<DuktapeContext*>(context);
  if (duktape == nullptr) {
    queueNullPointerException(env, "Null Duktape context - did you close your Duktape?");
    return nullptr;
  }

  const JavaScriptObject* object = reinterpret_cast<const JavaScriptObject*>(instance);
  if (object == nullptr) {
    queueNullPointerException(env, "Invalid JavaScript object");
    return nullptr;
  }

  try {
    return object->call(env, method, args);
  } catch (const std::invalid_argument& e) {
    queueIllegalArgumentException(env, e.what());
  } catch (const std::exception& e) {
    queueDuktapeException(env, e.what());
  }
  return nullptr;
}

} // extern "C"
