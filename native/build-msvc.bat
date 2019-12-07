C:\Python27\Scripts\pip2.exe install pyyaml
git clone --quiet https://github.com/svaarala/duktape.git
cd duktape
git checkout --quiet f033815df73441bb440fdfcfd1c11c48a85705f0
C:\Python27\python.exe tools/configure.py ^
 --source-directory src-input ^
 --output-directory src-custom ^
 --config-metadata config ^
 --platform windows ^
 --architecture x64 ^
 --compiler msvc ^
 --option-file config/examples/performance_sensitive.yaml ^
 -DDUK_USE_GET_MONOTONIC_TIME_WINDOWS_QPC ^
 -DDUK_USE_DATE_NOW_WINDOWS_SUBMS
cd ..
git clone --quiet https://github.com/square/duktape-android.git
cd duktape-android
git checkout --quiet c4f21bf2fd9f24d1739048dc6f51d3bd1d5261b8
cd ..
copy /Y duktape\src-custom\*.* duktape-android\duktape\src\main\jni\duktape
copy /Y duktape\extras\module-duktape\duk_module_duktape.h duktape-android\duktape\src\main\jni\duktape
copy /Y duktape\extras\module-duktape\duk_module_duktape.c duktape-android\duktape\src\main\jni\duktape
copy /Y CMakeLists.txt duktape-android\duktape\src\main\jni\CMakeLists.txt
copy /Y duktape-jni.cpp duktape-android\duktape\src\main\jni\duktape-jni.cpp
copy /Y JavaMethod.cpp duktape-android\duktape\src\main\jni\java\JavaMethod.cpp
copy /Y JavaExceptions.cpp duktape-android\duktape\src\main\jni\java\JavaExceptions.cpp
copy /Y strptime.h duktape-android\duktape\src\main\jni\strptime.h
copy /Y strptime.c duktape-android\duktape\src\main\jni\strptime.c
"C:\Program Files\git\usr\bin\patch.exe" duktape-android\duktape\src\main\jni\duktape\duk_config.h timezone.patch
"C:\Program Files\git\usr\bin\patch.exe" duktape-android\duktape\src\main\jni\java\JavaType.cpp javatype.patch
"C:\Program Files\git\usr\bin\patch.exe" duktape-android\duktape\src\main\jni\DuktapeContext.h DuktapeContext.h.patch
"C:\Program Files\git\usr\bin\patch.exe" duktape-android\duktape\src\main\jni\DuktapeContext.cpp DuktapeContext.cpp.patch
mkdir duktape-android\duktape\src\main\jni\build 2> NUL
copy /Y duktape.vcxproj.patch duktape-android\duktape\src\main\jni\build\duktape.vcxproj.patch
cd duktape-android\duktape\src\main\jni\build
cmake .. -G "Visual Studio 15 2017 Win64"
"C:\Program Files\Git\usr\bin\dos2unix.exe" duktape.vcxproj
"C:\Program Files\git\usr\bin\patch.exe" duktape.vcxproj duktape.vcxproj.patch
"C:\Program Files\Git\usr\bin\unix2dos.exe" duktape.vcxproj
cmake --build . --config Release
cd ..\..\..\..\..\..\..
mkdir src\main\resources\META-INF 2> NUL
copy /Y native\duktape-android\duktape\src\main\jni\build\Release\duktape.dll src\main\resources\META-INF\duktape.dll