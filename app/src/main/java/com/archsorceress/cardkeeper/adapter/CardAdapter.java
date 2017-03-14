package com.archsorceress.cardkeeper.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.archsorceress.cardkeeper.R;
import com.archsorceress.cardkeeper.model.CardItem;
import com.bumptech.glide.Glide;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Realm RecyclerView adapter, auto updates card items from DB
 * Displays closed cards at initial state, keeps track of open cards in
 * expandedCards array
 *
 * Created by burcuarabaci on 17/11/16.
 */

public class CardAdapter extends RealmRecyclerViewAdapter<CardItem,CardAdapter.CardViewHolder> {
    private SparseBooleanArray expandedCards;

    public CardAdapter(@NonNull Context context, @Nullable OrderedRealmCollection data, boolean autoUpdate) {
        super(context, data, autoUpdate);
        expandedCards = new SparseBooleanArray();
    }

    @Override
    public CardAdapter.CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_cardfullsize, parent, false);

        return new CardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CardViewHolder holder, int position) {
        CardItem cardItem = getItem(position);
        final int cardPosition = position;
        holder.title.setText(cardItem.getTitle());
        holder.title.setTextColor(cardItem.getTextColor());
        holder.cardView.setCardBackgroundColor(cardItem.getBackgroundColor());
        if(TextUtils.isEmpty(cardItem.getDetail())){
            holder.details.setVisibility(View.GONE);
        } else {
            holder.details.setVisibility(View.VISIBLE);
            holder.details.setText(cardItem.getDetail());
            holder.details.setTextColor(cardItem.getTextColor());
        }
        if (cardItem.getImageUrl() != null) {
            Glide.with(context).load(cardItem.getImageUrl()).placeholder(R.drawable.fab_add).into(holder.cardImage);
        }
        //if the card is expanded, set detail views' visibility to VISIBLE
        setDetailViewsVisibility(holder, expandedCards.get(position, false));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expandedCards.get(cardPosition, false)) {
                    expandedCards.delete(cardPosition);
                    setDetailViewsVisibility(holder, false);
                } else {
                    expandedCards.put(cardPosition, true);
                    setDetailViewsVisibility(holder, true);
                }
            }
        });
    }

    private void setDetailViewsVisibility(CardAdapter.CardViewHolder holder, boolean isVisible) {
        if (isVisible) {
            holder.layoutDetail.setVisibility(View.VISIBLE);
        } else {
            holder.layoutDetail.setVisibility(View.GONE);
        }
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView details;
        ImageView cardImage;
        CardView cardView;
        LinearLayout layoutDetail;

        CardViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.textview_cardtitle);
            cardImage = (ImageView) view.findViewById(R.id.imageView_cardimage);
            details = (TextView) view.findViewById(R.id.textView_carddetails);
            cardView = (CardView) view.findViewById(R.id.cardview_background);
            layoutDetail = (LinearLayout) view.findViewById(R.id.layout_detail);
        }
    }


}
