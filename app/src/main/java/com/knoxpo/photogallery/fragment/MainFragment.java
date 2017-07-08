package com.knoxpo.photogallery.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.knoxpo.photogallery.R;
import com.knoxpo.photogallery.model.GalleryItem;
import com.knoxpo.photogallery.model.ImageSingleTon;
import com.knoxpo.photogallery.network.FlickrFetch;
import com.knoxpo.photogallery.service.PollService;
import com.knoxpo.photogallery.storage.QueryPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tejas Sherdiwala on 12/1/2016.
 * &copy; Knoxpo
 */

public class MainFragment extends VisibleFragment {

    private static final String TAG = MainFragment.class.getSimpleName();

    private RecyclerView mPhotoGalleryRV;
    private PhotoAdapter mPhotoAdapter;
    private List<GalleryItem> mGalleryItems;


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery,menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG,"Query Text Submit" + query);
                QueryPreferences.setStoredQuery(getActivity(),query);
                updateItem();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG,"Text Change" + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Log.d(TAG,QueryPreferences.getStoredQuery(getActivity()));
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query,false);
            }
        });

        MenuItem toogleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity())){
            toogleItem.setTitle(R.string.stop_polling);
        }else{
            toogleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(),null);
                updateItem();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldAlarmStart = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(),shouldAlarmStart);
                getActivity().invalidateOptionsMenu();
                return  true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItem(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemTask(query).execute();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
       // Intent intent = PollService.newIntent(getActivity());
        //getActivity().startService(intent);
       // PollService.setSystemAlarm(getActivity(),true);
       // updateItem();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        init(rootView);
        mPhotoGalleryRV.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        // mPhotoGalleryRV.setAdapter(mPhotoAdapter);
        if(mGalleryItems.isEmpty()){
            updateItem();
        }else{
            setUpAdapter();
        }
        return rootView;
    }

    private void init(View view) {
        mPhotoGalleryRV = (RecyclerView) view.findViewById(R.id.rv_photo_gallery);
        if(mGalleryItems==null){
            mGalleryItems = new ArrayList<>();
        }
        mPhotoAdapter = new PhotoAdapter(mGalleryItems);
    }

    private void setUpAdapter() {
        if (isAdded()) {
            mPhotoGalleryRV.setAdapter(mPhotoAdapter);
        }
    }

    private class FetchItemTask extends AsyncTask<Void, Void, List<GalleryItem>> {
        private String mQuery;

        public FetchItemTask(String query){
            mQuery = query;
        }
        @Override
        protected List<GalleryItem> doInBackground(Void... voids) {
            if(mQuery==null){
                return new FlickrFetch().fetchRecentPhotos();
            }else{
                return new FlickrFetch().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
             mPhotoAdapter.notifyDataSetChanged();
            setUpAdapter();
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private LayoutInflater mInflater;

        public PhotoAdapter(List<GalleryItem> items) {
            mInflater = LayoutInflater.from(getActivity());
            mGalleryItems = items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_photo, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            //Drawable placeHolder = getResources().getDrawable(R.drawable.ic_photo);
            holder.bindDrawable(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageLoader mImageLoader;
        private NetworkImageView mNetworkImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mNetworkImageView = (NetworkImageView)itemView.findViewById(R.id.iv_photo);
            mImageLoader= ImageSingleTon.getInstance(getActivity()).getImageLoader();
        }

        public void bindDrawable(GalleryItem item) {
            mNetworkImageView.setDefaultImageResId(R.drawable.ic_photo);
            mNetworkImageView.setErrorImageResId(R.drawable.ic_photo);
            Log.d(TAG , item.getUrl());
            mNetworkImageView.setImageUrl(item.getUrl(),mImageLoader);
        }
    }
}
