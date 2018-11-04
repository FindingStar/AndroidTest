package com.example.acer.androidtest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RecyclerItemClickActivity extends AppCompatActivity {

    private List<String>mStringList=new ArrayList<>();
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    private static final String TAG = "RecyclerItemClickActivi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_item_click);

        for (int i = 0; i < 20; i++) {
            mStringList.add("***"+i);
        }

        RecyclerView recyclerView=findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RvAdapter rvAdapter=new RvAdapter(this);

        setOnItemClickListener(new OnRecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Log.d(TAG, "onClick: 点击了itemview"+position);
            }
        });
        recyclerView.setAdapter(rvAdapter);

    }

    public class RvAdapter extends RecyclerView.Adapter<RvAdapter.VHolder> implements View.OnClickListener{

        private Context context;

        public RvAdapter(Context context) {
            this.context=context;
        }

        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();

            if (mOnItemClickListener != null) {
                mOnItemClickListener.onClick(v,position);
            }

        }

        class VHolder extends RecyclerView.ViewHolder{

            private Button mButton;
            private TextView mTextView;

            public VHolder(View itemView) {
                super(itemView);
                mButton=itemView.findViewById(R.id.bt1);
                mTextView=itemView.findViewById(R.id.tv);

                itemView.setOnClickListener(RvAdapter.this);

            }
        }


        @NonNull
        @Override
        public VHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(context).inflate(R.layout.include_rv_item,parent,false);

            return new VHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VHolder holder, int position) {
            String s=mStringList.get(position);
            holder.mTextView.setText(s);

            holder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return mStringList.size();
        }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemClickListener {
        void onClick(View view,  int position);
    }

}
