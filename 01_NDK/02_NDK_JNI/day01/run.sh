#编译java
javac Demo01AccessJavaField.java
#生成头文件
javah -jni Demo01AccessJavaField
javap -s -p Demo01AccessJavaField
#编译c/c++
clang -I/Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home/include/darwin/ Demo01AccessJavaField.c -dynamiclib -o libDemo01AccessJavaField.dylib -std=c99
clang++ -I/Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_66.jdk/Contents/Home/include/darwin/ Demo01AccessJavaField.cpp -dynamiclib -o libDemo01AccessJavaField.dylib
#加载动态库，通过jni调用c/c++方法
java -Djava.library.path=. Demo01AccessJavaField
