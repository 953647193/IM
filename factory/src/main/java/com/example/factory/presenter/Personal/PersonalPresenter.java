package com.example.factory.presenter.Personal;

import android.app.Activity;
import android.app.Person;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.common.factory.base.BasePresenter;
import com.example.factory.Factory;
import com.example.factory.R;
import com.example.factory.model.User;
import com.example.factory.model.api.Account.PersonalModel;

import java.io.File;
import java.lang.reflect.Type;

import butterknife.BindView;

/**
 * The type Personal presenter.
 */
public class PersonalPresenter implements PersonalContract.Presenter {
    //用户信息更改的方法实现

    private User user;

/*    //绑定View与Presenter
    private PersonalContract.View mPersonalView;
    public PersonalPresenter(PersonalContract.View personalView){
        mPersonalView = personalView;
        mPersonalView.setPresenter(this);
    }*/

    @Override
    public User getUserPersonal() {
        return user;
    }

    //解析服务器返回数据
    @Override
    public void parsePersonalResult(String result) {
        PersonalModel personalModel = Factory.getInstance()
                .getGson().fromJson(result,PersonalModel.class);

        String id = personalModel.getId();
        String username = personalModel.getUserName();
        String portrait = personalModel.getPortrait();

    }

    //拉取用户界面数据
    @Override
    public void start() {

        Factory.getInstance().getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                /*PersonalModel personalModel = new PersonalModel();*/
            }
        });

    }


}
