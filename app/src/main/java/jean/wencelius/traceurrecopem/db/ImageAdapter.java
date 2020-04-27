package jean.wencelius.traceurrecopem.db;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import jean.wencelius.traceurrecopem.R;
import jean.wencelius.traceurrecopem.model.ImageUrl;

/**
 * Created by Jean Wencélius on 26/04/2020.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private ArrayList<ImageUrl> imageUrls;
    private Context context;

    public ImageAdapter(Context context, ArrayList<ImageUrl> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_item, viewGroup, false);
        return new ViewHolder(view);
    }

    /**
     * gets the image url from adapter and passes to Glide API to load the image
     *
     * @param viewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {

        String imagePath = imageUrls.get(i).getImageUrl();
        Uri imageUri = null;
        if(!imagePath.equals("android.resource://jean.wencelius.traceurrecopem/drawable/add_image")){
            File file = new File(imagePath);
            imageUri = Uri.fromFile(file);
        }else{
            imageUri = Uri.parse(imagePath);
        }

        Glide.with(context).load(imageUri).into(viewHolder.img);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;

        public ViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.image_item_id);
        }
    }
}