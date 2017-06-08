package com.example.webee.blesps;


import java.nio.ByteBuffer;

import org.apache.http.util.ByteArrayBuffer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener {

    private TextView mDeviceName;
    private TextView mDataRecvText;
    private TextView mRecvBytes;
    private TextView mDataRecvFormat;
    private EditText mEditBox;
    private TextView mSendBytes;
    private TextView mDataSendFormat;
    private Button mScanDeviceBtn;

    private long recvBytes;        // 当前接收的字节数
    private long sendBytes;        // 当前发送的字节数
    //	    private boolean isTimerEnable; // 定时器的使能标志
    private StringBuilder mData;   // 要显示的数据
    //	    private Timer timer;
    private BluetoothUtils mBluetoothUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews(); // 初始化views

        mBluetoothUtils = new BluetoothUtils(this, mHandler);
        mBluetoothUtils.initialize();
//	        timer = new Timer();
        mData = new StringBuilder();

        recvBytes = 0;
        sendBytes = 0;

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

            case R.id.data_sended_format:
                if (mDataSendFormat.getText().equals("Ascii")) {
                    mBluetoothUtils.convertText(mDataSendFormat,
                            R.string.data_format_hex);
                } else {
                    mBluetoothUtils.convertText(mDataSendFormat,
                            R.string.data_format_default);
                }
                break;

            case R.id.byte_received_text:
                recvBytes = 0;
                mBluetoothUtils.convertText(mRecvBytes, R.string.zero);
                break;

            case R.id.byte_send_text:
                sendBytes = 0;
                mBluetoothUtils.convertText(mSendBytes, R.string.zero);
                break;

            case R.id.scan_device_btn:
                if (mScanDeviceBtn.getText().equals("Scan BLE Device")) {
                    mBluetoothUtils.scanBleDevice(true);
                } else if (mScanDeviceBtn.getText().equals("Disconnect")) {
                    mBluetoothUtils.checkGattConnected();
                    mScanDeviceBtn.setText(R.string.scan_ble_device);
                }
                break;

            case R.id.send_data_btn:
                onSendBtnClicked();
                break;

            case R.id.clean_data_btn:
                mData.delete(0, mData.length());
                mDataRecvText.setText(mData.toString());
                break;

            case R.id.clean_text_btn:
                mEditBox.setText("");
                break;

            default:
                break;
        }
    }

    /**
     * 对各个view的实例化，设置点击事件监听器
     */
    private void initViews() {
        mDeviceName = (TextView) findViewById(R.id.device_name_text);
        mDataRecvText = (TextView) findViewById(R.id.data_read_text);
        mRecvBytes = (TextView) findViewById(R.id.byte_received_text);
        mDataRecvFormat = (TextView) findViewById(R.id.data_received_format);
        mEditBox = (EditText) findViewById(R.id.data_edit_box);
        mSendBytes = (TextView) findViewById(R.id.byte_send_text);
        mDataSendFormat = (TextView) findViewById(R.id.data_sended_format);
        mScanDeviceBtn = (Button) findViewById(R.id.scan_device_btn);
        Button mSendBtn = (Button) findViewById(R.id.send_data_btn);
        Button mCleanBtn = (Button) findViewById(R.id.clean_data_btn);
        Button mCleanTextBtn = (Button) findViewById(R.id.clean_text_btn);

        mDataRecvFormat.setOnClickListener(this);
        mDataSendFormat.setOnClickListener(this);
        mRecvBytes.setOnClickListener(this);
        mSendBytes.setOnClickListener(this);
        mScanDeviceBtn.setOnClickListener(this);
        mCleanBtn.setOnClickListener(this);
        mSendBtn.setOnClickListener(this);
        mCleanTextBtn.setOnClickListener(this);
        mDataRecvText.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    /**
     * 发送按钮点击后调用，写入数据
     */
    private void onSendBtnClicked() {
        if (mDataSendFormat.getText().equals("Ascii")) {
            byte[] buf = mEditBox.getText().toString().trim().getBytes();
            sendBytes += buf.length;
            mBluetoothUtils.writeData(buf);
        } else {
            byte[] buf = mBluetoothUtils.stringToBytes(getHexString());
            sendBytes += buf.length;
            mBluetoothUtils.writeData(buf);
        }
    }

    /**
     * 得到16进制字符串，过滤掉不正确的字符
     *
     * @return 字符串
     */
    private String getHexString() {
        String s = mEditBox.getText().toString();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (('0' <= c && c <= '9') || ('a' <= c && c <= 'f') ||
                    ('A' <= c && c <= 'F')) {
                sb.append(c);
            }
        }
        if ((sb.length() % 2) != 0) {
            sb.deleteCharAt(sb.length());
        }
        return sb.toString();
    }

    /**
     * 在getHexString基础上每两个字符后加空格
     *
     * @return 格式化后的字符串
     */
    private String getFormattedString() {
        String s = getHexString();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length() - 1; i += 2) {
            sb.append(s.substring(i, i + 2));
            sb.append(' ');
        }
        return sb.toString();
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
            mData.append(s);
        } else {
            String s = mBluetoothUtils.bytesToString(buf);
            mData.append(s);
        }
        mDataRecvText.setText(mData.toString());
        mRecvBytes.setText(recvBytes + " ");
    }

    /**
     * 设置定时器，递归调用不断定时来实时更新接受和发送的字节数
     *
     * @param enable 使能标志
     */
	    /*private void setTimer(boolean enable) {
	        if (enable) {
	            timer.schedule(new TimerTask() {
	                @Override
	                public void run() {
	                    Message message = new Message();
	                    message.what = DATA_REFRESH;
	                    mHandler.sendMessage(message);

	                    setTimer(isTimerEnable);
	                }
	            }, 2000);
	        }
	    }*/

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

                case BluetoothUtils.DATA_SENDED:
                    if (mDataSendFormat.getText().equals("Hex")) {
                        mEditBox.setText(getFormattedString());
                    }
                    mSendBytes.setText(sendBytes + " ");
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
