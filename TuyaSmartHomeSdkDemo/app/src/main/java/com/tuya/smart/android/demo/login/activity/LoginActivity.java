package com.tuya.smart.android.demo.login.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tuya.smart.android.common.utils.ValidatorUtil;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.base.activity.BaseActivity;
import com.tuya.smart.android.demo.base.activity.PersonalInfoActivity;
import com.tuya.smart.android.demo.base.utils.LoginHelper;
import com.tuya.smart.android.demo.login.presenter.LoginPresenter;
import com.tuya.smart.android.demo.base.utils.ProgressUtil;
import com.tuya.smart.android.demo.base.utils.ToastUtil;
import com.tuya.smart.android.demo.login.ILoginView;
import com.tuya.smart.android.mvp.bean.Result;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.SocketTimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by letian on 16/7/15.
 */
public class LoginActivity extends BaseActivity implements ILoginView, TextWatcher {
    private static String metodo = "validarAcceso";
    private static String namespace = "http://WebServices/";
    private static String accionSoap = "http://WebServices/validarAcceso";
    private static String url = "http://eneplay.dyndns.org:8080/dashboard/IntegracionWebService?wsdl/";
    private static String usernameElegido = "";
    //private static boolean esMoroso = false;
    //private static boolean tareaFinalizada = false;
    private static consumirAsync hiloWS = null;
    @BindView(R.id.login_submit)
    public Button mLoginSubmit;

    //@BindView(R.id.bnt_qrcode_login)
    //public Button mQRLogin;
    @BindView(R.id.country_name)
    public TextView mCountryName;

    @BindView(R.id.password)
    public EditText mPassword;

    @BindView(R.id.password_switch)
    public ImageButton mPasswordSwitch;
    @BindView(R.id.user_name)
    public TextView mUserName;
    private Unbinder mBind;

    private LoginPresenter mLoginPresenter;

    private boolean passwordOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mBind = ButterKnife.bind(this);
        initToolbar();
        initView();
        initTitle();
        initMenu();
        disableLogin();
        mLoginPresenter = new LoginPresenter(this, this);
    }


    // 注册按钮
    private void initMenu() {
        setMenu(R.menu.toolbar_login_menu, new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_login_reg_onclick) {
                    AccountInputActivity.gotoAccountInputActivity(LoginActivity.this, AccountInputActivity.MODE_REGISTER, 0);
                }
                return false;
            }
        });
    }

    private void initTitle() {
        setTitle(getString(R.string.login));
    }

    private void initView() {
        passwordOn = false;
        mUserName.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        mPasswordSwitch.setImageResource(R.drawable.ty_password_off);
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }


    // 输入账号监听
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String userName = mUserName.getText().toString();
        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(password)) {
            disableLogin();
        } else {
            if (ValidatorUtil.isEmail(userName)) {
                // 邮箱
                enableLogin();
            } else {
                // 手机号码
                try {
                    Long.valueOf(userName);
                    enableLogin();
                } catch (Exception e) {
                    disableLogin();
                }
            }
        }
    }

    //@OnClick(R.id.option_validate_code)
    public void loginWithPhoneCode() {
        startActivity(new Intent(LoginActivity.this, LoginWithPhoneActivity.class));
    }

    @OnClick(R.id.option_forget_password)
    public void retrievePassword() {
        Intent intent = new Intent(LoginActivity.this, AccountInputActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.country_name)
    public void onClickSelectCountry() {
        mLoginPresenter.selectCountry();
    }

    @OnClick(R.id.password_switch)
    public void onClickPasswordSwitch() {
        passwordOn = !passwordOn;

        // 切换显示图标
        if (passwordOn) {
            mPasswordSwitch.setImageResource(R.drawable.ty_password_on);
            mPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            mPasswordSwitch.setImageResource(R.drawable.ty_password_off);
            mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        // 更新光标位置
        if (mPassword.getText().length() > 0) {
            mPassword.setSelection(mPassword.getText().length());
        }
    }

    @OnClick(R.id.login_submit)
    public void onClickLogin() {
        // 登录
        /*nbent*/
        LoginActivity.usernameElegido = mUserName.getText().toString();
        //consumir();
        LoginActivity.hiloWS = new consumirAsync();
        LoginActivity.hiloWS.execute();
        /*nbent*/

        if (mLoginSubmit.isEnabled()) {

            String userName = mUserName.getText().toString();
            if (!ValidatorUtil.isEmail(userName) && mCountryName.getText().toString().contains("+86") && mUserName.getText().length() != 11) {
                ToastUtil.shortToast(LoginActivity.this, getString(R.string.ty_phone_num_error));
                return;
            }
            hideIMM();
            disableLogin();
            //ProgressUtil.showLoading(LoginActivity.this, R.string.logining);
            //mLoginPresenter.login(userName, mPassword.getText().toString());
        }
        /*boolean continuar = true;
        while(continuar && !LoginActivity.tareaFinalizada){
        if(esMoroso && !LoginActivity.tareaFinalizada){
            continuar = false;
            Toast.makeText(getApplicationContext(), "Error de acceso, comunicate con tu operador.", Toast.LENGTH_LONG).show();
            this.onDestroy();
            finish();

        }
        }
        esMoroso = false;
        LoginActivity.tareaFinalizada = false;

         */
    }
    private class consumirAsync extends AsyncTask<String,Integer,Boolean> {

        protected Boolean doInBackground(String... params) {
            boolean resul = true;

            String email = LoginActivity.usernameElegido;
            SoapObject request = new SoapObject(namespace, metodo);
            request.addProperty("email", email);
            SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            sobre.setOutputSoapObject(request);
            HttpTransportSE tr = new HttpTransportSE(url);

            try {

                tr.call(accionSoap, sobre);
                SoapPrimitive retorno = (SoapPrimitive) sobre.getResponse();
                //LoginActivity.tareaFinalizada=true;
                    if (retorno.toString().equals("false")) {
                        //LoginActivity.esMoroso = true;
                        resul = false;
                        return resul;
                    }

            } catch (SoapFault soapFault) {
                soapFault.printStackTrace();
                resul = false;
            } catch (XmlPullParserException xmlPullParserException) {
                xmlPullParserException.printStackTrace();
                resul = false;
            } catch (SocketTimeoutException socketTimeoutException) {
                socketTimeoutException.printStackTrace();
                resul = false;
            } catch (IOException ioException) {
                ioException.printStackTrace();
                resul = false;
            }

            //invocarWS();
            //this.cancel(true);
            //return null;
            return resul;
        }

        protected void onPostExecute(Boolean result){
            if(!result){
                try {
                    this.finalize();
                    finish();
                    LoginActivity.this.finalize();
                    onDestroy();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
            ProgressUtil.showLoading(LoginActivity.this, R.string.logining);
            mLoginPresenter.login(usernameElegido, mPassword.getText().toString());
        }

    }



    //public void consumir(){
    //    LoginActivity.hiloWS = new consumirAsync();
    //    LoginActivity.hiloWS.execute();
    //}

    //public void invocarWS(){
        //try {
            //ProgressUtil.showLoading(LoginActivity.this, R.string.logining);
            //String email = LoginActivity.usernameElegido;
            //SoapObject request = new SoapObject(namespace, metodo);
            //request.addProperty("email", email);
            //SoapSerializationEnvelope sobre = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            //sobre.setOutputSoapObject(request);
            //HttpTransportSE tr = new HttpTransportSE(url);
            //tr.call(accionSoap, sobre);
            //SoapPrimitive retorno = (SoapPrimitive) sobre.getResponse();
                //if (retorno.toString().equals("false")) {
                //        LoginActivity.esMoroso = true;
                //}

            //LoginActivity.tareaFinalizada=true;

        /*}catch(Exception ex){

            ex.printStackTrace();
        }*/
        //catch (SoapFault soapFault) {
            //soapFault.printStackTrace();
        //} catch (XmlPullParserException xmlPullParserException) {
            //xmlPullParserException.printStackTrace();
        //} catch (SocketTimeoutException socketTimeoutException) {
            //socketTimeoutException.printStackTrace();
        //} catch (IOException ioException) {
        //ioException.printStackTrace();
        //}


        //}

    //}
    //@OnClick(R.id.bnt_qrcode_login)
    public void qrCodeLogin(){
        Intent intent = new Intent(this, QRCodeLoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void setCountry(String name, String code) {
        mCountryName.setText(String.format("%s +%s", name, code));
    }

    @Override
    public void modelResult(int what, Result result) {
        switch (what) {
            case LoginPresenter.MSG_LOGIN_SUCCESS:
                ProgressUtil.hideLoading();
                break;
            case LoginPresenter.MSG_LOGIN_FAILURE:
                ProgressUtil.hideLoading();
                ToastUtil.shortToast(this, result.error);
                enableLogin();
                break;
            default:
                break;
        }
    }

    // 登录按钮状态
    public void enableLogin() {
        if (!mLoginSubmit.isEnabled()) mLoginSubmit.setEnabled(true);
    }

    public void disableLogin() {
        if (mLoginSubmit.isEnabled()) mLoginSubmit.setEnabled(false);
    }

    @Override
    public boolean needLogin() {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLoginPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());
        return res;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBind.unbind();
        mLoginPresenter.onDestroy();
    }
}
