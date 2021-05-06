package com.tuya.smart.android.demo.base.fragment;

import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.presenter.PersonalCenterFragmentPresenter;
import com.tuya.smart.android.demo.personal.IPersonalCenterView;
import com.tuya.smart.android.user.api.IQurryDomainCallback;
import com.tuya.smart.api.MicroContext;
import com.tuya.smart.api.router.UrlRouter;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.message.base.activity.message.MessageContainerActivity;
import com.tuyasmart.stencil.utils.ActivityUtils;

/**
 * Created by letian on 16/7/18.
 */
public class PersonalCenterFragment extends BaseFragment implements IPersonalCenterView {

    public Toolbar mToolBar;
    public TextView mUserName;
    public TextView mNickName;
    private View mContentView;

    protected PersonalCenterFragmentPresenter mPersonalCenterFragmentPresenter;
    private static PersonalCenterFragment mPersonalCenterFragment;

    public static synchronized Fragment newInstance() {
        if (mPersonalCenterFragment == null) {
            mPersonalCenterFragment = new PersonalCenterFragment();
        }
        return mPersonalCenterFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_personal_center, container, false);
        initToolbar(mContentView);
        initView();
        initMenu();
        initPresenter();
        return mContentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPersonalCenterFragmentPresenter.setPersonalInfo();
    }


    private void initView() {
        mToolBar = (Toolbar) mContentView.findViewById(R.id.toolbar_top_view);
        mUserName = (TextView) mContentView.findViewById(R.id.tv_username);
        mNickName = (TextView) mContentView.findViewById(R.id.tv_nickname);

        mContentView.findViewById(R.id.rl_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        mContentView.findViewById(R.id.rl_question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TuyaHomeSdk.getUserInstance().queryDomainByBizCodeAndKey("help_center", "main_page", new IQurryDomainCallback() {
                    @Override
                    public void onSuccess(String domain) {
                        UrlRouter.execute(UrlRouter.makeBuilder(getActivity(), "helpCenter"));
                    }

                    @Override
                    public void onError(String code, String error) {
                        return;
                    }
                });
            }
            });

        mContentView.findViewById(R.id.rl_edit_person).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPersonalCenterFragmentPresenter.gotoPersonalInfoActivity();
            }
        });
        mContentView.findViewById(R.id.rl_messageCenter).setOnClickListener(new View.OnClickListener(){
            @Override
                    public void onClick(View v){
                    /*Intent intent = new Intent(getActivity(), MessageActivity.class);
                    getActivity().startActivity(intent);*/
                com.tuyasmart.stencil.utils.ActivityUtils.gotoActivity(getActivity(),
                        MessageContainerActivity.class,
                        ActivityUtils.ANIMATE_SLIDE_TOP_FROM_BOTTOM,
                        false);
            }
        });
        TypedArray a = getActivity().obtainStyledAttributes(new int[]{
                R.attr.user_default_portrait});
        int portraitRes = a.getResourceId(0, -1);
        if (portraitRes != -1) {
            mContentView.findViewById(R.id.iv_head_photo).setBackgroundResource(portraitRes);
        }
        a.recycle();
    }

    private void initPresenter() {
        mPersonalCenterFragmentPresenter = new PersonalCenterFragmentPresenter(getActivity(), this);
    }

    private void initMenu() {
        setTitle(getString(R.string.personal_center));
    }

    @Override
    public void setUserName(String userName) {
        if (TextUtils.isEmpty(userName)) {
            mUserName.setText(R.string.click_bind_phone);
        } else {
            mUserName.setText(userName);
        }
    }

    @Override
    public void setNickName(String nickName) {
        if (TextUtils.isEmpty(nickName)) {
            mNickName.setText(R.string.click_set_neekname);
        } else {
            mNickName.setText(nickName);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPersonalCenterFragmentPresenter.onDestroy();
    }


}
