package vn.edu.stu.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import vn.edu.stu.Adapter.ViewPagerSearchAdapter;
import vn.edu.stu.luanvanmxhhippo.R;


public class SearchFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerSearchAdapter pagerSearchAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        pagerSearchAdapter = new ViewPagerSearchAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerSearchAdapter);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }


}