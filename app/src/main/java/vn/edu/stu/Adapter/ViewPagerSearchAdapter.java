package vn.edu.stu.Adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import vn.edu.stu.Fragment.SearchPostFragment;
import vn.edu.stu.Fragment.SearchUserFragment;
import vn.edu.stu.luanvanmxhhippo.R;

public class ViewPagerSearchAdapter extends FragmentPagerAdapter {
    Context context;

    public ViewPagerSearchAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
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
        String searchUser = context.getString(R.string.searchUser);
        String searchPosts = context.getString(R.string.searchPosts);
        switch (position) {
            case 0:
                return searchUser;
            case 1:
                return searchPosts;

            default:
                return null;
        }
    }
}
