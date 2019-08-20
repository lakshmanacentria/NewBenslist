package com.acentria.benslist.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.acentria.benslist.R;
import com.acentria.benslist.response.ProductAdd;

import java.util.List;

public class ProductAddAdapter extends RecyclerView.Adapter<ProductAddAdapter.ViewHolder> {


    private static final String TAG = ProductAddAdapter.class.getSimpleName();
    private Context mcontext;
    private List<ProductAdd> mlist;
    private OnclickPos monclickPos;

    public ProductAddAdapter(Context context, List<ProductAdd> list, OnclickPos onclickPos) {
        this.mcontext = context;
        this.mlist = list;
        this.monclickPos = onclickPos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_product_add, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if (mlist.get(position).getProdcutname().equalsIgnoreCase("")) {
            holder.et_product.setHint("Enter Product");
        }
        if (mlist.get(position).getProdcut_qauntity().equalsIgnoreCase("")) {
            holder.et_qauntity.setHint("Enter Quantity");
        }
        if (mlist.size() > 1) {
            holder.iv_close.setVisibility(View.VISIBLE);
        }

        holder.iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Log.e(TAG, "onClick: remove posion" + position);

            }
        });


        holder.iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mlist.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mlist.size());
                Log.e(TAG, "onClick: removeitems");
            }
        });


        holder.ll_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mlist.get(position).getProdcutname().equalsIgnoreCase("")) {
                    monclickPos.onClickIems(position, mlist.get(position).getProdcutname(), mlist.get(position).getProdcut_qauntity());
                } else {
                    Log.e(TAG, position + "onClick: please enter product name ");
                }
            }
        });


        holder.et_product.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e(TAG, " beforeTextChanged: et_product " + charSequence.toString());
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e(TAG, "onTextChanged: et_product " + charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mlist.get(position).setProdcutname(editable.toString().trim());

                Log.e(TAG, "afterTextChanged: et_product " + editable.toString());
            }
        });

        holder.et_qauntity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e(TAG, "beforeTextChanged: et_qauntity " + charSequence.toString());

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e(TAG, "onTextChanged: et_qauntity " + charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mlist.get(position).setProdcut_qauntity(editable.toString().trim());

                Log.e(TAG, "afterTextChanged: et_qauntity " + editable.toString());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private EditText et_product, et_qauntity;
        private LinearLayout ll_parent;
        private ImageView iv_close;

        public ViewHolder(View itemView) {
            super(itemView);
            et_product = itemView.findViewById(R.id.et_product);
            et_qauntity = itemView.findViewById(R.id.et_qauntity);
            ll_parent = itemView.findViewById(R.id.ll_parent);
            iv_close = itemView.findViewById(R.id.iv_close);

        }
    }


    public interface OnclickPos {
        void onClickIems(int pos, String productname, String prductqaunity);
    }
}
