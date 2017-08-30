package com.example.dewaagung.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dewaagung.popularmovies.domains.Review;

import java.util.ArrayList;

/**
 * Created by Dewa Agung on 30/08/17.
 */

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    private static final String LOG_TAG = ReviewAdapter.class.getSimpleName();

    ArrayList<Review> mReviews;
    Context mContext;
    LayoutInflater mLayoutInflater;

    public ReviewAdapter(Context context, ArrayList<Review> reviews) {
        this.mReviews = reviews;
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.review, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.tv_author.setText(mReviews.get(position).getAuthor());
        holder.tv_content.setText(mReviews.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    public void clear() {
        mReviews.clear();
        notifyDataSetChanged();
    }

    public void add(Review review) {
        if (review != null) {
            mReviews.add(review);
            notifyItemInserted(mReviews.size() - 1);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_author;
        TextView tv_content;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_content = (TextView) itemView.findViewById(R.id.review_content);
            tv_author = (TextView) itemView.findViewById(R.id.review_author);
        }
    }
}
