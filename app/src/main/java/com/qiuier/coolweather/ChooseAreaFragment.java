package com.qiuier.coolweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qiuier.coolweather.db.City;
import com.qiuier.coolweather.db.County;
import com.qiuier.coolweather.db.Province;
import com.qiuier.coolweather.util.HttpUtil;
import com.qiuier.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by qiuyi on 2017/6/2.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE =0;
    public static final int LEVEL_CITY =1;
    public static final int LEVEL_COUNTY =2;

    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ProgressDialog progressDialog;//进度条

    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectProvince;
    private City selectCity;
    private int currentLevel;//当前选中的级别

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.choose_area,container,false);
        titleText= (TextView) view.findViewById(R.id.title_text);
        backButton= (Button) view.findViewById(R.id.back_button);
        listView= (ListView) view.findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectProvince=provinceList.get(i);
                    queryCity();
                }else if (currentLevel==LEVEL_CITY){
                    selectCity=cityList.get(i);
                    queryCounty();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }else if (currentLevel==LEVEL_COUNTY){
                    queryCity();
                }
            }
        });
        queryProvinces();//会首先执行
    }

    /*
    查询省份列表，优先数据库
     */
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);//返回按钮不可见
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province pro:provinceList) {
                dataList.add(pro.getProvinceName());//将省份名字添加到列表
            }
            adapter.notifyDataSetChanged();//通知页面数据更新
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            String addressProvince="http://guolin.tech/api/china";
            queryFromServer(addressProvince,"province");
        }
    }



    /**
     * 查询市
     */
    private void queryCity(){
        titleText.setText(selectProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);//返回按钮可见
        cityList=DataSupport.where("provinceid = ?",String.valueOf(selectProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode =selectProvince.getProvinceCode();
            String addressCity="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(addressCity,"city");
        }
    }
    private void queryCounty(){
        titleText.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("city=?",String.valueOf(selectCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County c:countyList){
                dataList.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int provinceCode=selectProvince.getProvinceCode();
            int cityCode=selectCity.getCityCode();
            String addressCounty="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(addressCounty,"county");
        }
    }


    private void queryFromServer(String address, final String type) {
        showPRogressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {//通知ui加载失败
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText =response.body().string();
                boolean result=false;
                if ("province".equals(type)){//如果是省数据，保存到省数据库表
                    result= Utility.handleProvinceResponse(responseText);
                }else if ("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectProvince.getId());
                }else if ("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectCity.getId());
                }
                if (result){//如果成功，通知ui线程刷新
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCity();
                            }else if ("county".equals(type)){
                                queryCounty();
                            }
                        }
                    });
                }
            }
        });
    }
    private void showPRogressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载。。");
            progressDialog.setCanceledOnTouchOutside(false);//关闭取消按钮
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();//关闭
        }
    }
}
