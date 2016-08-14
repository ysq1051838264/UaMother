package com.uamother.bluetooth.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.uamother.bluetooth.utils.UIUtils;

/**
 * Created by hdr on 15/9/10.
 */
public class MessageDialog extends BaseCustomDialog {


    private TextView messageTv;

    public MessageDialog(Context context) {
        super(context);
    }

    @Override
    protected void initContentView(FrameLayout parent) {
        messageTv = new TextView(getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        messageTv.setLayoutParams(lp);
        messageTv.setGravity(Gravity.CENTER);
        messageTv.setMinimumWidth(parent.getMinimumWidth());
        messageTv.setMinHeight(parent.getMinimumHeight());
        messageTv.setLineSpacing(UIUtils.dpToPx(3), 1);
        messageTv.setPadding(UIUtils.dpToPx(10), 0, UIUtils.dpToPx(10), 0);
        int padding = UIUtils.dpToPx(10);
        messageTv.setPadding(padding, padding, padding, padding);
        parent.addView(messageTv);
    }

    public void setMessage(String message) {
        this.messageTv.setText(message);
    }

    public static class Builder extends BaseBuilder<MessageDialog> {
        private String message;
        private int textSize = 16;
        private int gravity = Gravity.CENTER;

        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public MessageDialog build() {
            this.initDefaultValue();
            MessageDialog messageDialog = new MessageDialog(context);
            super.initBaseProperties(messageDialog);
            messageDialog.setMessage(message);
            messageDialog.messageTv.setGravity(gravity);
            messageDialog.messageTv.setTextSize(textSize);

            return messageDialog;
        }

    }
}
