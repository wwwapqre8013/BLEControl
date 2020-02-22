package com.zistone.blecontrol.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.blecontrol.R;
import com.zistone.blecontrol.pojo.Material;
import com.zistone.blecontrol.util.ConvertUtil;
import com.zistone.blecontrol.util.MyOkHttpUtil;
import com.zistone.blecontrol.util.ProgressDialogUtil;
import com.zistone.blecontrol.util.PropertiesUtil;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BluetoothFragment_DB extends Fragment implements View.OnClickListener, MyOkHttpUtil.MyOkHttpListener, Spinner.OnItemSelectedListener
{
    private static final String TAG = "BluetoothFragment_DB";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final int MESSAGE_1 = 1;
    private static final int MESSAGE_2 = 2;
    private static final int MESSAGE_3 = 3;
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int MESSAGE_ERROR_2 = -2;
    private static final int MESSAGE_ERROR_3 = -3;
    private static final int MESSAGE_ERROR_4 = -4;
    private static final int MESSAGE_ERROR_5 = -5;
    private static UUID SERVICE_UUID;
    private static UUID WRITE_UUID;
    private static UUID READ_UUID;
    private static UUID CONFIG_UUID;
    private static String URL;
    private OnFragmentInteractionListener _onFragmentInteractionListener;
    private Context _context;
    private View _view;
    private Spinner _spinner1, _spinner2;
    private Button _btn1, _btn2;
    private ImageButton _btnReturn;
    private TextView _txt1, _txt2, _txt3, _txt4, _txt5, _txt7, _txt8;
    private EditText _edt1;
    private BluetoothDevice _bluetoothDevice;
    private BluetoothGatt _bluetoothGatt;
    private BluetoothGattService _bluetoothGattService;
    private BluetoothGattCharacteristic _bluetoothGattCharacteristic_write;
    private BluetoothGattCharacteristic _bluetoothGattCharacteristic_read;
    private Map<String, UUID> _uuidMap;
    private int _rssi;
    private MaterialRefreshLayout _materialRefreshLayout;
    private MaterialRefreshListener _materialRefreshListener;

    public static BluetoothFragment_DB newInstance(BluetoothDevice bluetoothDevice, Map<String, UUID> uuidMap, int rssi)
    {
        BluetoothFragment_DB fragment = new BluetoothFragment_DB();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        args.putSerializable(ARG_PARAM2, (Serializable) uuidMap);
        args.putInt(ARG_PARAM3, rssi);
        fragment.setArguments(args);
        MyOkHttpUtil.Init();
        return fragment;
    }

    private View.OnKeyListener backListener = new View.OnKeyListener()
    {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
            {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_DB.this).commitNow();
                return true;
            }
            return false;
        }
    };

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            String result = (String) message.obj;
            switch(message.what)
            {
                //成功连接蓝牙设备
                case MESSAGE_1:
                    _btn1.setEnabled(true);
                    ProgressDialogUtil.Dismiss();
                    break;
                //成功读取到蓝牙设备的电量
                case MESSAGE_2:
                    _txt4.setText(result + "%");
                    break;
                //物料绑定成功
                case MESSAGE_3:
                    _materialRefreshLayout.finishRefresh();
                    Material material = JSON.parseObject(result, Material.class);
                    if(material != null)
                    {
                        int row = material.getDepotRow();
                        int column = material.getDepotColumn();
                        _txt5.setText(String.valueOf(material.getId()));
                        _edt1.setText(material.getMaterialName());
                        //这里可通过Spinner的选中事件进行赋值
                        //_txt7.setText(String.valueOf(row));
                        //_txt8.setText(String.valueOf(column));
                        _spinner1.setSelection(row--, true);
                        _spinner2.setSelection(column--, true);
                        if(URL.contains("FindByDeviceAddress"))
                        {
                        }
                        else if(URL.contains("UPDATE"))
                        {
                            ShowWarning(MESSAGE_3);
                        }
                    }
                    //物料绑定失败
                    else
                    {
                        ShowWarning(MESSAGE_ERROR_5);
                    }
                    break;
                //与蓝牙设备的连接已断开
                case MESSAGE_ERROR_2:
                    ShowWarning(MESSAGE_ERROR_2);
                    break;
                //网络异常
                case MESSAGE_ERROR_3:
                    _materialRefreshLayout.finishRefresh();
                    ShowWarning(MESSAGE_ERROR_3);
                    break;
                //服务异常
                case MESSAGE_ERROR_4:
                    ShowWarning(MESSAGE_ERROR_4);
                    break;
            }
        }
    };

    private void ShowWarning(int param)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("警告");
        switch(param)
        {
            case MESSAGE_3:
                builder.setMessage("物料绑定成功!");
                break;
            case MESSAGE_ERROR_1:
                builder.setMessage("未获取到蓝牙设备,请重试!");
                break;
            case MESSAGE_ERROR_2:
                builder.setMessage("该设备的连接已断开,如需再次连接请重试!");
                break;
            case MESSAGE_ERROR_3:
                builder.setMessage("网络连接失败,请检查!");
                break;
            case MESSAGE_ERROR_4:
                builder.setMessage("服务异常,请与管理员联系!");
                break;
            case MESSAGE_ERROR_5:
                builder.setMessage("物料绑定失败,请重试!");
                break;
        }
        builder.setPositiveButton("知道了", (dialog, which) ->
        {
        });
        builder.show();
    }

    @Override
    public void onClick(View v)
    {
        //隐藏键盘
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        _edt1.clearFocus();
        switch(v.getId())
        {
            case R.id.btn_return_db:
            {
                ProgressDialogUtil.Dismiss();
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_DB.this).commitNow();
            }
            break;
            case R.id.btn2_db:
                break;
            case R.id.btn1_db:
            {
                ProgressDialogUtil.ShowProgressDialog(_context, "正在绑定物料...");
                URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/Material/Update";
                Material materiel = new Material();
                materiel.setDeviceName(_txt2.getText().toString());
                materiel.setMaterialName(_edt1.getText().toString());
                materiel.setDeviceAddress(_bluetoothDevice.getAddress());
                materiel.setDepotRow(Integer.valueOf(_spinner1.getSelectedItem().toString()));
                materiel.setDepotColumn(Integer.valueOf(_spinner2.getSelectedItem().toString()));
                String jsonData = JSON.toJSONString(materiel);
                MyOkHttpUtil.AsySendBody(URL, jsonData, this::AsyOkHttpResult);
            }
            break;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            _bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
            _uuidMap = (Map<String, UUID>) getArguments().getSerializable(ARG_PARAM2);
            SERVICE_UUID = _uuidMap.get("SERVICE_UUID");
            WRITE_UUID = _uuidMap.get("WRITE_UUID");
            READ_UUID = _uuidMap.get("READ_UUID");
            CONFIG_UUID = _uuidMap.get("CONFIG_UUID");
            _rssi = getArguments().getInt(ARG_PARAM3);
        }
        _context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _view = inflater.inflate(R.layout.fragment_bluetooth_db, container, false);
        //强制获得焦点
        _view.requestFocus();
        _view.setFocusable(true);
        _view.setFocusableInTouchMode(true);
        _view.setOnKeyListener(backListener);
        _btnReturn = _view.findViewById(R.id.btn_return_db);
        _btnReturn.setOnClickListener(this);
        _spinner1 = _view.findViewById(R.id.spinner1_db);
        _spinner1.setOnItemSelectedListener(this);
        _spinner2 = _view.findViewById(R.id.spinner2_db);
        _spinner2.setOnItemSelectedListener(this);
        _btn1 = _view.findViewById(R.id.btn1_db);
        _btn2 = _view.findViewById(R.id.btn2_db);
        _txt1 = _view.findViewById(R.id.text1_db);
        _txt2 = _view.findViewById(R.id.text2_db);
        _txt3 = _view.findViewById(R.id.text3_db);
        _txt3.setText(String.valueOf(_rssi));
        _txt4 = _view.findViewById(R.id.text4_db);
        _txt5 = _view.findViewById(R.id.text5_db);
        _txt7 = _view.findViewById(R.id.text7_db);
        _txt8 = _view.findViewById(R.id.text8_db);
        _btn1.setOnClickListener(this::onClick);
        _btn2.setOnClickListener(this::onClick);
        _edt1 = _view.findViewById(R.id.edt1_db);
        _edt1.clearFocus();
        if(_bluetoothDevice != null)
        {
            _txt1.setText(_bluetoothDevice.getAddress());
            _txt2.setText(_bluetoothDevice.getName());
            //电池电量在连接成功后通过UUID获取
            Log.d(TAG, ">>>开始连接...");
            ProgressDialogUtil.ShowProgressDialog(_context, "正在连接...");
            //连接蓝牙设备的回调
            _bluetoothGatt = _bluetoothDevice.connectGatt(_context, false, new BluetoothGattCallback()
            {
                /**
                 * 连接状态改变时回调
                 * @param gatt
                 * @param status
                 * @param newState
                 */
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
                {
                    if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED)
                    {
                        Log.d(TAG, ">>>成功建立连接!");
                        //发现服务
                        gatt.discoverServices();
                    }
                    else
                    {
                        Log.d(TAG, ">>>连接已断开!");
                        _bluetoothGatt.disconnect();
                        ProgressDialogUtil.Dismiss();
                        Message message = handler.obtainMessage(MESSAGE_ERROR_2, "");
                        handler.sendMessage(message);
                    }
                }

                /**
                 * 发现设备(真正建立连接)
                 * @param gatt
                 * @param status
                 */
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status)
                {
                    //直到这里才是真正建立了可通信的连接
                    //通过UUID找到服务
                    _bluetoothGattService = _bluetoothGatt.getService(SERVICE_UUID);
                    if(_bluetoothGattService != null)
                    {
                        //写数据的服务和特征
                        _bluetoothGattCharacteristic_write = _bluetoothGattService.getCharacteristic(WRITE_UUID);
                        if(_bluetoothGattCharacteristic_write != null)
                        {
                            Log.d(TAG, ">>>已找到写入数据的特征值!");
                            Message message = handler.obtainMessage(MESSAGE_1, "");
                            handler.sendMessage(message);
                        }
                        else
                        {
                            Log.e(TAG, ">>>该UUID无写入数据的特征值!");
                        }
                        //读取数据的服务和特征
                        _bluetoothGattCharacteristic_read = _bluetoothGattService.getCharacteristic(READ_UUID);
                        if(_bluetoothGattCharacteristic_read != null)
                        {
                            Log.d(TAG, ">>>已找到读取数据的特征值!");
                            //手动读取蓝牙设备的参数内容,所以不需要再订阅
                            //                            订阅读取通知
                            //                            gatt.setCharacteristicNotification(_bluetoothGattCharacteristic_read, true);
                            //                            BluetoothGattDescriptor descriptor = _bluetoothGattCharacteristic_read.getDescriptor(CONFIG_UUID);
                            //                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            //                            gatt.writeDescriptor(descriptor);

                            new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    Looper.prepare();
                                    try
                                    {
                                        Thread.sleep(1000);
                                        _bluetoothGatt.readCharacteristic(_bluetoothGattCharacteristic_read);
                                    }
                                    catch(InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                    Looper.loop();
                                }
                            }).start();
                            ProgressDialogUtil.Dismiss();
                        }
                        else
                        {
                            Log.e(TAG, ">>>该UUID无读取数据的特征值!");
                        }
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                {
                    byte[] byteArray = characteristic.getValue();
                    String result = ConvertUtil.ByteArrayToHexStr(byteArray);
                    result = ConvertUtil.HexStrAddCharacter(result, " ");
                    Log.d(TAG, ">>>接收:" + result);
                    Message message = handler.obtainMessage(MESSAGE_2, result);
                    handler.sendMessage(message);
                }

                /**
                 * 写入成功后回调
                 *
                 * @param gatt
                 * @param characteristic
                 * @param status
                 */
                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
                {
                    byte[] byteArray = characteristic.getValue();
                    String result = ConvertUtil.ByteArrayToHexStr(byteArray);
                    Log.d(TAG, ">>>发送:" + result);
                }

                /**
                 * 收到硬件返回的数据时回调,如果是Notify的方式
                 * @param gatt
                 * @param characteristic
                 */
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
                {
                    byte[] byteArray = characteristic.getValue();
                    String result = ConvertUtil.ByteArrayToHexStr(byteArray);
                    result = ConvertUtil.HexStrAddCharacter(result, " ");
                    Log.d(TAG, ">>>接收:" + result);

                }
            });
        }
        //未获取到蓝牙设备
        else
        {
            _btn1.setEnabled(false);
            ProgressDialogUtil.Dismiss();
            ShowWarning(MESSAGE_ERROR_1);
        }
        //下拉刷新的监听
        _materialRefreshListener = new MaterialRefreshListener()
        {
            /**
             * 下拉刷新
             * 下拉刷新的时候需要清空ListView然后重新绑定
             *
             * @param materialRefreshLayout
             */
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout)
            {
                //结束下拉刷新
                materialRefreshLayout.postDelayed(() -> new Thread(() ->
                {
                    Looper.prepare();
                    URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/Material/FindByDeviceAddress";
                    Map<String, String> map = new HashMap<String, String>()
                    {{
                        this.put("deviceAddress", _bluetoothDevice.getAddress());
                    }};
                    MyOkHttpUtil.AsySendMap(URL, map, BluetoothFragment_DB.this::AsyOkHttpResult);
                    Looper.loop();
                }).start(), 500);
            }

            /**
             * 加载完毕
             */
            @Override
            public void onfinish()
            {
                //Toast.makeText(_context, "完成", Toast.LENGTH_LONG).show();
            }

            /**
             * 加载更多
             *
             * @param materialRefreshLayout
             */
            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout)
            {
                Toast.makeText(_context, "别滑了,到底了", Toast.LENGTH_SHORT).show();
            }
        };
        //下拉刷新控件
        _materialRefreshLayout = _view.findViewById(R.id.refresh_bluetoothDB);
        //启用加载更多
        _materialRefreshLayout.setLoadMore(false);
        //自动刷新
        _materialRefreshLayout.autoRefresh();
        _materialRefreshLayout.setMaterialRefreshListener(_materialRefreshListener);
        return _view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        switch(parent.getId())
        {
            case R.id.spinner1_db:
                _txt7.setText(_spinner1.getSelectedItem().toString());
                break;
            case R.id.spinner2_db:
                _txt8.setText(_spinner2.getSelectedItem().toString());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
    }

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri)
    {
        if(_onFragmentInteractionListener != null)
        {
            _onFragmentInteractionListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            _onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        _materialRefreshLayout.finishRefresh();
        if(_bluetoothGatt != null)
            _bluetoothGatt.disconnect();
        _bluetoothDevice = null;
    }

    @Override
    public void AsyOkHttpResult(int result, String content)
    {
        ProgressDialogUtil.Dismiss();
        int what = 0;
        switch(result)
        {
            //响应正常
            case 1:
                what = MESSAGE_3;
                break;
            //服务异常
            case -1:
                what = MESSAGE_ERROR_4;
                break;
            //网络异常
            case -2:
                what = MESSAGE_ERROR_3;
                break;
        }
        Message message = handler.obtainMessage(what, content);
        handler.sendMessage(message);
    }

}