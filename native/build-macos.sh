pip2 show pyyaml || pip2 install pyyaml
git clone --quiet https://github.com/svaarala/duktape.git
cd duktape
git checkout --quiet 04a08e8cbe7101ee70e7f6de33619c5cb7f199ce
python2 tools/configure.py \
 --source-directory src-input \
 --output-directory src-custom \
 --config-metadata config \
 --platform apple \
 --architecture x64 \
 --compiler gcc \
 --option-file config/examples/performance_sensitive.yaml
cd ..
git clone https://github.com/square/duktape-android.git
cd duktape-android
git checkout 85e17a5e2e3826a5fc1fbf1e33534c726cca327f
cd ..
cp duktape/src-custom/* duktape-android/duktape/src/main/jni/duktape
cp CMakeLists.txt duktape-android/duktape/src/main/jni/CMakeLists.txt
cp duktape-jni.cpp duktape-android/duktape/src/main/jni/duktape-jni.cpp
cp JavaMethod.cpp duktape-android/duktape/src/main/jni/java/JavaMethod.cpp
cp JavaExceptions.cpp duktape-android/duktape/src/main/jni/java/JavaExceptions.cpp
patch duktape-android/duktape/src/main/jni/duktape/duk_config.h timezone.patch
patch duktape-android/duktape/src/main/jni/java/JavaType.cpp javatype.patch
cd duktape-android/duktape/src/main/jni
mkdir build
cd build
CC=gcc-8 CXX=g++-8 cmake .. -DCMAKE_BUILD_TYPE=Release
make
cd ../../../../../..
mkdir -p ../src/main/resources/META-INF
cp -f duktape-android/duktape/src/main/jni/build/libduktape.dylib ../src/main/resources/META-INF/duktape.dylib
