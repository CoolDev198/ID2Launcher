package id2.id2me.com.id2launcher;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import id2.id2me.com.id2launcher.models.ItemInfoModel;

/**
 * Created by sunita on 10/7/16.
 */

public class Utility {
    public static ArrayList<ImageView> folderImgs;

    public static void setFolderImagesList(LinearLayout folder_view) {

        folderImgs = new ArrayList<>();

        for (int i = 0; i < folder_view.getChildCount(); i++) {
            LinearLayout horzontalLayout = (LinearLayout) folder_view.getChildAt(i);

            for (int j = 0; j < horzontalLayout.getChildCount(); j++) {
                folderImgs.add((ImageView) horzontalLayout.getChildAt(j));
            }
        }
    }

    public static void setFolderView(Context context, View view, ArrayList<ItemInfoModel> itemInfoModels) {

        setFolderImagesList((LinearLayout) view);

        for (int i = 0; i < folderImgs.size(); i++) {
            if (i < itemInfoModels.size()) {
                folderImgs.get(i).setImageBitmap(ItemInfoModel.getIconFromCursor(itemInfoModels.get(i).getIcon(), context));
                folderImgs.get(i).setVisibility(View.VISIBLE);
            } else {
                folderImgs.get(i).setVisibility(View.INVISIBLE);
            }
        }


    }
}
