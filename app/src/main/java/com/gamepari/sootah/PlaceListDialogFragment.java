package com.gamepari.sootah;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gamepari.sootah.location.Places;

import java.util.List;

/**
 * Created by seokceed on 2014-12-01.
 */
public class PlaceListDialogFragment extends DialogFragment {

    List<Places> mPlacesList;
    OnPlaceClickListener mOnPlaceClickListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnPlaceClickListener = (OnPlaceClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement.");
        }
    }

    public void setPlacesList(List<Places> mPlacesList) {
        this.mPlacesList = mPlacesList;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton(R.string.not_here, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mOnPlaceClickListener.onPlaceCancel();
            }
        });
        builder.setTitle(R.string.choose_place);
        builder.setAdapter(new PlacesAdapter(mPlacesList), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mOnPlaceClickListener.onPlaceSelect(mPlacesList.get(which));
            }
        });


        return builder.create();
    }

    public interface OnPlaceClickListener {
        public void onPlaceSelect(Places o);
        public void onPlaceCancel();
    }

    private class PlacesAdapter extends BaseAdapter {

        List<Places> mPlacesList;

        private PlacesAdapter(List<Places> placesList) {
            mPlacesList = placesList;
        }

        @Override
        public int getCount() {
            return mPlacesList.size();
        }

        @Override
        public Places getItem(int position) {
            return mPlacesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mPlacesList.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.cell_placelist_dialog, null);
                holder = new ViewHolder();
                holder.tvAddress = (TextView) convertView.findViewById(R.id.cell_address);
                holder.tvTitle = (TextView) convertView.findViewById(R.id.cell_title);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.tvTitle.setText(getItem(position).getName());
            holder.tvAddress.setText(getItem(position).getVicinity());

            return convertView;
        }

    }

    private static class ViewHolder {
        TextView tvTitle;
        TextView tvAddress;
    }
}
