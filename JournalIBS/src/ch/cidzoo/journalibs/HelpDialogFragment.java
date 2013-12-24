package ch.cidzoo.journalibs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HelpDialogFragment extends DialogFragment {

    public HelpDialogFragment() {
        // Empty constructor required for DialogFragment
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container);

        getDialog().setTitle(R.string.action_help);
        return view;
    }

}
