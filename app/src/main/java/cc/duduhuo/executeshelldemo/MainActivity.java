package cc.duduhuo.executeshelldemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private Spinner mSpCommands;
    private TextView mTvOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSpCommands = findViewById(R.id.sp_commands);
        Button btnExecute1 = findViewById(R.id.btn_execute1);
        Button btnExecute2 = findViewById(R.id.btn_execute2);
        mTvOutput = findViewById(R.id.tv_output);
        // TextView 可滚动
        mTvOutput.setMovementMethod(ScrollingMovementMethod.getInstance());

        btnExecute1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = (String) mSpCommands.getSelectedItem();
                Log.d(TAG, command);
                String[] cmd = command.split(" ");
                String outpot = run1(cmd, ".");
                Log.d(TAG, outpot);
                mTvOutput.setText(outpot);
            }
        });
        btnExecute2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = (String) mSpCommands.getSelectedItem();
                Log.d(TAG, command);
                String outpot = run2(command);
                Log.d(TAG, outpot);
                mTvOutput.setText(outpot);
            }
        });
    }


    /**
     * 方式1：
     * 执行一个shell命令，并返回执行结果
     *
     * @param cmd           命令&参数组成的数组（例如：{"/system/bin/cat", "/system/build.prop"}）
     * @param workDirectory 命令执行路径（例如："/system/bin/"）
     * @return 执行结果
     */
    private static synchronized String run1(String[] cmd, String workDirectory) {
        StringBuffer result = new StringBuffer();
        ProcessBuilder builder;
        Process process;
        try {
            builder = new ProcessBuilder(cmd);
            InputStream in = null;
            if (workDirectory != null) {
                builder.directory(new File(workDirectory));
                // 合并标准错误和标准输出
                builder.redirectErrorStream(true);
                // 启动一个新进程
                process = builder.start();

                // 读取进程标准输出流
                in = process.getInputStream();
                byte[] b = new byte[1024];
                while (in.read(b) != -1) {
                    result = result.append(new String(b));
                }
                process.destroy();
            }
            // 关闭输入流
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

        return result.toString();
    }

    /**
     * 方式2：
     * 执行一个shell命令，并返回执行结果
     *
     * @param cmd 命令，如 cat /system/build.prop
     * @return 执行结果
     */
    public static synchronized String run2(String cmd) {
        // shell进程
        Process process;
        // 对应进程的3个流
        BufferedReader successResult;
        BufferedReader errorResult;
        DataOutputStream os;

        // 保存的执行结果
        StringBuilder result = new StringBuilder();

        try {
            // 普通shell: sh；root shell：su
            process = Runtime.getRuntime().exec("su");
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }

        successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
        errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        os = new DataOutputStream(process.getOutputStream());

        try {
            // 写入要执行的命令
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.flush();

            os.writeBytes("exit\n");
            os.flush();

            os.close();

            String line;
            // 读取标准输出
            try {
                while ((line = successResult.readLine()) != null) {
                    result.append(line).append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    successResult.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 读取错误输出
            try {
                while ((line = errorResult.readLine()) != null) {
                    result.append(line).append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    errorResult.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        process.destroy();
        return result.toString();
    }
}
