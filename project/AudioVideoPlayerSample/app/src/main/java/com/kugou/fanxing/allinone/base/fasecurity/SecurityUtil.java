package com.kugou.fanxing.allinone.base.fasecurity;


public class SecurityUtil {
    private static boolean hasInitLibrary = false;
    private static boolean hasInitSuccess = false;

    public SecurityUtil() {

    }

    public byte[] encrypt(byte[] plain_bytes) {
        boolean init_ret = init();
        if (!init_ret)
            return null;

        return encrypt_by_DES(plain_bytes);
    }

    public byte[] decrypt(byte[] secrete_bytes) {
        boolean init_ret = init();
        if (!init_ret)
            return null;

        return decrypt_by_DES(secrete_bytes);
    }

    private boolean init() {
        if (!hasInitLibrary) {
            hasInitLibrary = true;
            try {
                System.loadLibrary("security_util");
                hasInitSuccess = true;
            } catch (Throwable e) {
                hasInitSuccess = false;
            }
        }

        return hasInitSuccess;
    }
    private native byte[] encrypt_by_DES(byte[] plain_bytes);
    private native byte[] decrypt_by_DES(byte[] secrete_bytes);
}
