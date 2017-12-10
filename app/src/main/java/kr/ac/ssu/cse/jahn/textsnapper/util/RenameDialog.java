package kr.ac.ssu.cse.jahn.textsnapper.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import kr.ac.ssu.cse.jahn.textsnapper.R;

/**
 * Created by ArchSlave on 2017-12-08.
 */

public class RenameDialog extends Dialog {
    TextView okButton = null;
    TextView cancelButton = null;
    EditText editText = null;

    public RenameDialog(Context context) {
        super(context);

        setContentView(R.layout.dialog_rename);
        okButton = (TextView)findViewById(R.id.okButton);
        cancelButton = (TextView)findViewById(R.id.cancelButton);
        editText = (EditText)findViewById(R.id.mText);
    }

    public void setOkClickListener(View.OnClickListener _okListener) {
        okButton.setOnClickListener(_okListener);
    }

    public void setCancelClickListener(View.OnClickListener _cancelListener) {
        cancelButton.setOnClickListener(_cancelListener);
    }

    public String getEditTextContent() {
        return editText.getText().toString();
    }
}
