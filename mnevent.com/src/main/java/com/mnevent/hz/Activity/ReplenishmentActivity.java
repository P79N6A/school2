package com.mnevent.hz.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mnevent.hz.Adapter.ReplenishmentAdapter;
import com.mnevent.hz.R;
import com.mnevent.hz.litemolder.PathwayMolder;
import com.mnevent.hz.Utils.BasicparameterPopuwindow;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import android_serialport_api.ComTrackMagex;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/12/25.
 * 补货
 */

public class ReplenishmentActivity extends Activity {

    @BindView(R.id.titlename)
    TextView titlename;
    @BindView(R.id.btn_back)
    Button btnBack;
    @BindView(R.id.topbar_title)
    RelativeLayout topbarTitle;
    @BindView(R.id.recy)
    RecyclerView recy;
    @BindView(R.id.move)
    Button move;

    private String[] textes = new String[]{"第一层","第二层","第三层","第四层","第五层","第六层","第七层","第八层"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_replenishment);
        ButterKnife.bind(this);

        initView();
    }

    private void initView() {
        List<PathwayMolder> all = LitePal.findAll(PathwayMolder.class);
        List<PathwayMolder> alls = new ArrayList<>();
        alls.clear();
        for (int i = 0;i<all.size();i++){
            if(all.get(i).getPds().equals("1")){
                alls.add(all.get(i));
                Log.d("zlc",i+"");
            }
        }
        LinearLayoutManager manager = new LinearLayoutManager(ReplenishmentActivity.this);
        recy.setLayoutManager(manager);
        ReplenishmentAdapter adapter = new ReplenishmentAdapter(ReplenishmentActivity.this, alls);
        recy.setAdapter(adapter);

    }



    @OnClick({R.id.btn_back, R.id.move})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.move:
                final BasicparameterPopuwindow popuwindow = new BasicparameterPopuwindow(ReplenishmentActivity.this,textes,0);
                popuwindow.showAtLocation(getLayoutInflater().inflate(R.layout.activity_replenishment, null), Gravity.CENTER,0,0);
                popuwindow.SetOnclike(new BasicparameterPopuwindow.Onckile() {
                    @Override
                    public void onclike(final int position) {
                       new Thread(new Runnable() {
                           @Override
                           public void run() {
                               int po=position+1;
                               ComTrackMagex magex = new ComTrackMagex("");

                                   magex.openSerialPort();

                               magex.moverobot(po);
                               magex.closeSerialPort();
                           }
                       }).start();
                        popuwindow.dismiss();
                    }
                });
                break;
        }
    }
}
