package com.example.administrator.godmapdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

public class MainActivity extends Activity implements LocationSource,
        AMapLocationListener /*声明定位回调监听器 */, AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener
        , View.OnClickListener {
    public static int REQUEST_OK = 2;
    private AMap aMap;
    private MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;//声明AMapLocationClient类对象
    private AMapLocationClientOption mLocationOption;//声明AMapLocationClientOption对象
    private Marker marker;
    private GeocodeSearch geocodeSearch;
    private TextView tvStart;
    private TextView forMe;
    private TextView forOthers;
    private TextView tvEnd;
    private TextView tvPhone;
    private TextView tvTime;
    private double nowLatitude;  //当前定位 经度
    private double nowLongitude; //当前定位 纬度
    private SharedPreferences sp;  //缓存经纬度

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写

        tvStart = findViewById(R.id.tv_start);
        tvStart.setOnClickListener(this);
        forMe = findViewById(R.id.for_me);
        forMe.setOnClickListener(this);
        forOthers = findViewById(R.id.for_others);
        forOthers.setOnClickListener(this);
        tvEnd = findViewById(R.id.tv_end);
        tvEnd.setOnClickListener(this);
        tvPhone = findViewById(R.id.tv_phone);
        tvPhone.setOnClickListener(this);
        tvTime = findViewById(R.id.tv_time);
        tvTime.setOnClickListener(this);
        findViewById(R.id.commit).setOnClickListener(this);

        sp = this.getSharedPreferences("address", 0);

        init();

    }

    private void init() {
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);

        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }

    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
//        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
//        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
//        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
//        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        myLocationStyle.showMyLocation(true);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(15f));
        aMap.setLocationSource(this);// 设置定位监听
        aMap.setOnCameraChangeListener(this);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setScaleControlsEnabled(true);
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并可触发定位，默认是false
        if (!TextUtils.isEmpty(sp.getString("latitude", "")) && !TextUtils.isEmpty(sp.getString("longitude", ""))) {
            final double latitude = Double.parseDouble(sp.getString("latitude", ""));
            final double longitude = Double.parseDouble(sp.getString("longitude", ""));
            aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
                @Override
                public void onMapLoaded() {
                    LatLng localLatLng = new LatLng(latitude, longitude);
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localLatLng, 18));
                }
            });
        }
        // aMap.setMyLocationType()
        // 设置定位的类型为定位模式，有定位、跟随或地图根据面向方向旋转几种
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                Log.e("我的位置:  latitude----", amapLocation.getLatitude() + "longitude------" + amapLocation.getLongitude());

                nowLatitude = amapLocation.getLatitude();
                nowLongitude = amapLocation.getLongitude();

                SharedPreferences.Editor edit = sp.edit();
                edit.putString("latitude", nowLatitude + "");
                edit.putString("Longitude", nowLongitude + "");
                edit.commit();
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            /**
             * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
             */
            mLocationOption.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
            mLocationOption.setInterval(2000);
            //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mLocationOption.setHttpTimeOut(20000);
            //关闭缓存机制
            mLocationOption.setLocationCacheEnable(false);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除

            mlocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    private boolean isChange = true;

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (isChange) {
            tvStart.setText("正在获取地理位置...");
            Log.e("onCameraChange:  ----", "onCameraChange");
            isChange = false;
        }
    }


    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        LatLng target = cameraPosition.target;
        Log.e("屏幕中点对应:  latitude----", target.latitude + "longitude------" + target.longitude);
        // 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        LatLonPoint latLonPoint = new LatLonPoint(target.latitude, target.longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);

        latLonPoint = null;
        query = null;
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        Log.e("rCode:      ----", rCode + "");
        String building = regeocodeResult.getRegeocodeAddress().getFormatAddress();
        int startIndex = building.indexOf("街道");
        String address = building.substring(startIndex + 2, building.length());
        tvStart.setText(address);
        isChange = true;

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
    }

    private boolean isForMe = true;
    public static final int START_ADDRESS = 0;
    public static final int END_ADDRESS = 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.for_me:
                isForMe = true;
                tvPhone.setVisibility(View.GONE);
                break;
            case R.id.for_others:
                isForMe = false;
                tvPhone.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_time:
                Toast.makeText(this, "选择时间", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_phone:
                Toast.makeText(this, "选择联系人电话", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_start:
                Toast.makeText(this, "选择出发地", Toast.LENGTH_SHORT).show();
                SelectAddressActivity.startUiForResult(this, START_ADDRESS);
                break;
            case R.id.tv_end:
                Toast.makeText(this, "选择目的地", Toast.LENGTH_SHORT).show();
                SelectAddressActivity.startUiForResult(this, END_ADDRESS);
                break;
            case R.id.commit:
                getInfo(isForMe);
                break;
        }
    }

    public void getInfo(boolean isForMe) {
        String time = tvTime.getText().toString().trim();
        String phone = tvPhone.getText().toString().trim();
        String star = tvStart.getText().toString().trim();
        String end = tvEnd.getText().toString().trim();
        Toast.makeText(this, time + ";" + (isForMe ? "" : phone + ";") + star + ";" + end, Toast.LENGTH_SHORT).show();
    }

    public static String ADDRESS = "address";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == START_ADDRESS) {
                tvStart.setText(data.getStringExtra(ADDRESS));
            }

            if (requestCode == END_ADDRESS) {
                tvEnd.setText(data.getStringExtra(ADDRESS));
            }
        }
    }
}
