class JniTest {
  static {
		System.loadLibrary("JniTest");
	}

  public native static String getStringFromC();

  public static void main(String[] args) {
		String str_from_c = getStringFromC();
		System.out.println(str_from_c);
	}
}
