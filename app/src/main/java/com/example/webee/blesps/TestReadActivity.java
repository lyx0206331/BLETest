package com.example.webee.blesps;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TestReadActivity extends Activity implements View.OnClickListener {

    private static final String[] KEYS = new String[]{"KEY1", "KEY2", "KEY3", "KEY4", "KEY5", "KEY6", "KEY7", "K2UP", "K2DW"};

    private TextView mDeviceName;
    private TextView mDataRecvText;
    private TextView mRecvBytes;
    private TextView mDataRecvFormat;
    private Button mScanDeviceBtn;

    private long recvBytes;        // 当前接收的字节数
    //	    private boolean isTimerEnable; // 定时器的使能标志
    private StringBuilder mData;   // 要显示的数据
    //	    private Timer timer;
    private BluetoothUtils mBluetoothUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_read);

        initViews();

        mBluetoothUtils = new BluetoothUtils(this, mHandler);
        mBluetoothUtils.initialize();
//	        timer = new Timer();
        mData = new StringBuilder();

        recvBytes = 0;
    }

    private void initViews() {
        mDeviceName = (TextView) findViewById(R.id.device_name_text);
        mDataRecvText = (TextView) findViewById(R.id.data_read_text);
        mRecvBytes = (TextView) findViewById(R.id.byte_received_text);
        mDataRecvFormat = (TextView) findViewById(R.id.data_received_format);
        mScanDeviceBtn = (Button) findViewById(R.id.scan_device_btn);
        Button mCleanBtn = (Button) findViewById(R.id.clean_data_btn);

        mDataRecvFormat.setOnClickListener(this);
        mRecvBytes.setOnClickListener(this);
        mScanDeviceBtn.setOnClickListener(this);
        mCleanBtn.setOnClickListener(this);
        mDataRecvText.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothUtils.checkBluetoothEnabled();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBluetoothUtils.checkDeviceScanning();
        mBluetoothUtils.checkGattConnected();
           /* if (timer != null) {
	            isTimerEnable = false;
	        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.data_received_format:
                if (mDataRecvFormat.getText().equals("Ascii")) {
                    mBluetoothUtils.convertText(mDataRecvFormat,
                            R.string.data_format_hex);
                } else {
                    mBluetoothUtils.convertText(mDataRecvFormat,
                            R.string.data_format_default);
                }
                break;

            case R.id.byte_received_text:
                recvBytes = 0;
                mBluetoothUtils.convertText(mRecvBytes, R.string.zero);
                break;

            case R.id.scan_device_btn:
                if (mScanDeviceBtn.getText().equals("Scan BLE Device")) {
                    mBluetoothUtils.scanBleDevice(true);
                } else if (mScanDeviceBtn.getText().equals("Disconnect")) {
                    mBluetoothUtils.checkGattConnected();
                    mScanDeviceBtn.setText(R.string.scan_ble_device);
                }
                break;

            case R.id.clean_data_btn:
                mData.delete(0, mData.length());
                mDataRecvText.setText(mData.toString());
                break;

            default:
                break;
        }
    }

    /**
     * 展示数据
     */
    private void displayData() {
        int len = mBluetoothUtils.getDataLen();
        recvBytes += len;
        byte[] buf = new byte[len];

        mBluetoothUtils.getData(buf, len);

        if (mDataRecvFormat.getText().equals("Ascii")) {
            String s = mBluetoothUtils.asciiToString(buf);
//            mData.append(getAvailableData(s) + "\n");
            mDataRecvText.append(getAvailableData(s) + "\n");
        } else {
            String s = mBluetoothUtils.bytesToString(buf);
            mData.append(s);
            mDataRecvText.setText(mData.toString());
        }
//        mDataRecvText.setText(mData.toString());
        mRecvBytes.setText(recvBytes + " ");
    }

    /**
     * 获取有效数据
     * @param data
     * @return
     */
    private String getAvailableData(String data) {
        for (String key :
                KEYS) {
            if (!TextUtils.isEmpty(data) && data.equals(key)) {
                return data;
            }
        }
        return "";
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothUtils.ENABLE_BLUETOOTH:
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, 1);
                    break;

                case BluetoothUtils.DEVICE_SCAN_STARTED:
                    mScanDeviceBtn.setText(R.string.scanning);
                    break;

                case BluetoothUtils.DEVICE_SCAN_STOPPED:
                    mScanDeviceBtn.setText(R.string.scan_ble_device);
                    break;

                case BluetoothUtils.DEVICE_SCAN_COMPLETED:
                    mBluetoothUtils.creatDeviceListDialog();
                    mScanDeviceBtn.setText(R.string.scan_ble_device);
                    break;

                case BluetoothUtils.DEVICE_CONNECTED:
                    mDeviceName.setText(mBluetoothUtils.getDeviceName());
                    break;

                case BluetoothUtils.DEVICE_DISCONNECTED:
                    mDeviceName.setText("Device Name");
                    mScanDeviceBtn.setText(R.string.scan_ble_device);
                    break;

                case BluetoothUtils.DATA_READED:
                    displayData();
                    break;

                case BluetoothUtils.CHARACTERISTIC_ACCESSIBLE:
                    mScanDeviceBtn.setText("Disconnect");
//	                    isTimerEnable = true;
//	                    setTimer(isTimerEnable);
                    break;

	                /*case DATA_REFRESH:
	                    mRecvBytes.setText(recvBytes + " ");
	                    mSendBytes.setText(sendBytes + " ");
	                    break;*/

                default:
                    break;
            }
        }
    };
}
