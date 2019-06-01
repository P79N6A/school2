package com.mnevent.hz.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mnevent.hz.Adapter.CheckAdapter;
import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.Utils.LogUtils;

import org.litepal.LitePal;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/12/24.
 * 盘点
 */

public class CheckActivity extends Activity {

    @BindView(R.id.titlename)
    TextView titlename;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.topbar_title)
    RelativeLayout topbarTitle;
    @BindView(R.id.recy)
    RecyclerView recy;
    @BindView(R.id.sava)
    Button sava;

    String classname = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        classname = getClass().getSimpleName();
        LogUtils.d("activity"+ getClass().getSimpleName());

        setContentView(R.layout.activity_check);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        List<PathwayMolder> preall = LitePal.findAll(PathwayMolder.class);
        LinearLayoutManager manager = new LinearLayoutManager(CheckActivity.this);
        recy.setLayoutManager(manager);
        CheckAdapter adapter = new CheckAdapter(CheckActivity.this, preall);
        recy.setAdapter(adapter);
    }



    @OnClick({R.id.btn_back, R.id.sava})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.sava:

                break;
        }
    }
}
