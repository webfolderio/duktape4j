C:\Python27\Scripts\pip2.exe install pyyaml
git clone --quiet https://github.com/svaarala/duktape.git
cd duktape
git checkout --quiet 04a08e8cbe7101ee70e7f6de33619c5cb7f199ce
C:\Python27\python.exe tools/configure.py ^
 --source-directory src-input ^
 --output-directory src-custom ^
 --config-metadata config ^
 --platform windows ^
 --architecture x64 ^
 --compiler gcc ^
 --option-file config/examples/performance_sensitive.yaml ^
 -DDUK_USE_GET_MONOTONIC_TIME_WINDOWS_QPC ^
 -DDUK_USE_DATE_NOW_WINDOWS
cd ..
git clone https://github.com/square/duktape-android.git
cd duktape-android
git checkout 85e17a5e2e3826a5fc1fbf1e33534c726cca327f
cd ..
copy /Y duktape\src-custom\*.* duktape-android\duktape\src\main\jni\duktape
copy /Y CMakeLists.txt duktape-android\duktape\src\main\jni\CMakeLists.txt
copy /Y duktape-jni.cpp duktape-android\duktape\src\main\jni\duktape-jni.cpp
copy /Y JavaMethod.cpp duktape-android\duktape\src\main\jni\java\JavaMethod.cpp
copy /Y JavaExceptions.cpp duktape-android\duktape\src\main\jni\java\JavaExceptions.cpp
"C:\Program Files\git\usr\bin\patch.exe" duktape-android\duktape\src\main\jni\duktape\duk_config.h timezone.patch
"C:\Program Files\git\usr\bin\patch.exe" duktape-android\duktape\src\main\jni\java\JavaType.cpp javatype.patch
cd duktape-android\duktape\src\main\jni
mkdir build
cd build
cmake .. -G "MSYS Makefiles"
make
cd ..\..\..\..\..\..
mkdir ..\src\main\resources\META-INF
copy /Y duktape-android\duktape\src\main\jni\build\libduktape.dll ..\src\main\resources\META-INF\duktape.dll
strip ..\src\main\resources\META-INF\duktape.dll