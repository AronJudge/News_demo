package com.xiangxue.network.wakeNetwork;

import android.os.SystemClock;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class SpeedLimitResponseBody extends ResponseBody {
    private static String TAG = "SpeedLimitResponseBody";
    /**
     * 限速字节
     */
    private long mSpeedByte;
    private ResponseBody mResponseBody;
    private BufferedSource mBufferedSource;

    SpeedLimitResponseBody(long speed, ResponseBody source) {
        this.mResponseBody = source;
        //转成字节
        this.mSpeedByte = speed * 1024L;
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            /**
             * 如果小于1s 会重置
             */
            private long cacheTotalBytesRead;
            /**
             * 分片读取1024个字节开始时间 小于1s会重置
             */
            private long cacheStartTime;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                if (cacheStartTime == 0) {
                    //记录 开机到当前的时间总数
                    cacheStartTime = SystemClock.uptimeMillis();
                }

                //byteCount默认8K  指定到读1K
                long bytesRead = super.read(sink.buffer(), 1024L);
                // -1代表已经读取完毕
                if (bytesRead == -1) {
                    return bytesRead;
                }
                //读这个数据 总读取字节数
                cacheTotalBytesRead = cacheTotalBytesRead + bytesRead;

                /**
                 * 判断当前请求累计消耗的时间 即相当于读取1024个字节所需要的时间
                 */
                long costTime = SystemClock.uptimeMillis() - cacheStartTime;

                //如果每次分片读取时间小于1s sleep 延迟时间
                if (costTime <= 1000L) {
                    if (cacheTotalBytesRead >= mSpeedByte) {
                        long sleep = 1000L - costTime;
                        SystemClock.sleep(sleep);
                        //重置计算
                        cacheStartTime = 0L;
                        cacheTotalBytesRead = 0L;
                    }
                }
                //如果读取超过1s，表示本来就很慢了，后面就不会延迟读取
                return bytesRead;
            }
        };
    }
}
