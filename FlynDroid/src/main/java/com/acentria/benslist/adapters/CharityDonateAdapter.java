package com.acentria.benslist.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acentria.benslist.R;
import com.acentria.benslist.controllers.CharityDetailActivity;
import com.acentria.benslist.response.DonateCharityResponse;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;


public class CharityDonateAdapter extends RecyclerView.Adapter<CharityDonateAdapter.ViewHolder> {

    private final String TAG = "CharityDonateAdapter=>";
    private String img_link = "";
    private Context mcontext;
    private List<DonateCharityResponse> mlist;
    private onclickposi monclickposi;
    private boolean is_food_select = false;

    public CharityDonateAdapter(Context context, List<DonateCharityResponse> list/*,onclickposi onclickposi*/) {
        this.mcontext = context;
        this.mlist = list;
        is_food_select = false;
//        this.monclickposi=onclickposi;
    }


    public CharityDonateAdapter(List<DonateCharityResponse> list, Context activity, boolean isfood_side) {
        this.mlist = list;
        this.mcontext = activity;
        this.is_food_select = isfood_side;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_row_charity_donate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (mlist.size() > 0) {
            holder.tv_donate_name.setText(mlist.get(position).getTitle());
            holder.tv_donate_date.setText(mlist.get(position).getPostedDate());

            holder.tv_donate_price.setText(mlist.get(position).getAmount());
            if (mlist.get(position).getAmount().equalsIgnoreCase("0")) {
                holder.tv_donate_price.setText("Goods");
            }

            holder.tv_donated_email.setText(mlist.get(position).getEmail());
            holder.tv_donated_phone.setText(mlist.get(position).getTel());
//www.benslist.com/files/charity/62062356profile_imageurl
            img_link = "https://www.benslist.com/files/" + mlist.get(position).getImage();
            Log.e(TAG, "imageLink by posi " + position + "\t" + img_link);
            Glide.with(mcontext).load(img_link).placeholder(R.mipmap.no_image).diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().into(holder.iv_itmes);
            holder.rl_select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    monclickposi.onPosClick(position,mlist.get(position).getRefNumber());
                    if (is_food_select == true) {
//                        Toast.makeText(mcontext ,"Under Working", Toast.LENGTH_SHORT).show();
                        mcontext.startActivity(new Intent(mcontext, CharityDetailActivity.class).putExtra("Ref_no", mlist.get(position).getRefNumber())
                                .putExtra("ImgLink", img_link).putExtra("ammount", mlist.get(position).getAmount()).putExtra("is_foodside", "Fooddonate"));
                    } else {
                        if (mlist.get(position).getAmount().equalsIgnoreCase("0")) {
                            mcontext.startActivity(new Intent(mcontext, CharityDetailActivity.class).putExtra("Ref_no", mlist.get(position).getRefNumber())
                                    .putExtra("ImgLink", img_link).putExtra("ammount", mlist.get(position).getAmount()).putExtra("is_foodside", "charitygooddsdonate"));

                        } else {
                            mcontext.startActivity(new Intent(mcontext, CharityDetailActivity.class).putExtra("Ref_no", mlist.get(position).getRefNumber())
                                    .putExtra("ImgLink", img_link).putExtra("ammount", mlist.get(position).getAmount()));
                        }

                    }

                }
            });
        } else {
            Log.e(TAG, "mlist size 0 ");
        }

    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_donate_name, tv_donate_date, tv_donate_price, tv_donated_email, tv_donated_phone;
        private ImageView iv_itmes;
        private RelativeLayout rl_select;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_itmes = itemView.findViewById(R.id.iv_itmes);
            tv_donate_name = itemView.findViewById(R.id.tv_donate_name);
            tv_donate_date = itemView.findViewById(R.id.tv_donate_date);
            tv_donate_price = itemView.findViewById(R.id.tv_donate_price);
            tv_donated_email = itemView.findViewById(R.id.tv_donated_email);
            tv_donated_phone = itemView.findViewById(R.id.tv_donated_phone);
            rl_select = itemView.findViewById(R.id.rl_select);


        }
    }

    public interface onclickposi {
        void onPosClick(int posi, String ref_number);
    }
}
