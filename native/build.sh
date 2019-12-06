pip2 show pyyaml || pip2 install pyyaml
git clone --quiet https://github.com/svaarala/duktape.git
cd duktape
git checkout --quiet f033815df73441bb440fdfcfd1c11c48a85705f0
python2 tools/configure.py \
 --source-directory src-input \
 --output-directory src-custom \
 --config-metadata config \
 --platform linux \
 --architecture x64 \
 --compiler gcc \
 --option-file config/examples/performance_sensitive.yaml
cd ..
git clone --quiet https://github.com/square/duktape-android.git
cd duktape-android
git checkout --quiet c4f21bf2fd9f24d1739048dc6f51d3bd1d5261b8
cd ..
cp duktape/src-custom/* duktape-android/duktape/src/main/jni/duktape
cp duktape/extras/module-duktape/duk_module_duktape.h duktape-android/duktape/src/main/jni/duktape
cp duktape/extras/module-duktape/duk_module_duktape.c duktape-android/duktape/src/main/jni/duktape
cp CMakeLists.txt duktape-android/duktape/src/main/jni/CMakeLists.txt
cp duktape-jni.cpp duktape-android/duktape/src/main/jni/duktape-jni.cpp
cp JavaMethod.cpp duktape-android/duktape/src/main/jni/java/JavaMethod.cpp
cp JavaExceptions.cpp duktape-android/duktape/src/main/jni/java/JavaExceptions.cpp
patch duktape-android/duktape/src/main/jni/duktape/duk_config.h timezone.patch
patch duktape-android/duktape/src/main/jni/java/JavaType.cpp javatype.patch
patch duktape-android/duktape/src/main/jni/DuktapeContext.h DuktapeContext.h.patch
patch duktape-android/duktape/src/main/jni/DuktapeContext.cpp DuktapeContext.cpp.patch
cd duktape-android/duktape/src/main/jni
mkdir build
cd build
cmake .. -DCMAKE_BUILD_TYPE=Release
make
cd ../../../../../..
mkdir -p ../src/main/resources/META-INF
cp -f duktape-android/duktape/src/main/jni/build/libduktape.so ../src/main/resources/META-INF/duktape.so
strip ../src/main/resources/META-INF/duktape.so