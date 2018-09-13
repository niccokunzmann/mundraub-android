package eu.quelltext.mundraub.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.common.Dialog;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends MundraubBaseActivity {

    // UI references.
    private AutoCompleteTextView usernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private Button registerButton;
    private LinearLayout emailLayout;
    private AutoCompleteTextView emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        usernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSignupView()) {
                    activateSignupView();
                } else {
                    attemptSignup();
                }
            }
        });

        emailLayout = (LinearLayout) findViewById(R.id.email_layout);
        emailText = (AutoCompleteTextView) findViewById(R.id.email);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        loadPassword();
        getPermissions().INTERNET.askIfNotGranted();
    }




    private String getPassword() {
        return mPasswordView.getText().toString();
    }

    private String getUsername() {
        return usernameView.getText().toString();
    }

    private String getEmail() {
        return emailText.getText().toString();
    }

    private boolean requestingValidUsername() {
        if (requestFillInIfEmpty(usernameView)) {
            return true;
        }
        if (!isUsernameValid(getUsername())) {
            setErrorAndFocus(usernameView, R.string.error_invalid_username);
            return true;
        }
        return false;
    }

    private boolean requestingValidPassword() {
        if (requestFillInIfEmpty(mPasswordView)) {
            return true;
        }
        if (!isPasswordValid(getPassword())) {
            setErrorAndFocus(mPasswordView, R.string.error_invalid_password);
            return true;
        }
        return false;
    }

    private boolean requestingValidEmail() {
        if (requestFillInIfEmpty(emailText)) {
            return true;
        }
        if (!getEmail().contains("@") || !getEmail().contains(".")) {
            setErrorAndFocus(emailText, R.string.error_invalid_email);
            return true;
        }
        return false;
    }

    private boolean requestFillInIfEmpty(EditText textView) {
        String text = textView.getText().toString();
        if (text.isEmpty()) {
            setErrorAndFocus(textView, R.string.error_field_required);
            return true;
        }
        return false;
    }

    private void loadPassword() {
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        usernameView.setText(settings.getString("Username", "").toString());
        mPasswordView.setText(settings.getString("Password", "").toString());
        emailText.setText(settings.getString("Email", "").toString());
    }

    private void savePassword() {
        // from https://stackoverflow.com/a/10209902
        SharedPreferences settings = getSharedPreferences("UserInfo", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Username", getUsername());
        editor.putString("Password", getPassword());
        editor.putString("Email", getEmail());
        editor.commit();
    }

    private void resetErrors() {
        usernameView.setError(null);
        mPasswordView.setError(null);
        emailText.setError(null);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        activateLoginView();
        resetErrors();
        if (requestingValidPassword() || requestingValidUsername()) {
            return;
        }
        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);
        API.instance().login(getUsername(), getPassword(), new API.Callback() {
            @Override
            public void onSuccess() {
                showProgress(false);
                loginSuccessful();
                finish();
            }

            @Override
            public void onFailure(int errorResourceId) {
                showProgress(false);
                setErrorAndFocus(mPasswordView, errorResourceId);
            }
        });
    }

    private void attemptSignup() {
        activateSignupView();
        resetErrors();
        if (requestingValidEmail() || requestingValidPassword() || requestingValidUsername()) {
            return;
        }
        savePassword();
        showProgress(true);
        API.instance().signup(getEmail(), getUsername(), getPassword(), new API.Callback() {
            @Override
            public void onSuccess() {
                showProgress(false);
                signupSuccessful();
            }

            @Override
            public void onFailure(int errorResourceId) {
                showProgress(false);
                setErrorAndFocus(usernameView, errorResourceId);
            }
        });
    }

    private boolean isSignupView() {
        return emailLayout.getVisibility() != View.GONE;
    }

    private void activateSignupView() {
        emailLayout.setVisibility(View.VISIBLE);
    }

    private void activateLoginView() {
        emailLayout.setVisibility(View.GONE);
    }

    private void signupSuccessful() {
        savePassword();
        new Dialog(this).alertSuccess(R.string.signup_successful_see_email);
        activateLoginView();
    }

    private void setErrorAndFocus(EditText editText, int errorResourceId) {
        // from https://stackoverflow.com/a/7350315/1320237
        int ecolor = R.color.colorAccent;
        String estring = getResources().getString(errorResourceId);
        ForegroundColorSpan fgcspan = new ForegroundColorSpan(getResources().getColor(ecolor));
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(estring);
        ssbuilder.setSpan(fgcspan, 0, estring.length(), 0);
        editText.setError(ssbuilder);
        editText.requestFocus();
    }

    private void loginSuccessful() {
        savePassword();
        finish();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    private boolean isUsernameValid(String username) {
        return username.length() > 0;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

