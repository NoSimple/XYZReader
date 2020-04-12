package com.example.xyzreader.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.DynamicHeightNetworkImageView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public final class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {

    private SimpleDateFormat dateFormat;
    private SimpleDateFormat outputFormat;

    private Context context;
    private Cursor mCursor;
    private OnClickItemListener mOnClick;
    private String mTextSizeStr;

    public ArticleListAdapter(Cursor cursor, OnClickItemListener onClick, String textSizeStr) {
        mCursor = cursor;
        mOnClick = onClick;
        mTextSizeStr = textSizeStr;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return new ViewHolder(inflater.inflate(R.layout.list_item_article, parent, false));
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(new GregorianCalendar(2, 1, 1).getTime())) {

            holder.subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            outputFormat = new SimpleDateFormat();
            holder.subtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        }

        setTextSize(holder);

        Picasso.get()
                .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                .error(R.drawable.ic_error_outline)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        assert holder.thumbnailView != null;
                        holder.thumbnailView.setImageBitmap(bitmap);
                        Palette.from(bitmap)
                                .generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        Palette.Swatch textSwatch = palette.getVibrantSwatch();
                                        if (textSwatch == null) {
                                            holder.itemArticleView.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));
                                            return;
                                        }
                                        holder.itemArticleView.setBackgroundColor(textSwatch.getRgb());
                                    }
                                });
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });

        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.thumbnailView.setTransitionName("transition_photo" + position);
        }
        holder.thumbnailView.setTag("transition_photo" + position);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    private void setTextSize(ViewHolder holder) {
        if (mTextSizeStr.equals("text_size_small")) {
            holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
            holder.subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 12);
        } else if (mTextSizeStr.equals("text_size_medium")) {
            holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
            holder.subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 14);
        } else if (mTextSizeStr.equals("text_size_large")) {
            holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18);
            holder.subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 16);
        } else if (mTextSizeStr.equals("text_size_extra_large")) {
            holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
            holder.subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 18);
        }
    }

    final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout itemArticleView;
        DynamicHeightNetworkImageView thumbnailView;
        TextView titleView;
        TextView subtitleView;

        ViewHolder(View view) {
            super(view);

            itemArticleView = view.findViewById(R.id.view_item_article);
            thumbnailView = view.findViewById(R.id.thumbnail);
            titleView = view.findViewById(R.id.article_title);
            subtitleView = view.findViewById(R.id.article_subtitle);

            view.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            mOnClick.onClick(getAdapterPosition(), thumbnailView);
        }
    }

    public interface OnClickItemListener {
        void onClick(int position, DynamicHeightNetworkImageView thumbnailView);
    }
}