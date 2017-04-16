package edu.thunderseethe.rapulazer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.opengl.GLSurfaceView;
import android.os.IBinder;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import edu.thunderseethe.rapulazer.AudioLib.AudioFeatures;

/**
 * Created by thunderseethe on 4/16/17.
 */

public class RapulazerWallpaperService extends WallpaperService {
    public class GLEngine extends Engine {
        public class WallpaperGLSurfaceView extends GLSurfaceView {
            private static final String TAG = "RapulazerWallpaperGLSurfaceView";

            WallpaperGLSurfaceView(Context context) {
                super(context);
            }

            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }
            public void onDestroy() {
                super.onDetachedFromWindow();
            }
        }

        private static final String TAG = "GLEngine";

        private AudioVisualizerService.AudioFeatureBinder mBinder = null;
        private VisualizerGLRenderer mRenderer;
        private WallpaperGLSurfaceView mGLView;
        private boolean mBound = false;

        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            mRenderer = new VisualizerGLRenderer(RapulazerWallpaperService.this, DataRef.<AudioFeatures>empty());

            mGLView = new WallpaperGLSurfaceView(RapulazerWallpaperService.this);
            mGLView.setEGLContextClientVersion(3);
            mGLView.setPreserveEGLContextOnPause(true);
            mGLView.setRenderer(mRenderer);

            bindService(new Intent(RapulazerWallpaperService.this, AudioVisualizerService.class), mConnection, BIND_AUTO_CREATE);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if(visible) {
                mGLView.onResume();
                if(mBound) mBinder.onResume();
            } else {
                mGLView.onPause();
                if(mBound) mBinder.onPause();
            }
        }

        public void onDestroy() {
            super.onDestroy();
            mGLView.onDestroy();
            if(mBound) {
                unbindService(mConnection);
            }
        }

        private ServiceConnection mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = (AudioVisualizerService.AudioFeatureBinder) service;
                mRenderer.updateRef(mBinder.dataRef());
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
            }
        };
    }

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }
}
