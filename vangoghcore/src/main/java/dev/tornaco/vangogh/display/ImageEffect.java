package dev.tornaco.vangogh.display;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import dev.tornaco.vangogh.media.Image;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */

public interface ImageEffect {
    @NonNull
    Image process(@NonNull Image image);
}