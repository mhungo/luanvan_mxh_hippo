package vn.edu.stu.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import vn.edu.stu.Fragment.SearchPostFragment;
import vn.edu.stu.Fragment.SearchUserFragment;

public class ViewPagerSearchAdapter extends FragmentPagerAdapter {
    public ViewPagerSearchAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SearchUserFragment searchUserFragment = new SearchUserFragment();
                return searchUserFragment;
            case 1:
                SearchPostFragment searchPostFragment = new SearchPostFragment();
                return searchPostFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Search User";
            case 1:
                return "Search Post";

            default:
                return null;
        }
    }
}
