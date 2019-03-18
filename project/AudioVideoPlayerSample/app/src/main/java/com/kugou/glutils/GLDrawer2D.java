package com.kugou.glutils;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.kugou.util.LogWrapper;
import com.kugou.util.MatrixUtils;

/**
 * Helper class to drawExternalTex to whole view using specific texture and texture matrix
 */
public class GLDrawer2D {
	private static final boolean DEBUG = true; // TODO set false on release
	private static final String TAG = "GLDrawer2D";

	public static final String vss
		= "uniform mat4 uMVPMatrix;\n"
		+ "uniform mat4 uTexMatrix;\n"
		+ "attribute highp vec4 aPosition;\n"
		+ "attribute highp vec4 aTextureCoord;\n"
		+ "varying highp vec2 vTextureCoord;\n"
		+ "void main() {\n"
		    + "gl_Position = uMVPMatrix * aPosition;\n"
		    + "vTextureCoord = (uTexMatrix * aTextureCoord).xy;\n"
		+ "}\n";

	public static final String fss
		= "#extension GL_OES_EGL_image_external : require\n"
		+ "precision highp float;\n"
		+ "uniform samplerExternalOES sTexture;\n"
		+ "varying highp vec2 vTextureCoord;\n"
		+ "void main() {\n"
            + "vec2 refTextureCoord = vec2(vTextureCoord.s + 0.5, vTextureCoord.t);\n"
            + "vec4 refColor = texture2D(sTexture, refTextureCoord);\n"
            + "vec4 texel = texture2D(sTexture, vTextureCoord);\n"
            + "gl_FragColor = vec4(texel.rgb, refColor.b);\n"
		+ "} \n";

	public static final String yuvFSS
            = "precision mediump float; \n"
            + "uniform mat3 uYUVTransform;\n"
            + "varying vec2 vTextureCoord; \n"
            + "uniform sampler2D y_texture; \n"
            + "uniform sampler2D u_texture; \n"
            + "uniform sampler2D v_texture; \n"
            + "void main (void){  \n"
                + "mediump vec3 yuv; \n"
                + "mediump vec3 yuv_ref; \n"
                + "mediump vec3 rgb; \n"
                + "float a; \n"
                + "vec2 refTextureCoord = vec2(vTextureCoord.s + 0.5, vTextureCoord.t); \n"
                + "yuv_ref.x = texture2D(y_texture, refTextureCoord).r;\n"
                + "yuv_ref.y = texture2D(u_texture, refTextureCoord).r - 0.5;\n"
                + "yuv_ref.z = texture2D(v_texture, refTextureCoord).r - 0.5;\n"
                + "yuv.x = texture2D(y_texture, vTextureCoord).r;\n"
                + "yuv.y = texture2D(u_texture, vTextureCoord).r - 0.5;\n"
                + "yuv.z = texture2D(v_texture, vTextureCoord).r - 0.5;\n"
                + "rgb = uYUVTransform * yuv; \n"
                + "a = yuv_ref.x - 0.34414 * yuv_ref.y - 0.71414 * yuv_ref.z;\n"
                + "gl_FragColor = vec4(rgb, a + 0.05);  \n"
                + "} \n";

	private static final float[] VERTICES = {
	        1.0f, 1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            -1.0f, -1.0f };

    private static final float[] TEXCOORD = {
            0.5f, 0.0f,
            0.0f, 0.0f,
            0.5f, 1.0f,
            0.0f, 1.0f };

	private final FloatBuffer pVertex;
	private final FloatBuffer pTexCoord;
	private int hProgram;
    int maPositionLoc;
    int maTextureCoordLoc;
    int muMVPMatrixLoc;
    int muTexMatrixLoc;
    int muYUVTransform;

    int mYTextureLoc;
    int mUTextureLoc;
    int mVTextureLoc;

	private float[] mMvpMatrix;// = new float[16];

    private float[] mYUVTransformMatrix;

	private static final int FLOAT_SZ = Float.SIZE / 8;
	private static final int VERTEX_NUM = 4;
	private static final int VERTEX_SZ = VERTEX_NUM * 2;

	private boolean mSupportHWDecode;
	/**
	 * Constructor
	 * this should be called in GL context
	 */
	public GLDrawer2D(boolean supportHWDecode, int hProgram) {
        this.mSupportHWDecode = supportHWDecode;
		this.hProgram = hProgram;
		pVertex = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ).order(ByteOrder.nativeOrder()).asFloatBuffer();
		pVertex.put(VERTICES);
		pVertex.flip();

		pTexCoord = ByteBuffer.allocateDirect(VERTEX_SZ * FLOAT_SZ).order(ByteOrder.nativeOrder()).asFloatBuffer();
		pTexCoord.put(TEXCOORD);
		pTexCoord.flip();

		GLES20.glUseProgram(hProgram);

        maPositionLoc = GLES20.glGetAttribLocation(hProgram, "aPosition");
        maTextureCoordLoc = GLES20.glGetAttribLocation(hProgram, "aTextureCoord");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uMVPMatrix");
        muTexMatrixLoc = GLES20.glGetUniformLocation(hProgram, "uTexMatrix");

        if (!supportHWDecode) {
            muYUVTransform = GLES20.glGetUniformLocation(hProgram, "uYUVTransform");
            mYTextureLoc = GLES20.glGetUniformLocation(hProgram, "y_texture");
            mUTextureLoc = GLES20.glGetUniformLocation(hProgram, "u_texture");
            mVTextureLoc = GLES20.glGetUniformLocation(hProgram, "v_texture");

            //yuv 转 rgb的矩阵
            mYUVTransformMatrix = new float[]{
                    1.0f,   1.0f,   1.0f,
                    0.0f, -0.39465f,  2.03211f,
                    1.13983f,   -0.58060f,  0.0f
            };

            GLES20.glUniformMatrix3fv(muYUVTransform, 1, false, mYUVTransformMatrix, 0);
        }

        mMvpMatrix = MatrixUtils.getOriginalMatrix();
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, mMvpMatrix, 0);
		GLES20.glVertexAttribPointer(maPositionLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pVertex);
		GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, VERTEX_SZ, pTexCoord);
		GLES20.glEnableVertexAttribArray(maPositionLoc);
		GLES20.glEnableVertexAttribArray(maTextureCoordLoc);

        GLES20.glUseProgram(0);
	}

	public void onViewPortChange(int imgWidth, int imgHeight, int width, int height) {
//		GLES20.glUseProgram(hProgram);
        MatrixUtils.getMatrix(mMvpMatrix, MatrixUtils.TYPE_CENTERCROP, imgWidth / 2, imgHeight, width, height);
        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mMvpMatrix, 0);
//		GLES20.glUseProgram(0);
    }

    public void active(int externalTexId, int y_tex_id, int u_tex_id, int v_tex_id) {
        GLES20.glUseProgram(hProgram);
        if (mSupportHWDecode) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, externalTexId);
        }
//        else {
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, y_tex_id);
//
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, u_tex_id);
//
//            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, v_tex_id);
//        }
    }

	/**
	 * terminatinng, this should be called in GL context
	 */
	public void release() {
		if (hProgram >= 0)
			GLES20.glDeleteProgram(hProgram);
		hProgram = -1;
	}
	
	/**
	 * drawExternalTex specific texture with specific texture matrix
	 * @param tex_id texture ID
	 * @param tex_matrix texture matrix、if this is null, the last one use(we don't check size of this array and needs at least 16 of float)
	 */
	public void drawExternalTex(int tex_id, float[] tex_matrix) {
//		GLES20.glUseProgram(hProgram);
		if (tex_matrix != null)
			GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, tex_matrix, 0);

//		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex_id);
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_NUM);
//		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

//        GLES20.glUseProgram(0);
	}

    public void drawYUVTex(int y_tex_id, int u_tex_id, int v_tex_id, Buffer channelY, Buffer channelU, Buffer channelV, int yuvWidth, int yuvHeight, int uvWidth, int uvHeight, float[] tex_matrix) {
        if (channelY == null || channelU == null || channelV == null) {
            return;
        }

//        GLES20.glUseProgram(hProgram);

        if (tex_matrix != null)
            GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, tex_matrix, 0);

        // bind textures
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, y_tex_id);
        GLES20.glUniform1i(mYTextureLoc, 0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, yuvWidth, yuvHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, channelY);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, u_tex_id);
        GLES20.glUniform1i(mUTextureLoc, 1);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, uvWidth, uvHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, channelU);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, v_tex_id);
        GLES20.glUniform1i(mVTextureLoc, 2);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, uvWidth, uvHeight, 0,
                GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, channelV);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_NUM);
        GLES20.glFinish();

//        GLES20.glUseProgram(0);
    }

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
//            Utils.LOGE("***** " + op + ": glError " + error, null);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
	/**创建普通的内部纹理
     * @return texture ID
     * **/
	public static int initTex(int texUnit) {
        final int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		 LogWrapper.LOGV(TAG, "initTex:" + tex[0]);
        return tex[0];
    }

	/**
	 * 创建一个外部纹理，硬解后的输出纹理，也是openGL的读取用纹理
	 * @return texture ID
	 */
	public static int initExternalOESTex() {
		 LogWrapper.LOGV(TAG, "initExternalOESTex:");
		final int[] tex = new int[1];
		GLES20.glGenTextures(1, tex, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		return tex[0];
	}

	/**
	 * delete specific texture
	 */
	public static void deleteTex(int hTex) {
		 LogWrapper.LOGV(TAG, "deleteTex:" + hTex);
		final int[] tex = new int[] {hTex};
		GLES20.glDeleteTextures(1, tex, 0);
	}

	public static int loadShader(String vss, String fss, int[] result) {
		 LogWrapper.LOGV(TAG, "loadShader:");
		int vs = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(vs, vss);
		GLES20.glCompileShader(vs);
		final int[] compiled = new int[1];
		GLES20.glGetShaderiv(vs, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			 LogWrapper.LOGE(TAG, "Failed to compile vertex shader:"
					+ GLES20.glGetShaderInfoLog(vs));
			GLES20.glDeleteShader(vs);
			vs = 0;
			result[0] = -1;
		} else {
			result[0] = 0;
		}

		int fs = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(fs, fss);
		GLES20.glCompileShader(fs);
		GLES20.glGetShaderiv(fs, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			 LogWrapper.LOGE(TAG, "Failed to compile fragment shader:"
					+ GLES20.glGetShaderInfoLog(fs));
			GLES20.glDeleteShader(fs);
			fs = 0;
			result[1] = -1;
		} else {
			result[1] = 0;
		}

		final int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vs);
		GLES20.glAttachShader(program, fs);
		GLES20.glLinkProgram(program);

		return program;
	}

}
