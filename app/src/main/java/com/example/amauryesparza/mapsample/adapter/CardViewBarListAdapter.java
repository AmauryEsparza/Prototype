//package com.example.amauryesparza.mapsample.adapter;
//
//import android.support.v7.widget.CardView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.example.amauryesparza.mapsample.model.Bar;
//
//import java.util.List;
//
///**
// * Created by AmauryEsparza on 12/10/15.
// */
//public class CardViewBarListAdapter extends RecyclerView.Adapter<Bar> {
//
//    List<Bar> barList;
//
//    public CardViewBarListAdapter(List<Bar> barList){
//        this.barList = barList;
//    }
//
//    @Override
//    public int getItemCount(){
//        return barList.size();
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
//
//    }
//
//    /**
//     * View Holder Pattern
//     */
//    public static BarViewHolder extends RecyclerView.ViewHolder{
//        CardView cv;
//        TextView personName;
//        TextView personAge;
//        ImageView personPhoto;
//
//        Bar(View itemView) {
//            super(itemView);
//            cv = (CardView)itemView.findViewById(R.id.cv);
//            personName = (TextView)itemView.findViewById(R.id.person_name);
//            personAge = (TextView)itemView.findViewById(R.id.person_age);
//            personPhoto = (ImageView)itemView.findViewById(R.id.person_photo);
//        }
//
//    }
//
//}
