package vn.edu.stu.Adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import vn.edu.stu.Fragment.ChatFragment;
import vn.edu.stu.Fragment.GroupChatFragment;
import vn.edu.stu.Fragment.SearchingUserChatFragment;
import vn.edu.stu.luanvanmxhhippo.R;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    Context context;

    public SectionsPagerAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 1:
                GroupChatFragment groupChatFragment = new GroupChatFragment();
                return groupChatFragment;

            case 2:
                SearchingUserChatFragment searchingUserChatFragment = new SearchingUserChatFragment();
                return searchingUserChatFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }


    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String Chat = context.getString(R.string.chat);
        String Group = context.getString(R.string.group);
        String Search = context.getString(R.string.search);

        switch (position) {
            case 0:
                return Chat;
            case 1:
                return Group;
            case 2:
                return Search;

            default:
                return null;
        }
    }


}