package com.coolweather.android;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coolweather.android.db.City;
import com.coolweather.android.db.County;
import com.coolweather.android.db.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog progressDialog;
    private TextView title_text;
    private Button back_button;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> dataList;
    /**
     * 省列表
     * **/
    private List<Province> provinceList;
    /**
     * 市列表
     * **/
    private List<City> cityList;
    /**
     * 县列表
     * **/
    private List<County> countyList;
    /**
     * 选中的省份
     * **/
    private Province selectedProvince;
    /**
     * 选中的城市
     * **/
    private City selectedCity;
    /**
     * 当前选中的级别
     * **/
    private int currentLevel=3;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        title_text=view.findViewById(R.id.title_text);
        back_button=view.findViewById(R.id.back_button);
        list_view=view.findViewById(R.id.list_view);
        dataList=new ArrayList<>();
        arrayAdapter=new ArrayAdapter<String>(AppClient.getContext(),android.R.layout.simple_list_item_1,dataList);
        list_view.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(i);
                    queryCities();
                }else  if (currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(i);
                    queryCounties();
                }
            }
        });
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else  if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    /**
     *查询全国所有的省，有限从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvinces(){
        title_text.setText("中国");
        back_button.setVisibility(View.GONE);
        provinceList= LitePal.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_view.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else {
            queryFromServer("china","province");
        }
    }
    /**
     *查询全国所有的市，有限从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities(){
        title_text.setText(selectedProvince.getProvinceName());
        back_button.setVisibility(View.VISIBLE);
        cityList= LitePal.where("provinceId=?",String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_view.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            queryFromServer("china/"+selectedProvince.getProvinceCode(),"city");
        }
    }
    /**
     *查询全国所有的县，有限从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties(){
        title_text.setText(selectedCity.getCityName());
        back_button.setVisibility(View.VISIBLE);
        countyList= LitePal.where("cityId=?",String.valueOf(selectedCity.getCityCode())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            arrayAdapter.notifyDataSetChanged();
            list_view.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else {
            int cityCode=selectedCity.getCityCode();
            int provinceCode=selectedProvince.getProvinceCode();
            String address="china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * **/
    private void queryFromServer(String address, final String type){
        final String city="city";
        final String province="province";
        final String county="county";
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(AppClient.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText= Objects.requireNonNull(response.body()).string();
                boolean result= false;
                switch (type){
                    case province:
                        result= Utility.handleProvinceResponse(responseText);
                        break;
                    case city:
                        result= Utility.handleCityResponse(responseText,selectedProvince.getProvinceCode());
                        break;
                    case county:
                        result= Utility.handleCountyResponse(responseText,selectedCity.getCityCode());
                        break;
                }
                if (result){
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type){
                                case province:
                                    queryProvinces();
                                    break;
                                case city:
                                    queryCities();
                                    break;
                                case county:
                                    queryCounties();
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }
    /**
     * 显示进度对话框
     * **/
    private void showProgressDialog(){
        if (progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            /**
             *不能使用getApplicationContext()获得的Context,而必须使用Activity,因为只有一个Activity才能添加一个窗体。
             * **/
            progressDialog.setMessage("loading...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        if (progressDialog!=null){
            progressDialog.show();
        }

    }
    /**
     * 关闭进度对话框
     * **/
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
