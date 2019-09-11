package com.acentria.benslist.chatprocess;

import android.app.Activity;
import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.acentria.benslist.Account;
import com.acentria.benslist.R;
import com.acentria.benslist.Utils;
import com.acentria.benslist.response.ChatMessageResponse;
import com.acentria.benslist.util.TimeStampConvertFrom;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ChatMessageAdatper extends RecyclerView.Adapter<ChatMessageAdatper.ViewHolder> {
    private String TAG = ChatMessageAdatper.class.getName(), img_url;
    private Context mcontext;
    private List<ChatMessageResponse> mlist;
    //    private boolean is_chat_bayer;
//    private boolean IsMine;


    public ChatMessageAdatper(Context context, List<ChatMessageResponse> list/*, boolean IsMine*/) {
        this.mcontext = context;
        this.mlist = list;
//        this.IsMine = IsMine;
//        Log.e(TAG, "is_IsMine " + IsMine);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_history_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            if (mlist != null) {
                ChatMessageResponse chatMessageResponse = mlist.get(position);
//                Log.e(TAG, "onBindViewHolder:merchent sms " + mlist.get(position).getMerchantMessage());
//                Log.e(TAG, "onBindViewHolder: user sms" + mlist.get(position).getUserMessage());
                Log.e(TAG, "onBindViewHolder: " + mlist.size());
                Log.e(TAG, "onBindViewHolder: isMine " + chatMessageResponse.getIsMine());
                String converttime;
                if (chatMessageResponse.getIsMine()) {
                    /*bayer side login*/
//                    if (mlist.get(position).getMerchantMessage().equalsIgnoreCase("") /*|| mlist.get(position).getMerchantMessage() != null*/) {
                    /*user message will be display on right side */
//                        holder.tv_date.setText(mlist.get(position).getDate());
                    holder.tv_fname.setText(Account.accountData.get("username"));
                    holder.tv_message.setText(mlist.get(position).getMessage());
                    Utils.imageLoaderDisc.displayImage(Account.accountData.get("photo"), holder.imageview_right, Utils.imageLoaderOptionsDisc);
                    holder.const_left.setVisibility(View.GONE);
                    Log.d(TAG, " message time:Right" + TimeStampConvertFrom.getDate(getDateCurrentTimeZone(chatMessageResponse.getMessage_time())));
                    converttime = TimeStampConvertFrom.getDate(chatMessageResponse.getMessage_time());
                    Log.v(TAG, " Right side time " + converttime);
                    holder.tv_date.setText(converttime);

                    Log.e(TAG, "sender side => isMine " + chatMessageResponse.getIsMine() + "\t username=> " + mlist.get(position).getUser_name() + "\tnmessage " +
                            mlist.get(position).getMessage() + "\nSender profile url " + Account.accountData.get("photo"));
//                    }
//                    else {
//                        /*marchent message will be display left side dispay*/
//                        holder.tv_date_left.setText(mlist.get(position).getDate());
//                        holder.tv_lname.setText(mlist.get(position).getUser_name());
//                        holder.tv_message_left.setText(mlist.get(position).getMerchantMessage());
//                        /*image null display*/
//                        holder.const_left.setVisibility(View.VISIBLE);
//                        holder.const_right.setVisibility(View.GONE);
//                        Log.e(TAG, "is_chat_bayer=>" + IsMine + "\tseller left side " + mlist.get(position).getUserMessage() + "merchent " + mlist.get(position).getMerchantMessage());
//
//                    }
                } else {
//                    holder.tv_date_left.setText(mlist.get(position).getDate());
                    holder.tv_lname.setText(mlist.get(position).getUser_name());
                    holder.tv_message_left.setText(mlist.get(position).getMessage());
                    /*image null display*/
                    holder.const_left.setVisibility(View.VISIBLE);
                    holder.const_right.setVisibility(View.GONE);
                    Log.d(TAG, " message time:Left" + TimeStampConvertFrom.getDate(getDateCurrentTimeZone(chatMessageResponse.getMessage_time())));
                    converttime = TimeStampConvertFrom.getDate(chatMessageResponse.getMessage_time());
                    Log.v(TAG, " Left side time " + converttime);
                    holder.tv_date_left.setText(converttime);

                    Utils.imageLoaderDisc.displayImage(chatMessageResponse.getImg_url(), holder.imageview_left, Utils.imageLoaderOptionsDisc);

                    Log.e(TAG, "reciver side=> isMine" + chatMessageResponse.getIsMine() + "\t username=> " + mlist.get(position).getUser_name() + "\nmessage=> " + mlist.get(position).getMessage()
                            + "\nReciver profile url " + chatMessageResponse.getImg_url());

                }


//                else {
//                    /*seller(merchent) side login*/
//                    if (mlist.get(position).getUserMessage().equalsIgnoreCase("")) {
//                        holder.tv_date.setText(mlist.get(position).getDate());
//                        holder.tv_fname.setText(Account.accountData.get("username"));
//                        holder.tv_message.setText(mlist.get(position).getMerchantMessage());
//                        Utils.imageLoaderDisc.displayImage(Account.accountData.get("photo"), holder.imageview_right, Utils.imageLoaderOptionsDisc);
//                        holder.const_left.setVisibility(View.GONE);
//                        Log.e(TAG, "is_chat_bayer=>" + IsMine + "\tseller right" + mlist.get(position).getUserMessage() + "merchent " + mlist.get(position).getMerchantMessage());
//
//                    } else {
//
//                        holder.tv_date_left.setText(mlist.get(position).getDate());
//                        holder.tv_lname.setText(mlist.get(position).getUser_name());
//                        holder.tv_message_left.setText(mlist.get(position).getUserMessage());
////                        Utils.imageLoaderDisc.displayImage(Account.accountData.get("photo"), holder.imageview_left, Utils.imageLoaderOptionsDisc);
//                        holder.const_left.setVisibility(View.VISIBLE);
//                        holder.const_right.setVisibility(View.GONE);
//                        Log.e(TAG, "is_chat_bayer=>" + IsMine + "bayerside left " + mlist.get(position).getUserMessage() + "\tmerchent " + mlist.get(position).getMerchantMessage());
//
//                    }
//                }


            } else {
                Log.e(TAG, "onBindViewHolder: list size less than zero ");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_date;
        TextView tv_fname;
        TextView tv_message;
        //        ConstraintLayout constraintLayout;
        ImageView imageview_right;

        ConstraintLayout const_right;
        ConstraintLayout const_left;
        TextView tv_lname;
        TextView tv_message_left;
        TextView tv_date_left;

        //        ConstraintLayout constraintLayout_left;
        ImageView imageview_left;
        ConstraintLayout const_parent;

        public ViewHolder(View itemView) {
            super(itemView);
            const_parent = itemView.findViewById(R.id.const_parent);
            tv_date = itemView.findViewById(R.id.tv_date);
            tv_fname = itemView.findViewById(R.id.tv_fname);
            tv_message = itemView.findViewById(R.id.tv_message);
            const_right = itemView.findViewById(R.id.const_right);
            imageview_right = itemView.findViewById(R.id.circularImageView);

            const_left = itemView.findViewById(R.id.const_left);
            tv_lname = itemView.findViewById(R.id.tv_lname);
            tv_message_left = itemView.findViewById(R.id.tv_messagee);
            tv_date_left = itemView.findViewById(R.id.tv_datee);
            imageview_left = itemView.findViewById(R.id.circularImageVieww);


        }
    }


    private long getDateCurrentTimeZone(long timestamp) {
        try {
            Calendar calendar = Calendar.getInstance();
            TimeZone timeZone = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, timeZone.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            Date currenTimeZone = (Date) calendar.getTime();
            currenTimeZone.getTime();
            Log.e(TAG, "getDateCurrentTimeZone: " + currenTimeZone.getTime());
            return currenTimeZone.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
