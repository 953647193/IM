package com.example.instantMessaging.Fragments.main;

import com.example.common.app.Fragment;
import com.example.instantMessaging.R;

/**
 * @author brsmsg
 * @time 2020/3/12
 */
public class MessageFragment extends Fragment {
    @Override
    protected int getContentLayoutId() {
        return com.example.instantMessaging.R.layout.fragment_message;
    }

    @Override
    protected void initData() {
        super.initData();
        String url = "http://101.200.240.107/root/resources/pictures/1.jpg";
    }
}
