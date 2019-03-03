package com.example.ndkguide.dm05LiveCamera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.ImageView;

import com.example.ndkguide.R;

import java.io.IOException;
import java.util.List;

public class LiveCameraActivity extends Activity implements TextureView.SurfaceTextureListener, Camera.PreviewCallback {
    static {
        System.loadLibrary("livecamera");
    }

    private Camera mCamera;
    private TextureView mTextureView;
    private byte[] mVideoSource;
    private ImageView mImageViewR, mImageViewG, mImageViewB;
    private Bitmap mImageR, mImageG, mImageB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livecamera);

        mTextureView = (TextureView) findViewById(R.id.preview);
        mImageViewR = ((ImageView)findViewById(R.id.imageViewR));
        mImageViewG = ((ImageView)findViewById(R.id.imageViewG));
        mImageViewB = ((ImageView)findViewById(R.id.imageViewB));
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCamera != null) {
            decode(mImageR, data, 0xFFFF0000);
            decode(mImageG, data, 0xFF00FF00);
            decode(mImageB, data, 0xFF0000FF);
            mImageViewR.invalidate();
            mImageViewG.invalidate();
            mImageViewB.invalidate();
            mCamera.addCallbackBuffer(mVideoSource);
        }
    }

    public native void decode(Bitmap pTarget, byte[] pSource, int pFilter);

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();
        try {
            mCamera.setPreviewTexture(surface);
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.setDisplayOrientation(0);

            Size size = findBestResolution(width, height);
            PixelFormat pixelFormat = new PixelFormat();

            Camera.Parameters parameters = mCamera.getParameters();

            PixelFormat.getPixelFormatInfo(parameters.getPreviewFormat(), pixelFormat);
            int sourceSize = size.width * size.height * pixelFormat.bitsPerPixel / 8;
            // Set-up camera size and video format.
            // should be the default on Android anyway.
            parameters.setPreviewSize(size.width, size.height);
            parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            mCamera.setParameters(parameters);

            mVideoSource = new byte[sourceSize];
            mImageR = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
            mImageG = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
            mImageB = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
            mImageViewR.setImageBitmap(mImageR);
            mImageViewG.setImageBitmap(mImageG);
            mImageViewB.setImageBitmap(mImageB);

            mCamera.addCallbackBuffer(mVideoSource);
            mCamera.startPreview();
        } catch (IOException e) {
            mCamera.release();
            mCamera = null;
            throw new IllegalStateException();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // Releases camera which is a shared resource.
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            // These variables can take a lot of memory. Get rid of
            // them as fast as we can.
            mCamera = null;
            mVideoSource = null;
            mImageR.recycle(); mImageR = null;
            mImageG.recycle(); mImageG = null;
            mImageB.recycle(); mImageB = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    private Size findBestResolution(int pWidth, int pHeight) {
        List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        Size selectedSize = mCamera.new Size(0, 0);
        for (Size size : sizes) {
            if ((size.width <= pWidth)
                    && (size.height <= pHeight)
                    && (size.width >= selectedSize.width)
                    && (size.height >= selectedSize.height)) {
                selectedSize = size;
            }
        }

        if ((selectedSize.width == 0) || (selectedSize.height == 0)) {
            selectedSize = sizes.get(0);
        }
        return selectedSize;
    }
}
