package com.zistone.bluetoothtest.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.bluetoothtest.R;
import com.zistone.bluetoothtest.activity.MainActivity;
import com.zistone.bluetoothtest.control.MyScrollView;
import com.zistone.bluetoothtest.util.ConvertUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothFragment_PowerControl extends Fragment implements View.OnClickListener
{
    private static final String TAG = "BluetoothFragment_PowerControl";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_ERROR_1 = -1;
    private static final int MESSAGE_ERROR_2 = -2;
    private static final int MESSAGE_ERROR_3 = -3;
    private static final int MESSAGE_1 = 100;
    private static final int MESSAGE_OPENDOOR = 0;
    private static final int MESSAGE_READCAR = 1;
    private static final int MESSAGE_BATTERY = 2;
    private static final int MESSAGE_MAGNETIC = 3;
    private static final int MESSAGE_DOORSTATE = 4;
    private static final int MESSAGE_TESTA = 80;
    private static final int MESSAGE_OPENDOORS1 = 81;
    private static final int MESSAGE_OPENDOORS2 = 82;
    private static final int MESSAGE_OPENALLDOORS = 83;
    private static final int MESSAGE_SEARCHCONTROLPARAM = 86;
    private static UUID SERVICE_UUID;
    private static UUID WRITE_UUID;
    private static UUID READ_UUID;
    private static UUID CONFIG_UUID;

    private OnFragmentInteractionListener m_listener;
    private Context m_context;
    private View m_view;
    private ImageButton m_btnReturn;
    private TextView m_debugView;
    private Button m_button1;
    private Button m_button2;
    private Button m_button3;
    private Button m_button4;
    private Button m_button5;
    private TextView m_textView1;
    private TextView m_textView2;
    private TextView m_textView3;
    private TextView m_textView4;
    private TextView m_textView5;
    private TextView m_textView6;
    private ProgressBar m_progressBar;
    private BluetoothDevice m_bluetoothDevice;
    private BluetoothGatt m_bluetoothGatt;
    private BluetoothGattService m_bluetoothGattService;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_write;
    private BluetoothGattCharacteristic m_bluetoothGattCharacteristic_read;
    private StringBuffer m_stringBuffer = new StringBuffer();
    private Timer m_refreshTimer;
    private TimerTask m_refreshTask;
    private Toolbar m_toolbar;
    private MyScrollView m_scrollView;
    private LinearLayout m_llPowerControl;
    private WriteValueDialog m_writeValueDialog;
    private ParamSettingDialog m_paramSettingDialog;

    public static BluetoothFragment_PowerControl newInstance(BluetoothDevice bluetoothDevice, Map<String, UUID> map)
    {
        BluetoothFragment_PowerControl fragment = new BluetoothFragment_PowerControl();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, bluetoothDevice);
        args.putSerializable(ARG_PARAM2, (Serializable) map);
        fragment.setArguments(args);
        return fragment;
    }

    private View.OnKeyListener backListener = (v, keyCode, event) ->
    {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
        {
            BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
            getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
            getFragmentManager().beginTransaction().remove(BluetoothFragment_PowerControl.this).commitNow();
            return true;
        }
        return false;
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
                case MESSAGE_ERROR_1:
                    ShowWarning(MESSAGE_ERROR_1);
                    m_button1.setText("连接");
                    break;
                case MESSAGE_1:
                {
                    m_button2.setEnabled(true);
                    m_button3.setEnabled(true);
                    m_button4.setEnabled(true);
                    m_refreshTimer = new Timer();
                    m_refreshTask = new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            getActivity().runOnUiThread(() ->
                            {
                                try
                                {
                                    //综合测试
                                    String hexStr = "680000000000006810000180E616";
                                    Log.i(TAG, ">>>发送:" + hexStr);
                                    byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                                    m_bluetoothGattCharacteristic_write.setValue(byteArray);
                                    m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
                                    Thread.sleep(100);
                                }
                                catch(InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                            });
                        }
                    };
                    //任务、延迟执行时间、重复调用间隔,Timer和TimerTask在调用cancel()取消后不能再执行schedule语句
                    m_refreshTimer.schedule(m_refreshTask, 0, 1 * 1000);
                    break;
                }
                case MESSAGE_OPENDOOR:
                {
                    if(result.equalsIgnoreCase("opendoor"))
                    {
                        m_debugView.append("已发送开门指令 ");
                    }
                    else if(result.equalsIgnoreCase("doorisopen"))
                    {
                        m_debugView.append("收到:门【已打开】\n");
                        m_textView1.setText("已开");
                    }
                    else
                    {
                        m_debugView.append("收到:门【未打开】\n");
                        m_textView1.setText("未开");
                    }
                    //定位到最后一行
                    int offset = m_debugView.getLineCount() * m_debugView.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if(offset > m_scrollView.getHeight())
                    {
                        m_debugView.scrollTo(0, offset - m_scrollView.getHeight());
                    }
                    break;
                }
                //电池电压
                case MESSAGE_BATTERY:
                    m_textView5.setText(result + "mV");
                    break;
                //磁场强度
                case MESSAGE_MAGNETIC:
                    m_textView6.setText(result);
                    break;
                //门状态
                case MESSAGE_DOORSTATE:
                    m_textView1.setText(result);
                    break;
                //目前只取电池容量
                case MESSAGE_TESTA:
                {
                    String strs[] = result.split(",");
                    String doorState1 = strs[0];
                    if(doorState1.equalsIgnoreCase("1"))
                    {
                        m_textView1.setText("已开");
                        m_textView1.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        m_textView1.setText("已关");
                        m_textView1.setTextColor(Color.RED);
                    }
                    String lockState1 = strs[1];
                    if(lockState1.equalsIgnoreCase("1"))
                    {
                        m_textView2.setText("已开");
                        m_textView2.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        m_textView2.setText("已关");
                        m_textView2.setTextColor(Color.RED);
                    }
                    String doorState2 = strs[2];
                    if(doorState2.equalsIgnoreCase("1"))
                    {
                        m_textView3.setText("已开");
                        m_textView3.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        m_textView3.setText("已关");
                        m_textView3.setTextColor(Color.RED);
                    }
                    String lockState2 = strs[3];
                    if(lockState2.equalsIgnoreCase("1"))
                    {
                        m_textView4.setText("已开");
                        m_textView4.setTextColor(Color.GREEN);
                    }
                    else
                    {
                        m_textView4.setText("已关");
                        m_textView4.setTextColor(Color.RED);
                    }
                    m_textView5.setText(strs[4] + "mV");
                    m_textView6.setText(String.format("下端:%sGs 上端:%sGs 前端:%sGs", strs[5], strs[6], strs[7]));
                    break;
                }
                //一号门锁
                case MESSAGE_OPENDOORS1:
                    break;
                //二号门锁
                case MESSAGE_OPENDOORS2:

                {
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    String doorState2 = String.valueOf(bitStr.charAt(7));
                    if(doorState2.equalsIgnoreCase("1"))
                        m_textView3.setText("已开");
                    else
                        m_textView3.setText("已关");
                    String lockState2 = String.valueOf(bitStr.charAt(6));
                    if(lockState2.equalsIgnoreCase("1"))
                        m_textView4.setText("已开");
                    else
                        m_textView4.setText("已关");
                    break;
                }
                //全部门锁
                case MESSAGE_OPENALLDOORS:
                {
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    String doorState1 = String.valueOf(bitStr.charAt(7));
                    if(doorState1.equalsIgnoreCase("1"))
                        m_textView1.setText("已开");
                    else
                        m_textView1.setText("已关");
                    String lockState1 = String.valueOf(bitStr.charAt(6));
                    if(lockState1.equalsIgnoreCase("1"))
                        m_textView2.setText("已开");
                    else
                        m_textView2.setText("已关");
                    String doorState2 = String.valueOf(bitStr.charAt(5));
                    if(doorState2.equalsIgnoreCase("1"))
                        m_textView3.setText("已开");
                    else
                        m_textView3.setText("已关");
                    String lockState2 = String.valueOf(bitStr.charAt(4));
                    if(lockState2.equalsIgnoreCase("1"))
                        m_textView4.setText("已开");
                    else
                        m_textView4.setText("已关");
                    break;
                }
                //查询内部控制参数
                case MESSAGE_SEARCHCONTROLPARAM:
                {
                    byte[] bytes = ConvertUtil.HexStrToByteArray(result);
                    String bitStr = ConvertUtil.ByteToBit(bytes[0]);
                    //门检测开关用采用常开型(关门开路)
                    String str1 = String.valueOf(bitStr.charAt(7));
                    //锁检测开定于关用采用常开型(锁上开路)
                    String str2 = String.valueOf(bitStr.charAt(6));
                    //正常开锁不告警
                    String str3 = String.valueOf(bitStr.charAt(5));
                    //有外电可以进入维护方式
                    String str4 = String.valueOf(bitStr.charAt(4));
                    //启用软关机
                    String str5 = String.valueOf(bitStr.charAt(3));
                    //不检测强磁
                    String str6 = String.valueOf(bitStr.charAt(2));
                    //使用低磁检测阀值
                    String str7 = String.valueOf(bitStr.charAt(1));
                    //启用DEBUG软串口
                    String str8 = String.valueOf(bitStr.charAt(0));
                    m_debugView.append("\n\n");
                    if(str1.equalsIgnoreCase("1"))
                    {
                        m_debugView.append("收到:\n关门开路【启用】\n");
                    }
                    else
                    {
                        m_debugView.append("收到:\n关门开路【禁用】\n");
                    }
                    if(str2.equalsIgnoreCase("1"))
                    {
                        m_debugView.append("锁上开路【启用】\n");
                    }
                    else
                    {
                        m_debugView.append("锁上开路【禁用】\n");
                    }
                    if(str3.equalsIgnoreCase("1"))
                    {
                        m_debugView.append("正常开锁不告警【启用】\n");
                    }
                    else
                    {
                        m_debugView.append("正常开锁不告警【禁用】\n");
                    }
                    if(str4.equalsIgnoreCase("1"))
                    {
                        m_debugView.append("有外电可以进入维护方式【启用】\n");
                    }
                    else
                    {
                        m_debugView.append("有外电可以进入维护方式【禁用】\n");
                    }
                    if(str5.equalsIgnoreCase("1"))
                    {
                        m_debugView.append("启用软关机【启用】\n");
                    }
                    else
                    {
                        m_debugView.append("启用软关机【禁用】\n");
                    }
                    if(str6.equalsIgnoreCase("1"))
                    {
                        m_debugView.append("不检测强磁【启用】\n");
                    }
                    else
                    {
                        m_debugView.append("不检测强磁【禁用】\n");
                    }
                    if(str7.equalsIgnoreCase("1"))
                    {
                        m_debugView.append("使用低磁检测阀值【启用】\n");
                    }
                    else
                    {
                        m_debugView.append("使用低磁检测阀值【禁用】\n");
                    }
                    if(str8.equalsIgnoreCase("1"))
                    {
                        m_debugView.append("启用DEBUG软串口【启用】\n");
                    }
                    else
                    {
                        m_debugView.append("启用DEBUG软串口【禁用】\n");
                    }
                    m_debugView.append("\n\n");
                    //定位到最后一行
                    int offset = m_debugView.getLineCount() * m_debugView.getLineHeight();
                    //如果文本的高度大于ScrollView的,就自动滑动
                    if(offset > m_debugView.getHeight())
                    {
                        m_debugView.scrollTo(0, offset - m_debugView.getHeight());
                    }
                    break;
                }
            }
        }
    };

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);

    }

    public void onButtonPressed(Uri uri)
    {
        if(m_listener != null)
        {
            m_listener.onFragmentInteraction(uri);
        }
    }

    private void Resolve(String data)
    {
        Log.d(TAG, ">>>共接收:" + data);
        String[] strArray = data.split(" ");
        String indexStr = strArray[12];
        switch(indexStr)
        {
            //开门
            case "00":
            {
                Message message = new Message();
                message.what = MESSAGE_OPENDOOR;
                if(strArray[14].equalsIgnoreCase("00"))
                {
                    message.obj = "doorisopen";
                }
                else if(ConvertUtil.HexStrToStr(strArray[13] + strArray[14]).equalsIgnoreCase("OK"))
                {
                    message.obj = "doorisopen";
                }
                else
                {
                    message.obj = "";
                }
                handler.sendMessage(message);
                break;
            }
            //读卡
            case "01":
            {
                break;
            }
            //电池电压
            case "02":
            {
                break;
            }
            //磁场强度
            case "03":
            {
                String responseValue1 = strArray[9].equals("00") ? "OK" : "Fail";
                //                String responseValue2 = ConvertUtil.HexStrToStr(strArray[14] + strArray[15] + strArray[16] + strArray[17] + strArray[18] + strArray[19] + strArray[20] + strArray[21] + strArray[22] + strArray[23] + strArray[24]);
                String responseValue2 = ConvertUtil.HexStrToStr(strArray[14] + strArray[15] + strArray[16] + strArray[17] + strArray[18]);
                Message message = new Message();
                message.what = MESSAGE_MAGNETIC;
                message.obj = "收到:磁场强度【" + responseValue2 + "】 ";
                handler.sendMessage(message);
                break;
            }
            //测量门状态
            case "04":
            {
                Message message = new Message();
                message.what = MESSAGE_DOORSTATE;
                if(strArray[13].equals("01"))
                {
                    message.obj = "已关";
                }
                else
                {
                    message.obj = "已开";
                }
                handler.sendMessage(message);
                break;
            }
            case "80":
            {
                //全部门锁状态
                byte[] bytes1 = ConvertUtil.HexStrToByteArray(strArray[13]);
                String bitStr = ConvertUtil.ByteToBit(bytes1[0]);
                String doorState1 = String.valueOf(bitStr.charAt(7));
                String lockState1 = String.valueOf(bitStr.charAt(6));
                String doorState2 = String.valueOf(bitStr.charAt(5));
                String lockState2 = String.valueOf(bitStr.charAt(4));
                //强磁开关状态
                String magneticState = String.valueOf(bitStr.charAt(3));
                //外接电源状态
                String outsideState = String.valueOf(bitStr.charAt(2));
                //内部电池充电状态
                String insideState = String.valueOf(bitStr.charAt(1));
                //电池电量
                int battery = Integer.parseInt(strArray[14] + strArray[15], 16);
                //下端磁强
                int magneticDown = Integer.parseInt(strArray[16] + strArray[17], 16);
                //上端磁强
                int magneticUp = Integer.parseInt(strArray[2] + strArray[3], 16);
                //前端磁强
                int magneticBefore = Integer.parseInt(strArray[4] + strArray[5], 16);
                Message message = new Message();
                message.what = MESSAGE_TESTA;
                message.obj = doorState1 + "," + lockState1 + "," + doorState2 + "," + lockState2 + "," + battery + "," + magneticDown + "," + magneticUp + "," + magneticBefore;
                handler.sendMessage(message);
                break;
            }
            //一号门锁
            case "81":
            {
                Message message = new Message();
                message.what = MESSAGE_OPENDOORS1;
                message.obj = "";
                handler.sendMessage(message);
                break;
            }
            //二号门锁
            case "82":
            {
                Message message = new Message();
                message.what = MESSAGE_OPENDOORS2;
                message.obj = strArray[13];
                handler.sendMessage(message);
                break;
            }
            //全部门锁
            case "83":
            {
                Message message = new Message();
                message.what = MESSAGE_OPENALLDOORS;
                message.obj = strArray[13];
                handler.sendMessage(message);
                break;
            }
            //查询内部控制参数
            case "86":
            {
                Message message = new Message();
                message.what = MESSAGE_SEARCHCONTROLPARAM;
                message.obj = strArray[16];
                handler.sendMessage(message);
                break;
            }
            //修改内部控制参数:
            case "87":
            {
                break;
            }
        }
    }

    private void ShowWarning(int param)
    {
        switch(param)
        {
            case MESSAGE_ERROR_1:
                Toast.makeText(m_context, "该设备的连接已断开", Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_ERROR_2:
                Toast.makeText(m_context, "该设备未连接蓝牙", Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_ERROR_3:
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
                builder.setTitle("警告");
                builder.setMessage("未获取到蓝牙,请重试!");
                builder.setPositiveButton("知道了", (dialog, which) ->
                {
                    BluetoothFragment_List bluetoothFragment_list = BluetoothFragment_List.newInstance("", "");
                    getFragmentManager().beginTransaction().replace(R.id.fragment_bluetooth, bluetoothFragment_list, "bluetoothFragment_list").commitNow();
                });
                builder.show();
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1)
        {
            String hexStr = data.getStringExtra("WriteValue");
            if(m_button1.getText().toString().equalsIgnoreCase("断开"))
            {
                m_debugView.append("已发送参数写入指令 ");
                int offset = m_debugView.getLineCount() * m_debugView.getLineHeight();
                if(offset > m_scrollView.getHeight())
                {
                    m_debugView.scrollTo(0, offset - m_scrollView.getHeight());
                }
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
            }
            else
            {
                ShowWarning(MESSAGE_ERROR_2);
            }
            m_writeValueDialog.dismiss();
        }
        else if(requestCode == 2)
        {
            String hexStr = data.getStringExtra("ParamSetting");
            if(m_button1.getText().toString().equalsIgnoreCase("断开"))
            {
                m_debugView.append("已发送参数设置指令 ");
                int offset = m_debugView.getLineCount() * m_debugView.getLineHeight();
                if(offset > m_scrollView.getHeight())
                {
                    m_debugView.scrollTo(0, offset - m_scrollView.getHeight());
                }
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
            }
            else
            {
                ShowWarning(MESSAGE_ERROR_2);
            }
            m_paramSettingDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        switch(item.getItemId())
        {
            case R.id.menu_1:
            {
                m_paramSettingDialog = new ParamSettingDialog();
                m_paramSettingDialog.setTargetFragment(BluetoothFragment_PowerControl.this, 1);
                m_paramSettingDialog.show(getFragmentManager(), "ParamSetting");
                break;
            }
            case R.id.menu_2:
            {
                m_writeValueDialog = new WriteValueDialog();
                m_writeValueDialog.setTargetFragment(BluetoothFragment_PowerControl.this, 2);
                m_writeValueDialog.show(getFragmentManager(), "WriteValueDialog");
                break;
            }
            case R.id.menu_3:
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //Activity的onCreateOptionsMenu会在之前调用,即先Clear一下,这样就只有Fragment自己设置的了
        menu.clear();
        inflater.inflate(R.menu.menu_setting, menu);
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_return:
            {
                BluetoothFragment_List bluetoothFragment_list = (BluetoothFragment_List) getFragmentManager().findFragmentByTag("bluetoothFragment_list");
                getFragmentManager().beginTransaction().show(bluetoothFragment_list).commitNow();
                getFragmentManager().beginTransaction().remove(BluetoothFragment_PowerControl.this).commitNow();
                break;
            }
            //连接
            case R.id.button1:
            {
                if(m_bluetoothDevice != null)
                {
                    if(m_button1.getText().toString().equalsIgnoreCase("连接"))
                    {
                        m_button1.setText("断开");
                        Log.d(TAG, ">>>开始连接...");
                        m_bluetoothGatt = m_bluetoothDevice.connectGatt(m_context, false, new BluetoothGattCallback()
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
                                    m_bluetoothGatt.close();
                                    Message message = new Message();
                                    message.what = MESSAGE_ERROR_1;
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
                                m_bluetoothGattService = m_bluetoothGatt.getService(SERVICE_UUID);
                                if(m_bluetoothGattService != null)
                                {
                                    //写数据的服务和特征
                                    m_bluetoothGattCharacteristic_write = m_bluetoothGattService.getCharacteristic(WRITE_UUID);
                                    if(m_bluetoothGattCharacteristic_write != null)
                                    {
                                        Log.d(TAG, ">>>已找到写入数据的特征值!");
                                    }
                                    else
                                    {
                                        Log.e(TAG, ">>>该UUID无写入数据的特征值!");
                                    }
                                    //读取数据的服务和特征
                                    m_bluetoothGattCharacteristic_read = m_bluetoothGattService.getCharacteristic(READ_UUID);
                                    if(m_bluetoothGattCharacteristic_read != null)
                                    {
                                        Log.d(TAG, ">>>已找到读取数据的特征值!");
                                        //订阅读取通知
                                        gatt.setCharacteristicNotification(m_bluetoothGattCharacteristic_read, true);
                                        BluetoothGattDescriptor descriptor = m_bluetoothGattCharacteristic_read.getDescriptor(CONFIG_UUID);
                                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        gatt.writeDescriptor(descriptor);
                                        //轮询
                                        Message message = new Message();
                                        message.what = MESSAGE_1;
                                        handler.sendMessage(message);
                                    }
                                    else
                                    {
                                        Log.e(TAG, ">>>该UUID无读取数据的特征值!");
                                    }
                                }
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
                                result = ConvertUtil.HexStrAddCharacter(result, " ");
                                String[] strArray = result.split(" ");
                                String indexStr = strArray[11];
                                switch(indexStr)
                                {
                                    case "00":
                                    {
                                        Message message = new Message();
                                        message.what = MESSAGE_OPENDOOR;
                                        Log.d(TAG, ">>>发送开门指令");
                                        message.obj = "opendoor";
                                        handler.sendMessage(message);
                                        break;
                                    }
                                    case "01":
                                        //sendResult = "发送读卡指令 ";
                                        break;
                                    case "02":
                                        //sendResult = "发送测量电池电压指令 ";
                                        break;
                                    case "03":
                                        //sendResult = "发送测量磁场强度指令 ";
                                        break;
                                    case "04":
                                        //sendResult = "发送测量门状态指令 ";
                                        break;
                                    case "80":
                                        //sendResult = "";
                                        break;
                                    case "81":
                                        //sendResult = "";
                                        break;
                                    case "82":
                                        //sendResult = "";
                                        break;
                                    case "83":
                                        //sendResult = "";
                                        break;
                                }
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
                                String[] strArray = result.split(" ");
                                //一个包(20个字节)
                                if(strArray[0].equals("68") && strArray[strArray.length - 1].equals("16"))
                                {
                                    Resolve(result);
                                    //清空缓存
                                    m_stringBuffer = new StringBuffer();
                                }
                                //分包
                                else
                                {
                                    if(!strArray[strArray.length - 1].equals("16"))
                                    {
                                        m_stringBuffer.append(result + " ");
                                    }
                                    //最后一个包
                                    else
                                    {
                                        m_stringBuffer.append(result);
                                        result = m_stringBuffer.toString();
                                        Resolve(result);
                                        //清空缓存
                                        m_stringBuffer = new StringBuffer();
                                    }
                                }
                            }
                        });
                    }
                    else
                    {
                        m_button1.setText("连接");
                        m_button2.setEnabled(false);
                        m_button3.setEnabled(false);
                        m_button4.setEnabled(false);
                        if(m_refreshTask != null)
                        {
                            m_refreshTask.cancel();
                        }
                        if(m_refreshTimer != null)
                        {
                            m_refreshTimer.cancel();
                        }
                        //先结束定时任务再关闭蓝牙
                        if(m_bluetoothGatt != null)
                        {
                            m_bluetoothGatt.disconnect();
                        }
                        m_textView1.setText("Null");
                        m_textView1.setTextColor(Color.GRAY);
                        m_textView2.setText("Null");
                        m_textView2.setTextColor(Color.GRAY);
                        m_textView3.setText("Null");
                        m_textView3.setTextColor(Color.GRAY);
                        m_textView4.setText("Null");
                        m_textView4.setTextColor(Color.GRAY);
                        m_textView5.setText("Null");
                        m_textView6.setText("Null");
                    }
                }
                else
                {
                    ShowWarning(MESSAGE_ERROR_3);
                }
                break;
            }
            //开一号门锁
            case R.id.button2:
            {
                String hexStr = "680000000000006810000100E116";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
            }
            break;
            //开二号门锁
            case R.id.button3:
            {
                String hexStr = "680000000000006810000182E716";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
            }
            break;
            //开全部门锁
            case R.id.button4:
            {
                String hexStr = "680000000000006810000181E716";
                Log.d(TAG, ">>>发送:" + hexStr);
                byte[] byteArray = ConvertUtil.HexStrToByteArray(hexStr);
                m_bluetoothGattCharacteristic_write.setValue(byteArray);
                m_bluetoothGatt.writeCharacteristic(m_bluetoothGattCharacteristic_write);
            }
            break;
            //清屏
            case R.id.button5:
                m_debugView.setText("");
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
            m_bluetoothDevice = getArguments().getParcelable(ARG_PARAM1);
            Map<String, UUID> map = (Map<String, UUID>) getArguments().getSerializable(ARG_PARAM2);
            SERVICE_UUID = map.get("SERVICE_UUID");
            WRITE_UUID = map.get("WRITE_UUID");
            READ_UUID = map.get("READ_UUID");
            CONFIG_UUID = map.get("CONFIG_UUID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_view = inflater.inflate(R.layout.fragment_bluetooth_powercontrol, container, false);
        m_context = getContext();
        try
        {
            //强制获得焦点
            m_view.requestFocus();
            m_view.setFocusable(true);
            m_view.setFocusableInTouchMode(true);
            m_view.setOnKeyListener(backListener);
            m_toolbar = m_view.findViewById(R.id.toolbar);
            //加上这句,才会调用Fragment的ToolBar,否则调用的是Activity传递过来的
            setHasOptionsMenu(true);
            //去掉标题
            m_toolbar.setTitle("");
            //此处强转,必须是Activity才有这个方法
            ((MainActivity) getActivity()).setSupportActionBar(m_toolbar);
            m_textView1 = m_view.findViewById(R.id.text1);
            m_textView2 = m_view.findViewById(R.id.text2);
            m_textView3 = m_view.findViewById(R.id.text3);
            m_textView4 = m_view.findViewById(R.id.text4);
            m_textView5 = m_view.findViewById(R.id.text5);
            m_textView6 = m_view.findViewById(R.id.text6);
            m_debugView = m_view.findViewById(R.id.debug_view);
            m_btnReturn = m_view.findViewById(R.id.btn_return);
            m_button1 = m_view.findViewById(R.id.button1);
            m_button2 = m_view.findViewById(R.id.button2);
            m_button3 = m_view.findViewById(R.id.button3);
            m_button4 = m_view.findViewById(R.id.button4);
            m_button5 = m_view.findViewById(R.id.button5);
            m_scrollView = m_view.findViewById(R.id.scrollView);
            m_llPowerControl = m_view.findViewById(R.id.fragment_bluetooth_powercontrol);
            m_btnReturn.setOnClickListener(this::onClick);
            m_button1.setOnClickListener(this::onClick);
            m_button2.setOnClickListener(this::onClick);
            m_button3.setOnClickListener(this::onClick);
            m_button4.setOnClickListener(this::onClick);
            m_button5.setOnClickListener(this::onClick);
            m_debugView.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Toast.makeText(m_context, "配对异常", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
        }
        return m_view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            m_listener = (OnFragmentInteractionListener) context;
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
        m_listener = null;
        if(m_bluetoothGatt != null)
            m_bluetoothGatt.close();
        if(m_refreshTimer != null)
            m_refreshTimer.cancel();
        if(m_refreshTask != null)
            m_refreshTask.cancel();
    }
}