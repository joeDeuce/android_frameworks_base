/* Copyright (c) 2011, Code Aurora Forum. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of Code Aurora Forum, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package android.webkit;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.webkit.HTML5VideoView;
import android.webkit.HTML5VideoViewProxy;
import android.view.Surface;
import android.opengl.GLES20;
import android.os.PowerManager;

/**
 * @hide This is only used by the browser
 */
public class HTML5VideoInline extends HTML5VideoView{

    // Due to the fact that the decoder consume a lot of memory, we make the
    // surface texture as singleton. But the GL texture (m_textureNames)
    // associated with the surface texture can be used for showing the screen
    // shot when paused, so they are not singleton.
    private static SurfaceTexture mSurfaceTexture = null;
    private static int[] mTextureNames = null;
    // Every time when the VideoLayer Id change, we need to recreate the
    // SurfaceTexture in order to delete the old video's decoder memory.
    private static int mVideoLayerUsingSurfaceTexture = -1;

    // Video control FUNCTIONS:
    @Override
    public void start() {
        if (!getPauseDuringPreparing()) {
            super.start();
        }
    }

    HTML5VideoInline(int videoLayerId, int position) {
        init(videoLayerId, position, false);
    }

    @Override
    public void decideDisplayMode() {
        SurfaceTexture surfaceTexture = getSurfaceTexture(getVideoLayerId());
        Surface surface = new Surface(surfaceTexture);
        mPlayer.setSurface(surface);
        surface.release();
    }

    // Normally called immediately after setVideoURI. But for full screen,
    // this should be after surface holder created
    @Override
    public void prepareDataAndDisplayMode(HTML5VideoViewProxy proxy) {
        super.prepareDataAndDisplayMode(proxy);
        setFrameAvailableListener(proxy);
        // TODO: This is a workaround, after b/5375681 fixed, we should switch
        // to the better way.
        if (mProxy.getContext().checkCallingOrSelfPermission(permission.WAKE_LOCK)
                == PackageManager.PERMISSION_GRANTED) {
            mPlayer.setWakeMode(proxy.getContext(), PowerManager.FULL_WAKE_LOCK);
        }
    }

    // Pause the play and update the play/pause button
    @Override
    public void pauseAndDispatch(HTML5VideoViewProxy proxy) {
        super.pauseAndDispatch(proxy);
    }

    // Inline Video specific FUNCTIONS:

    public static SurfaceTexture getSurfaceTexture(int videoLayerId) {
        // Create the surface texture.
        if (videoLayerId != mVideoLayerUsingSurfaceTexture
            || mSurfaceTexture == null
            || mTextureNames == null) {
            // The GL texture will store in the VideoLayerManager at native side.
            // They will be clean up when requested.
            // The reason we recreated GL texture name is for screen shot support.
            mTextureNames = new int[1];
            GLES20.glGenTextures(1, mTextureNames, 0);
            mSurfaceTexture = new SurfaceTexture(mTextureNames[0]);
        }
        return mSurfaceTexture;
    }

    public boolean surfaceTextureDeleted() {
        return (mSurfaceTexture == null);
    }

    @Override
    public void deleteSurfaceTexture() {
        cleanupSurfaceTexture();
        return;
    }

    public static void cleanupSurfaceTexture() {
        mSurfaceTexture = null;
        mVideoLayerUsingSurfaceTexture = -1;
        return;
    }

    @Override
    public int getTextureName() {
        if (mTextureNames != null) {
            return mTextureNames[0];
        } else {
            return 0;
        }
    }

    private void setFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener l) {
        mSurfaceTexture.setOnFrameAvailableListener(l);
    }

}
