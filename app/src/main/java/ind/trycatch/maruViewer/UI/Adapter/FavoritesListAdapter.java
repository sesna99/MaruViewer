package ind.trycatch.maruViewer.UI.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ind.trycatch.maruViewer.Model.ComicsModel;
import ind.trycatch.maruViewer.R;
import ind.trycatch.maruViewer.Service.PreferencesManager;
import ind.trycatch.maruViewer.UI.Activity.ComicsEpisodeActivity;
import ind.trycatch.maruViewer.UI.Activity.ComicsViewer;

/**
 * Created by trycatch on 2017. 9. 4..
 */

public class FavoritesListAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private ArrayList<ComicsModel> comicsModel;
    private LayoutInflater inflater;
    private PreferencesManager pm;
    private int AD_POSITION = 1;

    public FavoritesListAdapter(Context mContext, ArrayList<ComicsModel> comicsModel) {
        this.mContext = mContext;
        this.comicsModel = comicsModel;
        pm = PreferencesManager.getInstance(mContext);
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType != 0){
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            View view = inflater.inflate(R.layout.fragment_list_item, null);
            view.setLayoutParams(lp);
            return new ContentViewHolder(view);
        }
        else{
            return new AdsViewHolder(inflater.inflate(R.layout.native_adview, null));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position + 1) / AD_POSITION;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int viewType = getItemViewType(position);
        if(viewType != 0){
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            if (comicsModel.get(position).getImage().contains("storage"))
                Glide.with(mContext).load("http://wasabisyrup.com" + comicsModel.get(position).getImage()).into(contentViewHolder.icon);
            else
                Glide.with(mContext).load(comicsModel.get(position).getImage()).into(contentViewHolder.icon);
            contentViewHolder.title.setText(comicsModel.get(position).getTitle());
            contentViewHolder.title.setSelected(true);
            contentViewHolder.item_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    if (pm.getCode(position).equals("c")) {
                        intent = new Intent(mContext, ComicsViewer.class);
                        intent.putExtra("comicsUrl", comicsModel.get(position).getComicsUrl());
                        intent.putExtra("episodeUrl", comicsModel.get(position).getEpisodeUrl());
                    } else {
                        intent = new Intent(mContext, ComicsEpisodeActivity.class);
                        intent.putExtra("url", comicsModel.get(position).getEpisodeUrl());
                    }
                    intent.putExtra("image", comicsModel.get(position).getImage());
                    intent.putExtra("title", comicsModel.get(position).getTitle());
                    mContext.startActivity(intent);
                }
            });
        }
        else{
            if(!comicsModel.get(position).getTitle().equals("ads"))
                comicsModel.add(position, new ComicsModel("ads", ""));
            AdsViewHolder adsViewHolder = (AdsViewHolder) holder;
            AdRequest request = new AdRequest.Builder()
                    .build();
            AdView adView = new AdView(mContext);
            adView.setAdSize(new AdSize(AdSize.FULL_WIDTH, 100));
            adView.setAdUnitId(mContext.getResources().getString(R.string.native_ad_unit_id));
            adsViewHolder.container.addView(adView);
            adView.loadAd(request);
        }
    }

    @Override
    public int getItemCount() {
        return comicsModel.size();
    }

    public void setComicsModel(ArrayList<ComicsModel> comicsModel){
        this.comicsModel = comicsModel;
        notifyDataSetChanged();
    }

    class ContentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_view)
        LinearLayout item_view;

        @BindView(R.id.icon)
        ImageView icon;

        @BindView(R.id.title)
        TextView title;

        public ContentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class AdsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        RelativeLayout container;

        public AdsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
