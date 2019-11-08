package com.tianzhuan.hookproject01;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MainActivity extends AppCompatActivity {
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.btn);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "" + ((Button) v).getText(), Toast.LENGTH_SHORT).show();
            }
        });

        //在不修改以上代码的情况下，利用Hook修改((Button)v).getText()的内容
        try {
            hook(mButton);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Hook失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void hook(View view) throws Exception{

        //拿到getListenerInfo这个方法
        Class mViewClass = Class.forName("android.view.View");
        Method getListenerInfoMethod = mViewClass.getDeclaredMethod("getListenerInfo");
        getListenerInfoMethod.setAccessible(true);//授权
        //执行getListenerInfo这个方法
        Object mListenerInfo = getListenerInfoMethod.invoke(view);

        Class mListenerInfoClass = Class.forName("android.view.View$ListenerInfo");
        Field mOnClickListenerField = mListenerInfoClass.getField("mOnClickListener");
        final Object mOnClickListenerObj = mOnClickListenerField.get(mListenerInfo);

        //mOnclickListener本质是OnClickListener
        Object mOnclickListenerProxy = Proxy.newProxyInstance(MainActivity.class.getClassLoader()//1.加载器
                , new Class[]{View.OnClickListener.class}//2.要加载的接口，监听什么接口，就返回什么接口
                , new InvocationHandler() {//3.监听接口方法里面的回调
                    /**
                     *  void onClick(View v);
                     *
                     *  onClick--->method
                     *  参数 View v---->Object[] args
                     *
                     * @param proxy
                     * @param method
                     * @param args
                     * @return
                     * @throws Throwable
                     */
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        //加入自己的逻辑
                         Button button=new Button(MainActivity.this);
                         button.setText("大家晚上好。。。");

                        //让系统程序片段--正常继续的执行下去

                        return method.invoke(mOnClickListenerObj,button);
                    }
                });

        //替换， public OnClickListener mOnClickListener;替换成我们自己的


        //狸猫换太子，将系统的mOnClickListener替换成我们自己写的动态代理
        mOnClickListenerField.set(mListenerInfo,mOnclickListenerProxy);
    }
}
