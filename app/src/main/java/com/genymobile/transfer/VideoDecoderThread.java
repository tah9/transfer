package com.genymobile.transfer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class VideoDecoderThread extends Thread {
    private SurfaceView surfaceView;
    private InputStream inputStream;
    private MediaCodec mediaCodec;
    private byte[] buffer;

    public VideoDecoderThread(SurfaceView surfaceView, InputStream inputStream) {
        this.surfaceView = surfaceView;
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat("video/avc", 1280, 720);
            mediaCodec = MediaCodec.createDecoderByType("video/avc");
            mediaCodec.configure(format, surfaceView.getHolder().getSurface(), null, 0);
            mediaCodec.start();

            while (!Thread.interrupted()) {
                int inputBufferIndex = mediaCodec.dequeueInputBuffer(10000);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                    int length = inputStream.read(buffer);
                    if (length == -1) {
                        break;
                    }
                    inputBuffer.put(buffer, 0, length);
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, length, System.currentTimeMillis(), 0);
                }

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                if (outputBufferIndex >= 0) {
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
            }
        }
    }
}
