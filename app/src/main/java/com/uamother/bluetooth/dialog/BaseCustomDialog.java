package com.uamother.bluetooth.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.uamother.bluetooth.R;
import com.uamother.bluetooth.utils.UIUtils;

public abstract class BaseCustomDialog extends BaseDialog {

    public BaseCustomDialog(Context context) {
        super(context);
    }

    @Override
    protected int getResId() {
        return R.layout.base_custom_dialog;
    }

    @Override
    public Button createBtn() {
        if (dialogBtnBar.getChildCount() > 0) {
            dialogBtnBar.addView(createDividerView(getContext()));
        }

        Button button = new Button(getContext());
        button.setBackground(null);
        button.setTextSize(14);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.weight = 1;
        button.setLayoutParams(lp);
        return button;
    }

    public View createDividerView(Context context) {
        View v = new View(context);
        v.setBackgroundColor(context.getResources().getColor(R.color.color_gray_e0e0e0));
        v.setLayoutParams(new ViewGroup.LayoutParams(UIUtils.dpToPx(1), ViewGroup.LayoutParams.MATCH_PARENT));
        return v;
    }

}
