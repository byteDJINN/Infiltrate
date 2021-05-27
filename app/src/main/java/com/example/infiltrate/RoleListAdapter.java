package com.example.infiltrate;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RoleListAdapter extends RecyclerView.Adapter<RoleListAdapter.ViewHolder>{
    private ArrayList<Pair<Game.Role,int[]>> localDataSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        private TextWatcher minTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    for (int i = 0; i < localDataSet.size(); i++) {
                        if (localDataSet.get(i).first == Game.Role.valueOf(textView.getText().toString())) {
                            localDataSet.get(i).second[0] = Integer.parseInt(s.toString());
                            break;
                        }
                    }
                } catch (Exception e) {}
            }
        };
        private TextWatcher maxTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    for (int i = 0; i < localDataSet.size(); i++) {
                        if (localDataSet.get(i).first == Game.Role.valueOf(textView.getText().toString())) {
                            localDataSet.get(i).second[1] = Integer.parseInt(s.toString());
                            break;
                        }
                    }
                } catch (Exception e) {}
            }
        };

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.role_list_item_string);
            EditText minEditText = (EditText) view.findViewById(R.id.min);
            EditText maxEditText = (EditText) view.findViewById(R.id.max);

            minEditText.addTextChangedListener(minTextWatcher);
            maxEditText.addTextChangedListener(maxTextWatcher);


        }

        public TextView getTextView() {
            return textView;
        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public RoleListAdapter(ArrayList<Pair<Game.Role,int[]>> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RoleListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.role_list_item, viewGroup, false);



        return new RoleListAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RoleListAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTextView().setText(localDataSet.get(position).first.name());


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

}

