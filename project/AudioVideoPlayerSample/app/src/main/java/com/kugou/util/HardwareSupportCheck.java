package com.kugou.util;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.util.Log;
import android.util.Range;

public class HardwareSupportCheck {
    @SuppressLint("NewApi")
    private static MediaCodecInfo selectCodec(String mimeType, boolean isEncoder) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (codecInfo.isEncoder() != isEncoder) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    @SuppressLint("NewApi")
    public static boolean isSupport(String mime, boolean isEncoder, int width, int height)
    {

        if( android.os.Build.VERSION.SDK_INT >= 16){
            boolean ret = false;
            try {
                MediaCodecInfo info = selectCodec(mime, isEncoder);
                if(info != null) {
                    if (android.os.Build.VERSION.SDK_INT >= 21) {//检测能否支持特定宽高
                        MediaCodecInfo.CodecCapabilities cap = info.getCapabilitiesForType(mime);
                        if (cap != null) {
                            MediaCodecInfo.VideoCapabilities vCap = cap.getVideoCapabilities();
                            if (vCap != null) {
                                ret = vCap.isSizeSupported(width, height);
                                Range<Integer> intWidthRange = vCap.getSupportedWidths();
                                Range<Integer> intHeightRange = vCap.getSupportedHeights();
                                Log.d("===HardwareSupportCheck isSupport===", "width: " + width + ", height: " + height + ", isSupport: " + ret);
                                Log.d("===HardwareSupportCheck isSupport===", "support widths " + intWidthRange);
                                Log.d("===HardwareSupportCheck isSupport===", "support height " + intHeightRange);
                            }
                        }
                    } else {
                        ret = true;
                    }

                }
            }catch (Exception e){
                Log.d("===HardwareSupportCheck isSupport===", "Exception:"+e );
                e.printStackTrace();
            }
            finally {
                return ret;
            }
        }
        else
        {
            return false;
        }
    }

    public static boolean isSupportH264() {
        return HardwareSupportCheck.isSupport("video/avc",false, -1, -1);
    }

    public static boolean isSupportH264(int width, int height) {
        return HardwareSupportCheck.isSupport("video/avc",false, width, height);
    }



}
