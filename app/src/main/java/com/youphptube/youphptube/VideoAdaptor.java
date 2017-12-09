package com.youphptube.youphptube;

        import android.app.Activity;
        import android.content.Context;
        import android.content.res.Resources;
        import android.graphics.Bitmap;
        import android.text.Layout;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.squareup.picasso.Picasso;

        import java.util.ArrayList;
        import java.util.HashMap;

class VideoAdaptor extends BaseAdapter {
    private int ShowView;
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    //public ImageLoader imageLoader;

    VideoAdaptor(Activity a, ArrayList<HashMap<String, String>> d, int ViewID) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ShowView = ViewID;
        //imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(ShowView, null);
        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView time = (TextView)vi.findViewById(R.id.time);
        TextView nomeuser = (TextView)vi.findViewById(R.id.nomeuser);

        TextView views_count = (TextView)vi.findViewById(R.id.views_count);

        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image);
        ImageView videouserimage=(ImageView)vi.findViewById(R.id.videouserimage); // thumb image
        HashMap<String, String> Imagens;
        Imagens = data.get(position);

        // Setting all values in listview
        title.setText(Imagens.get("title"));
        time.setText(Imagens.get("duration"));
        nomeuser.setText(Imagens.get("name"));
        views_count.setText(Imagens.get("views_count") + " " + activity.getString(R.string.views));
        //imageLoader.DisplayImage(Imagens.get("Thumbnail"), thumb_image);

        Picasso.with(vi.getContext()).load(Imagens.get("Thumbnail")).into(thumb_image);
        Picasso.with(vi.getContext()).load(Imagens.get("UserPhoto")).into(videouserimage);


        return vi;
    }
}
