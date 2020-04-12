package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.SharedElementCallback;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.example.xyzreader.R;
import com.example.xyzreader.adapter.ArticleListAdapter;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.util.Constants;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.example.xyzreader.util.Constants.CUBE;
import static com.example.xyzreader.util.Constants.DEPTH;
import static com.example.xyzreader.util.Constants.EXTRA_LARGE;
import static com.example.xyzreader.util.Constants.LARGE;
import static com.example.xyzreader.util.Constants.SMALL;
import static com.example.xyzreader.util.Constants.ZOOM;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener, ArticleListAdapter.OnClickItemListener {

    public static final String EXTRA_STARTING_POSITION = "extra_starting_position";
    public static final String EXTRA_CURRENT_POSITION = "extra_current_position";
    public static final String EXTRA_PAGE_TRANSFORMATION = "extra_page_transformation";
    public static final String EXTRA_TEXT_SIZE = "extra_text_size";

    private String mPageTransformerStr;
    private String mTextSizeStr;

    private Bundle mReenterState;

    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private ArticleListAdapter adapter;

    private boolean mIsRefreshing = false;

    private final SharedElementCallback mCallback = new SharedElementCallback() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            if (mReenterState != null) {
                int startingPosition = mReenterState.getInt(EXTRA_STARTING_POSITION);
                int currentPosition = mReenterState.getInt(EXTRA_CURRENT_POSITION);
                if (startingPosition != currentPosition) {
                    String newTransitionName = "transition_photo" + currentPosition;
                    View newSharedElement = mRecyclerView.findViewWithTag(newTransitionName);
                    if (newSharedElement != null) {
                        names.clear();
                        names.add(newTransitionName);
                        sharedElements.clear();
                        sharedElements.put(newTransitionName, newSharedElement);
                    }
                }

                mReenterState = null;
            } else {
                View navigationBar = findViewById(android.R.id.navigationBarBackground);
                View statusBar = findViewById(android.R.id.statusBarBackground);
                if (navigationBar != null) {
                    names.add(navigationBar.getTransitionName());
                    sharedElements.put(navigationBar.getTransitionName(), navigationBar);
                }
                if (statusBar != null) {
                    names.add(statusBar.getTransitionName());
                    sharedElements.put(statusBar.getTransitionName(), statusBar);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setExitSharedElementCallback(mCallback);
        }

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setSwipeRefreshLayout();

        mRecyclerView = findViewById(R.id.recycler_view);
        getSupportLoaderManager().initLoader(Constants.XYZ_LOADER_ID, null, this);

        if (savedInstanceState == null) {
            refresh();
        }

        mPageTransformerStr = getPreferredPageTransformationStr();
        mTextSizeStr = getPreferredTextSizeStr();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    private String getPreferredPageTransformationStr() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String keyForPageAnimation = "page_animation";
        String defaultPageAnimation = "page_animation_default";
        return prefs.getString(keyForPageAnimation, defaultPageAnimation);
    }

    private String getPreferredTextSizeStr() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String keyForTextSize = "text_size_key";
        String defaultTextSize = "text_size_default";
        return prefs.getString(keyForTextSize, defaultTextSize);
    }

    private void setSwipeRefreshLayout() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeColors(
                getResources().getColor(R.color.swipeColor1),
                getResources().getColor(R.color.swipeColor2),
                getResources().getColor(R.color.swipeColor3));

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                runLayoutAnimation(mRecyclerView);
            }
        });
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    private void runLayoutAnimation(RecyclerView recyclerView) {
        Context context = recyclerView.getContext();
        LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_from_bottom);
        recyclerView.setLayoutAnimation(controller);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        mReenterState = new Bundle(data.getExtras());
        int startingPosition = mReenterState.getInt(EXTRA_STARTING_POSITION);
        int currentPosition = mReenterState.getInt(EXTRA_CURRENT_POSITION);
        if (startingPosition != currentPosition) {
            mRecyclerView.scrollToPosition(currentPosition);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityCompat.postponeEnterTransition(this);
        }
        scheduleStartPostponedTransition(mRecyclerView);
    }

    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                mRecyclerView.requestLayout();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityCompat.startPostponedEnterTransition(ArticleListActivity.this);
                }
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver, new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    @NonNull
    @Override
    public androidx.loader.content.Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(@NonNull androidx.loader.content.Loader<Cursor> cursorLoader, Cursor cursor) {

        adapter = new ArticleListAdapter(cursor, this, mTextSizeStr);
        adapter.setHasStableIds(true);
        mRecyclerView.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("page_animation")) {
            String pageAnimation = sharedPreferences.getString(key, "page_animation_default");
            switch (pageAnimation) {
                case ZOOM:
                    mPageTransformerStr = "page_animation_zoom";
                    break;
                case DEPTH:
                    mPageTransformerStr = "page_animation_depth";
                    break;
                case CUBE:
                    mPageTransformerStr = "page_animation_cube";
                    break;
                default:
                    mPageTransformerStr = "page_animation_pop";
            }

        } else if (key.equals("text_size_key")) {
            String textSize = sharedPreferences.getString(key, "text_size_default");
            switch (textSize) {
                case SMALL:
                    mTextSizeStr = "text_size_small";
                    break;
                case LARGE:
                    mTextSizeStr = "text_size_large";
                    break;
                case EXTRA_LARGE:
                    mTextSizeStr = "text_size_extra_large";
                    break;
                default:
                    mTextSizeStr = "text_size_medium";
            }
        }
    }

    @Override
    public void onClick(int position, DynamicHeightNetworkImageView thumbnailView) {

        Intent intent = new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(adapter.getItemId(position)));
        intent.putExtra(EXTRA_STARTING_POSITION, position);
        intent.putExtra(EXTRA_PAGE_TRANSFORMATION, mPageTransformerStr);
        intent.putExtra(EXTRA_TEXT_SIZE, mTextSizeStr);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String transitionName = thumbnailView.getTransitionName();
            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    ArticleListActivity.this,
                    thumbnailView,
                    transitionName
            ).toBundle();
            startActivity(intent, bundle);
        } else {
            startActivity(intent);
        }
    }
}