package com.bumptech.glide.samples.giphy;

import static com.bumptech.glide.request.RequestOptions.diskCacheStrategyOf;

import com.google.gson.Gson;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

/**
 * An {@link android.app.Activity} for displaying full size original GIFs.
 */
public class FullscreenActivity extends Activity {
    private static final String EXTRA_RESULT_JSON = "result_json";
    private GifDrawable gifDrawable;

    public static Intent getIntent(Context context, Api.GifResult result) {
        Intent intent = new Intent(context, FullscreenActivity.class);
        intent.putExtra(EXTRA_RESULT_JSON, new Gson().toJson(result));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fullscreen_activity);

        String resultJson = getIntent().getStringExtra(EXTRA_RESULT_JSON);
        final Api.GifResult result = new Gson().fromJson(resultJson, Api.GifResult.class);

        ImageView gifView = (ImageView) findViewById(R.id.fullscreen_gif);

        gifView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("giphy_url", result.images.original.url);
                clipboard.setPrimaryClip(clip);

                if (gifDrawable != null) {
                    if (gifDrawable.isRunning()) {
                        gifDrawable.stop();
                    } else {
                        gifDrawable.start();
                    }
                }
            }
        });

        RequestBuilder<Drawable> thumbnailRequest = Glide.with(this)
                .asDrawable()
                .apply(diskCacheStrategyOf(DiskCacheStrategy.SOURCE)
                        .decode(Bitmap.class))
                .load(result);

        Glide.with(this)
                .asDrawable()
                .thumbnail(thumbnailRequest)
                .load(result.images.original.url)
                .apply(diskCacheStrategyOf(DiskCacheStrategy.SOURCE))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<Drawable> target,
                            boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                            boolean isFromMemoryCache, boolean isFirstResource) {
                        if (resource instanceof GifDrawable) {
                            gifDrawable = (GifDrawable) resource;
                        } else {
                            gifDrawable = null;
                        }
                        return false;
                    }
                })
                .into(gifView);
    }
}
