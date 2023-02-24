package com.example.carrenting.FragmentPages.Customer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.example.carrenting.ActivityPages.OwnerMainActivity;
import com.example.carrenting.R;
import com.example.carrenting.Service.Map.MapMainActivity;


public class CustomerMessageFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.customer_fragment_message, container, false);
        Button btn_message = (Button) view.findViewById(R.id.btn_message);
        btn_message.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(getActivity(), MapMainActivity.class);
                startActivity(i);
                ((Activity) getActivity()).overridePendingTransition(0, 0);
            }
        });
        return view;
        /*        return inflater.inflate(R.layout.customer_fragment_user, container, false);*/
    }

}