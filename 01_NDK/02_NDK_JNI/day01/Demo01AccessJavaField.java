class Demo01AccessJavaField {
  static {
    System.loadLibrary("Demo01AccessJavaField");
  }
  public String key = "jack";
  public static int count = 0;

  public native void changeJavaField();
  public native void changeJavaStaticField();
  public native void callJavaMethod();
  public native void callJavaStaticMethod();

  public static void main(String[] args) {
    Demo01AccessJavaField accessJavaField = new Demo01AccessJavaField();
    System.out.printf("before modify, key = " + accessJavaField.key + "\n");
    accessJavaField.changeJavaField();
    System.out.printf("after modify, key = " + accessJavaField.key + "\n");

    System.out.printf("before modify, count = " + Demo01AccessJavaField.count + "\n");
    accessJavaField.changeJavaStaticField();
    System.out.printf("after modify, key = " + Demo01AccessJavaField.count + "\n");

    accessJavaField.callJavaMethod();
    
    accessJavaField.callJavaStaticMethod();
  }

  public int getAge(int base) {
    System.out.printf("java method: int getAge(int) has been call\n");
    return base * 2;
  }

  public static String getUUID() {
    System.out.printf("java static method: gegetUUIDtAge() has been call\n");
    return "hi suyi";
  }
}
