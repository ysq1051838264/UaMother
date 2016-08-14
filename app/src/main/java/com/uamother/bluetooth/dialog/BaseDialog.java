package com.uamother.bluetooth.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.uamother.bluetooth.R;

/**
 * Created by hdr on 15/9/17.
 */
public abstract class BaseDialog extends Dialog {
    protected Button[] buttons;

    public LinearLayout dialogBtnBar;

    protected FrameLayout contentView;

    protected String[] buttonTexts;
    protected DialogButtonClickListener dialogButtonClickListener;
    protected boolean autoDismiss = true;

    protected View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (autoDismiss) {
                dismiss();
            }
            if (dialogButtonClickListener != null) {
                int index = (Integer) v.getTag();
                dialogButtonClickListener.onClick(index);
            }
        }
    };

    protected abstract int getResId();

    public BaseDialog(Context context) {
        this(context, R.style.myDialogTheme);
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
        this.setContentView(getResId());
        this.setCancelable(false);
        contentView = (FrameLayout) findViewById(R.id.contentView);
        initContentView(contentView);

        dialogBtnBar = (LinearLayout) findViewById(R.id.dialog_btn_bar);
    }

    public BaseDialog setButtonTexts(String... buttonTexts) {
        this.buttonTexts = buttonTexts;
        initViews();
        return this;
    }

    private void initViews() {
        dialogBtnBar.removeAllViews();
        int count = buttonTexts.length;
        buttons = new Button[count];
        for (int i = 0; i < count; i++) {
            final Button btn = createBtn();

            btn.setText(buttonTexts[i]);

            btn.setTag(i);
            btn.setOnClickListener(onClickListener);
            dialogBtnBar.addView(btn);
            buttons[i] = btn;
        }
    }

    void openInput(final EditText editText) {
        if (editText == null) {
            return;
        }
        editText.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                        InputMethodManager.HIDE_IMPLICIT_ONLY);
                editText.setSelection(editText.length());

            }
        });
    }

    void hideInput() {
        // 不显示键盘
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0); // 强制隐藏键盘
        }
    }

    public abstract Button createBtn();

    protected abstract void initContentView(FrameLayout parent);

    public interface DialogButtonClickListener {
        void onClick(int index);
    }

    public static abstract class BaseBuilder<D extends BaseDialog> {
        protected Context context;
        protected DialogButtonClickListener dialogButtonClickListener;
        protected String[] buttonTexts;
        protected boolean autoDismiss = true;

        protected void initDefaultValue() {
            if (dialogButtonClickListener == null) {
                dialogButtonClickListener = new DefaultDialogButtonClickListener();
                if (buttonTexts == null) {
                    buttonTexts = new String[]{"取消"};
                }
            }
            if (buttonTexts == null) {
                buttonTexts = new String[]{"取消", "确定"};
            }
        }

        protected void initBaseProperties(D d) {
            d.setButtonTexts(buttonTexts);
            d.dialogButtonClickListener = dialogButtonClickListener;
            d.autoDismiss = autoDismiss;
        }

        public BaseBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public BaseBuilder setAutoDismiss(boolean autoDismiss) {
            this.autoDismiss = autoDismiss;
            return this;
        }

        public BaseBuilder setDialogButtonClickListener(DialogButtonClickListener dialogButtonClickListener) {
            this.dialogButtonClickListener = dialogButtonClickListener;
            return this;
        }

        public BaseBuilder setButtonTexts(String... buttonTexts) {
            this.buttonTexts = buttonTexts;
            return this;
        }

        public abstract D build();

    }

    public static class DefaultDialogButtonClickListener implements DialogButtonClickListener {

        @Override
        public final void onClick(int index) {
            if (index == 0) {
                onCancelClick();
            } else {
                onConfirmClick();
            }
        }

        public void onConfirmClick() {
        }

        public void onCancelClick() {
        }
    }
}
