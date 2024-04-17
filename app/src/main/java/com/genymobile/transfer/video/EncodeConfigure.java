package com.genymobile.transfer.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;

import com.genymobile.transfer.Options;

import java.io.IOException;

public class EncodeConfigure {
    public static MediaCodec createCodec() throws IOException {
        return MediaCodec.createEncoderByType("video/avc");
    }

    public static MediaFormat createFormat(Options options) throws IOException {
        MediaFormat format = new MediaFormat();
        format.setInteger(MediaFormat.KEY_WIDTH, options.getDisplayRegion().width());
        format.setInteger(MediaFormat.KEY_HEIGHT, options.getDisplayRegion().height());
        format.setString(MediaFormat.KEY_MIME, MediaFormat.MIMETYPE_VIDEO_AVC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, options.getBitRate());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, options.getFps());
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, options.getRefreshInterval());
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, options.getRepeatFrame()); // µs
        // The key existed privately before Android 10:
        // <https://android.googlesource.com/platform/frameworks/base/+/625f0aad9f7a259b6881006ad8710adce57d1384%5E%21/>
        // <https://github.com/Genymobile/scrcpy/issues/488#issuecomment-567321437>
        format.setFloat("max-fps-to-encoder", options.getMaxFps());

        System.out.println(format);
        return format;
    }
}
