package id2.id2me.com.id2launcher.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import id2.id2me.com.id2launcher.R;

/**
 * Created by bliss76 on 26/05/16.
 */
public class FolderFragment extends Fragment {

    public static final FolderFragment newInstance(long folderId) {
        FolderFragment f = new FolderFragment();
        Bundle bundle = new Bundle();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.user_folder, container, false);

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public boolean getUserVisibleHint() {
        return super.getUserVisibleHint();

    }

}
