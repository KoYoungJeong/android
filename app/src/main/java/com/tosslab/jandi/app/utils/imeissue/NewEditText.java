package com.tosslab.jandi.app.utils.imeissue;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

/**
 * Created by tee on 15. 8. 4..
 */
public class NewEditText extends EditText {

    public NewEditText(Context context) {
        super(context);

    }

    public NewEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NewEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        //Passing FALSE as the SECOND ARGUMENT (fullEditor) to the constructor
        // will result in the key events continuing to be passed in to this
        // view.  Use our special BaseInputConnection-derived view
        InputConnectionAccomodatingLatinIMETypeNullIssues baseInputConnection =
                new InputConnectionAccomodatingLatinIMETypeNullIssues(this, false);

        //In some cases an IME may be able to display an arbitrary label for a
        // command the user can perform, which you can specify here.  A null value
        // here asks for the default for this key, which is usually something
        // like Done.
        outAttrs.actionLabel = null;

        //Special content type for when no explicit type has been specified.
        // This should be interpreted (by the IME that invoked
        // onCreateInputConnection())to mean that the target InputConnection
        // is not rich, it can not process and show things like candidate text
        // nor retrieve the current text, so the input method will need to run
        // in a limited "generate key events" mode.  This disables the more
        // sophisticated kinds of editing that use a text buffer.
        outAttrs.inputType = InputType.TYPE_NULL;

        //This creates a Done key on the IME keyboard if you need one
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;

        return baseInputConnection;
    }


}
