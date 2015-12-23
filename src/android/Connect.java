package com.jnuine.cordova.facebook;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Connect extends CordovaPlugin {

    private final String TAG = "com.jnuine.cordova.facebook.Connect";
    private CallbackContext callbackContext;

    CallbackManager callbackManager;
    FacebookCallback<LoginResult> callback;

    @Override
    public void initialize (CordovaInterface cordova, CordovaWebView webView) {
        callbackManager = CallbackManager.Factory.create();
        callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                try {
                    Connect.this.onLoginSuccess(loginResult);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancel() {
                try {
                    Connect.this.onLoginCancel();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(FacebookException e) {
                try {
                    Connect.this.onLoginError(e);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        };
        LoginManager.getInstance().registerCallback(callbackManager, callback);
    }

    private List<String> transformJSONArrayToStringList (JSONArray array) throws JSONException {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < array.length(); ++i) {
            list.add(array.getString(i));
        }
        return list;
    }

    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("isLoggedIn")) {
            this.isLoggedIn();
            return true;
        }
        if (action.equals("logInWithReadPermissions")) {
            List<String> permissionsList = this.transformJSONArrayToStringList(args);
            this.logInWithReadPermissions(permissionsList);
            return true;
        }
        if (action.equals("logInWithPublishPermissions")) {
            List<String> permissionsList = this.transformJSONArrayToStringList(args);
            this.logInWithPublishPermissions(permissionsList);
            return true;
        }
        if (action.equals("getUserInfo")) {
            String fields = args.getString(0);
            this.getUserInfo(fields);
            return true;
        }
        if (action.equals("logout")) {
            this.logout();
            return true;
        }
        return false;
    }

    private void isLoggedIn() {
        JSONObject result = new JSONObject();
        try {
            result.put("isLoggedIn", AccessToken.getCurrentAccessToken() != null);
            this.callbackContext.success(result);
        } catch (JSONException e) {
            e.printStackTrace();
            this.callbackContext.error("Could not send back response");
        }
    }

    private void logInWithReadPermissions (List<String> permissionsList) {
        this.cordova.setActivityResultCallback(this);
        LoginManager
                .getInstance()
                .logInWithReadPermissions(
                    this.cordova.getActivity(),
                    permissionsList
                );
    }

    private void logInWithPublishPermissions (List<String> permissionsList) {
        this.cordova.setActivityResultCallback(this);
        LoginManager
                .getInstance()
                .logInWithPublishPermissions(
                    this.cordova.getActivity(),
                    permissionsList
                );
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void getUserInfo (String fields) {
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        FacebookRequestError error = graphResponse.getError();
                        if (error != null) {
                            JSONObject jsonError = new JSONObject();
                            try {
                                jsonError.put("errorCode", error.getErrorCode());
                                jsonError.put("errorMessage", error.getErrorMessage());
                                jsonError.put("errorRecoveryMessage", error.getErrorRecoveryMessage());
                                jsonError.put("errorUserMessage", error.getErrorUserMessage());
                                Connect.this.callbackContext.error(jsonError);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Connect.this.callbackContext.error("Could not send back error object :(");
                            }
                        }
                        else {
                            Connect.this.callbackContext.success(jsonObject);
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", fields);
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void logout () {
        LoginManager
                .getInstance()
                .logOut();
        this.callbackContext.success();
    }

    private void onLoginSuccess(LoginResult loginResult) throws JSONException {
        JSONObject result = new JSONObject();
        result.put(
            "accessToken",
            loginResult.getAccessToken()
        );
        result.put(
            "recentlyGrantedPermissions",
            loginResult.getRecentlyGrantedPermissions()
        );
        result.put(
            "recentlyDeniedPermissions",
            loginResult.getRecentlyDeniedPermissions()
        );
        this.callbackContext.success(result);
    }

    private void onLoginCancel() throws JSONException {
        JSONObject error = new JSONObject();
        error.put("canceled", true);
        this.callbackContext.error(error);
    }

    private void onLoginError(FacebookException e) throws JSONException {
        JSONObject error = new JSONObject();
        error.put("canceled", false);
        error.put("message", e.getLocalizedMessage());
        this.callbackContext.error(error);
    }

}
