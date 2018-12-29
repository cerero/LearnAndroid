#java环境变量配置
JAVA_HOME=`/usr/libexec/java_home -v 1.8`
export JAVA_HOME

PATH=$JAVA_HOME/bin:$PATH
export PATH

CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
export CLASSPATH
#--------------

#编译java
javac JniTest.java
#生成头文件
javah -jni JniTest &&
#编译c/c++
clang -I/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/include/darwin/ JniTest.c -dynamiclib -o libJniTest.dylib -std=c99
clang++ -I/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home/include/darwin/ JniTest.cpp -dynamiclib -o libJniTest.dylib
#加载动态库，通过jni调用c/c++方法
java -Djava.library.path=. JniTest
